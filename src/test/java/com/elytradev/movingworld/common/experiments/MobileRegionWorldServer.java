package com.elytradev.movingworld.common.experiments;

import com.elytradev.concrete.reflect.invoker.Invoker;
import com.elytradev.concrete.reflect.invoker.Invokers;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.util.concurrent.ListenableFuture;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEventData;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.profiler.Profiler;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.village.VillageCollection;
import net.minecraft.world.*;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.template.TemplateManager;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldInfo;
import net.minecraft.world.storage.loot.LootTableManager;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import javax.annotation.Nullable;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by darkevilmac on 2/3/2017.
 */
public class MobileRegionWorldServer extends WorldServer implements IWorldMixin {

    private Invoker initCapabilities;

    public WorldServer realWorld;
    public WorldServer parentWorld;

    public MobileRegion region;

    public MobileRegionWorldServer(MinecraftServer server, int dimensionId, Profiler profilerIn, World realWorld, World parentWorld, MobileRegion region) {
        super(server, parentWorld.getSaveHandler(), parentWorld.getWorldInfo(), dimensionId, profilerIn);
        this.region = region;
    }

    @Override
    public void onInstantiate(int dimension) {
        System.out.println("OnInstantiate was called! Hallelujah!");

        World parentWorld = DimensionManager.getWorld(dimension);
        World realWorld = DimensionManager.getWorld(MovingWorldExperimentsMod.registeredDimensions.inverse().get(dimension));

        this.realWorld = (WorldServer) realWorld;
        this.parentWorld = (WorldServer) parentWorld;
    }

    public boolean isPosWithinRegion(BlockPos pos) {
        return region.isPosWithinBounds(pos);
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
    public void updateWeather() {
        parentWorld.updateWeather();
    }

    @Override
    public void playMoodSoundAndCheckLight(int p_147467_1_, int p_147467_2_, Chunk chunkIn) {
        parentWorld.playMoodSoundAndCheckLight(p_147467_1_, p_147467_2_, chunkIn);
    }

    @Override
    public void updateBlocks() {
    }

    @Override
    public void tickPlayers() {
    }

    @Override
    public void tick() {
        parentWorld.tick();
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

    @Nullable
    @Override
    public Entity getEntityByID(int id) {
        return parentWorld.getEntityByID(id);
    }

    @Override
    public void sendQuittingDisconnectingPacket() {
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
    public World init() {
        return parentWorld.init();
    }

    @Override
    public Biome getBiome(BlockPos pos) {
        pos = region.convertRegionPosToRealWorld(pos);
        return realWorld.getBiome(pos);
    }

    @Override
    public Biome getBiomeForCoordsBody(BlockPos pos) {
        pos = region.convertRegionPosToRealWorld(pos);
        return realWorld.getBiomeForCoordsBody(pos);
    }

    @Override
    public BiomeProvider getBiomeProvider() {
        return realWorld.getBiomeProvider();
    }

    @Override
    public void initialize(WorldSettings settings) {
        parentWorld.initialize(settings);
    }

    @Nullable
    @Override
    public MinecraftServer getMinecraftServer() {
        if (parentWorld != null)
            return parentWorld.getMinecraftServer();
        else
            return mcServer;
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
        realWorld.playSound(player, pos, soundIn, category, volume, pitch);
    }

    @Override
    public void playRecord(BlockPos blockPositionIn, @Nullable SoundEvent soundEventIn) {
        realWorld.playRecord(blockPositionIn, soundEventIn);
    }

    @Override
    public void spawnParticle(EnumParticleTypes particleType, double xCoord, double yCoord, double zCoord, double xSpeed, double ySpeed, double zSpeed, int... parameters) {
        realWorld.spawnParticle(particleType, xCoord, yCoord, zCoord, xSpeed, ySpeed, zSpeed, parameters);
    }

    @Override
    public void spawnAlwaysVisibleParticle(int p_190523_1_, double p_190523_2_, double p_190523_4_, double p_190523_6_, double p_190523_8_, double p_190523_10_, double p_190523_12_, int... p_190523_14_) {
        realWorld.spawnAlwaysVisibleParticle(p_190523_1_, p_190523_2_, p_190523_4_, p_190523_6_, p_190523_8_, p_190523_10_, p_190523_12_, p_190523_14_);
    }

    @Override
    public void spawnParticle(EnumParticleTypes particleType, boolean ignoreRange, double xCoord, double yCoord, double zCoord, double xSpeed, double ySpeed, double zSpeed, int... parameters) {
        realWorld.spawnParticle(particleType, ignoreRange, xCoord, yCoord, zCoord, xSpeed, ySpeed, zSpeed, parameters);
    }

    @Override
    public boolean addWeatherEffect(Entity entityIn) {
        return realWorld.addWeatherEffect(entityIn);
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
    public boolean isInsideBorder(WorldBorder worldBorderIn, Entity entityIn) {
        return parentWorld.isInsideBorder(worldBorderIn, entityIn);
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
    public boolean isAreaLoaded(int xStart, int yStart, int zStart, int xEnd, int yEnd, int zEnd, boolean allowEmpty) {
        BlockPos start = new BlockPos(xStart, yStart, zStart);
        BlockPos end = new BlockPos(xEnd, yEnd, zEnd);

        if (isPosWithinRegion(start) && isPosWithinRegion(end))
            return parentWorld.isAreaLoaded(xStart, yStart, zStart, xEnd, yEnd, zEnd, allowEmpty);
        else
            return false;
    }

    @Override
    public float getSunBrightnessFactor(float partialTicks) {
        if (realWorld != null)
            return realWorld.getSunBrightnessFactor(partialTicks);
        else
            return super.getSunBrightnessFactor(partialTicks);
    }

    @Override
    public float getSunBrightness(float partialTicks) {
        return realWorld.getSunBrightness(partialTicks);
    }

    @Override
    public float getSunBrightnessBody(float partialTicks) {
        return realWorld.getSunBrightnessBody(partialTicks);
    }

    @Override
    public Vec3d getSkyColor(Entity entityIn, float partialTicks) {
        return realWorld.getSkyColor(entityIn, partialTicks);
    }

    @Override
    public Vec3d getSkyColorBody(Entity entityIn, float partialTicks) {
        return realWorld.getSkyColorBody(entityIn, partialTicks);
    }

    @Override
    public float getCelestialAngle(float partialTicks) {
        if (realWorld != null)
            return realWorld.getCelestialAngle(partialTicks);
        else return super.getCelestialAngle(partialTicks);
    }

    @Override
    public int getMoonPhase() {
        return realWorld.getMoonPhase();
    }

    @Override
    public float getCurrentMoonPhaseFactor() {
        return realWorld.getCurrentMoonPhaseFactor();
    }

    @Override
    public float getCurrentMoonPhaseFactorBody() {
        return realWorld.getCurrentMoonPhaseFactorBody();
    }

    @Override
    public float getCelestialAngleRadians(float partialTicks) {
        return realWorld.getCelestialAngleRadians(partialTicks);
    }

    @Override
    public Vec3d getCloudColour(float partialTicks) {
        return realWorld.getCloudColour(partialTicks);
    }

    @Override
    public Vec3d getCloudColorBody(float partialTicks) {
        return realWorld.getCloudColorBody(partialTicks);
    }

    @Override
    public Vec3d getFogColor(float partialTicks) {
        return realWorld.getFogColor(partialTicks);
    }

    @Override
    public BlockPos getPrecipitationHeight(BlockPos pos) {
        return realWorld.getPrecipitationHeight(pos);
    }

    @Override
    public BlockPos getTopSolidOrLiquidBlock(BlockPos pos) {
        return realWorld.getTopSolidOrLiquidBlock(pos);
    }

    @Override
    public float getStarBrightness(float partialTicks) {
        return realWorld.getStarBrightness(partialTicks);
    }

    @Override
    public float getStarBrightnessBody(float partialTicks) {
        return realWorld.getStarBrightnessBody(partialTicks);
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
    public void setAllowedSpawnTypes(boolean hostile, boolean peaceful) {
        parentWorld.setAllowedSpawnTypes(hostile, peaceful);
    }

    @Override
    public void calculateInitialSkylight() {
        if (parentWorld != null)
            parentWorld.calculateInitialSkylight();
        else
            super.calculateInitialSkylight();
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
            return realWorld.canSnowAt(pos, checkLight);
        } else {
            return false;
        }
    }

    @Override
    public boolean canSnowAtBody(BlockPos pos, boolean checkLight) {
        if (isPosWithinRegion(pos)) {
            pos = region.convertRegionPosToRealWorld(pos);
            return realWorld.canSnowAtBody(pos, checkLight);
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
        return realWorld.getEntitiesWithinAABBExcludingEntity(entityIn, region.convertRegionBBToRealWorld(bb));
    }

    @Override
    public List<Entity> getEntitiesInAABBexcluding(@Nullable Entity entityIn, AxisAlignedBB bb, @Nullable Predicate<? super Entity> predicate) {
        return realWorld.getEntitiesInAABBexcluding(entityIn, region.convertRegionBBToRealWorld(bb), predicate);
    }

    @Override
    public <T extends Entity> List<T> getEntities(Class<? extends T> entityType, Predicate<? super T> filter) {
        return realWorld.getEntities(entityType, filter);
    }

    @Override
    public <T extends Entity> List<T> getPlayers(Class<? extends T> playerType, Predicate<? super T> filter) {
        return realWorld.getPlayers(playerType, filter);
    }

    @Override
    public <T extends Entity> List<T> getEntitiesWithinAABB(Class<? extends T> clazz, AxisAlignedBB bb, @Nullable Predicate<? super T> filter) {
        return realWorld.getEntitiesWithinAABB(clazz, region.convertRegionBBToRealWorld(bb), filter);
    }

    @Override
    public <T extends Entity> List<T> getEntitiesWithinAABB(Class<? extends T> classEntity, AxisAlignedBB bb) {
        return realWorld.getEntitiesWithinAABB(classEntity, region.convertRegionBBToRealWorld(bb));
    }

    @Nullable
    @Override
    public <T extends Entity> T findNearestEntityWithinAABB(Class<? extends T> entityType, AxisAlignedBB bb, T closestTo) {
        return realWorld.findNearestEntityWithinAABB(entityType, region.convertRegionBBToRealWorld(bb), closestTo);
    }

    @Override
    public List<Entity> getLoadedEntityList() {
        return realWorld.getLoadedEntityList();
    }

    @Override
    public void markChunkDirty(BlockPos pos, TileEntity unusedTileEntity) {
        parentWorld.markChunkDirty(pos, unusedTileEntity);
    }

    @Override
    public int countEntities(Class<?> entityType) {
        return realWorld.countEntities(entityType);
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

        realWorld.loadEntities(movedEntityCollection);
    }

    @Override
    public void unloadEntities(Collection<Entity> entityCollection) {
        realWorld.unloadEntities(entityCollection);
    }

    @Override
    public boolean mayPlace(Block p_190527_1_, BlockPos pos, boolean p_190527_3_, EnumFacing p_190527_4_, @Nullable Entity p_190527_5_) {
        if (isPosWithinRegion(pos))
            return realWorld.mayPlace(p_190527_1_, pos, p_190527_3_, p_190527_4_, p_190527_5_);
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
        return realWorld.getClosestPlayerToEntity(entityIn, distance);
    }

    @Nullable
    @Override
    public EntityPlayer getNearestPlayerNotCreative(Entity entityIn, double distance) {
        return realWorld.getNearestPlayerNotCreative(entityIn, distance);
    }

    @Nullable
    @Override
    public EntityPlayer getClosestPlayer(double posX, double posY, double posZ, double distance, boolean spectator) {
        Vec3d pos = new Vec3d(posX, posY, posZ);
        pos = region.convertRegionPosToRealWorld(pos);

        return realWorld.getClosestPlayer(pos.xCoord, pos.yCoord, pos.zCoord, distance, spectator);
    }

    @Nullable
    @Override
    public EntityPlayer getClosestPlayer(double x, double y, double z, double distance, Predicate<Entity> predicate) {
        Vec3d pos = new Vec3d(x, y, z);
        pos = region.convertRegionPosToRealWorld(pos);

        return realWorld.getClosestPlayer(pos.xCoord, pos.yCoord, pos.zCoord, distance, predicate);
    }

    @Override
    public boolean isAnyPlayerWithinRangeAt(double x, double y, double z, double range) {
        Vec3d pos = new Vec3d(x, y, z);
        pos = region.convertRegionPosToRealWorld(pos);

        return realWorld.isAnyPlayerWithinRangeAt(pos.xCoord, pos.yCoord, pos.zCoord, range);
    }

    @Nullable
    @Override
    public EntityPlayer getNearestAttackablePlayer(Entity entityIn, double maxXZDistance, double maxYDistance) {
        return realWorld.getNearestAttackablePlayer(entityIn, maxXZDistance, maxYDistance);
    }

    @Nullable
    @Override
    public EntityPlayer getNearestAttackablePlayer(BlockPos pos, double maxXZDistance, double maxYDistance) {
        return realWorld.getNearestAttackablePlayer(region.convertRegionPosToRealWorld(pos), maxXZDistance, maxYDistance);
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
        return realWorld.getPlayerEntityByName(name);
    }

    @Nullable
    @Override
    public EntityPlayer getPlayerEntityByUUID(UUID uuid) {
        return realWorld.getPlayerEntityByUUID(uuid);
    }

    @Override
    public void checkSessionLock() throws MinecraftException {
        parentWorld.checkSessionLock();
    }

    @Override
    public long getSeed() {
        if (parentWorld != null)
            return parentWorld.getSeed();
        else
            return super.getSeed();
    }

    @Override
    public long getTotalWorldTime() {
        return realWorld.getTotalWorldTime();
    }

    @Override
    public void setTotalWorldTime(long worldTime) {
        realWorld.setTotalWorldTime(worldTime);
    }

    @Override
    public long getWorldTime() {
        return realWorld.getWorldTime();
    }

    @Override
    public void setWorldTime(long time) {
        realWorld.setWorldTime(time);
    }

    @Override
    public BlockPos getSpawnPoint() {
        return parentWorld.getSpawnPoint();
    }

    @Override
    public void setSpawnPoint(BlockPos pos) {
        parentWorld.setSpawnPoint(pos);
    }

    @Override
    public void joinEntityInSurroundings(Entity entityIn) {
        Vec3d entityPos = new Vec3d(entityIn.posX, entityIn.posY, entityIn.posZ);
        entityPos = region.convertRegionPosToRealWorld(entityPos);
        entityIn.setPosition(entityPos.xCoord, entityPos.yCoord, entityPos.zCoord);
        realWorld.joinEntityInSurroundings(entityIn);
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
        return realWorld.getGameRules();
    }

    @Override
    public void updateAllPlayersSleepingFlag() {
        realWorld.updateAllPlayersSleepingFlag();
    }

    @Override
    public float getThunderStrength(float delta) {
        if (realWorld != null)
            return realWorld.getThunderStrength(delta);
        else
            return super.getThunderStrength(delta);
    }

    @Override
    public void setThunderStrength(float strength) {
        realWorld.setThunderStrength(strength);
    }

    @Override
    public float getRainStrength(float delta) {
        if (realWorld != null)
            return realWorld.getRainStrength(delta);
        else
            return super.getRainStrength(delta);
    }

    @Override
    public void setRainStrength(float strength) {
        realWorld.setRainStrength(strength);
    }

    @Override
    public boolean isThundering() {
        return realWorld.isThundering();
    }

    @Override
    public boolean isRaining() {
        return realWorld.isRaining();
    }

    @Override
    public boolean isRainingAt(BlockPos strikePosition) {
        strikePosition = region.convertRegionPosToRealWorld(strikePosition);

        return realWorld.isRainingAt(strikePosition);
    }

    @Override
    public boolean isBlockinHighHumidity(BlockPos pos) {
        pos = region.convertRegionPosToRealWorld(pos);

        return realWorld.isBlockinHighHumidity(pos);
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
        return realWorld.setRandomSeed(p_72843_1_, p_72843_2_, p_72843_3_);
    }

    @Override
    public double getHorizon() {
        return realWorld.getHorizon();
    }

    @Override
    public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress) {
        parentWorld.sendBlockBreakProgress(breakerId, pos, progress);
    }

    @Override
    public Calendar getCurrentDate() {
        return realWorld.getCurrentDate();
    }

    @Override
    public Scoreboard getScoreboard() {
        return realWorld.getScoreboard();
    }

    @Override
    public void updateComparatorOutputLevel(BlockPos pos, Block blockIn) {
        parentWorld.updateComparatorOutputLevel(pos, blockIn);
    }

    @Override
    public DifficultyInstance getDifficultyForLocation(BlockPos pos) {
        pos = region.convertRegionPosToRealWorld(pos);
        return realWorld.getDifficultyForLocation(pos);
    }

    @Override
    public EnumDifficulty getDifficulty() {
        return realWorld.getDifficulty();
    }

    @Override
    public int getSkylightSubtracted() {
        return realWorld.getSkylightSubtracted();
    }

    @Override
    public void setSkylightSubtracted(int newSkylightSubtracted) {
        realWorld.setSkylightSubtracted(newSkylightSubtracted);
    }

    @Override
    public int getLastLightningBolt() {
        return realWorld.getLastLightningBolt();
    }

    @Override
    public void setLastLightningBolt(int lastLightningBoltIn) {
        realWorld.setLastLightningBolt(lastLightningBoltIn);
    }

    @Override
    public VillageCollection getVillageCollection() {
        return parentWorld.getVillageCollection();
    }

    @Override
    public WorldBorder getWorldBorder() {
        if (parentWorld != null)
            return parentWorld.getWorldBorder();
        else
            return super.getWorldBorder();
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
        return realWorld.countEntities(type, forSpawnCount);
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

        return realWorld.findNearestStructure(p_190528_1_, pos, p_190528_3_);
    }

    @Override
    public void initCapabilities() {
        if(initCapabilities == null)
            initCapabilities = Invokers.findMethod(World.class, this, new String[]{"initCapabilities"});

        initCapabilities.invoke(null);
    }

    @Override
    public void wakeAllPlayers() {
        parentWorld.wakeAllPlayers();
    }

    @Override
    public void playerCheckLight() {
        parentWorld.playerCheckLight();
    }

    @Override
    public BlockPos adjustPosToNearbyEntity(BlockPos pos) {
        return parentWorld.adjustPosToNearbyEntity(pos);
    }

    @Override
    public void createBonusChest() {
        parentWorld.createBonusChest();
    }

    @Override
    public void saveLevel() throws MinecraftException {
        parentWorld.saveLevel();
    }

    @Nullable
    @Override
    public Biome.SpawnListEntry getSpawnListEntryForTypeAt(EnumCreatureType creatureType, BlockPos pos) {
        return parentWorld.getSpawnListEntryForTypeAt(creatureType, pos);
    }

    @Override
    public boolean canCreatureTypeSpawnHere(EnumCreatureType creatureType, Biome.SpawnListEntry spawnListEntry, BlockPos pos) {
        return parentWorld.canCreatureTypeSpawnHere(creatureType, spawnListEntry, pos);
    }

    @Override
    public boolean areAllPlayersAsleep() {
        return parentWorld.areAllPlayersAsleep();
    }

    @Override
    public void resetUpdateEntityTick() {
        parentWorld.resetUpdateEntityTick();
    }

    @Nullable
    @Override
    public BlockPos getSpawnCoordinate() {
        return parentWorld.getSpawnCoordinate();
    }

    @Override
    public void saveAllChunks(boolean p_73044_1_, @Nullable IProgressUpdate progressCallback) throws MinecraftException {
        parentWorld.saveAllChunks(p_73044_1_, progressCallback);
    }

    @Override
    public void saveChunkData() {
        parentWorld.saveChunkData();
    }

    @Override
    public void flush() {
        parentWorld.flush();
    }

    @Override
    public EntityTracker getEntityTracker() {
        return parentWorld.getEntityTracker();
    }

    @Override
    public PlayerChunkMap getPlayerChunkMap() {
        return parentWorld.getPlayerChunkMap();
    }

    @Override
    public Teleporter getDefaultTeleporter() {
        return parentWorld.getDefaultTeleporter();
    }

    @Override
    public TemplateManager getStructureTemplateManager() {
        return parentWorld.getStructureTemplateManager();
    }

    @Override
    public void spawnParticle(EnumParticleTypes particleType, double xCoord, double yCoord, double zCoord, int numberOfParticles, double xOffset, double yOffset, double zOffset, double particleSpeed, int... particleArguments) {
        parentWorld.spawnParticle(particleType, xCoord, yCoord, zCoord, numberOfParticles, xOffset, yOffset, zOffset, particleSpeed, particleArguments);
    }

    @Override
    public void spawnParticle(EnumParticleTypes particleType, boolean longDistance, double xCoord, double yCoord, double zCoord, int numberOfParticles, double xOffset, double yOffset, double zOffset, double particleSpeed, int... particleArguments) {
        parentWorld.spawnParticle(particleType, longDistance, xCoord, yCoord, zCoord, numberOfParticles, xOffset, yOffset, zOffset, particleSpeed, particleArguments);
    }

    @Override
    public void spawnParticle(EntityPlayerMP player, EnumParticleTypes particle, boolean longDistance, double x, double y, double z, int count, double xOffset, double yOffset, double zOffset, double speed, int... arguments) {
        parentWorld.spawnParticle(player, particle, longDistance, x, y, z, count, xOffset, yOffset, zOffset, speed, arguments);
    }

    @Nullable
    @Override
    public Entity getEntityFromUuid(UUID uuid) {
        return parentWorld.getEntityFromUuid(uuid);
    }

    @Override
    public ListenableFuture<Object> addScheduledTask(Runnable runnableToSchedule) {
        return parentWorld.addScheduledTask(runnableToSchedule);
    }

    @Override
    public boolean isCallingFromMinecraftThread() {
        return parentWorld.isCallingFromMinecraftThread();
    }

    @Override
    public File getChunkSaveLocation() {
        if (parentWorld != null)
            return parentWorld.getChunkSaveLocation();
        else
            return DimensionManager.getWorld(provider.getDimension()).getChunkSaveLocation();
    }

    @Override
    public void resetRainAndThunder() {
        realWorld.resetRainAndThunder();
    }

    @Override
    public boolean canSpawnNPCs() {
        return realWorld.canSpawnNPCs();
    }

    @Override
    public boolean canSpawnAnimals() {
        return realWorld.canSpawnAnimals();
    }

    @Override
    public void setDebugWorldSettings() {
        parentWorld.setDebugWorldSettings();
    }

    @Override
    public void createSpawnPosition(WorldSettings settings) {
        parentWorld.createSpawnPosition(settings);
    }

    @Override
    public boolean canAddEntity(Entity entityIn) {
        return realWorld.canAddEntity(entityIn);
    }

    @Override
    public ChunkProviderServer getChunkProvider() {
        if (parentWorld != null)
            return parentWorld.getChunkProvider();
        else
            return super.getChunkProvider();
    }

    @Override
    public void sendQueuedBlockEvents() {
        parentWorld.sendQueuedBlockEvents();
    }

    @Override
    public boolean fireBlockEvent(BlockEventData event) {
        return parentWorld.fireBlockEvent(event);
    }

    @Override
    public void sendPacketWithinDistance(EntityPlayerMP player, boolean longDistance, double x, double y, double z, Packet<?> packetIn) {
        parentWorld.sendPacketWithinDistance(player, longDistance, x, y, z, packetIn);
    }

}