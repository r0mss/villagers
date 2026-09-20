package com.r0mss.villagers.registry;

import com.r0mss.villagers.VillagersMod;
import com.r0mss.villagers.block.InfoBoardBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, VillagersMod.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<InfoBoardBlockEntity>> INFO_BOARD =
            BLOCK_ENTITIES.register(
                    "info_board",
                    () -> BlockEntityType.Builder.of(InfoBoardBlockEntity::new, ModBlocks.INFO_BOARD.get()).build(null)
            );
}
