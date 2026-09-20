package com.r0mss.villagers.block;

import com.mojang.serialization.MapCodec;
import com.r0mss.villagers.network.InfoBoardNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Tablon de informacion: al hacerle clic derecho, muestra (y permite editar)
 * el nombre del asentamiento, su tipo (segun poblacion), cuantos aldeanos
 * hay y que trabajos tienen, escaneando un radio configurable alrededor.
 */
public class InfoBoardBlock extends BaseEntityBlock {

    public static final MapCodec<InfoBoardBlock> CODEC = simpleCodec(InfoBoardBlock::new);

    public InfoBoardBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new InfoBoardBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof InfoBoardBlockEntity board) {
            InfoBoardNetworking.openFor(serverPlayer, board, pos);
        }
        return InteractionResult.CONSUME;
    }
}
