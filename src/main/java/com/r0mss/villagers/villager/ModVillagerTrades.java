package com.r0mss.villagers.villager;

import com.r0mss.villagers.registry.ModProfessions;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.BasicItemListing;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;

/**
 * Comercios de las profesiones nuevas.
 * <p>
 * El Campanero vende un reloj (tiene sentido: es quien controla el horario
 * del pueblo). El Guardian y el Pregonero no tienen comercio: al Guardian
 * se le da pan/agua directamente (ver {@link GuardianFeedingHandler}), y el
 * Pregonero no tiene ninguna interaccion de comercio por ahora.
 */
public final class ModVillagerTrades {

    private static final int CLOCK_PRICE_EMERALDS = 3;

    private ModVillagerTrades() {
    }

    @SubscribeEvent
    public static void onVillagerTrades(VillagerTradesEvent event) {
        if (event.getType() != ModProfessions.CAMPANERO.get()) {
            return;
        }

        event.getTrades().get(1).add(new BasicItemListing(
                CLOCK_PRICE_EMERALDS,
                new ItemStack(Items.CLOCK),
                12,
                5
        ));
    }
}
