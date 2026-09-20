package com.r0mss.villagers;

import com.r0mss.villagers.registry.ModBlocks;
import com.r0mss.villagers.registry.ModItems;
import com.r0mss.villagers.registry.ModPoiTypes;
import com.r0mss.villagers.registry.ModProfessions;
import com.r0mss.villagers.villager.VillagerBehaviorHandler;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

/**
 * Punto de entrada del mod "Aldeanos Inmersivos".
 * <p>
 * Este mod agrega dos trabajos nuevos de aldeano:
 * - "Guardian de Tierras": se queda estatico vigilando su bloque de trabajo
 *   (guard_post).
 * - "Pregonero": se queda estatico en su podio (town_crier_podium) y grita
 *   noticias aleatorias predeterminadas (texto flotante + chat + sonido).
 */
@Mod(VillagersMod.MODID)
public class VillagersMod {

    public static final String MODID = "villagers";
    public static final Logger LOGGER = LogUtils.getLogger();

    // Pestaña de creativo propia del mod, para encontrar fácil los bloques nuevos
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> VILLAGERS_TAB =
            CREATIVE_MODE_TABS.register("villagers_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.villagers"))
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon(() -> ModItems.GUARD_POST_ITEM.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.GUARD_POST_ITEM.get());
                        output.accept(ModItems.TOWN_CRIER_PODIUM_ITEM.get());
                    }).build());

    public VillagersMod(IEventBus modEventBus, ModContainer modContainer) {
        // Registrar todos los DeferredRegister de este mod
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModPoiTypes.POI_TYPES.register(modEventBus);
        ModProfessions.PROFESSIONS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        modEventBus.addListener(this::addCreative);

        // Registrar el manejador de comportamiento de aldeanos (trabajos estaticos + pregonero)
        NeoForge.EVENT_BUS.register(VillagerBehaviorHandler.class);

        LOGGER.info("Aldeanos Inmersivos cargado correctamente.");
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(ModItems.GUARD_POST_ITEM);
            event.accept(ModItems.TOWN_CRIER_PODIUM_ITEM);
        }
    }
}
