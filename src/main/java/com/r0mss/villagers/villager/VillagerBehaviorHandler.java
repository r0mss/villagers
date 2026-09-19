package com.r0mss.villagers.villager;

import com.r0mss.villagers.registry.ModProfessions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/**
 * Escucha el tick de cada entidad para:
 * <p>
 * 1) Hacer que los aldeanos hablen de vez en cuando (frase ambiental aleatoria).
 * 2) Mantener "estaticos" (sin caminar) a los aldeanos con las profesiones
 *    Guardian de Tierras y Pregonero, mientras esten empleados.
 * 3) Hacer que el Pregonero grite noticias predeterminadas cada cierto tiempo.
 * 4) Limpiar las burbujas de texto flotante cuando expira su tiempo de vida.
 * <p>
 * Nota de rendimiento: esto corre en el tick de CADA entidad del mundo, pero
 * el trabajo real solo se hace para instancias de Villager o de nuestras
 * burbujas de texto, asi que el costo extra es minimo.
 */
public final class VillagerBehaviorHandler {

    private static final String TAG_NEXT_CHATTER = "villagers_next_chatter";
    private static final String TAG_NEXT_SHOUT = "villagers_next_shout";

    // Rango de ticks entre frases ambientales de un aldeano cualquiera (20 ticks = 1 segundo)
    private static final int CHATTER_MIN_TICKS = 20 * 60 * 2;   // 2 minutos
    private static final int CHATTER_MAX_TICKS = 20 * 60 * 6;   // 6 minutos

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

        if (isGuardian || isCrier) {
            keepStatic(villager);
        }

        CompoundTag data = villager.getPersistentData();
        long time = villager.level().getGameTime();

        handleAmbientChatter(villager, data, time, isGuardian);

        if (isCrier) {
            handleTownCrierShout(villager, data, time);
        }
    }

    /**
     * Frase ambiental aleatoria para cualquier aldeano (mas frecuente si es
     * el Guardian de Tierras, para reforzar la sensacion de vigilancia).
     */
    private static void handleAmbientChatter(Villager villager, CompoundTag data, long time, boolean isGuardian) {
        long nextChatter = data.getLong(TAG_NEXT_CHATTER);
        if (nextChatter == 0L) {
            // Primera vez que vemos a este aldeano: programar su primera frase
            data.putLong(TAG_NEXT_CHATTER, time + randomBetween(villager, CHATTER_MIN_TICKS, CHATTER_MAX_TICKS));
            return;
        }
        if (time >= nextChatter) {
            SpeechBubbles.say(villager, VillagerLines.randomChatter(isGuardian));
            data.putLong(TAG_NEXT_CHATTER, time + randomBetween(villager, CHATTER_MIN_TICKS, CHATTER_MAX_TICKS));
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
            SpeechBubbles.shout(villager, VillagerLines.randomNews());
            data.putLong(TAG_NEXT_SHOUT, time + randomBetween(villager, SHOUT_MIN_TICKS, SHOUT_MAX_TICKS));
        }
    }

    /**
     * Evita que el aldeano se mueva de su puesto: cancela la navegacion cada
     * tick y anula el movimiento horizontal. De vez en cuando lo hace mirar
     * en una direccion aleatoria para que no se vea completamente inerte.
     */
    private static void keepStatic(Villager villager) {
        if (!villager.getNavigation().isDone()) {
            villager.getNavigation().stop();
        }

        Vec3 motion = villager.getDeltaMovement();
        villager.setDeltaMovement(0.0, Math.min(motion.y, 0.0), 0.0);

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
