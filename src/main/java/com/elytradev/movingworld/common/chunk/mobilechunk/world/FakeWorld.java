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
        return this.getMobileChunk().getTileEntity(pos);
    }

    @Override
    public IChunkProvider createChunkProvider() {
        return null;
    }

    @Override
    public IBlockState getBlockState(BlockPos pos) {
        return this.getMobileChunk().getBlockState(pos);
    }

    @Override
    public boolean setBlockState(BlockPos pos, IBlockState state) {
        this.getMobileChunk().setBlockState(pos, state);
        return false;
    }

    @Override
    public boolean setBlockState(BlockPos pos, IBlockState state, int flags) {
        this.getMobileChunk().setBlockState(pos, state);
        return false;
    }

    @Override
    public void setTileEntity(BlockPos pos, @Nullable TileEntity tileEntityIn) {
        this.getMobileChunk().setTileEntity(pos, tileEntityIn);
    }

    @Override
    public void markChunkDirty(BlockPos pos, TileEntity unusedTileEntity) {
        this.getMobileChunk().setChunkModified();
        if (this.getMobileChunk().side() == LogicalSide.SERVER) {
            this.getMobileChunk().markTileDirty(pos);
        }
    }

    private boolean isValidPosition(BlockPos pos) {
        return pos.getX() >= this.getMobileChunk().minX() && pos.getZ() >= this.getMobileChunk().minZ() && pos.getX() < this.getMobileChunk().maxX() && pos.getZ() < this.getMobileChunk().maxZ() && pos.getY() >= 0 && pos.getY() < this.getMobileChunk().maxY();
    }

    @Override
    public boolean isBlockLoaded(BlockPos pos, boolean allowEmpty) {
        return this.isValidPosition(pos);
    }

    @Override
    public boolean isChunkLoaded(int x, int z, boolean allowEmpty) {
        return this.isValidPosition(new BlockPos(x << 4, 0, z << 4));
    }

    @Override
    public float getBrightness(BlockPos pos) {
        return this.getBlockState(pos).getLightValue(this.getMobileChunk(), pos);
    }

    @Override
    public long getGameTime() {
        return this.getMobileChunk().world.getGameTime();
    }

    @Override
    public long getDayTime() {
        return this.getMobileChunk().world.getDayTime();
    }

    @Override
    public Scoreboard getScoreboard() {
        return this.getMobileChunk().world.getScoreboard();
    }

    @Override
    public RecipeManager getRecipeManager() {
        return this.getMobileChunk().world.getRecipeManager();
    }

    @Override
    public NetworkTagManager getTags() {
        return this.getMobileChunk().world.getTags();
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public int getCombinedLight(BlockPos pos, int lightValue) {
        return this.getMobileChunk().getNeighborAwareLightSubtracted(pos, lightValue);
    }

    @Override
    public Chunk getChunk(int chunkX, int chunkZ) {
        return new FakeChunk(this.getMobileChunk(), chunkX, chunkZ, new Biome[]{this.mobileChunk.getCreationSpotBiome()});
    }

    public MobileChunk getMobileChunk() {
        return this.mobileChunk;
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
