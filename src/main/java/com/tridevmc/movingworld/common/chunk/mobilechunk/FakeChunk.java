package com.tridevmc.movingworld.common.chunk.mobilechunk;

import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;

import javax.annotation.Nullable;

public class FakeChunk extends Chunk {
    public FakeChunk(MobileChunk mobileChunk, int chunkX, int chunkZ, Biome[] biomes) {
        super(mobileChunk.getFakeWorld(), chunkX, chunkZ, biomes);
    }

    @Override
    public IBlockState getBlockState(int x, int y, int z) {
        return getWorld().getBlockState(new BlockPos(x, y, z));
    }

    @Nullable
    @Override
    public TileEntity getTileEntity(BlockPos pos, EnumCreateEntityType p_177424_2_) {
        return getWorld().getTileEntity(pos);
    }

    @Override
    public boolean isLoaded() {
        return true;
    }


}
