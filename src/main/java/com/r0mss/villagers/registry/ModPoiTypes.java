package com.r0mss.villagers.registry;

import com.google.common.collect.ImmutableSet;
import com.r0mss.villagers.VillagersMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Puntos de interes (POI) que actuan como "job site" (bloque de trabajo)
 * para las nuevas profesiones. Un aldeano desempleado que camine cerca de
 * uno de estos bloques (y no este reclamado por otro aldeano) puede tomar
 * la profesion asociada automaticamente, igual que con un atril o una mesa
 * de cartografo vanilla.
 */
public class ModPoiTypes {

    public static final DeferredRegister<PoiType> POI_TYPES =
            DeferredRegister.create(Registries.POINT_OF_INTEREST_TYPE, VillagersMod.MODID);

    // maxTickets = 1 (solo un aldeano puede reclamar el puesto)
    // validRange = 1 (el aldeano debe estar pegado al bloque para "trabajar")
    public static final DeferredHolder<PoiType, PoiType> GUARD_POST_POI = POI_TYPES.register(
            "guard_post_poi",
            () -> new PoiType(
                    ImmutableSet.copyOf(ModBlocks.GUARD_POST.get().getStateDefinition().getPossibleStates()),
                    1,
                    1
            )
    );

    public static final DeferredHolder<PoiType, PoiType> TOWN_CRIER_POI = POI_TYPES.register(
            "town_crier_poi",
            () -> new PoiType(
                    ImmutableSet.copyOf(ModBlocks.TOWN_CRIER_PODIUM.get().getStateDefinition().getPossibleStates()),
                    1,
                    1
            )
    );

    public static final DeferredHolder<PoiType, PoiType> CAMPANERO_BELL_POI = POI_TYPES.register(
            "campanero_bell_poi",
            () -> new PoiType(
                    ImmutableSet.copyOf(ModBlocks.CAMPANERO_BELL.get().getStateDefinition().getPossibleStates()),
                    1,
                    1
            )
    );
}
