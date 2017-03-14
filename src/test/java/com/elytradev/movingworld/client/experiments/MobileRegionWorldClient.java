package com.elytradev.movingworld.client.experiments;

import com.elytradev.concrete.reflect.invoker.Invoker;
import com.elytradev.concrete.reflect.invoker.Invokers;
import com.elytradev.movingworld.common.experiments.region.MobileRegion;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSetMultimap;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.profiler.Profiler;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.*;
import net.minecraft.village.VillageCollection;
import net.minecraft.world.*;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldInfo;
import net.minecraft.world.storage.loot.LootTableManager;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A wrapper for a MobileRegion and it's associated world,
 */
@SuppressWarnings("deprecation")
public class MobileRegionWorldClient extends WorldClient {

    public WorldClient parentWorld;
    public MobileRegion region;
    private Invoker initCapabilities;

    public MobileRegionWorldClient(NetHandlerPlayClient netHandler, WorldSettings settings, int dimension, EnumDifficulty difficulty, Profiler profilerIn, World parentWorld, MobileRegion region) {
        super(netHandler, settings, dimension, difficulty, profilerIn);
        this.parentWorld = (WorldClient) parentWorld;
        this.region = region;
    }

    public boolean isPosWithinRegion(BlockPos pos) {
        return region.isPosWithinBounds(pos);
    }

    @Override
    public void tick() {
        parentWorld.tick();
    }

    @Override
    public void invalidateBlockReceiveRegion(int x1, int y1, int z1, int x2, int y2, int z2) {
        parentWorld.invalidateBlockReceiveRegion(x1, y1, z1, x2, y2, z2);
    }    @Override
    public void initCapabilities() {
        if (initCapabilities == null) {
            initCapabilities = Invokers.findMethod(World.class, null, new String[]{"initCapabilities"});
        }

        if (parentWorld != null)
            initCapabilities.invoke(parentWorld);
        else
            super.initCapabilities();
    }

    @Override
    public IChunkProvider createChunkProvider() {
        if (parentWorld != null)
            return parentWorld.createChunkProvider();
        else
            return null;
    }

    @Override
    public boolean isChunkLoaded(int x, int z, boolean allowEmpty) {
        if (region.isChunkInRegion(x, z))
            return parentWorld.isChunkLoaded(x, z, allowEmpty);
        else
            return false;
    }

    @Override
    public void buildChunkCoordList() {
    }

    @Override
    public void updateBlocks() {
    }

    @Override
    public void doPreChunk(int chunkX, int chunkZ, boolean loadChunk) {
        if (region.isChunkInRegion(chunkX, chunkZ))
            parentWorld.doPreChunk(chunkX, chunkZ, loadChunk);
    }

    @Override
    public boolean spawnEntity(Entity entityIn) {
        return parentWorld.spawnEntity(entityIn);
    }

    @Override
    public void removeEntity(Entity entityIn) {
        parentWorld.removeEntity(entityIn);
    }

    @Override
    public void onEntityAdded(Entity entityIn) {
        parentWorld.onEntityAdded(entityIn);
    }

    @Override
    public void onEntityRemoved(Entity entityIn) {
        parentWorld.onEntityRemoved(entityIn);
    }

    @Override
    public void addEntityToWorld(int entityID, Entity entityToSpawn) {
        parentWorld.addEntityToWorld(entityID, entityToSpawn);
    }

    @Nullable
    @Override
    public Entity getEntityByID(int id) {
        return parentWorld.getEntityByID(id);
    }

    @Override
    public Entity removeEntityFromWorld(int entityID) {
        return parentWorld.removeEntityFromWorld(entityID);
    }

    @Override
    public boolean invalidateRegionAndSetBlock(BlockPos pos, IBlockState state) {
        if (region.isPosWithinBounds(pos))
            return parentWorld.invalidateRegionAndSetBlock(pos, state);
        else
            return false;
    }

    @Override
    public void sendQuittingDisconnectingPacket() {
    }

    @Override
    public void updateWeather() {
        parentWorld.updateWeather();
    }

    @Override
    public void playMoodSoundAndCheckLight(int p_147467_1_, int p_147467_2_, Chunk chunkIn) {
        parentWorld.playMoodSoundAndCheckLight(p_147467_1_, p_147467_2_, chunkIn);
    }

    @Override
    public void doVoidFogParticles(int posX, int posY, int posZ) {
    }

    @Override
    public void showBarrierParticles(int p_184153_1_, int p_184153_2_, int p_184153_3_, int p_184153_4_, Random random, boolean p_184153_6_, BlockPos.MutableBlockPos pos) {
    }

    @Override
    public void removeAllEntities() {
        parentWorld.removeAllEntities();
    }

    @Override
    public CrashReportCategory addWorldInfoToCrashReport(CrashReport report) {
        return parentWorld.addWorldInfoToCrashReport(report);
    }

    @Override
    public void playSound(@Nullable EntityPlayer player, double x, double y, double z, SoundEvent soundIn, SoundCategory category, float volume, float pitch) {
        Vec3d pos = new Vec3d(x, y, z);
        pos = region.convertRegionPosToRealWorld(pos);
        parentWorld.playSound(player, pos.xCoord, pos.yCoord, pos.zCoord, soundIn, category, volume, pitch);
    }

    @Override
    public void playSound(BlockPos blockPos, SoundEvent soundIn, SoundCategory category, float volume, float pitch, boolean distanceDelay) {
        Vec3d pos = new Vec3d(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        pos = region.convertRegionPosToRealWorld(pos);
        blockPos = new BlockPos(Math.round(pos.xCoord), Math.round(pos.yCoord), Math.round(pos.zCoord));
        parentWorld.playSound(blockPos, soundIn, category, volume, pitch, distanceDelay);
    }

    @Override
    public void playSound(double x, double y, double z, SoundEvent soundIn, SoundCategory category, float volume, float pitch, boolean distanceDelay) {
        Vec3d pos = new Vec3d(x, y, z);
        pos = region.convertRegionPosToRealWorld(pos);
        parentWorld.playSound(pos.xCoord, pos.yCoord, pos.zCoord, soundIn, category, volume, pitch, distanceDelay);
    }

    @Override
    public void makeFireworks(double x, double y, double z, double motionX, double motionY, double motionZ, @Nullable NBTTagCompound compund) {
        Vec3d pos = new Vec3d(x, y, z);
        pos = region.convertRegionPosToRealWorld(pos);
        parentWorld.makeFireworks(pos.xCoord, pos.yCoord, pos.zCoord, motionX, motionY, motionZ, compund);
    }

    @Override
    public void sendPacketToServer(Packet<?> packetIn) {
        parentWorld.sendPacketToServer(packetIn);
    }

    @Override
    public void setWorldScoreboard(Scoreboard scoreboardIn) {
        parentWorld.setWorldScoreboard(scoreboardIn);
    }

    @Override
    public World init() {
        return parentWorld.init();
    }

    @Override
    public Biome getBiome(BlockPos pos) {
        pos = region.convertRegionPosToRealWorld(pos);
        return getRealWorld().getBiome(pos);
    }

    @Override
    public Biome getBiomeForCoordsBody(BlockPos pos) {
        pos = region.convertRegionPosToRealWorld(pos);
        return getRealWorld().getBiomeForCoordsBody(pos);
    }

    @Override
    public BiomeProvider getBiomeProvider() {
        return getRealWorld().getBiomeProvider();
    }

    @Override
    public void initialize(WorldSettings settings) {
        parentWorld.initialize(settings);
    }    @Override
    public ChunkProviderClient getChunkProvider() {
        return parentWorld.getChunkProvider();
    }

    @Nullable
    @Override
    public MinecraftServer getMinecraftServer() {
        return parentWorld.getMinecraftServer();
    }

    @Override
    public void setInitialSpawnLocation() {
        parentWorld.setInitialSpawnLocation();
    }

    @Override
    public IBlockState getGroundAboveSeaLevel(BlockPos pos) {
        return parentWorld.getGroundAboveSeaLevel(pos);
    }

    @Override
    public boolean isAirBlock(BlockPos pos) {
        return parentWorld.isAirBlock(pos);
    }

    @Override
    public boolean isBlockLoaded(BlockPos pos) {
        return parentWorld.isBlockLoaded(pos);
    }

    @Override
    public boolean isBlockLoaded(BlockPos pos, boolean allowEmpty) {
        return parentWorld.isBlockLoaded(pos, allowEmpty);
    }

    @Override
    public boolean isAreaLoaded(int xStart, int yStart, int zStart, int xEnd, int yEnd, int zEnd, boolean allowEmpty) {
        BlockPos start = new BlockPos(xStart, yStart, zStart);
        BlockPos end = new BlockPos(xEnd, yEnd, zEnd);

        if (isPosWithinRegion(start) && isPosWithinRegion(end))
            return parentWorld.isAreaLoaded(xStart, yStart, zStart, xEnd, yEnd, zEnd, allowEmpty);
        else
            return false;
    }

    @Override
    public Chunk getChunkFromBlockCoords(BlockPos pos) {
        if (isPosWithinRegion(pos))
            return parentWorld.getChunkFromBlockCoords(pos);
        else return null;
    }

    @Override
    public Chunk getChunkFromChunkCoords(int chunkX, int chunkZ) {
        return parentWorld.getChunkFromChunkCoords(chunkX, chunkZ);
    }

    @Override
    public boolean isChunkGeneratedAt(int x, int z) {
        return parentWorld.isChunkGeneratedAt(x, z);
    }

    @Override
    public boolean setBlockState(BlockPos pos, IBlockState newState, int flags) {
        return parentWorld.setBlockState(pos, newState, flags);
    }

    @Override
    public void markAndNotifyBlock(BlockPos pos, @Nullable Chunk chunk, IBlockState iblockstate, IBlockState newState, int flags) {
        parentWorld.markAndNotifyBlock(pos, chunk, iblockstate, newState, flags);
    }

    @Override
    public boolean setBlockToAir(BlockPos pos) {
        return parentWorld.setBlockToAir(pos);
    }

    @Override
    public boolean destroyBlock(BlockPos pos, boolean dropBlock) {
        return parentWorld.destroyBlock(pos, dropBlock);
    }

    @Override
    public boolean setBlockState(BlockPos pos, IBlockState state) {
        return parentWorld.setBlockState(pos, state);
    }

    @Override
    public void notifyBlockUpdate(BlockPos pos, IBlockState oldState, IBlockState newState, int flags) {
        parentWorld.notifyBlockUpdate(pos, oldState, newState, flags);
    }

    @Override
    public void notifyNeighborsRespectDebug(BlockPos pos, Block blockType, boolean p_175722_3_) {
        parentWorld.notifyNeighborsRespectDebug(pos, blockType, p_175722_3_);
    }

    @Override
    public void markBlocksDirtyVertical(int x1, int z1, int x2, int z2) {
        parentWorld.markBlocksDirtyVertical(x1, z1, x2, z2);
    }

    @Override
    public void markBlockRangeForRenderUpdate(BlockPos rangeMin, BlockPos rangeMax) {
        parentWorld.markBlockRangeForRenderUpdate(rangeMin, rangeMax);
    }

    @Override
    public void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2, int y2, int z2) {
        parentWorld.markBlockRangeForRenderUpdate(x1, y1, z1, x2, y2, z2);
    }

    @Override
    public void updateObservingBlocksAt(BlockPos pos, Block blockType) {
        parentWorld.updateObservingBlocksAt(pos, blockType);
    }

    @Override
    public void notifyNeighborsOfStateChange(BlockPos pos, Block blockType, boolean updateObservers) {
        parentWorld.notifyNeighborsOfStateChange(pos, blockType, updateObservers);
    }

    @Override
    public void notifyNeighborsOfStateExcept(BlockPos pos, Block blockType, EnumFacing skipSide) {
        parentWorld.notifyNeighborsOfStateExcept(pos, blockType, skipSide);
    }

    @Override
    public void neighborChanged(BlockPos pos, Block p_190524_2_, BlockPos p_190524_3_) {
        parentWorld.neighborChanged(pos, p_190524_2_, p_190524_3_);
    }

    @Override
    public void observedNeighborChanged(BlockPos pos, Block p_190529_2_, BlockPos p_190529_3_) {
        parentWorld.observedNeighborChanged(pos, p_190529_2_, p_190529_3_);
    }

    @Override
    public boolean isBlockTickPending(BlockPos pos, Block blockType) {
        return parentWorld.isBlockTickPending(pos, blockType);
    }

    @Override
    public boolean canSeeSky(BlockPos pos) {
        return parentWorld.canSeeSky(pos);
    }

    @Override
    public boolean canBlockSeeSky(BlockPos pos) {
        return parentWorld.canBlockSeeSky(pos);
    }

    @Override
    public int getLight(BlockPos pos) {
        return parentWorld.getLight(pos);
    }

    @Override
    public int getLightFromNeighbors(BlockPos pos) {
        return parentWorld.getLightFromNeighbors(pos);
    }

    @Override
    public int getLight(BlockPos pos, boolean checkNeighbors) {
        return parentWorld.getLight(pos, checkNeighbors);
    }

    @Override
    public BlockPos getHeight(BlockPos pos) {
        return parentWorld.getHeight(pos);
    }

    @Override
    public int getHeight(int x, int z) {
        return parentWorld.getHeight(x, z);
    }

    @Override
    public int getChunksLowestHorizon(int x, int z) {
        return parentWorld.getChunksLowestHorizon(x, z);
    }

    @Override
    public int getLightFromNeighborsFor(EnumSkyBlock type, BlockPos pos) {
        if (isPosWithinRegion(pos))
            return parentWorld.getLightFromNeighborsFor(type, pos);
        else
            return 0;
    }

    @Override
    public int getLightFor(EnumSkyBlock type, BlockPos pos) {
        if (isPosWithinRegion(pos))
            return parentWorld.getLightFor(type, pos);
        else
            return 0;
    }

    @Override
    public void setLightFor(EnumSkyBlock type, BlockPos pos, int lightValue) {
        if (isPosWithinRegion(pos))
            parentWorld.setLightFor(type, pos, lightValue);
    }

    @Override
    public void notifyLightSet(BlockPos pos) {
        if (isPosWithinRegion(pos))
            parentWorld.notifyLightSet(pos);
    }

    @Override
    public int getCombinedLight(BlockPos pos, int lightValue) {
        if (isPosWithinRegion(pos))
            return parentWorld.getCombinedLight(pos, lightValue);
        else
            return 0;
    }

    @Override
    public float getLightBrightness(BlockPos pos) {
        if (isPosWithinRegion(pos))
            return parentWorld.getLightBrightness(pos);
        else
            return 0F;
    }

    @Override
    public IBlockState getBlockState(BlockPos pos) {
        if (isPosWithinRegion(pos))
            return parentWorld.getBlockState(pos);
        else
            return Blocks.AIR.getDefaultState();
    }

    @Override
    public boolean isDaytime() {
        return parentWorld.isDaytime();
    }

    @Nullable
    @Override
    public RayTraceResult rayTraceBlocks(Vec3d start, Vec3d end) {
        return parentWorld.rayTraceBlocks(start, end);
    }

    @Nullable
    @Override
    public RayTraceResult rayTraceBlocks(Vec3d start, Vec3d end, boolean stopOnLiquid) {
        return parentWorld.rayTraceBlocks(start, end, stopOnLiquid);
    }

    @Nullable
    @Override
    public RayTraceResult rayTraceBlocks(Vec3d vec31, Vec3d vec32, boolean stopOnLiquid, boolean ignoreBlockWithoutBoundingBox, boolean returnLastUncollidableBlock) {
        return parentWorld.rayTraceBlocks(vec31, vec32, stopOnLiquid, ignoreBlockWithoutBoundingBox, returnLastUncollidableBlock);
    }

    @Override
    public void playSound(@Nullable EntityPlayer player, BlockPos pos, SoundEvent soundIn, SoundCategory category, float volume, float pitch) {
        getRealWorld().playSound(player, pos, soundIn, category, volume, pitch);
    }

    @Override
    public void playRecord(BlockPos blockPositionIn, @Nullable SoundEvent soundEventIn) {
        getRealWorld().playRecord(blockPositionIn, soundEventIn);
    }

    @Override
    public void spawnParticle(EnumParticleTypes particleType, double xCoord, double yCoord, double zCoord, double xSpeed, double ySpeed, double zSpeed, int... parameters) {
        getRealWorld().spawnParticle(particleType, xCoord, yCoord, zCoord, xSpeed, ySpeed, zSpeed, parameters);
    }

    @Override
    public void spawnAlwaysVisibleParticle(int p_190523_1_, double p_190523_2_, double p_190523_4_, double p_190523_6_, double p_190523_8_, double p_190523_10_, double p_190523_12_, int... p_190523_14_) {
        getRealWorld().spawnAlwaysVisibleParticle(p_190523_1_, p_190523_2_, p_190523_4_, p_190523_6_, p_190523_8_, p_190523_10_, p_190523_12_, p_190523_14_);
    }

    @Override
    public void spawnParticle(EnumParticleTypes particleType, boolean ignoreRange, double xCoord, double yCoord, double zCoord, double xSpeed, double ySpeed, double zSpeed, int... parameters) {
        getRealWorld().spawnParticle(particleType, ignoreRange, xCoord, yCoord, zCoord, xSpeed, ySpeed, zSpeed, parameters);
    }

    @Override
    public boolean addWeatherEffect(Entity entityIn) {
        return getRealWorld().addWeatherEffect(entityIn);
    }

    @Override
    public void removeEntityDangerously(Entity entityIn) {
        parentWorld.removeEntityDangerously(entityIn);
    }

    @Override
    public void addEventListener(IWorldEventListener listener) {
        parentWorld.addEventListener(listener);
    }

    @Override
    public List<AxisAlignedBB> getCollisionBoxes(@Nullable Entity entityIn, AxisAlignedBB aabb) {
        return parentWorld.getCollisionBoxes(entityIn, aabb);
    }

    @Override
    public void removeEventListener(IWorldEventListener listener) {
        parentWorld.removeEventListener(listener);
    }

    @Override
    public boolean collidesWithAnyBlock(AxisAlignedBB bbox) {
        return parentWorld.collidesWithAnyBlock(bbox);
    }

    @Override
    public int calculateSkylightSubtracted(float partialTicks) {
        if (parentWorld != null)
            return parentWorld.calculateSkylightSubtracted(partialTicks);
        else
            return super.calculateSkylightSubtracted(partialTicks);
    }

    @Override
    public float getSunBrightnessFactor(float partialTicks) {
        if (getRealWorld() != null)
            return getRealWorld().getSunBrightnessFactor(partialTicks);
        else
            return super.getSunBrightnessFactor(partialTicks);
    }

    @Override
    public float getSunBrightness(float partialTicks) {
        return getRealWorld().getSunBrightness(partialTicks);
    }

    @Override
    public float getSunBrightnessBody(float partialTicks) {
        return getRealWorld().getSunBrightnessBody(partialTicks);
    }

    @Override
    public Vec3d getSkyColor(Entity entityIn, float partialTicks) {
        return getRealWorld().getSkyColor(entityIn, partialTicks);
    }

    @Override
    public Vec3d getSkyColorBody(Entity entityIn, float partialTicks) {
        return getRealWorld().getSkyColorBody(entityIn, partialTicks);
    }

    @Override
    public float getCelestialAngle(float partialTicks) {
        if (getRealWorld() != null)
            return getRealWorld().getCelestialAngle(partialTicks);
        else return super.getCelestialAngle(partialTicks);
    }

    @Override
    public int getMoonPhase() {
        return getRealWorld().getMoonPhase();
    }

    @Override
    public float getCurrentMoonPhaseFactor() {
        return getRealWorld().getCurrentMoonPhaseFactor();
    }

    @Override
    public float getCurrentMoonPhaseFactorBody() {
        return getRealWorld().getCurrentMoonPhaseFactorBody();
    }

    @Override
    public float getCelestialAngleRadians(float partialTicks) {
        return getRealWorld().getCelestialAngleRadians(partialTicks);
    }

    @Override
    public Vec3d getCloudColour(float partialTicks) {
        return getRealWorld().getCloudColour(partialTicks);
    }

    @Override
    public Vec3d getCloudColorBody(float partialTicks) {
        return getRealWorld().getCloudColorBody(partialTicks);
    }

    @Override
    public Vec3d getFogColor(float partialTicks) {
        return getRealWorld().getFogColor(partialTicks);
    }

    @Override
    public BlockPos getPrecipitationHeight(BlockPos pos) {
        return getRealWorld().getPrecipitationHeight(pos);
    }

    @Override
    public BlockPos getTopSolidOrLiquidBlock(BlockPos pos) {
        return getRealWorld().getTopSolidOrLiquidBlock(pos);
    }

    @Override
    public float getStarBrightness(float partialTicks) {
        return getRealWorld().getStarBrightness(partialTicks);
    }

    @Override
    public float getStarBrightnessBody(float partialTicks) {
        return getRealWorld().getStarBrightnessBody(partialTicks);
    }

    @Override
    public boolean isUpdateScheduled(BlockPos pos, Block blk) {
        return parentWorld.isUpdateScheduled(pos, blk);
    }

    @Override
    public void scheduleUpdate(BlockPos pos, Block blockIn, int delay) {
        parentWorld.scheduleUpdate(pos, blockIn, delay);
    }

    @Override
    public void updateBlockTick(BlockPos pos, Block blockIn, int delay, int priority) {
        parentWorld.updateBlockTick(pos, blockIn, delay, priority);
    }

    @Override
    public void scheduleBlockUpdate(BlockPos pos, Block blockIn, int delay, int priority) {
        parentWorld.scheduleBlockUpdate(pos, blockIn, delay, priority);
    }

    @Override
    public void updateEntities() {
    }

    @Override
    public void tickPlayers() {
    }

    @Override
    public boolean addTileEntity(TileEntity tile) {
        return parentWorld.addTileEntity(tile);
    }

    @Override
    public void addTileEntities(Collection<TileEntity> tileEntityCollection) {
        parentWorld.addTileEntities(tileEntityCollection);
    }

    @Override
    public void updateEntity(Entity ent) {
        parentWorld.updateEntity(ent);
    }

    @Override
    public void updateEntityWithOptionalForce(Entity entityIn, boolean forceUpdate) {
        parentWorld.updateEntityWithOptionalForce(entityIn, forceUpdate);
    }

    @Override
    public boolean checkNoEntityCollision(AxisAlignedBB bb) {
        return parentWorld.checkNoEntityCollision(bb);
    }

    @Override
    public boolean checkNoEntityCollision(AxisAlignedBB bb, @Nullable Entity entityIn) {
        return parentWorld.checkNoEntityCollision(bb, entityIn);
    }

    @Override
    public boolean checkBlockCollision(AxisAlignedBB bb) {
        return parentWorld.checkBlockCollision(bb);
    }

    @Override
    public boolean containsAnyLiquid(AxisAlignedBB bb) {
        return parentWorld.containsAnyLiquid(bb);
    }

    @Override
    public boolean isFlammableWithin(AxisAlignedBB bb) {
        return parentWorld.isFlammableWithin(bb);
    }

    @Override
    public boolean handleMaterialAcceleration(AxisAlignedBB bb, Material materialIn, Entity entityIn) {
        return parentWorld.handleMaterialAcceleration(bb, materialIn, entityIn);
    }

    @Override
    public boolean isMaterialInBB(AxisAlignedBB bb, Material materialIn) {
        return parentWorld.isMaterialInBB(bb, materialIn);
    }

    @Override
    public Explosion createExplosion(@Nullable Entity entityIn, double x, double y, double z, float strength, boolean isSmoking) {
        return parentWorld.createExplosion(entityIn, x, y, z, strength, isSmoking);
    }

    @Override
    public Explosion newExplosion(@Nullable Entity entityIn, double x, double y, double z, float strength, boolean isFlaming, boolean isSmoking) {
        return parentWorld.newExplosion(entityIn, x, y, z, strength, isFlaming, isSmoking);
    }

    @Override
    public float getBlockDensity(Vec3d vec, AxisAlignedBB bb) {
        return parentWorld.getBlockDensity(vec, bb);
    }

    @Override
    public boolean extinguishFire(@Nullable EntityPlayer player, BlockPos pos, EnumFacing side) {
        return parentWorld.extinguishFire(player, pos, side);
    }

    @Override
    public String getDebugLoadedEntities() {
        return parentWorld.getDebugLoadedEntities();
    }

    @Override
    public String getProviderName() {
        return parentWorld.getProviderName();
    }

    @Nullable
    @Override
    public TileEntity getTileEntity(BlockPos pos) {
        if (isPosWithinRegion(pos))
            return parentWorld.getTileEntity(pos);
        else
            return null;
    }

    @Override
    public void setTileEntity(BlockPos pos, @Nullable TileEntity tileEntityIn) {
        if (isPosWithinRegion(pos))
            parentWorld.setTileEntity(pos, tileEntityIn);
    }

    @Override
    public void removeTileEntity(BlockPos pos) {
        if (isPosWithinRegion(pos))
            parentWorld.removeTileEntity(pos);
    }

    @Override
    public void markTileEntityForRemoval(TileEntity tileEntityIn) {
        parentWorld.markTileEntityForRemoval(tileEntityIn);
    }

    @Override
    public boolean isBlockFullCube(BlockPos pos) {
        if (isPosWithinRegion(pos))
            return parentWorld.isBlockFullCube(pos);
        else return false;
    }

    @Override
    public boolean isBlockNormalCube(BlockPos pos, boolean _default) {
        if (isPosWithinRegion(pos))
            return parentWorld.isBlockNormalCube(pos, _default);
        else
            return false;
    }

    @Override
    public void calculateInitialSkylight() {
        if (parentWorld != null)
            parentWorld.calculateInitialSkylight();
        else
            super.calculateInitialSkylight();
    }

    @Override
    public void setAllowedSpawnTypes(boolean hostile, boolean peaceful) {
        parentWorld.setAllowedSpawnTypes(hostile, peaceful);
    }

    @Override
    public void calculateInitialWeather() {
        if (parentWorld != null)
            parentWorld.calculateInitialWeather();
        else
            super.calculateInitialWeather();
    }

    @Override
    public void calculateInitialWeatherBody() {
        if (parentWorld != null)
            parentWorld.calculateInitialWeatherBody();
        else
            super.calculateInitialWeatherBody();
    }

    @Override
    public void updateWeatherBody() {
        parentWorld.updateWeatherBody();
    }

    @Override
    public void immediateBlockTick(BlockPos pos, IBlockState state, Random random) {
        if (isPosWithinRegion(pos))
            parentWorld.immediateBlockTick(pos, state, random);
    }

    @Override
    public boolean canBlockFreezeWater(BlockPos pos) {
        if (isPosWithinRegion(pos))
            return parentWorld.canBlockFreezeWater(pos);
        else
            return false;
    }

    @Override
    public boolean canBlockFreezeNoWater(BlockPos pos) {
        if (isPosWithinRegion(pos))
            return parentWorld.canBlockFreezeNoWater(pos);
        else
            return false;
    }

    @Override
    public boolean canBlockFreeze(BlockPos pos, boolean noWaterAdj) {
        if (isPosWithinRegion(pos))
            return parentWorld.canBlockFreeze(pos, noWaterAdj);
        else
            return false;
    }

    @Override
    public boolean canBlockFreezeBody(BlockPos pos, boolean noWaterAdj) {
        if (isPosWithinRegion(pos))
            return parentWorld.canBlockFreezeBody(pos, noWaterAdj);
        else
            return false;
    }

    @Override
    public boolean canSnowAt(BlockPos pos, boolean checkLight) {
        if (isPosWithinRegion(pos)) {
            pos = region.convertRegionPosToRealWorld(pos);
            return getRealWorld().canSnowAt(pos, checkLight);
        } else {
            return false;
        }
    }

    @Override
    public boolean canSnowAtBody(BlockPos pos, boolean checkLight) {
        if (isPosWithinRegion(pos)) {
            pos = region.convertRegionPosToRealWorld(pos);
            return getRealWorld().canSnowAtBody(pos, checkLight);
        } else {
            return false;
        }
    }

    @Override
    public boolean checkLight(BlockPos pos) {
        if (isPosWithinRegion(pos))
            return parentWorld.checkLight(pos);
        else
            return false;
    }

    @Override
    public boolean checkLightFor(EnumSkyBlock lightType, BlockPos pos) {
        if (isPosWithinRegion(pos))
            return parentWorld.checkLightFor(lightType, pos);
        else
            return false;
    }

    @Override
    public boolean tickUpdates(boolean runAllPending) {
        return parentWorld.tickUpdates(runAllPending);
    }

    @Nullable
    @Override
    public List<NextTickListEntry> getPendingBlockUpdates(Chunk chunkIn, boolean p_72920_2_) {
        return parentWorld.getPendingBlockUpdates(chunkIn, p_72920_2_);
    }

    @Nullable
    @Override
    public List<NextTickListEntry> getPendingBlockUpdates(StructureBoundingBox structureBB, boolean p_175712_2_) {
        return parentWorld.getPendingBlockUpdates(structureBB, p_175712_2_);
    }

    @Override
    public List<Entity> getEntitiesWithinAABBExcludingEntity(@Nullable Entity entityIn, AxisAlignedBB bb) {
        return getRealWorld().getEntitiesWithinAABBExcludingEntity(entityIn, region.convertRegionBBToRealWorld(bb));
    }

    @Override
    public List<Entity> getEntitiesInAABBexcluding(@Nullable Entity entityIn, AxisAlignedBB bb, @Nullable Predicate<? super Entity> predicate) {
        return getRealWorld().getEntitiesInAABBexcluding(entityIn, region.convertRegionBBToRealWorld(bb), predicate);
    }

    @Override
    public <T extends Entity> List<T> getEntities(Class<? extends T> entityType, Predicate<? super T> filter) {
        return getRealWorld().getEntities(entityType, filter);
    }

    @Override
    public <T extends Entity> List<T> getPlayers(Class<? extends T> playerType, Predicate<? super T> filter) {
        return getRealWorld().getPlayers(playerType, filter);
    }

    @Override
    public <T extends Entity> List<T> getEntitiesWithinAABB(Class<? extends T> classEntity, AxisAlignedBB bb) {
        return getRealWorld().getEntitiesWithinAABB(classEntity, region.convertRegionBBToRealWorld(bb));
    }

    @Override
    public <T extends Entity> List<T> getEntitiesWithinAABB(Class<? extends T> clazz, AxisAlignedBB bb, @Nullable Predicate<? super T> filter) {
        return getRealWorld().getEntitiesWithinAABB(clazz, region.convertRegionBBToRealWorld(bb), filter);
    }

    @Nullable
    @Override
    public <T extends Entity> T findNearestEntityWithinAABB(Class<? extends T> entityType, AxisAlignedBB bb, T closestTo) {
        return getRealWorld().findNearestEntityWithinAABB(entityType, region.convertRegionBBToRealWorld(bb), closestTo);
    }

    @Override
    public List<Entity> getLoadedEntityList() {
        return getRealWorld().getLoadedEntityList();
    }

    @Override
    public void markChunkDirty(BlockPos pos, TileEntity unusedTileEntity) {
        parentWorld.markChunkDirty(pos, unusedTileEntity);
    }

    @Override
    public int countEntities(Class<?> entityType) {
        return getRealWorld().countEntities(entityType);
    }

    @Override
    public void loadEntities(Collection<Entity> entityCollection) {
        Collection<Entity> movedEntityCollection =
                entityCollection.stream().map(entity -> {
                    Vec3d entityPos = new Vec3d(entity.posX, entity.posY, entity.posZ);
                    entityPos = region.convertRegionPosToRealWorld(entityPos);
                    entity.setPosition(entityPos.xCoord, entityPos.yCoord, entityPos.zCoord);
                    return entity;
                }).collect(Collectors.toList());

        getRealWorld().loadEntities(movedEntityCollection);
    }

    @Override
    public void unloadEntities(Collection<Entity> entityCollection) {
        getRealWorld().unloadEntities(entityCollection);
    }

    @Override
    public boolean mayPlace(Block p_190527_1_, BlockPos pos, boolean p_190527_3_, EnumFacing p_190527_4_, @Nullable Entity p_190527_5_) {
        if (isPosWithinRegion(pos))
            return parentWorld.mayPlace(p_190527_1_, pos, p_190527_3_, p_190527_4_, p_190527_5_);
        else
            return false;
    }

    @Override
    public int getSeaLevel() {
        return parentWorld.getSeaLevel();
    }

    @Override
    public void setSeaLevel(int seaLevelIn) {
        parentWorld.setSeaLevel(seaLevelIn);
    }

    @Override
    public int getStrongPower(BlockPos pos, EnumFacing direction) {
        return parentWorld.getStrongPower(pos, direction);
    }

    @Override
    public WorldType getWorldType() {
        return parentWorld.getWorldType();
    }

    @Override
    public int getStrongPower(BlockPos pos) {
        return parentWorld.getStrongPower(pos);
    }

    @Override
    public boolean isSidePowered(BlockPos pos, EnumFacing side) {
        return parentWorld.isSidePowered(pos, side);
    }

    @Override
    public int getRedstonePower(BlockPos pos, EnumFacing facing) {
        return parentWorld.getRedstonePower(pos, facing);
    }

    @Override
    public boolean isBlockPowered(BlockPos pos) {
        return parentWorld.isBlockPowered(pos);
    }

    @Override
    public int isBlockIndirectlyGettingPowered(BlockPos pos) {
        return parentWorld.isBlockIndirectlyGettingPowered(pos);
    }

    @Nullable
    @Override
    public EntityPlayer getClosestPlayerToEntity(Entity entityIn, double distance) {
        return getRealWorld().getClosestPlayerToEntity(entityIn, distance);
    }

    @Nullable
    @Override
    public EntityPlayer getNearestPlayerNotCreative(Entity entityIn, double distance) {
        return getRealWorld().getNearestPlayerNotCreative(entityIn, distance);
    }

    @Nullable
    @Override
    public EntityPlayer getClosestPlayer(double posX, double posY, double posZ, double distance, boolean spectator) {
        Vec3d pos = new Vec3d(posX, posY, posZ);
        pos = region.convertRegionPosToRealWorld(pos);

        return getRealWorld().getClosestPlayer(pos.xCoord, pos.yCoord, pos.zCoord, distance, spectator);
    }

    @Nullable
    @Override
    public EntityPlayer getClosestPlayer(double x, double y, double z, double distance, Predicate<Entity> predicate) {
        Vec3d pos = new Vec3d(x, y, z);
        pos = region.convertRegionPosToRealWorld(pos);

        return getRealWorld().getClosestPlayer(pos.xCoord, pos.yCoord, pos.zCoord, distance, predicate);
    }

    @Override
    public boolean isAnyPlayerWithinRangeAt(double x, double y, double z, double range) {
        Vec3d pos = new Vec3d(x, y, z);
        pos = region.convertRegionPosToRealWorld(pos);

        return getRealWorld().isAnyPlayerWithinRangeAt(pos.xCoord, pos.yCoord, pos.zCoord, range);
    }

    @Nullable
    @Override
    public EntityPlayer getNearestAttackablePlayer(Entity entityIn, double maxXZDistance, double maxYDistance) {
        return getRealWorld().getNearestAttackablePlayer(entityIn, maxXZDistance, maxYDistance);
    }

    @Nullable
    @Override
    public EntityPlayer getNearestAttackablePlayer(BlockPos pos, double maxXZDistance, double maxYDistance) {
        return getRealWorld().getNearestAttackablePlayer(region.convertRegionPosToRealWorld(pos), maxXZDistance, maxYDistance);
    }

    @Nullable
    @Override
    public EntityPlayer getNearestAttackablePlayer(double x, double y, double z, double maxXZDistance, double maxYDistance, @Nullable Function<EntityPlayer, Double> playerToDouble, @Nullable Predicate<EntityPlayer> p_184150_12_) {
        Vec3d pos = new Vec3d(x, y, z);
        pos = region.convertRegionPosToRealWorld(pos);
        return parentWorld.getNearestAttackablePlayer(pos.xCoord, pos.yCoord, pos.zCoord, maxXZDistance, maxYDistance, playerToDouble, p_184150_12_);
    }

    @Nullable
    @Override
    public EntityPlayer getPlayerEntityByName(String name) {
        return getRealWorld().getPlayerEntityByName(name);
    }

    @Nullable
    @Override
    public EntityPlayer getPlayerEntityByUUID(UUID uuid) {
        return getRealWorld().getPlayerEntityByUUID(uuid);
    }

    @Override
    public void checkSessionLock() throws MinecraftException {
        parentWorld.checkSessionLock();
    }

    public WorldClient getRealWorld() {
        return Minecraft.getMinecraft().world;
    }




    @Override
    public long getSeed() {
        return parentWorld.getSeed();
    }

    @Override
    public long getTotalWorldTime() {
        return getRealWorld().getTotalWorldTime();
    }

    @Override
    public void setTotalWorldTime(long worldTime) {
        getRealWorld().setTotalWorldTime(worldTime);
    }

    @Override
    public long getWorldTime() {
        return getRealWorld().getWorldTime();
    }

    @Override
    public void setWorldTime(long time) {
        getRealWorld().setWorldTime(time);
    }

    @Override
    public BlockPos getSpawnPoint() {
        return parentWorld.getSpawnPoint();
    }

    @Override
    public void setSpawnPoint(BlockPos pos) {
        if (parentWorld != null)
            parentWorld.setSpawnPoint(pos);
    }

    @Override
    public void joinEntityInSurroundings(Entity entityIn) {
        Vec3d entityPos = new Vec3d(entityIn.posX, entityIn.posY, entityIn.posZ);
        entityPos = region.convertRegionPosToRealWorld(entityPos);
        entityIn.setPosition(entityPos.xCoord, entityPos.yCoord, entityPos.zCoord);
        getRealWorld().joinEntityInSurroundings(entityIn);
    }

    @Override
    public boolean isBlockModifiable(EntityPlayer player, BlockPos pos) {
        return parentWorld.isBlockModifiable(player, pos);
    }

    @Override
    public boolean canMineBlockBody(EntityPlayer player, BlockPos pos) {
        return parentWorld.canMineBlockBody(player, pos);
    }

    @Override
    public void setEntityState(Entity entityIn, byte state) {
        parentWorld.setEntityState(entityIn, state);
    }

    @Override
    public void addBlockEvent(BlockPos pos, Block blockIn, int eventID, int eventParam) {
        parentWorld.addBlockEvent(pos, blockIn, eventID, eventParam);
    }

    @Override
    public ISaveHandler getSaveHandler() {
        return parentWorld.getSaveHandler();
    }

    @Override
    public WorldInfo getWorldInfo() {
        if (parentWorld != null)
            return parentWorld.getWorldInfo();
        else
            return worldInfo;
    }

    @Override
    public GameRules getGameRules() {
        return getRealWorld().getGameRules();
    }

    @Override
    public void updateAllPlayersSleepingFlag() {
        getRealWorld().updateAllPlayersSleepingFlag();
    }

    @Override
    public float getThunderStrength(float delta) {
        if (getRealWorld() != null)
            return getRealWorld().getThunderStrength(delta);
        else
            return super.getThunderStrength(delta);
    }

    @Override
    public void setThunderStrength(float strength) {
        getRealWorld().setThunderStrength(strength);
    }

    @Override
    public float getRainStrength(float delta) {
        if (getRealWorld() != null)
            return getRealWorld().getRainStrength(delta);
        else
            return super.getRainStrength(delta);
    }

    @Override
    public void setRainStrength(float strength) {
        getRealWorld().setRainStrength(strength);
    }

    @Override
    public boolean isThundering() {
        return getRealWorld().isThundering();
    }

    @Override
    public boolean isRaining() {
        return getRealWorld().isRaining();
    }

    @Override
    public boolean isRainingAt(BlockPos strikePosition) {
        strikePosition = region.convertRegionPosToRealWorld(strikePosition);

        return getRealWorld().isRainingAt(strikePosition);
    }

    @Override
    public boolean isBlockinHighHumidity(BlockPos pos) {
        pos = region.convertRegionPosToRealWorld(pos);

        return getRealWorld().isBlockinHighHumidity(pos);
    }

    @Nullable
    @Override
    public MapStorage getMapStorage() {
        return parentWorld.getMapStorage();
    }

    @Override
    public void setData(String dataID, WorldSavedData worldSavedDataIn) {
        parentWorld.setData(dataID, worldSavedDataIn);
    }

    @Nullable
    @Override
    public WorldSavedData loadData(Class<? extends WorldSavedData> clazz, String dataID) {
        return parentWorld.loadData(clazz, dataID);
    }

    @Override
    public int getUniqueDataId(String key) {
        return parentWorld.getUniqueDataId(key);
    }

    @Override
    public void playBroadcastSound(int id, BlockPos pos, int data) {
        parentWorld.playBroadcastSound(id, pos, data);
    }

    @Override
    public void playEvent(int type, BlockPos pos, int data) {
        parentWorld.playEvent(type, pos, data);
    }

    @Override
    public void playEvent(@Nullable EntityPlayer player, int type, BlockPos pos, int data) {
        parentWorld.playEvent(player, type, pos, data);
    }

    @Override
    public int getHeight() {
        return parentWorld.getHeight();
    }

    @Override
    public int getActualHeight() {
        return parentWorld.getActualHeight();
    }

    @Override
    public Random setRandomSeed(int p_72843_1_, int p_72843_2_, int p_72843_3_) {
        return getRealWorld().setRandomSeed(p_72843_1_, p_72843_2_, p_72843_3_);
    }

    @Override
    public double getHorizon() {
        return getRealWorld().getHorizon();
    }

    @Override
    public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress) {
        parentWorld.sendBlockBreakProgress(breakerId, pos, progress);
    }

    @Override
    public Calendar getCurrentDate() {
        return getRealWorld().getCurrentDate();
    }

    @Override
    public Scoreboard getScoreboard() {
        return getRealWorld().getScoreboard();
    }

    @Override
    public void updateComparatorOutputLevel(BlockPos pos, Block blockIn) {
        parentWorld.updateComparatorOutputLevel(pos, blockIn);
    }

    @Override
    public DifficultyInstance getDifficultyForLocation(BlockPos pos) {
        pos = region.convertRegionPosToRealWorld(pos);
        return getRealWorld().getDifficultyForLocation(pos);
    }

    @Override
    public EnumDifficulty getDifficulty() {
        return getRealWorld().getDifficulty();
    }

    @Override
    public int getSkylightSubtracted() {
        return getRealWorld().getSkylightSubtracted();
    }

    @Override
    public void setSkylightSubtracted(int newSkylightSubtracted) {
        getRealWorld().setSkylightSubtracted(newSkylightSubtracted);
    }

    @Override
    public int getLastLightningBolt() {
        return getRealWorld().getLastLightningBolt();
    }

    @Override
    public void setLastLightningBolt(int lastLightningBoltIn) {
        getRealWorld().setLastLightningBolt(lastLightningBoltIn);
    }

    @Override
    public VillageCollection getVillageCollection() {
        return parentWorld.getVillageCollection();
    }

    @Override
    public WorldBorder getWorldBorder() {
        return parentWorld.getWorldBorder();
    }

    @Override
    public boolean isSpawnChunk(int x, int z) {
        return parentWorld.isSpawnChunk(x, z);
    }

    @Override
    public boolean isSideSolid(BlockPos pos, EnumFacing side) {
        return parentWorld.isSideSolid(pos, side);
    }

    @Override
    public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default) {
        return parentWorld.isSideSolid(pos, side, _default);
    }

    @Override
    public ImmutableSetMultimap<ChunkPos, ForgeChunkManager.Ticket> getPersistentChunks() {
        return parentWorld.getPersistentChunks();
    }

    @Override
    public Iterator<Chunk> getPersistentChunkIterable(Iterator<Chunk> chunkIterator) {
        return parentWorld.getPersistentChunkIterable(chunkIterator);
    }

    @Override
    public int getBlockLightOpacity(BlockPos pos) {
        return parentWorld.getBlockLightOpacity(pos);
    }

    @Override
    public int countEntities(EnumCreatureType type, boolean forSpawnCount) {
        return getRealWorld().countEntities(type, forSpawnCount);
    }


    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return parentWorld.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        return parentWorld.getCapability(capability, facing);
    }

    @Override
    public MapStorage getPerWorldStorage() {
        return parentWorld.getPerWorldStorage();
    }

    @Override
    public LootTableManager getLootTableManager() {
        return parentWorld.getLootTableManager();
    }

    @Nullable
    @Override
    public BlockPos findNearestStructure(String p_190528_1_, BlockPos pos, boolean p_190528_3_) {
        pos = region.convertRegionPosToRealWorld(pos);

        return getRealWorld().findNearestStructure(p_190528_1_, pos, p_190528_3_);
    }


}
