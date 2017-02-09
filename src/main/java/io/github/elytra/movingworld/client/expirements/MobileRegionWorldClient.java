package io.github.elytra.movingworld.client.expirements;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSetMultimap;
import io.github.elytra.movingworld.common.experiments.MobileRegion;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
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
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A wrapper for a MobileRegion and it's associated world,
 */
public class MobileRegionWorldClient extends WorldClient {

    public WorldClient realWorld;
    public WorldClient parent;

    public MobileRegion region;

    public MobileRegionWorldClient(NetHandlerPlayClient netHandler, WorldSettings settings, int dimension, EnumDifficulty difficulty, Profiler profilerIn) {
        super(netHandler, settings, dimension, difficulty, profilerIn);
    }

    public boolean isPosWithinRegion(BlockPos pos) {
        return region.isPosWithinBounds(pos);
    }

    @Override
    public void initCapabilities() {
        try {
            ReflectionHelper.findMethod(WorldClient.class, parent, new String[]{"initCapabilities"}, Void.class).invoke(null);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Override
    public IChunkProvider createChunkProvider() {
        return parent.createChunkProvider();
    }

    @Override
    public boolean isChunkLoaded(int x, int z, boolean allowEmpty) {
        if (region.isChunkInRegion(x, z))
            return parent.isChunkLoaded(x, z, allowEmpty);
        else
            return false;
    }

    @Override
    public void updateWeather() {
        parent.updateWeather();
    }

    @Override
    public void playMoodSoundAndCheckLight(int p_147467_1_, int p_147467_2_, Chunk chunkIn) {
        parent.playMoodSoundAndCheckLight(p_147467_1_, p_147467_2_, chunkIn);
    }

    @Override
    public void buildChunkCoordList() {
    }

    @Override
    public void updateBlocks() {
    }

    @Override
    public void tickPlayers() {
    }

    @Override
    public void tick() {
        parent.tick();
    }

    @Override
    public void invalidateBlockReceiveRegion(int x1, int y1, int z1, int x2, int y2, int z2) {
        parent.invalidateBlockReceiveRegion(x1, y1, z1, x2, y2, z2);
    }

    @Override
    public void doPreChunk(int chunkX, int chunkZ, boolean loadChunk) {
        if (region.isChunkInRegion(chunkX, chunkZ))
            parent.doPreChunk(chunkX, chunkZ, loadChunk);
    }

    @Override
    public boolean spawnEntity(Entity entityIn) {
        return parent.spawnEntity(entityIn);
    }

    @Override
    public void removeEntity(Entity entityIn) {
        parent.removeEntity(entityIn);
    }

    @Override
    public void onEntityAdded(Entity entityIn) {
        parent.onEntityAdded(entityIn);
    }

    @Override
    public void onEntityRemoved(Entity entityIn) {
        parent.onEntityRemoved(entityIn);
    }

    @Override
    public void addEntityToWorld(int entityID, Entity entityToSpawn) {
        parent.addEntityToWorld(entityID, entityToSpawn);
    }

    @Nullable
    @Override
    public Entity getEntityByID(int id) {
        return parent.getEntityByID(id);
    }

    @Override
    public Entity removeEntityFromWorld(int entityID) {
        return parent.removeEntityFromWorld(entityID);
    }

    @Override
    public boolean invalidateRegionAndSetBlock(BlockPos pos, IBlockState state) {
        if (region.isPosWithinBounds(pos))
            return parent.invalidateRegionAndSetBlock(pos, state);
        else
            return false;
    }

    @Override
    public void sendQuittingDisconnectingPacket() {
    }

    @Override
    public void doVoidFogParticles(int posX, int posY, int posZ) {
    }

    @Override
    public void showBarrierParticles(int p_184153_1_, int p_184153_2_, int p_184153_3_, int p_184153_4_, Random random, boolean p_184153_6_, BlockPos.MutableBlockPos pos) {
    }

    @Override
    public void removeAllEntities() {
        parent.removeAllEntities();
    }

    @Override
    public CrashReportCategory addWorldInfoToCrashReport(CrashReport report) {
        return parent.addWorldInfoToCrashReport(report);
    }

    @Override
    public void playSound(@Nullable EntityPlayer player, double x, double y, double z, SoundEvent soundIn, SoundCategory category, float volume, float pitch) {
        Vec3d pos = new Vec3d(x, y, z);
        pos = region.convertRegionPosToRealWorld(pos);
        parent.playSound(player, pos.xCoord, pos.yCoord, pos.zCoord, soundIn, category, volume, pitch);
    }

    @Override
    public void playSound(BlockPos blockPos, SoundEvent soundIn, SoundCategory category, float volume, float pitch, boolean distanceDelay) {
        Vec3d pos = new Vec3d(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        pos = region.convertRegionPosToRealWorld(pos);
        blockPos = new BlockPos(Math.round(pos.xCoord), Math.round(pos.yCoord), Math.round(pos.zCoord));
        parent.playSound(blockPos, soundIn, category, volume, pitch, distanceDelay);
    }

    @Override
    public void playSound(double x, double y, double z, SoundEvent soundIn, SoundCategory category, float volume, float pitch, boolean distanceDelay) {
        Vec3d pos = new Vec3d(x, y, z);
        pos = region.convertRegionPosToRealWorld(pos);
        parent.playSound(pos.xCoord, pos.yCoord, pos.zCoord, soundIn, category, volume, pitch, distanceDelay);
    }

    @Override
    public void makeFireworks(double x, double y, double z, double motionX, double motionY, double motionZ, @Nullable NBTTagCompound compund) {
        Vec3d pos = new Vec3d(x, y, z);
        pos = region.convertRegionPosToRealWorld(pos);
        parent.makeFireworks(pos.xCoord, pos.yCoord, pos.zCoord, motionX, motionY, motionZ, compund);
    }

    @Override
    public void sendPacketToServer(Packet<?> packetIn) {
        parent.sendPacketToServer(packetIn);
    }

    @Override
    public void setWorldScoreboard(Scoreboard scoreboardIn) {
        parent.setWorldScoreboard(scoreboardIn);
    }

    @Override
    public ChunkProviderClient getChunkProvider() {
        return parent.getChunkProvider();
    }

    @Override
    public World init() {
        return parent.init();
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
        parent.initialize(settings);
    }

    @Nullable
    @Override
    public MinecraftServer getMinecraftServer() {
        return parent.getMinecraftServer();
    }

    @Override
    public void setInitialSpawnLocation() {
        parent.setInitialSpawnLocation();
    }

    @Override
    public IBlockState getGroundAboveSeaLevel(BlockPos pos) {
        return parent.getGroundAboveSeaLevel(pos);
    }

    @Override
    public boolean isAirBlock(BlockPos pos) {
        return parent.isAirBlock(pos);
    }

    @Override
    public boolean isBlockLoaded(BlockPos pos) {
        return parent.isBlockLoaded(pos);
    }

    @Override
    public boolean isBlockLoaded(BlockPos pos, boolean allowEmpty) {
        return parent.isBlockLoaded(pos, allowEmpty);
    }


    @Override
    public Chunk getChunkFromBlockCoords(BlockPos pos) {
        if (isPosWithinRegion(pos))
            return parent.getChunkFromBlockCoords(pos);
        else return null;
    }

    @Override
    public Chunk getChunkFromChunkCoords(int chunkX, int chunkZ) {
        return parent.getChunkFromChunkCoords(chunkX, chunkZ);
    }

    @Override
    public boolean isChunkGeneratedAt(int x, int z) {
        return parent.isChunkGeneratedAt(x, z);
    }

    @Override
    public boolean setBlockState(BlockPos pos, IBlockState newState, int flags) {
        return parent.setBlockState(pos, newState, flags);
    }

    @Override
    public void markAndNotifyBlock(BlockPos pos, @Nullable Chunk chunk, IBlockState iblockstate, IBlockState newState, int flags) {
        parent.markAndNotifyBlock(pos, chunk, iblockstate, newState, flags);
    }

    @Override
    public boolean setBlockToAir(BlockPos pos) {
        return parent.setBlockToAir(pos);
    }

    @Override
    public boolean destroyBlock(BlockPos pos, boolean dropBlock) {
        return parent.destroyBlock(pos, dropBlock);
    }

    @Override
    public boolean setBlockState(BlockPos pos, IBlockState state) {
        return parent.setBlockState(pos, state);
    }

    @Override
    public void notifyBlockUpdate(BlockPos pos, IBlockState oldState, IBlockState newState, int flags) {
        parent.notifyBlockUpdate(pos, oldState, newState, flags);
    }

    @Override
    public void notifyNeighborsRespectDebug(BlockPos pos, Block blockType, boolean p_175722_3_) {
        parent.notifyNeighborsRespectDebug(pos, blockType, p_175722_3_);
    }

    @Override
    public void markBlocksDirtyVertical(int x1, int z1, int x2, int z2) {
        parent.markBlocksDirtyVertical(x1, z1, x2, z2);
    }

    @Override
    public void markBlockRangeForRenderUpdate(BlockPos rangeMin, BlockPos rangeMax) {
        parent.markBlockRangeForRenderUpdate(rangeMin, rangeMax);
    }

    @Override
    public void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2, int y2, int z2) {
        parent.markBlockRangeForRenderUpdate(x1, y1, z1, x2, y2, z2);
    }

    @Override
    public void updateObservingBlocksAt(BlockPos pos, Block blockType) {
        parent.updateObservingBlocksAt(pos, blockType);
    }

    @Override
    public void notifyNeighborsOfStateChange(BlockPos pos, Block blockType, boolean updateObservers) {
        parent.notifyNeighborsOfStateChange(pos, blockType, updateObservers);
    }

    @Override
    public void notifyNeighborsOfStateExcept(BlockPos pos, Block blockType, EnumFacing skipSide) {
        parent.notifyNeighborsOfStateExcept(pos, blockType, skipSide);
    }

    @Override
    public void neighborChanged(BlockPos pos, Block p_190524_2_, BlockPos p_190524_3_) {
        parent.neighborChanged(pos, p_190524_2_, p_190524_3_);
    }

    @Override
    public void observedNeighborChanged(BlockPos pos, Block p_190529_2_, BlockPos p_190529_3_) {
        parent.observedNeighborChanged(pos, p_190529_2_, p_190529_3_);
    }

    @Override
    public boolean isBlockTickPending(BlockPos pos, Block blockType) {
        return parent.isBlockTickPending(pos, blockType);
    }

    @Override
    public boolean canSeeSky(BlockPos pos) {
        return parent.canSeeSky(pos);
    }

    @Override
    public boolean canBlockSeeSky(BlockPos pos) {
        return parent.canBlockSeeSky(pos);
    }

    @Override
    public int getLight(BlockPos pos) {
        return parent.getLight(pos);
    }

    @Override
    public int getLightFromNeighbors(BlockPos pos) {
        return parent.getLightFromNeighbors(pos);
    }

    @Override
    public int getLight(BlockPos pos, boolean checkNeighbors) {
        return parent.getLight(pos, checkNeighbors);
    }

    @Override
    public BlockPos getHeight(BlockPos pos) {
        return parent.getHeight(pos);
    }

    @Override
    public int getHeight(int x, int z) {
        return parent.getHeight(x, z);
    }

    @Override
    public int getChunksLowestHorizon(int x, int z) {
        return parent.getChunksLowestHorizon(x, z);
    }

    @Override
    public int getLightFromNeighborsFor(EnumSkyBlock type, BlockPos pos) {
        if (isPosWithinRegion(pos))
            return parent.getLightFromNeighborsFor(type, pos);
        else
            return 0;
    }

    @Override
    public int getLightFor(EnumSkyBlock type, BlockPos pos) {
        if (isPosWithinRegion(pos))
            return parent.getLightFor(type, pos);
        else
            return 0;
    }

    @Override
    public void setLightFor(EnumSkyBlock type, BlockPos pos, int lightValue) {
        if (isPosWithinRegion(pos))
            parent.setLightFor(type, pos, lightValue);
    }

    @Override
    public void notifyLightSet(BlockPos pos) {
        if (isPosWithinRegion(pos))
            parent.notifyLightSet(pos);
    }

    @Override
    public int getCombinedLight(BlockPos pos, int lightValue) {
        if (isPosWithinRegion(pos))
            return parent.getCombinedLight(pos, lightValue);
        else
            return 0;
    }

    @Override
    public float getLightBrightness(BlockPos pos) {
        if (isPosWithinRegion(pos))
            return parent.getLightBrightness(pos);
        else
            return 0F;
    }

    @Override
    public IBlockState getBlockState(BlockPos pos) {
        if (isPosWithinRegion(pos))
            return parent.getBlockState(pos);
        else
            return Blocks.AIR.getDefaultState();
    }

    @Override
    public boolean isDaytime() {
        return parent.isDaytime();
    }

    @Nullable
    @Override
    public RayTraceResult rayTraceBlocks(Vec3d start, Vec3d end) {
        return parent.rayTraceBlocks(start, end);
    }

    @Nullable
    @Override
    public RayTraceResult rayTraceBlocks(Vec3d start, Vec3d end, boolean stopOnLiquid) {
        return parent.rayTraceBlocks(start, end, stopOnLiquid);
    }

    @Nullable
    @Override
    public RayTraceResult rayTraceBlocks(Vec3d vec31, Vec3d vec32, boolean stopOnLiquid, boolean ignoreBlockWithoutBoundingBox, boolean returnLastUncollidableBlock) {
        return parent.rayTraceBlocks(vec31, vec32, stopOnLiquid, ignoreBlockWithoutBoundingBox, returnLastUncollidableBlock);
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
        parent.removeEntityDangerously(entityIn);
    }

    @Override
    public void addEventListener(IWorldEventListener listener) {
        parent.addEventListener(listener);
    }

    @Override
    public List<AxisAlignedBB> getCollisionBoxes(@Nullable Entity entityIn, AxisAlignedBB aabb) {
        return parent.getCollisionBoxes(entityIn, aabb);
    }

    @Override
    public boolean isInsideBorder(WorldBorder worldBorderIn, Entity entityIn) {
        return parent.isInsideBorder(worldBorderIn, entityIn);
    }

    @Override
    public void removeEventListener(IWorldEventListener listener) {
        parent.removeEventListener(listener);
    }

    @Override
    public boolean collidesWithAnyBlock(AxisAlignedBB bbox) {
        return parent.collidesWithAnyBlock(bbox);
    }

    @Override
    public int calculateSkylightSubtracted(float partialTicks) {
        return parent.calculateSkylightSubtracted(partialTicks);
    }

    @Override
    public boolean isAreaLoaded(int xStart, int yStart, int zStart, int xEnd, int yEnd, int zEnd, boolean allowEmpty) {
        BlockPos start = new BlockPos(xStart, yStart, zStart);
        BlockPos end = new BlockPos(xEnd, yEnd, zEnd);

        if (isPosWithinRegion(start) && isPosWithinRegion(end))
            return parent.isAreaLoaded(xStart, yStart, zStart, xEnd, yEnd, zEnd, allowEmpty);
        else
            return false;
    }

    @Override
    public float getSunBrightnessFactor(float partialTicks) {
        return realWorld.getSunBrightnessFactor(partialTicks);
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
        return realWorld.getCelestialAngle(partialTicks);
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
        return parent.isUpdateScheduled(pos, blk);
    }

    @Override
    public void scheduleUpdate(BlockPos pos, Block blockIn, int delay) {
        parent.scheduleUpdate(pos, blockIn, delay);
    }

    @Override
    public void updateBlockTick(BlockPos pos, Block blockIn, int delay, int priority) {
        parent.updateBlockTick(pos, blockIn, delay, priority);
    }

    @Override
    public void scheduleBlockUpdate(BlockPos pos, Block blockIn, int delay, int priority) {
        parent.scheduleBlockUpdate(pos, blockIn, delay, priority);
    }

    @Override
    public void updateEntities() {
    }

    @Override
    public boolean addTileEntity(TileEntity tile) {
        return parent.addTileEntity(tile);
    }

    @Override
    public void addTileEntities(Collection<TileEntity> tileEntityCollection) {
        parent.addTileEntities(tileEntityCollection);
    }

    @Override
    public void updateEntity(Entity ent) {
        parent.updateEntity(ent);
    }

    @Override
    public void updateEntityWithOptionalForce(Entity entityIn, boolean forceUpdate) {
        parent.updateEntityWithOptionalForce(entityIn, forceUpdate);
    }

    @Override
    public boolean checkNoEntityCollision(AxisAlignedBB bb) {
        return parent.checkNoEntityCollision(bb);
    }

    @Override
    public boolean checkNoEntityCollision(AxisAlignedBB bb, @Nullable Entity entityIn) {
        return parent.checkNoEntityCollision(bb, entityIn);
    }

    @Override
    public boolean checkBlockCollision(AxisAlignedBB bb) {
        return parent.checkBlockCollision(bb);
    }

    @Override
    public boolean containsAnyLiquid(AxisAlignedBB bb) {
        return parent.containsAnyLiquid(bb);
    }

    @Override
    public boolean isFlammableWithin(AxisAlignedBB bb) {
        return parent.isFlammableWithin(bb);
    }

    @Override
    public boolean handleMaterialAcceleration(AxisAlignedBB bb, Material materialIn, Entity entityIn) {
        return parent.handleMaterialAcceleration(bb, materialIn, entityIn);
    }

    @Override
    public boolean isMaterialInBB(AxisAlignedBB bb, Material materialIn) {
        return parent.isMaterialInBB(bb, materialIn);
    }

    @Override
    public Explosion createExplosion(@Nullable Entity entityIn, double x, double y, double z, float strength, boolean isSmoking) {
        return parent.createExplosion(entityIn, x, y, z, strength, isSmoking);
    }

    @Override
    public Explosion newExplosion(@Nullable Entity entityIn, double x, double y, double z, float strength, boolean isFlaming, boolean isSmoking) {
        return parent.newExplosion(entityIn, x, y, z, strength, isFlaming, isSmoking);
    }

    @Override
    public float getBlockDensity(Vec3d vec, AxisAlignedBB bb) {
        return parent.getBlockDensity(vec, bb);
    }

    @Override
    public boolean extinguishFire(@Nullable EntityPlayer player, BlockPos pos, EnumFacing side) {
        return parent.extinguishFire(player, pos, side);
    }

    @Override
    public String getDebugLoadedEntities() {
        return parent.getDebugLoadedEntities();
    }

    @Override
    public String getProviderName() {
        return parent.getProviderName();
    }

    @Nullable
    @Override
    public TileEntity getTileEntity(BlockPos pos) {
        if (isPosWithinRegion(pos))
            return parent.getTileEntity(pos);
        else
            return null;
    }

    @Override
    public void setTileEntity(BlockPos pos, @Nullable TileEntity tileEntityIn) {
        if (isPosWithinRegion(pos))
            parent.setTileEntity(pos, tileEntityIn);
    }

    @Override
    public void removeTileEntity(BlockPos pos) {
        if (isPosWithinRegion(pos))
            parent.removeTileEntity(pos);
    }

    @Override
    public void markTileEntityForRemoval(TileEntity tileEntityIn) {
        parent.markTileEntityForRemoval(tileEntityIn);
    }

    @Override
    public boolean isBlockFullCube(BlockPos pos) {
        if (isPosWithinRegion(pos))
            return parent.isBlockFullCube(pos);
        else return false;
    }

    @Override
    public boolean isBlockNormalCube(BlockPos pos, boolean _default) {
        if (isPosWithinRegion(pos))
            return parent.isBlockNormalCube(pos, _default);
        else
            return false;
    }

    @Override
    public void calculateInitialSkylight() {
        parent.calculateInitialSkylight();
    }

    @Override
    public void setAllowedSpawnTypes(boolean hostile, boolean peaceful) {
        parent.setAllowedSpawnTypes(hostile, peaceful);
    }

    @Override
    public void calculateInitialWeather() {
        parent.calculateInitialWeather();
    }

    @Override
    public void calculateInitialWeatherBody() {
        parent.calculateInitialWeatherBody();
    }

    @Override
    public void updateWeatherBody() {
        parent.updateWeatherBody();
    }

    @Override
    public void immediateBlockTick(BlockPos pos, IBlockState state, Random random) {
        if (isPosWithinRegion(pos))
            parent.immediateBlockTick(pos, state, random);
    }

    @Override
    public boolean canBlockFreezeWater(BlockPos pos) {
        if (isPosWithinRegion(pos))
            return parent.canBlockFreezeWater(pos);
        else
            return false;
    }

    @Override
    public boolean canBlockFreezeNoWater(BlockPos pos) {
        if (isPosWithinRegion(pos))
            return parent.canBlockFreezeNoWater(pos);
        else
            return false;
    }

    @Override
    public boolean canBlockFreeze(BlockPos pos, boolean noWaterAdj) {
        if (isPosWithinRegion(pos))
            return parent.canBlockFreeze(pos, noWaterAdj);
        else
            return false;
    }

    @Override
    public boolean canBlockFreezeBody(BlockPos pos, boolean noWaterAdj) {
        if (isPosWithinRegion(pos))
            return parent.canBlockFreezeBody(pos, noWaterAdj);
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
            return parent.checkLight(pos);
        else
            return false;
    }

    @Override
    public boolean checkLightFor(EnumSkyBlock lightType, BlockPos pos) {
        if (isPosWithinRegion(pos))
            return parent.checkLightFor(lightType, pos);
        else
            return false;
    }

    @Override
    public boolean tickUpdates(boolean runAllPending) {
        return parent.tickUpdates(runAllPending);
    }

    @Nullable
    @Override
    public List<NextTickListEntry> getPendingBlockUpdates(Chunk chunkIn, boolean p_72920_2_) {
        return parent.getPendingBlockUpdates(chunkIn, p_72920_2_);
    }

    @Nullable
    @Override
    public List<NextTickListEntry> getPendingBlockUpdates(StructureBoundingBox structureBB, boolean p_175712_2_) {
        return parent.getPendingBlockUpdates(structureBB, p_175712_2_);
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
        parent.markChunkDirty(pos, unusedTileEntity);
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
        return parent.getSeaLevel();
    }

    @Override
    public void setSeaLevel(int seaLevelIn) {
        parent.setSeaLevel(seaLevelIn);
    }

    @Override
    public int getStrongPower(BlockPos pos, EnumFacing direction) {
        return parent.getStrongPower(pos, direction);
    }

    @Override
    public WorldType getWorldType() {
        return parent.getWorldType();
    }

    @Override
    public int getStrongPower(BlockPos pos) {
        return parent.getStrongPower(pos);
    }

    @Override
    public boolean isSidePowered(BlockPos pos, EnumFacing side) {
        return parent.isSidePowered(pos, side);
    }

    @Override
    public int getRedstonePower(BlockPos pos, EnumFacing facing) {
        return parent.getRedstonePower(pos, facing);
    }

    @Override
    public boolean isBlockPowered(BlockPos pos) {
        return parent.isBlockPowered(pos);
    }

    @Override
    public int isBlockIndirectlyGettingPowered(BlockPos pos) {
        return parent.isBlockIndirectlyGettingPowered(pos);
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
        return parent.getNearestAttackablePlayer(pos.xCoord, pos.yCoord, pos.zCoord, maxXZDistance, maxYDistance, playerToDouble, p_184150_12_);
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
        parent.checkSessionLock();
    }

    @Override
    public long getSeed() {
        return parent.getSeed();
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
        return parent.getSpawnPoint();
    }

    @Override
    public void setSpawnPoint(BlockPos pos) {
        parent.setSpawnPoint(pos);
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
        return parent.isBlockModifiable(player, pos);
    }

    @Override
    public boolean canMineBlockBody(EntityPlayer player, BlockPos pos) {
        return parent.canMineBlockBody(player, pos);
    }

    @Override
    public void setEntityState(Entity entityIn, byte state) {
        parent.setEntityState(entityIn, state);
    }

    @Override
    public void addBlockEvent(BlockPos pos, Block blockIn, int eventID, int eventParam) {
        parent.addBlockEvent(pos, blockIn, eventID, eventParam);
    }

    @Override
    public ISaveHandler getSaveHandler() {
        return parent.getSaveHandler();
    }

    @Override
    public WorldInfo getWorldInfo() {
        return parent.getWorldInfo();
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
        return realWorld.getThunderStrength(delta);
    }

    @Override
    public void setThunderStrength(float strength) {
        realWorld.setThunderStrength(strength);
    }

    @Override
    public float getRainStrength(float delta) {
        return realWorld.getRainStrength(delta);
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
        return parent.getMapStorage();
    }

    @Override
    public void setData(String dataID, WorldSavedData worldSavedDataIn) {
        parent.setData(dataID, worldSavedDataIn);
    }

    @Nullable
    @Override
    public WorldSavedData loadData(Class<? extends WorldSavedData> clazz, String dataID) {
        return parent.loadData(clazz, dataID);
    }

    @Override
    public int getUniqueDataId(String key) {
        return parent.getUniqueDataId(key);
    }

    @Override
    public void playBroadcastSound(int id, BlockPos pos, int data) {
        parent.playBroadcastSound(id, pos, data);
    }

    @Override
    public void playEvent(int type, BlockPos pos, int data) {
        parent.playEvent(type, pos, data);
    }

    @Override
    public void playEvent(@Nullable EntityPlayer player, int type, BlockPos pos, int data) {
        parent.playEvent(player, type, pos, data);
    }

    @Override
    public int getHeight() {
        return parent.getHeight();
    }

    @Override
    public int getActualHeight() {
        return parent.getActualHeight();
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
        parent.sendBlockBreakProgress(breakerId, pos, progress);
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
        parent.updateComparatorOutputLevel(pos, blockIn);
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
        return parent.getVillageCollection();
    }

    @Override
    public WorldBorder getWorldBorder() {
        return parent.getWorldBorder();
    }

    @Override
    public boolean isSpawnChunk(int x, int z) {
        return parent.isSpawnChunk(x, z);
    }

    @Override
    public boolean isSideSolid(BlockPos pos, EnumFacing side) {
        return parent.isSideSolid(pos, side);
    }

    @Override
    public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default) {
        return parent.isSideSolid(pos, side, _default);
    }

    @Override
    public ImmutableSetMultimap<ChunkPos, ForgeChunkManager.Ticket> getPersistentChunks() {
        return parent.getPersistentChunks();
    }

    @Override
    public Iterator<Chunk> getPersistentChunkIterable(Iterator<Chunk> chunkIterator) {
        return parent.getPersistentChunkIterable(chunkIterator);
    }

    @Override
    public int getBlockLightOpacity(BlockPos pos) {
        return parent.getBlockLightOpacity(pos);
    }

    @Override
    public int countEntities(EnumCreatureType type, boolean forSpawnCount) {
        return realWorld.countEntities(type, forSpawnCount);
    }


    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return parent.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        return parent.getCapability(capability, facing);
    }

    @Override
    public MapStorage getPerWorldStorage() {
        return parent.getPerWorldStorage();
    }

    @Override
    public LootTableManager getLootTableManager() {
        return parent.getLootTableManager();
    }

    @Nullable
    @Override
    public BlockPos findNearestStructure(String p_190528_1_, BlockPos pos, boolean p_190528_3_) {
        pos = region.convertRegionPosToRealWorld(pos);

        return realWorld.findNearestStructure(p_190528_1_, pos, p_190528_3_);
    }
}
