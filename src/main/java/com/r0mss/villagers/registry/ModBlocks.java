package com.r0mss.villagers.registry;

import com.r0mss.villagers.VillagersMod;
import com.r0mss.villagers.block.GuardPostBlock;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Bloques de trabajo (job site) para las nuevas profesiones de aldeano.
 */
public class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(VillagersMod.MODID);

    // Bloque de trabajo del Guardian de Tierras: una placa de pared (no un bloque completo),
    // para poder colocarla junto a una puerta sin estorbar el paso.
    public static final DeferredBlock<GuardPostBlock> GUARD_POST = BLOCKS.register(
            "guard_post",
            () -> new GuardPostBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(2.0f, 6.0f)
                    .requiresCorrectToolForDrops()
                    .sound(net.minecraft.world.level.block.SoundType.STONE)
                    .noOcclusion())
    );

    // Bloque de trabajo del Pregonero: un podio para anunciar noticias
    public static final DeferredBlock<Block> TOWN_CRIER_PODIUM = BLOCKS.registerSimpleBlock(
            "town_crier_podium",
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0f, 3.0f)
                    .sound(net.minecraft.world.level.block.SoundType.WOOD)
    );

    // Bloque de trabajo del Campanero: una campana propia (misma clase que la vanilla,
    // asi que suena y se balancea igual). Por ahora usa la textura vanilla; mas
    // adelante se puede reemplazar por una propia sin tocar el codigo.
    public static final DeferredBlock<BellBlock> CAMPANERO_BELL = BLOCKS.register(
            "campanero_bell",
            () -> new BellBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BELL))
    );
}
