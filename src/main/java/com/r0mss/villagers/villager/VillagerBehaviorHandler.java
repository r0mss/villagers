package com.r0mss.villagers.villager;

import com.r0mss.villagers.VillagersMod;
import com.r0mss.villagers.block.GuardPostBlock;
import com.r0mss.villagers.registry.ModProfessions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.Optional;

/**
 * Escucha el tick de cada entidad para:
 * <p>
 * 1) Mantener "estaticos" (sin caminar) a los aldeanos con las profesiones
 *    Guardian de Tierras y Pregonero, mientras esten empleados.
 * 2) Hacer que el Guardian de Tierras mantenga el cuerpo fijo mirando hacia
 *    donde apunta la placa de su puesto (guard_post), mientras la cabeza
 *    puede seguir moviendose libre.
 * 3) Hacer que el Pregonero grite noticias predeterminadas cada cierto tiempo
 *    (texto flotante + chat + sonido).
 * 4) Limpiar las burbujas de texto flotante cuando expira su tiempo de vida.
 * <p>
 * Nota de rendimiento: esto corre en el tick de CADA entidad del mundo, pero
 * el trabajo real solo se hace para instancias de Villager o de nuestras
 * burbujas de texto, asi que el costo extra es minimo.
 */
public final class VillagerBehaviorHandler {

    private static final String TAG_NEXT_SHOUT = "villagers_next_shout";
    private static final String TAG_JOB_LOGGED = "villagers_job_logged";
    private static final String TAG_FACING_YAW = "villagers_facing_yaw";

    // Rango de ticks entre gritos del pregonero
    private static final int SHOUT_MIN_TICKS = 20 * 20;   // 20 segundos
    private static final int SHOUT_MAX_TICKS = 20 * 45;   // 45 segundos

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
        Float fixedBodyYaw = isGuardian ? resolveFacingYaw(villager, data) : null;

        keepStatic(villager, fixedBodyYaw);
        logJobAcquiredOnce(villager, data, isGuardian ? "land_guardian" : "town_crier");

        if (isCrier) {
            handleTownCrierShout(villager, data, villager.level().getGameTime());
        }
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
     * Calcula (una sola vez, y la guarda en datos persistentes) hacia donde
     * debe mirar el cuerpo del Guardian: la misma direccion hacia la que
     * apunta la placa de guard_post en su puesto de trabajo.
     */
    private static Float resolveFacingYaw(Villager villager, CompoundTag data) {
        if (data.contains(TAG_FACING_YAW)) {
            return data.getFloat(TAG_FACING_YAW);
        }

        float yaw = villager.getYRot();
        Optional<GlobalPos> jobSite = villager.getBrain().getMemory(MemoryModuleType.JOB_SITE);
        if (jobSite.isPresent()) {
            BlockPos pos = jobSite.get().pos();
            BlockState state = villager.level().getBlockState(pos);
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

    private static int randomBetween(Villager villager, int min, int max) {
        return min + villager.getRandom().nextInt(max - min + 1);
    }
}
