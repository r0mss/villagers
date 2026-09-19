package com.r0mss.villagers.registry;

import com.r0mss.villagers.VillagersMod;
import net.minecraft.world.item.BlockItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Items del mod (por ahora, los BlockItem de los bloques de trabajo).
 */
public class ModItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(VillagersMod.MODID);

    public static final DeferredItem<BlockItem> GUARD_POST_ITEM =
            ITEMS.registerSimpleBlockItem("guard_post", ModBlocks.GUARD_POST);

    public static final DeferredItem<BlockItem> TOWN_CRIER_PODIUM_ITEM =
            ITEMS.registerSimpleBlockItem("town_crier_podium", ModBlocks.TOWN_CRIER_PODIUM);
}
