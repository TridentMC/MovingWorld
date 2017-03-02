package com.elytradev.movingworld.common.experiments.network;

import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

/**
 * Simple block storage mechanism for lists.
 */
public class BlockData {
    private final BlockPos pos;
    private final IBlockState state;
    private final TileEntity tileEntity;

    public BlockData(BlockPos pos, IBlockState state, TileEntity tileEntity) {
        this.pos = pos;
        this.state = state;
        this.tileEntity = tileEntity;
    }

    public BlockData(BlockPos pos, IBlockState state) {
        this.pos = pos;
        this.state = state;
        this.tileEntity = null;
    }

    public BlockPos getPos() {
        return pos;
    }

    public IBlockState getState() {
        return state;
    }

    public TileEntity getTileEntity() {
        return tileEntity;
    }

    public boolean hasTile() {
        return tileEntity != null;
    }

    @Override
    public String toString() {
        return "BlockData{" +
                "pos=" + pos +
                ", state=" + state +
                ", tileEntity=" + tileEntity +
                '}';
    }
}
