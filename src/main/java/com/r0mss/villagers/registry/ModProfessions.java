package com.r0mss.villagers.registry;

import com.google.common.collect.ImmutableSet;
import com.r0mss.villagers.VillagersMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Nuevas profesiones de aldeano.
 * <p>
 * - LAND_GUARDIAN ("Guardian de Tierras"): se para estatico junto a su
 *   guard_post para vigilar una zona (puertas, calles, etc).
 * - TOWN_CRIER ("Pregonero"): se para estatico junto a su podio y grita
 *   noticias aleatorias predeterminadas de vez en cuando.
 * <p>
 * El comportamiento "estatico" y el habla se maneja en
 * {@link com.r0mss.villagers.villager.VillagerBehaviorHandler}.
 */
public class ModProfessions {

    public static final DeferredRegister<VillagerProfession> PROFESSIONS =
            DeferredRegister.create(Registries.VILLAGER_PROFESSION, VillagersMod.MODID);

    public static final DeferredHolder<VillagerProfession, VillagerProfession> LAND_GUARDIAN =
            PROFESSIONS.register("land_guardian", () -> new VillagerProfession(
                    "land_guardian",
                    holder -> holder.value() == ModPoiTypes.GUARD_POST_POI.get(),
                    holder -> holder.value() == ModPoiTypes.GUARD_POST_POI.get(),
                    ImmutableSet.of(),
                    ImmutableSet.of(),
                    SoundEvents.VILLAGER_WORK_ARMORER
            ));

    public static final DeferredHolder<VillagerProfession, VillagerProfession> TOWN_CRIER =
            PROFESSIONS.register("town_crier", () -> new VillagerProfession(
                    "town_crier",
                    holder -> holder.value() == ModPoiTypes.TOWN_CRIER_POI.get(),
                    holder -> holder.value() == ModPoiTypes.TOWN_CRIER_POI.get(),
                    ImmutableSet.of(),
                    ImmutableSet.of(),
                    SoundEvents.VILLAGER_WORK_CARTOGRAPHER
            ));

    public static final DeferredHolder<VillagerProfession, VillagerProfession> CAMPANERO =
            PROFESSIONS.register("campanero", () -> new VillagerProfession(
                    "campanero",
                    holder -> holder.value() == ModPoiTypes.CAMPANERO_BELL_POI.get(),
                    holder -> holder.value() == ModPoiTypes.CAMPANERO_BELL_POI.get(),
                    ImmutableSet.of(),
                    ImmutableSet.of(),
                    SoundEvents.VILLAGER_WORK_LIBRARIAN
            ));
}
