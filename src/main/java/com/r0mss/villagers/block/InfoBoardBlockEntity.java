package com.r0mss.villagers.block;

import com.r0mss.villagers.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Guarda el nombre del asentamiento. Los demas datos (poblacion, tipo de
 * lugar, trabajadores) NO se guardan aca: se calculan al momento, escaneando
 * los aldeanos cercanos cada vez que se abre el tablon.
 */
public class InfoBoardBlockEntity extends BlockEntity {

    private String settlementName = "";

    public InfoBoardBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.INFO_BOARD.get(), pos, state);
    }

    public String getSettlementName() {
        return settlementName;
    }

    public void setSettlementName(String settlementName) {
        this.settlementName = settlementName;
        setChanged();
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        settlementName = tag.getString("SettlementName");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putString("SettlementName", settlementName);
    }
}
