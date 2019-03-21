package com.tridevmc.movingworld.common.chunk.mobilechunk;

import com.tridevmc.movingworld.common.entity.EntityMovingWorld;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.LogicalSide;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class MobileChunkServer extends MobileChunk {
    private Set<BlockPos> blockQueue;
    private Set<BlockPos> tileQueue;

    public MobileChunkServer(World world, EntityMovingWorld entityMovingWorld) {
        super(world, entityMovingWorld);
        this.blockQueue = new HashSet<>();
        this.tileQueue = new HashSet<>();
    }

    @Override
    public Collection<BlockPos> getBlockQueue() {
        return this.blockQueue;
    }

    @Override
    public Collection<BlockPos> getTileQueue() {
        return this.tileQueue;
    }

    @Override
    public boolean addBlockWithState(BlockPos pos, IBlockState blockState) {
        if (super.addBlockWithState(pos, blockState)) {
            this.blockQueue.add(pos);
            return true;
        }
        return false;
    }

    @Override
    public boolean setBlockState(BlockPos pos, IBlockState state) {
        if (super.setBlockState(pos, state)) {
            this.blockQueue.add(pos);
            return true;
        }
        return false;
    }

    @Override
    public void setTileEntity(BlockPos pos, TileEntity tileentity) {
        this.tileQueue.add(pos);
        super.setTileEntity(pos, tileentity);
    }

    @Override
    public void removeChunkBlockTileEntity(BlockPos pos) {
        this.tileQueue.add(pos);
        super.removeChunkBlockTileEntity(pos);
    }

    @Override
    public void markTileDirty(BlockPos pos) {
        this.tileQueue.add(pos);
        super.markTileDirty(pos);
    }

    @Override
    public LogicalSide side() {
        return LogicalSide.SERVER;
    }
}
