package com.r0mss.villagers.villager;

import com.r0mss.villagers.VillagersMod;
import com.r0mss.villagers.block.GuardPostBlock;
import com.r0mss.villagers.registry.ModProfessions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.List;

/**
 * Escucha el tick de cada entidad para:
 * <p>
 * 1) Mantener a los aldeanos con nuestras profesiones cerca de su puesto
 *    (radio de {@value #LEASH_RADIUS} bloques): dentro del radio quedan
 *    fijos, si se alejan mas caminan de vuelta normalmente (sin teletransporte).
 * 2) Guardian de Tierras: cuerpo fijo mirando hacia su placa, nunca duerme,
 *    no entra en panico, y ataca cuerpo a cuerpo a mobs hostiles que se
 *    acerquen demasiado.
 * 3) Pregonero: grita noticias predeterminadas cada cierto tiempo (texto
 *    flotante + chat + sonido).
 * 4) Limpia las burbujas de texto flotante cuando expira su tiempo de vida.
 * <p>
 * Nota de rendimiento: esto corre en el tick de CADA entidad del mundo, pero
 * el trabajo real solo se hace para instancias de Villager o de nuestras
 * burbujas de texto, asi que el costo extra es minimo.
 */
public final class VillagerBehaviorHandler {

    private static final String TAG_NEXT_SHOUT = "villagers_next_shout";
    private static final String TAG_JOB_LOGGED = "villagers_job_logged";
    private static final String TAG_FACING_YAW = "villagers_facing_yaw";
    private static final String TAG_NEXT_ATTACK = "villagers_next_attack";

    // Rango de ticks entre gritos del pregonero
    private static final int SHOUT_MIN_TICKS = 20 * 20;   // 20 segundos
    private static final int SHOUT_MAX_TICKS = 20 * 45;   // 45 segundos

    // Cada cuanto revisa el Pregonero si hay jugadores cerca con logros pendientes
    private static final String TAG_NEXT_ACHIEVEMENT_CHECK = "villagers_next_achievement_check";
    private static final int ACHIEVEMENT_CHECK_INTERVAL_TICKS = 20 * 5; // cada 5 segundos
    private static final double ACHIEVEMENT_RANGE = 16.0;

    // Que tan lejos de su puesto puede alejarse antes de que lo hagamos volver caminando
    private static final double LEASH_RADIUS = 4.0;
    private static final double LEASH_RADIUS_SQ = LEASH_RADIUS * LEASH_RADIUS;

    // Combate del Guardian
    private static final double ATTACK_RANGE = 2.5;
    private static final float ATTACK_DAMAGE = 7.0f; // ~3.5 corazones
    private static final int ATTACK_COOLDOWN_TICKS = 20; // 1 segundo

    private VillagerBehaviorHandler() {
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        Entity entity = event.getEntity();
        if (entity.level().isClientSide()) {
            return;
        }

        if (entity instanceof Villager villager) {
            handleVillager(villager);
        } else if (entity instanceof Display.TextDisplay display) {
            SpeechBubbles.tickDisplay(display);
        }
    }

    private static void handleVillager(Villager villager) {
        VillagerProfession profession = villager.getVillagerData().getProfession();
        boolean isGuardian = profession == ModProfessions.LAND_GUARDIAN.get();
        boolean isCrier = profession == ModProfessions.TOWN_CRIER.get();

        if (!isGuardian && !isCrier) {
            return;
        }

        CompoundTag data = villager.getPersistentData();
        logJobAcquiredOnce(villager, data, isGuardian ? "land_guardian" : "town_crier");

        BlockPos jobSitePos = villager.getBrain().getMemory(MemoryModuleType.JOB_SITE)
                .map(GlobalPos::pos)
                .orElse(null);

        boolean withinPost = jobSitePos == null
                || villager.position().distanceToSqr(Vec3.atCenterOf(jobSitePos)) <= LEASH_RADIUS_SQ;

        if (withinPost) {
            Float fixedBodyYaw = isGuardian ? resolveFacingYaw(villager, data, jobSitePos) : null;
            keepStatic(villager, fixedBodyYaw);
        } else {
            walkBackToPost(villager, jobSitePos);
        }

        if (isGuardian) {
            suppressGuardianInstincts(villager);
            handleGuardianAttack(villager, data);
        }

        if (isCrier) {
            if (!announceNearbyAchievement(villager, data)) {
                handleTownCrierShout(villager, data, villager.level().getGameTime());
            }
        }
    }

    /**
     * Revisa (con su propio enfriamiento, cada {@value #ACHIEVEMENT_CHECK_INTERVAL_TICKS}
     * ticks) si hay algun jugador cerca con un logro pendiente por anunciar.
     * Si anuncia uno, ese ciclo no se hace ademas una noticia aleatoria.
     */
    private static boolean announceNearbyAchievement(Villager villager, CompoundTag data) {
        long time = villager.level().getGameTime();
        if (data.contains(TAG_NEXT_ACHIEVEMENT_CHECK) && time < data.getLong(TAG_NEXT_ACHIEVEMENT_CHECK)) {
            return false;
        }
        data.putLong(TAG_NEXT_ACHIEVEMENT_CHECK, time + ACHIEVEMENT_CHECK_INTERVAL_TICKS);

        if (!(villager.level() instanceof ServerLevel serverLevel)) {
            return false;
        }

        double rangeSq = ACHIEVEMENT_RANGE * ACHIEVEMENT_RANGE;
        for (ServerPlayer player : serverLevel.players()) {
            if (player.distanceToSqr(villager) > rangeSq) {
                continue;
            }
            String pending = CrierAchievements.popPending(player);
            if (pending != null) {
                SpeechBubbles.announce(villager, pending);
                return true;
            }
        }
        return false;
    }

    /**
     * El Pregonero grita una noticia predeterminada cada cierto tiempo.
     */
    private static void handleTownCrierShout(Villager villager, CompoundTag data, long time) {
        long nextShout = data.getLong(TAG_NEXT_SHOUT);
        if (nextShout == 0L) {
            data.putLong(TAG_NEXT_SHOUT, time + randomBetween(villager, SHOUT_MIN_TICKS, SHOUT_MAX_TICKS));
            return;
        }
        if (time >= nextShout) {
            SpeechBubbles.announce(villager, VillagerLines.randomNews());
            data.putLong(TAG_NEXT_SHOUT, time + randomBetween(villager, SHOUT_MIN_TICKS, SHOUT_MAX_TICKS));
        }
    }

    /**
     * Quita al Guardian los instintos que no encajan con "estar de guardia":
     * nunca duerme (si se llega a quedar dormido, lo despertamos al toque) y
     * nunca entra en panico (borramos la memoria de "me golpearon" que
     * dispara la huida, asi se queda firme y puede contraatacar).
     */
    private static void suppressGuardianInstincts(Villager villager) {
        if (villager.isSleeping()) {
            villager.stopSleeping();
        }
        villager.getBrain().eraseMemory(MemoryModuleType.HOME);
        villager.getBrain().eraseMemory(MemoryModuleType.HURT_BY);
        villager.getBrain().eraseMemory(MemoryModuleType.HURT_BY_ENTITY);
    }

    /**
     * El Guardian ataca cuerpo a cuerpo a cualquier mob hostil que se acerque
     * demasiado, sin moverse de su puesto ni perseguir.
     */
    private static void handleGuardianAttack(Villager villager, CompoundTag data) {
        long time = villager.level().getGameTime();
        if (time < data.getLong(TAG_NEXT_ATTACK)) {
            return;
        }

        AABB searchArea = villager.getBoundingBox().inflate(ATTACK_RANGE);
        List<LivingEntity> nearbyEnemies = villager.level().getEntitiesOfClass(
                LivingEntity.class, searchArea,
                entity -> entity instanceof Enemy && entity.isAlive()
        );

        LivingEntity target = nearbyEnemies.stream()
                .min((a, b) -> Double.compare(a.distanceToSqr(villager), b.distanceToSqr(villager)))
                .orElse(null);

        if (target == null || villager.distanceToSqr(target) > ATTACK_RANGE * ATTACK_RANGE) {
            return;
        }

        target.hurt(villager.damageSources().mobAttack(villager), ATTACK_DAMAGE);
        villager.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
        data.putLong(TAG_NEXT_ATTACK, time + ATTACK_COOLDOWN_TICKS);
    }

    /**
     * Calcula (una sola vez, y la guarda en datos persistentes) hacia donde
     * debe mirar el cuerpo del Guardian: la misma direccion hacia la que
     * apunta la placa de guard_post en su puesto de trabajo.
     */
    private static Float resolveFacingYaw(Villager villager, CompoundTag data, BlockPos jobSitePos) {
        if (data.contains(TAG_FACING_YAW)) {
            return data.getFloat(TAG_FACING_YAW);
        }

        float yaw = villager.getYRot();
        if (jobSitePos != null) {
            BlockState state = villager.level().getBlockState(jobSitePos);
            if (state.getBlock() instanceof GuardPostBlock) {
                Direction facing = state.getValue(GuardPostBlock.FACING);
                yaw = facing.toYRot();
            }
        }

        data.putFloat(TAG_FACING_YAW, yaw);
        return yaw;
    }

    /**
     * Escribe una linea en el log del servidor la primera vez que vemos a
     * este aldeano con una de nuestras profesiones, para poder confirmar
     * (mirando latest.log) si el sistema de trabajos vanilla efectivamente
     * se lo esta asignando.
     */
    private static void logJobAcquiredOnce(Villager villager, CompoundTag data, String professionId) {
        if (data.getBoolean(TAG_JOB_LOGGED)) {
            return;
        }
        data.putBoolean(TAG_JOB_LOGGED, true);
        VillagersMod.LOGGER.info("[villagers] Aldeano {} tomo el trabajo '{}' en {}",
                villager.getUUID(), professionId, villager.blockPosition());
    }

    /**
     * Evita que el aldeano se mueva de su puesto: cancela la navegacion cada
     * tick y anula el movimiento horizontal. De vez en cuando hace que la
     * cabeza mire en una direccion aleatoria para que no se vea inerte.
     * <p>
     * Si se pasa un fixedBodyYaw (solo para el Guardian), el CUERPO se fuerza
     * a esa direccion cada tick; la cabeza sigue libre de moverse via el
     * look control normal.
     */
    private static void keepStatic(Villager villager, Float fixedBodyYaw) {
        if (!villager.getNavigation().isDone()) {
            villager.getNavigation().stop();
        }

        Vec3 motion = villager.getDeltaMovement();
        villager.setDeltaMovement(0.0, Math.min(motion.y, 0.0), 0.0);

        if (fixedBodyYaw != null) {
            villager.setYRot(fixedBodyYaw);
            villager.setYBodyRot(fixedBodyYaw);
        }

        if (villager.getRandom().nextInt(100) == 0) {
            double dx = (villager.getRandom().nextDouble() * 2.0) - 1.0;
            double dz = (villager.getRandom().nextDouble() * 2.0) - 1.0;
            villager.getLookControl().setLookAt(
                    villager.getX() + dx,
                    villager.getEyeY(),
                    villager.getZ() + dz
            );
        }
    }

    /**
     * Se paso del radio permitido: lo dejamos caminar normalmente de vuelta
     * a su puesto (sin teletransporte), en vez de congelarlo.
     */
    private static void walkBackToPost(Villager villager, BlockPos jobSitePos) {
        if (villager.getNavigation().isDone()) {
            villager.getNavigation().moveTo(
                    jobSitePos.getX() + 0.5,
                    jobSitePos.getY(),
                    jobSitePos.getZ() + 0.5,
                    0.5
            );
        }
    }

    private static int randomBetween(Villager villager, int min, int max) {
        return min + villager.getRandom().nextInt(max - min + 1);
    }
}
