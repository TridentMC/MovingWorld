package com.tridevmc.movingworld.common.chunk.mobilechunk.world;

import com.tridevmc.movingworld.common.chunk.mobilechunk.FakeChunk;
import com.tridevmc.movingworld.common.chunk.mobilechunk.MobileChunk;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.tags.NetworkTagManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EmptyTickList;
import net.minecraft.world.IEnviromentBlockReader;
import net.minecraft.world.ITickList;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.LogicalSide;

import javax.annotation.Nullable;
import java.util.List;

/**
 * A wrapper for MobileChunks, used to give blocks accurate information about it's neighbors.
 */
public class FakeWorld extends World implements IEnviromentBlockReader {

    private MobileChunk mobileChunk;

    private FakeWorld(boolean remote, World parentWorld) {
        super(parentWorld.getWorldInfo(), parentWorld.getDimension().getType(), (world, dimension) -> parentWorld.getChunkProvider(), parentWorld.getProfiler(), remote);
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
    public BlockState getBlockState(BlockPos pos) {
        return this.getMobileChunk().getBlockState(pos);
    }

    @Override
    public void playSound(@Nullable PlayerEntity player, double x, double y, double z, SoundEvent soundIn, SoundCategory category, float volume, float pitch) {
        Vec3d at = mobileChunk.getWorldPosForChunkPos(new Vec3d(x, y, z));
        mobileChunk.world.playSound(player, at.x, at.y, at.z, soundIn, category, volume, pitch);
    }

    @Override
    public void playMovingSound(@Nullable PlayerEntity p_217384_1_, Entity p_217384_2_, SoundEvent p_217384_3_, SoundCategory p_217384_4_, float p_217384_5_, float p_217384_6_) {
        mobileChunk.world.playMovingSound(p_217384_1_, p_217384_2_, p_217384_3_, p_217384_4_, p_217384_5_, p_217384_6_);
    }

    @Override
    public boolean setBlockState(BlockPos pos, BlockState state) {
        return this.getMobileChunk().setBlockState(pos, state);
    }

    @Override
    public void notifyBlockUpdate(BlockPos pos, BlockState oldState, BlockState newState, int flags) {
    }

    @Override
    public boolean setBlockState(BlockPos pos, BlockState state, int flags) {
        return this.getMobileChunk().setBlockState(pos, state, flags);
    }

    @Override
    public void setTileEntity(BlockPos pos, @Nullable TileEntity tileEntityIn) {
        this.getMobileChunk().setTileEntity(pos, tileEntityIn);
    }

    @Nullable
    @Override
    public Entity getEntityByID(int id) {
        return null;
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

    @Nullable
    @Override
    public MapData func_217406_a(String p_217406_1_) {
        return mobileChunk.world.func_217406_a(p_217406_1_);
    }

    @Override
    public void func_217399_a(MapData p_217399_1_) {
        mobileChunk.world.func_217399_a(p_217399_1_);
    }

    @Override
    public int getNextMapId() {
        return mobileChunk.world.getNextMapId();
    }

    @Override
    public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress) {
        // NO-OP
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
        return new FakeChunk(this.getMobileChunk(), new ChunkPos(chunkX, chunkZ), new Biome[]{this.mobileChunk.getCreationSpotBiome()});
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

    @Override
    public void playEvent(@Nullable PlayerEntity player, int type, BlockPos pos, int data) {

    }

    @Override
    public List<? extends PlayerEntity> getPlayers() {
        return null;
    }
}
