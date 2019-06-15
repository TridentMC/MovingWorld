package com.tridevmc.movingworld.common.chunk.mobilechunk;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;

import javax.annotation.Nullable;

public class FakeChunk extends Chunk {
    public FakeChunk(MobileChunk mobileChunk, ChunkPos chunkPos, Biome[] biomes) {
        super(mobileChunk.getFakeWorld(), chunkPos, biomes);
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        return getWorld().getBlockState(pos);
    }

    @Nullable
    @Override
    public TileEntity getTileEntity(BlockPos pos) {
        return getWorld().getTileEntity(pos);
    }

    @Override
    public void setLoaded(boolean loaded) {
        super.setLoaded(loaded);
    }
}
