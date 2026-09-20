package com.r0mss.villagers;

import com.r0mss.villagers.network.InfoBoardNetworking;
import com.r0mss.villagers.registry.ModBlockEntities;
import com.r0mss.villagers.registry.ModBlocks;
import com.r0mss.villagers.registry.ModItems;
import com.r0mss.villagers.registry.ModPoiTypes;
import com.r0mss.villagers.registry.ModProfessions;
import com.r0mss.villagers.villager.CampaneroBehaviorHandler;
import com.r0mss.villagers.villager.CrierAchievements;
import com.r0mss.villagers.villager.GuardianFeedingHandler;
import com.r0mss.villagers.villager.ModVillagerTrades;
import com.r0mss.villagers.villager.VillagerBehaviorHandler;
import com.r0mss.villagers.villager.VillagerNaming;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

/**
 * Punto de entrada del mod "Aldeanos Inmersivos".
 * <p>
 * Este mod agrega tres trabajos nuevos de aldeano:
 * - "Guardian de Tierras": se queda estatico vigilando su puesto (guard_post),
 *   ataca mobs hostiles cercanos y puede recibir pan/agua como roleplay.
 * - "Pregonero": se queda estatico en su podio (town_crier_podium) y grita
 *   noticias aleatorias predeterminadas, ademas de anunciar los logros del
 *   jugador cuando este se acerca.
 * - "Campanero": junto a su propia campana (campanero_bell), toca la campana
 *   y anuncia por chat los cambios de jornada (mañana/mediodia/noche), y
 *   muestra la hora actual en un reloj flotante.
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
                        output.accept(ModItems.CAMPANERO_BELL_ITEM.get());
                        output.accept(ModItems.INFO_BOARD_ITEM.get());
                    }).build());

    public VillagersMod(IEventBus modEventBus, ModContainer modContainer) {
        // Registrar todos los DeferredRegister de este mod
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModPoiTypes.POI_TYPES.register(modEventBus);
        ModProfessions.PROFESSIONS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        modEventBus.addListener(this::addCreative);

        // Nuestra campana usa la misma clase que la vanilla (BellBlock), pero
        // necesita registrarse como "bloque valido" del BlockEntityType.BELL
        // de vanilla para heredar gratis su animacion, sonido y logica de tañido.
        modEventBus.addListener((BlockEntityTypeAddBlocksEvent event) ->
                event.modify(BlockEntityType.BELL, ModBlocks.CAMPANERO_BELL.get()));

        modContainer.registerConfig(net.neoforged.fml.config.ModConfig.Type.COMMON, Config.SPEC);

        modEventBus.addListener(InfoBoardNetworking::register);

        // Registrar los manejadores de comportamiento de aldeanos
        NeoForge.EVENT_BUS.register(VillagerBehaviorHandler.class);
        NeoForge.EVENT_BUS.register(GuardianFeedingHandler.class);
        NeoForge.EVENT_BUS.register(CampaneroBehaviorHandler.class);
        NeoForge.EVENT_BUS.register(CrierAchievements.class);
        NeoForge.EVENT_BUS.register(ModVillagerTrades.class);
        NeoForge.EVENT_BUS.register(VillagerNaming.class);

        LOGGER.info("Aldeanos Inmersivos cargado correctamente.");
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(ModItems.GUARD_POST_ITEM);
            event.accept(ModItems.TOWN_CRIER_PODIUM_ITEM);
            event.accept(ModItems.CAMPANERO_BELL_ITEM);
            event.accept(ModItems.INFO_BOARD_ITEM);
        }
    }
}
