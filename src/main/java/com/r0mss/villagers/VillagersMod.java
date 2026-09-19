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
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

/**
 * Punto de entrada del mod "Aldeanos Inmersivos".
 * <p>
 * Este mod agrega:
 * - Aldeanos que hablan de vez en cuando (frases ambientales aleatorias).
 * - Un nuevo trabajo "Guardian de Tierras": el aldeano se queda estatico
 *   vigilando el bloque de trabajo (guard_post).
 * - Un nuevo trabajo "Pregonero": el aldeano se queda estatico en su podio
 *   y grita noticias aleatorias predeterminadas.
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

        // Registrar el config
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        // Registrar el manejador de comportamiento de aldeanos (habla + trabajos estaticos)
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
