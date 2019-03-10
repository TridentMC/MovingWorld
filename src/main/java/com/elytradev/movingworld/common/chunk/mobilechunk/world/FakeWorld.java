package com.elytradev.movingworld.common.chunk.mobilechunk.world;

import com.elytradev.movingworld.common.chunk.mobilechunk.FakeChunk;
import com.elytradev.movingworld.common.chunk.mobilechunk.MobileChunk;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.tags.NetworkTagManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EmptyTickList;
import net.minecraft.world.ITickList;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.LogicalSide;

import javax.annotation.Nullable;

/**
 * A wrapper for MobileChunks, used to give blocks accurate information about it's neighbors.
 */
public class FakeWorld extends World {

    private MobileChunk mobileChunk;

    private FakeWorld(boolean remote, World parentWorld) {
        super(parentWorld.getSaveHandler(), parentWorld.getSavedDataStorage(), parentWorld.getWorldInfo(),
                parentWorld.getDimension(), parentWorld.profiler, remote);
    }

    public static FakeWorld getFakeWorld(MobileChunk chunk) {
        FakeWorld retVal = new FakeWorld(chunk.world.isRemote, chunk.world);
        retVal.setMobileChunk(chunk);
        return retVal;
    }

    @Override
    public TileEntity getTileEntity(BlockPos pos) {
        return getMobileChunk().getTileEntity(pos);
    }

    @Override
    public IChunkProvider createChunkProvider() {
        return null;
    }

    @Override
    public IBlockState getBlockState(BlockPos pos) {
        return getMobileChunk().getBlockState(pos);
    }

    @Override
    public boolean setBlockState(BlockPos pos, IBlockState state) {
        getMobileChunk().setBlockState(pos, state);
        return false;
    }

    @Override
    public boolean setBlockState(BlockPos pos, IBlockState state, int flags) {
        getMobileChunk().setBlockState(pos, state);
        return false;
    }

    @Override
    public void setTileEntity(BlockPos pos, @Nullable TileEntity tileEntityIn) {
        getMobileChunk().setTileEntity(pos, tileEntityIn);
    }

    @Override
    public void markChunkDirty(BlockPos pos, TileEntity unusedTileEntity) {
        getMobileChunk().setChunkModified();
        if (getMobileChunk().side() == LogicalSide.SERVER) {
            getMobileChunk().markTileDirty(pos);
        }
    }

    private boolean isValidPosition(BlockPos pos) {
        return pos.getX() >= getMobileChunk().minX() && pos.getZ() >= getMobileChunk().minZ() && pos.getX() < getMobileChunk().maxX() && pos.getZ() < getMobileChunk().maxZ() && pos.getY() >= 0 && pos.getY() < getMobileChunk().maxY();
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
    public float getBrightness(BlockPos pos) {
        return this.getBlockState(pos).getLightValue(getMobileChunk(), pos);
    }

    @Override
    public long getGameTime() {
        return getMobileChunk().world.getGameTime();
    }

    @Override
    public long getDayTime() {
        return getMobileChunk().world.getDayTime();
    }

    @Override
    public Scoreboard getScoreboard() {
        return null;
    }

    @Override
    public RecipeManager getRecipeManager() {
        return null;
    }

    @Override
    public NetworkTagManager getTags() {
        return null;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public int getCombinedLight(BlockPos pos, int lightValue) {
        return getMobileChunk().getCombinedLight(pos, lightValue);
    }

    @Override
    public Chunk getChunk(int chunkX, int chunkZ) {
        return new FakeChunk(getMobileChunk(), chunkX, chunkZ, new Biome[]{mobileChunk.getCreationSpotBiome()});
    }

    public MobileChunk getMobileChunk() {
        return mobileChunk;
    }

    public void setMobileChunk(MobileChunk mobileChunk) {
        this.mobileChunk = mobileChunk;
    }

    @Override
    public ITickList<Block> getPendingBlockTicks() {
        return new EmptyTickList<>();
    }

    @Override
    public ITickList<Fluid> getPendingFluidTicks() {
        return new EmptyTickList<>();
    }
}
