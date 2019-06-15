package com.tridevmc.movingworld.common.chunk.mobilechunk;

import com.google.common.collect.HashBiMap;
import com.tridevmc.compound.core.reflect.WrappedField;
import com.tridevmc.movingworld.MovingWorldMod;
import com.tridevmc.movingworld.api.IMovingTile;
import com.tridevmc.movingworld.common.chunk.LocatedBlock;
import com.tridevmc.movingworld.common.chunk.mobilechunk.world.FakeWorld;
import com.tridevmc.movingworld.common.entity.EntityMovingWorld;
import com.tridevmc.movingworld.common.util.AABBRotator;
import com.tridevmc.movingworld.common.util.Vec3dMod;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.ITickable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.IFluidState;
import net.minecraft.particles.IParticleData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.*;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.AbstractChunkProvider;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.LogicalSide;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;

public abstract class MobileChunk implements IWorld, IWorldReader {

    public static final WrappedField<BlockState> TILE_BLOCK_STATE = WrappedField.create(TileEntity.class, new String[]{"cachedBlockState", "field_195045_e"});

    public static final int CHUNK_SIZE = 16;
    public static final int CHUNK_MEMORY_USING = CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE * (4 + 2);    //(16*16*16 shorts and ints)

    public final World world;
    protected final EntityMovingWorld entityMovingWorld;
    public Map<BlockPos, TileEntity> chunkTileEntityMap;
    public List<TileEntity> updatableTiles;
    public boolean isChunkLoaded;
    public boolean isModified;
    public LocatedBlock marker;
    public ArrayList<IMovingTile> movingWorldTileEntities;
    private Map<BlockPos, ChunkSection> blockStorageMap;
    private boolean boundsInit;
    private BlockPos minBounds;
    private BlockPos maxBounds;
    private int blockCount;
    private Biome creationSpotBiome;
    private HashBiMap<BlockPos, AxisAlignedBB> boundingBoxes;
    private HashBiMap<BlockPos, AxisAlignedBB> chunkBoundingBoxes;
    private FakeWorld fakeWorld;

    public MobileChunk(World world, EntityMovingWorld entitymovingWorld) {
        this.world = world;
        this.entityMovingWorld = entitymovingWorld;
        this.blockStorageMap = new HashMap<>(1);
        this.chunkTileEntityMap = new HashMap<>(2);
        this.updatableTiles = new ArrayList<>();
        this.boundingBoxes = HashBiMap.create();
        this.chunkBoundingBoxes = HashBiMap.create();
        this.movingWorldTileEntities = new ArrayList<>();
        this.marker = null;

        this.isChunkLoaded = false;
        this.isModified = false;

        this.boundsInit = false;
        this.minBounds = new BlockPos(-1, -1, -1);
        this.maxBounds = new BlockPos(-1, -1, -1);

        this.blockCount = 0;

        this.creationSpotBiome = Biomes.OCEAN;
    }

    public FakeWorld getFakeWorld() {
        if (this.fakeWorld != null) return this.fakeWorld;
        this.fakeWorld = FakeWorld.getFakeWorld(this);
        return this.fakeWorld;
    }

    public EntityMovingWorld getEntityMovingWorld() {
        return this.entityMovingWorld;
    }

    public BlockPos getBlockPosFromBounds(AxisAlignedBB bb) {
        return this.boundingBoxes.inverse().get(bb);
    }

    public Vec3d getWorldPosForChunkPos(BlockPos pos) {
        Vec3d movingWorldPos = new Vec3d(this.entityMovingWorld.posX, this.entityMovingWorld.posY, this.entityMovingWorld.posZ);
        movingWorldPos = movingWorldPos.subtract((double) this.maxX() / 2, (double) this.maxY() / 2, (double) this.maxZ() / 2);
        Vec3d returnPos = new Vec3d(pos.getX(), pos.getY(), pos.getZ());
        returnPos.add(movingWorldPos);
        return returnPos;
    }

    public Vec3d getWorldPosForChunkPos(Vec3d vec) {
        Vec3d movingWorldPos = new Vec3d(this.entityMovingWorld.posX, this.entityMovingWorld.posY, this.entityMovingWorld.posZ);
        movingWorldPos = movingWorldPos.subtract((double) this.maxX() / 2, (double) this.maxY() / 2, (double) this.maxZ() / 2);
        Vec3d returnPos = new Vec3d(vec.x, vec.y, vec.z);
        returnPos.add(movingWorldPos);
        return returnPos;
    }


    public Vec3d getChunkPosForWorldPos(Vec3d pos) {
        Vec3d movingWorldPos = new Vec3d(this.entityMovingWorld.posX, this.entityMovingWorld.posY, this.entityMovingWorld.posZ);
        movingWorldPos = movingWorldPos.subtract((double) this.maxX() / 2, (double) this.maxY() / 2, (double) this.maxZ() / 2);
        Vec3d returnPos = new Vec3d(pos.x, pos.y, pos.z);
        returnPos = returnPos.subtract(movingWorldPos);
        return returnPos;
    }

    public AxisAlignedBB offsetWorldBBToChunkBB(AxisAlignedBB axisAlignedBB) {
        double minX = axisAlignedBB.minX;
        double minY = axisAlignedBB.minY;
        double minZ = axisAlignedBB.minZ;
        double maxX = axisAlignedBB.maxX;
        double maxY = axisAlignedBB.maxY;
        double maxZ = axisAlignedBB.maxZ;

        Vec3d minVec = new Vec3d(minX, minY, minZ);
        Vec3d maxVec = new Vec3d(maxX, maxY, maxZ);
        minVec = this.getChunkPosForWorldPos(minVec);
        maxVec = this.getChunkPosForWorldPos(maxVec);

        axisAlignedBB = new AxisAlignedBB(minVec.x, minVec.y, minVec.z, maxVec.x, maxVec.y, maxVec.z);

        return axisAlignedBB;
    }

    public BlockPos shiftToStorageMapPos(BlockPos blockPosition) {
        return new BlockPos(blockPosition.getX() >> 4, blockPosition.getY() >> 4, blockPosition.getZ() >> 4);
    }

    public BlockPos shiftToInternalStoragePos(BlockPos blockPosition) {
        return new BlockPos(blockPosition.getX() & 15, blockPosition.getY() & 15, blockPosition.getZ() & 15);
    }

    public ChunkSection getBlockStorage(BlockPos pos) {
        return this.blockStorageMap.get(this.shiftToStorageMapPos(pos));
    }

    public ChunkSection getBlockStorageOrCreate(BlockPos blockPos) {
        BlockPos shiftedPos = this.shiftToStorageMapPos(blockPos);
        ChunkSection storage = this.blockStorageMap.get(shiftedPos);
        if (storage != null) return storage;
        storage = new ChunkSection(shiftedPos.getY());
        this.blockStorageMap.put(shiftedPos, storage);
        return storage;
    }

    public int getBlockCount() {
        return this.blockCount;
    }

    public float getCenterX() {
        return (this.minBounds.getX() + this.maxBounds.getX()) / 2F;
    }

    public float getCenterY() {
        return (this.minBounds.getY() + this.maxBounds.getY()) / 2F;
    }

    public float getCenterZ() {
        return (this.minBounds.getZ() + this.maxBounds.getZ()) / 2F;
    }

    public int minX() {
        return this.minBounds.getX();
    }

    public int maxX() {
        return this.maxBounds.getX();
    }

    public int minY() {
        return this.minBounds.getY();
    }

    public int maxY() {
        return this.maxBounds.getY();
    }

    public int minZ() {
        return this.minBounds.getZ();
    }

    public int maxZ() {
        return this.maxBounds.getZ();
    }

    public Biome getCreationSpotBiome() {
        return this.creationSpotBiome;
    }

    public void setCreationSpotBiome(Biome biome) {
        this.creationSpotBiome = biome;
    }

    public boolean addBlockWithState(BlockPos pos, BlockState state) {
        if (state == null) return false;

        ChunkSection storage = this.getBlockStorageOrCreate(pos);
        BlockPos internalStoragePos = this.shiftToInternalStoragePos(pos);

        BlockState currentState = storage.get(internalStoragePos.getX(), internalStoragePos.getY(), internalStoragePos.getZ());
        MovingWorldMod.LOG.debug(String.format("Adding block with state: %s, at position %s in a mobile chunk.", state, pos));
        if (currentState.equals(state)) {
            return false;
        }

        storage.set(internalStoragePos.getX(), internalStoragePos.getY(), internalStoragePos.getZ(), state);

        if (this.boundsInit) {
            int minX = Math.min(this.minBounds.getX(), pos.getX());
            int minY = Math.min(this.minBounds.getY(), pos.getY());
            int minZ = Math.min(this.minBounds.getZ(), pos.getZ());
            int maxX = Math.max(this.maxBounds.getX(), pos.getX() + 1);
            int maxY = Math.max(this.maxBounds.getY(), pos.getY() + 1);
            int maxZ = Math.max(this.maxBounds.getZ(), pos.getZ() + 1);

            this.minBounds = new BlockPos(minX, minY, minZ);
            this.maxBounds = new BlockPos(maxX, maxY, maxZ);
        } else {
            this.boundsInit = true;
            int minX = pos.getX();
            int minY = pos.getY();
            int minZ = pos.getZ();
            int maxX = pos.getX() + 1;
            int maxY = pos.getY() + 1;
            int maxZ = pos.getZ() + 1;

            this.minBounds = new BlockPos(minX, minY, minZ);
            this.maxBounds = new BlockPos(maxX, maxY, maxZ);
        }

        this.blockCount++;
        this.setChunkModified();

        TileEntity tileentity;
        if (state.hasTileEntity()) {
            tileentity = this.getTileEntity(pos);

            if (tileentity == null) {
                this.setTileEntity(pos, tileentity);
            } else {
                tileentity.updateContainingBlockInfo();
                TILE_BLOCK_STATE.set(tileentity, state);
            }
        }

        return true;
    }

    public void calculateBounds() {
        for (int i = this.minX(); i < this.maxX(); i++) {
            for (int j = this.minY(); j < this.maxY(); j++) {
                for (int k = this.minZ(); k < this.maxZ(); k++) {
                    BlockPos pos = new BlockPos(i, j, k);
                    this.calculateBlockBounds(pos);
                }
            }
        }
    }

    public AxisAlignedBB calculateBlockBounds(BlockPos pos) {
        BlockState state = this.getBlockState(pos);
        if (state == null || (state.getMaterial().equals(Material.AIR))) {
            return null;
        }

        if (state.isAir())
            return null;

        AxisAlignedBB axisAlignedBB = this.getBlockState(pos).getCollisionShape(this.getFakeWorld(), pos).getBoundingBox();
        this.chunkBoundingBoxes.put(pos, axisAlignedBB);

        double maxDX = (double) this.maxX();
        double maxDY = (double) this.maxY();
        double maxDZ = (double) this.maxZ();

        maxDX = maxDX / 2 * -1;
        maxDY = maxDY / 2 * -1;
        maxDZ = maxDZ / 2 * -1;

        axisAlignedBB = axisAlignedBB.offset(this.entityMovingWorld.posX + maxDX - 0.5, this.entityMovingWorld.posY + maxDY, this.entityMovingWorld.posZ + maxDZ - 0.5);
        this.boundingBoxes.put(pos, axisAlignedBB);

        return axisAlignedBB;
    }

    public List<AxisAlignedBB> getBoxes() {
        ArrayList<AxisAlignedBB> boxes = new ArrayList<>();
        boxes.addAll(this.boundingBoxes.values());

        return boxes;
    }

    /**
     * Based off of the implementation in net.minecraft.world.World
     *
     * @return applicable bounding boxes with applicable position.
     */
    public List getCollidingBoundingBoxes(boolean chunkPos, AxisAlignedBB startBox, AxisAlignedBB endBox) {
        ArrayList<AxisAlignedBB> axisAlignedBBs = new ArrayList<>();

        AxisAlignedBB boxUnion = startBox.union(endBox);

        if (!chunkPos) {
            for (AxisAlignedBB axisAlignedBB : this.boundingBoxes.values()) {
                if (axisAlignedBB.intersects(boxUnion)) {
                    axisAlignedBBs.add(axisAlignedBB);
                }
            }
        } else {
            for (AxisAlignedBB axisAlignedBB : this.chunkBoundingBoxes.values()) {
                if (axisAlignedBB.intersects(boxUnion)) {
                    axisAlignedBBs.add(axisAlignedBB);
                }
            }
        }

        return axisAlignedBBs;
    }

    public List getCollidingBoundingBoxes(boolean chunkPos, AxisAlignedBB box) {
        ArrayList<AxisAlignedBB> axisAlignedBBs = new ArrayList<>();

        if (!chunkPos) {
            for (AxisAlignedBB axisAlignedBB : this.boundingBoxes.values()) {
                if (axisAlignedBB.intersects(box)) {
                    axisAlignedBBs.add(axisAlignedBB);
                }
            }
        } else {
            for (AxisAlignedBB axisAlignedBB : this.chunkBoundingBoxes.values()) {
                if (axisAlignedBB.intersects(box)) {
                    axisAlignedBBs.add(axisAlignedBB);
                }
            }
        }

        return axisAlignedBBs;
    }

    /**
     * Offsets all the bounding boxes as needed.
     */
    public void updateBlockBounds(float rotationYaw) {
        HashBiMap<BlockPos, AxisAlignedBB> newBoundingBoxes = HashBiMap.create();

        for (AxisAlignedBB bb : this.chunkBoundingBoxes.values()) {
            if (bb != null) {
                BlockPos offset = this.chunkBoundingBoxes.inverse().get(bb);
                float rotationRadians = (float) Math.toRadians(rotationYaw);

                AxisAlignedBB axisAlignedBB = bb;
                BlockPos pos = this.chunkBoundingBoxes.inverse().get(bb);

                double maxDX = (double) this.maxX();
                double maxDY = (double) this.maxY();
                double maxDZ = (double) this.maxZ();

                maxDX = maxDX / 2 * -1;
                maxDY = maxDY / 2 * -1 + 1;
                maxDZ = maxDZ / 2 * -1;


                axisAlignedBB = AABBRotator.rotateAABBAroundY(axisAlignedBB, offset.getX(), offset.getZ(), rotationRadians);
                Vec3dMod vec3 = new Vec3dMod(maxDX, maxDY, maxDZ).rotateAroundY(rotationRadians);
                axisAlignedBB = axisAlignedBB.offset(this.entityMovingWorld.posX + vec3.x, this.entityMovingWorld.posY + vec3.y, this.entityMovingWorld.posZ + vec3.z);

                newBoundingBoxes.put(pos, axisAlignedBB);
            }
        }

        this.boundingBoxes = newBoundingBoxes;
    }

    public boolean setBlockState(LocatedBlock locatedBlock) {
        return this.setBlockState(locatedBlock.pos, locatedBlock.state);
    }

    public boolean setBlockState(BlockPos pos, BlockState state) {
        ChunkSection storage = this.getBlockStorage(pos);
        if (storage == null) return this.addBlockWithState(pos, state);

        BlockState checkState = this.getBlockState(pos);
        if (checkState.getBlock().equals(state.getBlock())) {
            return false;
        }
        BlockPos internalStoragePos = this.shiftToInternalStoragePos(pos);

        if (storage.get(internalStoragePos.getX(), internalStoragePos.getY(), internalStoragePos.getZ()) == null) {
            this.blockCount++;
        }

        storage.set(internalStoragePos.getX(), internalStoragePos.getY(), internalStoragePos.getZ(), state);
        state = storage.get(internalStoragePos.getX(), internalStoragePos.getY(), internalStoragePos.getZ());

        if (state.hasTileEntity()) {
            TileEntity tileentity = this.getTileEntity(pos);

            if (tileentity != null) {
                tileentity.updateContainingBlockInfo();
                TILE_BLOCK_STATE.set(tileentity, state);
            }
        }

        if (this.boundsInit) {
            int minX = Math.min(this.minBounds.getX(), pos.getX());
            int minY = Math.min(this.minBounds.getY(), pos.getY());
            int minZ = Math.min(this.minBounds.getZ(), pos.getZ());
            int maxX = Math.max(this.maxBounds.getX(), pos.getX() + 1);
            int maxY = Math.max(this.maxBounds.getY(), pos.getY() + 1);
            int maxZ = Math.max(this.maxBounds.getZ(), pos.getZ() + 1);

            this.minBounds = new BlockPos(minX, minY, minZ);
            this.maxBounds = new BlockPos(maxX, maxY, maxZ);
        } else {
            this.boundsInit = true;
            int minX = pos.getX();
            int minY = pos.getY();
            int minZ = pos.getZ();
            int maxX = pos.getX() + 1;
            int maxY = pos.getY() + 1;
            int maxZ = pos.getZ() + 1;

            this.minBounds = new BlockPos(minX, minY, minZ);
            this.maxBounds = new BlockPos(maxX, maxY, maxZ);
        }

        this.setChunkModified();
        return true;
    }

    /**
     * Gets the TileEntity for a given block in this chunk
     */
    @Override
    public TileEntity getTileEntity(BlockPos pos) {
        TileEntity tileentity = this.chunkTileEntityMap.get(pos);

        if (tileentity == null) {
            BlockState blockState = this.getBlockState(pos);
            Block block = blockState.getBlock();

            if (block == null || !block.hasTileEntity(blockState)) {
                return null;
            }

            tileentity = block.createTileEntity(blockState, this.world);
            this.setTileEntity(pos, tileentity);

            tileentity = this.chunkTileEntityMap.get(pos);
        }

        return tileentity;
    }

    public void setTileEntity(BlockPos pos, TileEntity tileentity) {
        if (tileentity == null) {
            this.removeChunkBlockTileEntity(pos);
            return;
        }

        this.setChunkBlockTileEntity(pos, tileentity);
    }

    /**
     * Sets the TileEntity for a given block in this chunk
     */
    private void setChunkBlockTileEntity(BlockPos pos, TileEntity newTile) {
        BlockPos chunkPosition = new BlockPos(pos.getX(), pos.getY(), pos.getZ());
        newTile.setPos(pos);
        newTile.setWorld(this.getFakeWorld());

        BlockState blockState = this.getBlockState(pos);
        if (blockState != null && blockState.hasTileEntity()) {
            if (this.chunkTileEntityMap.containsKey(chunkPosition)) {
                this.chunkTileEntityMap.get(chunkPosition).remove(); //RIP
            }

            TILE_BLOCK_STATE.set(newTile, blockState);
            this.chunkTileEntityMap.put(chunkPosition, newTile);

            if (newTile instanceof IMovingTile) {
                if (!this.movingWorldTileEntities.contains(newTile))
                    this.movingWorldTileEntities.add((IMovingTile) newTile);
                ((IMovingTile) newTile).setParentMovingWorld(this.entityMovingWorld, chunkPosition);
            } else if (newTile instanceof ITickable && MovingWorldMod.CONFIG.isTileUpdatable(newTile)) {
                this.updatableTiles.add(newTile);
            }
        }
    }

    /**
     * Removes the TileEntity for a given block in this chunk
     */
    public void removeChunkBlockTileEntity(BlockPos pos) {
        BlockPos chunkPosition = new BlockPos(pos.getX(), pos.getY(), pos.getZ());
        if (this.isChunkLoaded) {
            TileEntity tileentity = this.chunkTileEntityMap.remove(chunkPosition);
            if (tileentity != null) {
                if (tileentity instanceof IMovingTile) {
                    if (!this.movingWorldTileEntities.contains(tileentity))
                        this.movingWorldTileEntities.add((IMovingTile) tileentity);
                    ((IMovingTile) tileentity).setParentMovingWorld(null, pos);
                }
                if (tileentity instanceof ITickable && MovingWorldMod.CONFIG.isTileUpdatable(tileentity)) {
                    this.updatableTiles.remove(tileentity);
                }

                tileentity.remove();
            }
        }
    }

    /**
     * Called when this Chunk is loaded by the ChunkProvider
     */
    public void onChunkLoad() {
        this.isChunkLoaded = true;
        this.world.addTileEntities(this.chunkTileEntityMap.values());
    }

    /**
     * Called when this Chunk is unloaded by the ChunkProvider
     */
    public void onChunkUnload() {
        this.isChunkLoaded = false;
    }

    public void setChunkModified() {
        this.isModified = true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public int getNeighborAwareLightSubtracted(BlockPos pos, int amount) {
        int lv = LightType.SKY.defaultLightValue;
        return lv << 20 | amount << 4;
    }

    @Override
    public boolean isAirBlock(BlockPos pos) {
        BlockState state = this.getBlockState(pos);
        return state == null || state.getMaterial().equals(Material.AIR);
    }

    public boolean isBlockTakingWaterVolume(BlockPos pos) {
        BlockState blockState = this.getBlockState(pos);
        return !blockState.getMaterial().equals(Material.AIR);
    }

    public int getHeight() {
        return CHUNK_SIZE;
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        BlockPos internalStoragePos = this.shiftToInternalStoragePos(pos);
        ChunkSection storage = this.getBlockStorage(pos);
        BlockState state = storage != null ?
                storage.get(internalStoragePos.getX(), internalStoragePos.getY(), internalStoragePos.getZ()) : null;
        if (state == null || storage == null)
            return Blocks.AIR.getDefaultState();
        return state;
    }

    @Override
    public Biome getBiome(BlockPos pos) {
        return this.creationSpotBiome;
    }

    @Override
    public int getStrongPower(BlockPos pos, Direction direction) {
        return 0;
    }

    public final int getMemoryUsage() {
        return 2 + this.blockCount * 9; // (3 bytes + 2 bytes (short) + 4 bytes (int) = 9 bytes per block) + 2 bytes (short)
    }

    public boolean needsCustomCollision(AxisAlignedBB axisAlignedBB) {

        boolean retVal = false;

        for (AxisAlignedBB bb : this.boundingBoxes.values()) {
            if (this.bbContainsBB(axisAlignedBB, bb)) {
                retVal = true;
                break;
            }
        }

        return retVal;
    }

    private boolean bbContainsBB(AxisAlignedBB container, AxisAlignedBB axisAlignedBB) {
        Vec3dMod minVec = new Vec3dMod(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ);
        //Vec3d midVec = new Vec3((axisAlignedBB.maxX - axisAlignedBB.minX) / 2, (axisAlignedBB.maxY - axisAlignedBB.minY) / 2, (axisAlignedBB.maxZ - axisAlignedBB.minZ) / 2);
        //midVec = midVec.add(minVec);
        Vec3d maxVec = new Vec3d(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ);

        if (container.minX < minVec.x || container.minY < minVec.y || container.minZ < minVec.z) {
            return true;
        }
        return container.maxX > maxVec.x || container.maxY > maxVec.y || container.maxZ > maxVec.z;
    }

    public abstract LogicalSide side();

    public void markTileDirty(BlockPos pos) {
        this.setChunkModified();
    }

    @Override
    public long getSeed() {
        return this.world.getSeed();
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
    public IChunk getChunk(int chunkX, int chunkZ) {
        return new FakeChunk(this, new ChunkPos(chunkX, chunkZ), new Biome[]{this.getCreationSpotBiome()});
    }

    @Override
    public World getWorld() {
        return this.fakeWorld;
    }

    @Override
    public WorldInfo getWorldInfo() {
        return this.world.getWorldInfo();
    }

    @Override
    public DifficultyInstance getDifficultyForLocation(BlockPos pos) {
        return this.world.getDifficultyForLocation(pos);
    }

    @Override
    public AbstractChunkProvider getChunkProvider() {
        return this.world.getChunkProvider();
    }

    @Override
    public Random getRandom() {
        return this.world.getRandom();
    }

    @Override
    public void notifyNeighbors(BlockPos pos, Block blockIn) {
        Vec3d worldPosForChunkPos = this.getWorldPosForChunkPos(pos);
        this.world.notifyNeighbors(new BlockPos(worldPosForChunkPos.x, worldPosForChunkPos.y, worldPosForChunkPos.z), blockIn);
    }

    @Override
    public BlockPos getSpawnPoint() {
        return BlockPos.ZERO;
    }

    @Override
    public void playSound(@Nullable PlayerEntity player, BlockPos pos, SoundEvent soundIn, SoundCategory category, float volume, float pitch) {
        Vec3d worldPosForChunkPos = this.getWorldPosForChunkPos(pos);
        this.world.playSound(player, new BlockPos(worldPosForChunkPos.x, worldPosForChunkPos.y, worldPosForChunkPos.z), soundIn, category, volume, pitch);
    }

    @Override
    public void addParticle(IParticleData particleData, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        Vec3d worldPosForChunkPos = this.getWorldPosForChunkPos(new Vec3d(x, y, z));
        this.world.addParticle(particleData, worldPosForChunkPos.x, worldPosForChunkPos.y, worldPosForChunkPos.z, xSpeed, ySpeed, zSpeed);
    }

    @Override
    public int getCombinedLight(BlockPos pos, int lightValue) {
        return LightType.SKY.defaultLightValue;
    }

    @Override
    public int getLightFor(LightType type, BlockPos pos) {
        return LightType.SKY.defaultLightValue;
    }

    @Override
    public int getLightSubtracted(BlockPos pos, int amount) {
        return LightType.SKY.defaultLightValue;
    }

    @Override
    public int getHeight(Heightmap.Type heightmapType, int x, int z) {
        return 0;
    }

    @Nullable
    @Override
    public PlayerEntity getClosestPlayer(double x, double y, double z, double distance, Predicate<Entity> predicate) {
        Vec3d worldPosForChunkPos = this.getWorldPosForChunkPos(new Vec3d(x, y, z));
        return this.world.getClosestPlayer(worldPosForChunkPos.x, worldPosForChunkPos.y, worldPosForChunkPos.z, distance, predicate);
    }

    @Override
    public int getSkylightSubtracted() {
        return this.world.getSkylightSubtracted();
    }

    @Override
    public WorldBorder getWorldBorder() {
        return this.world.getWorldBorder();
    }

    @Override
    public boolean checkNoEntityCollision(@Nullable Entity entityIn, VoxelShape shape) {
        return false;
    }

    @Override
    public boolean isRemote() {
        return this.world.isRemote();
    }

    @Override
    public int getSeaLevel() {
        return 0;
    }

    @Override
    public Dimension getDimension() {
        return this.world.getDimension();
    }

    @Override
    public IFluidState getFluidState(BlockPos pos) {
        return null;
    }

    @Override
    public boolean setBlockState(BlockPos pos, BlockState newState, int flags) {
        return this.setBlockState(pos, newState);
    }

    @Override
    public void playEvent(@Nullable PlayerEntity player, int type, BlockPos pos, int data) {
        this.world.playEvent(player, type, new BlockPos(getWorldPosForChunkPos(pos)), data);
    }

    @Override
    public List<Entity> getEntitiesInAABBexcluding(@Nullable Entity entityIn, AxisAlignedBB bb, @Nullable Predicate<? super Entity> predicate) {
        AxisAlignedBB offsetBB = new AxisAlignedBB(getWorldPosForChunkPos(new Vec3d(bb.minX, bb.minY, bb.minZ)),
                getWorldPosForChunkPos(new Vec3d(bb.maxX, bb.maxY, bb.maxZ)));
        return world.getEntitiesInAABBexcluding(entityIn, offsetBB, predicate);
    }

    @Override
    public <T extends Entity> List<T> getEntitiesWithinAABB(Class<? extends T> clazz, AxisAlignedBB bb, @Nullable Predicate<? super T> predicate) {
        AxisAlignedBB offsetBB = new AxisAlignedBB(getWorldPosForChunkPos(new Vec3d(bb.minX, bb.minY, bb.minZ)),
                getWorldPosForChunkPos(new Vec3d(bb.maxX, bb.maxY, bb.maxZ)));
        return world.getEntitiesWithinAABB(clazz, offsetBB, predicate);
    }

    @Override
    public List<? extends PlayerEntity> getPlayers() {
        return world.getPlayers();
    }

    @Nullable
    @Override
    public IChunk getChunk(int x, int z, ChunkStatus requiredStatus, boolean nonnull) {
        return new FakeChunk(this, new ChunkPos(x, z), new Biome[]{this.getCreationSpotBiome()});
    }

    @Override
    public BlockPos getHeight(Heightmap.Type heightmapType, BlockPos pos) {
        return pos;
    }

    @Override
    public boolean removeBlock(BlockPos pos, boolean force) {
        ChunkSection storage = this.getBlockStorage(pos);
        if (storage == null) return true;

        BlockState state = this.getBlockState(pos);
        if (Objects.equals(state.getBlock(), Blocks.AIR)) {
            return true;
        }
        if (state.getMaterial().equals(Material.AIR)) {
            BlockPos internalStoragePos = this.shiftToInternalStoragePos(pos);
            storage.set(internalStoragePos.getX(), internalStoragePos.getY(), internalStoragePos.getZ(), Blocks.AIR.getDefaultState());
            return true;
        }
        return false;
    }

    @Override
    public boolean hasBlockState(BlockPos pos, Predicate<BlockState> predicate) {
        return predicate.test(this.getBlockState(pos));
    }

    @Override
    public boolean destroyBlock(BlockPos pos, boolean dropBlock) {
        Vec3d worldPosForChunkPos = this.getWorldPosForChunkPos(pos);
        BlockPos offsetPos = new BlockPos(worldPosForChunkPos.x, worldPosForChunkPos.y, worldPosForChunkPos.z);

        BlockState blockState = this.getBlockState(pos);
        if (blockState.isAir(this, pos)) {
            return false;
        } else {
            IFluidState fluidState = this.getFluidState(pos);
            this.world.playEvent(2001, offsetPos, Block.getStateId(blockState));
            if (dropBlock) {
                Block.spawnDrops(blockState, this.world, offsetPos);
            }

            return this.setBlockState(pos, fluidState.getBlockState(), 3);
        }
    }

    @Override
    public boolean addEntity(Entity entity) {
        Vec3d positionVector = entity.getPositionVector();
        positionVector = this.getWorldPosForChunkPos(positionVector);
        entity.setPosition(positionVector.x, positionVector.y, positionVector.z);
        return this.world.addEntity(entity);
    }

    public Collection<BlockPos> getBlockQueue() {
        return Collections.emptyList();
    }

    public Collection<BlockPos> getTileQueue() {
        return Collections.emptyList();
    }

}
