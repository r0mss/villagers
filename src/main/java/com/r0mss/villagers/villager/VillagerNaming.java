package com.r0mss.villagers.villager;

import com.r0mss.villagers.Config;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Le da un nombre propio permanente a cada aldeano (cualquiera, no solo los
 * de nuestras profesiones), la primera vez que lo vemos sin nombre. El
 * nombre queda para siempre (es el "CustomName" vanilla de la entidad, se
 * guarda solo con el mundo, no necesitamos datos propios).
 * <p>
 * Apagado por defecto (ver {@link Config#VILLAGER_NAMES_ENABLED}) para
 * evitar conflictos con otros mods que agregan la misma funcionalidad.
 * <p>
 * Los aldeanos de Minecraft no tienen genero como tal; el pool esta dividido
 * en dos listas solo para variar el estilo de los nombres, elegidas al azar
 * con la misma probabilidad.
 */
public final class VillagerNaming {

    private static final List<String> NAMES_A = List.of(
            "Dante", "Ignacio", "Bartolomeo", "Gaspar", "Rengar",
            "Roman", "Xander", "Dimitry", "Dracula", "Monq"
    );

    private static final List<String> NAMES_B = List.of(
            "Renata", "Ignacia", "Francisca", "Dracania", "Valentina",
            "Camila", "Antonia", "Selene", "Carmilla", "Fernanda"
    );

    private VillagerNaming() {
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!Config.VILLAGER_NAMES_ENABLED.get()) {
            return;
        }

        Entity entity = event.getEntity();
        if (entity.level().isClientSide()) {
            return;
        }
        if (!(entity instanceof Villager villager)) {
            return;
        }
        if (villager.hasCustomName()) {
            return;
        }

        List<String> pool = ThreadLocalRandom.current().nextBoolean() ? NAMES_A : NAMES_B;
        String name = pool.get(ThreadLocalRandom.current().nextInt(pool.size()));

        villager.setCustomName(Component.literal(name));
        villager.setCustomNameVisible(true);
    }
}
