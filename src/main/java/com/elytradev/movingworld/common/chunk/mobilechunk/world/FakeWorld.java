package com.elytradev.movingworld.common.chunk.mobilechunk.world;

import com.elytradev.movingworld.common.chunk.mobilechunk.FakeChunk;
import com.elytradev.movingworld.common.chunk.mobilechunk.MobileChunk;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

/**
 * A wrapper for MobileChunks, used to give blocks accurate information about it's neighbors.
 */
public class FakeWorld extends World {

    MobileChunk mobileChunk;

    private FakeWorld(boolean remote, World parentWorld) {
        super(parentWorld.getSaveHandler(), parentWorld.getWorldInfo(), parentWorld.provider, parentWorld.profiler, remote);
    }

    public static FakeWorld getFakeWorld(MobileChunk chunk) {
        FakeWorld retVal = new FakeWorld(chunk.world.isRemote, chunk.world);
        retVal.mobileChunk = chunk;
        return retVal;
    }

    @Override
    public TileEntity getTileEntity(BlockPos pos) {
        return mobileChunk.getTileEntity(pos);
    }

    @Override
    public IChunkProvider createChunkProvider() {
        return null;
    }

    @Override
    public IBlockState getBlockState(BlockPos pos) {
        return mobileChunk.getBlockState(pos);
    }

    @Override
    public boolean setBlockState(BlockPos pos, IBlockState state) {
        mobileChunk.setBlockState(pos, state);
        return false;
    }

    @Override
    public boolean setBlockState(BlockPos pos, IBlockState state, int flags) {
        mobileChunk.setBlockState(pos, state);
        return false;
    }

    @Override
    public void setTileEntity(BlockPos pos, @Nullable TileEntity tileEntityIn) {
        mobileChunk.setTileEntity(pos, tileEntityIn);
    }

    @Override
    public void markChunkDirty(BlockPos pos, TileEntity unusedTileEntity) {
        mobileChunk.setChunkModified();
        if (mobileChunk.side().isServer()) {
            mobileChunk.markTileDirty(pos);
        }
    }

    private boolean isValidPosition(BlockPos pos) {
        return pos.getX() >= mobileChunk.minX() && pos.getZ() >= mobileChunk.minZ() && pos.getX() < mobileChunk.maxX() && pos.getZ() < mobileChunk.maxZ() && pos.getY() >= 0 && pos.getY() < mobileChunk.maxY();
    }

    @Override
    public boolean isBlockLoaded(BlockPos pos, boolean allowEmpty) {
        return isValidPosition(pos);
    }

    @Override
    public boolean isChunkLoaded(int x, int z, boolean allowEmpty) {
        return true;
    }

    @Override
    public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default) {
        if (getBlockState(pos) == null || isAirBlock(pos)) return _default;

        return getBlockState(pos).isSideSolid(this, pos, side);
    }

    @Override
    public float getLightBrightness(BlockPos pos) {
        return this.getBlockState(pos).getLightValue(mobileChunk, pos);
    }

    @Override
    public long getTotalWorldTime() {
        return mobileChunk.world.getTotalWorldTime();
    }

    @Override
    public long getWorldTime() {
        return mobileChunk.world.getWorldTime();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public int getCombinedLight(BlockPos pos, int lightValue) {
        return mobileChunk.getCombinedLight(pos, lightValue);
    }

    @Override
    public Chunk getChunk(int chunkX, int chunkZ) {
        return new FakeChunk(mobileChunk, chunkX, chunkZ);
    }
}
