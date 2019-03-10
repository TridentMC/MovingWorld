package com.elytradev.movingworld.common.chunk.mobilechunk;

import com.elytradev.movingworld.MovingWorldMod;
import com.elytradev.movingworld.api.IMovingTile;
import com.elytradev.movingworld.common.chunk.LocatedBlock;
import com.elytradev.movingworld.common.chunk.mobilechunk.world.FakeWorld;
import com.elytradev.movingworld.common.entity.EntityMovingWorld;
import com.elytradev.movingworld.common.util.AABBRotator;
import com.elytradev.movingworld.common.util.Vec3dMod;
import com.google.common.collect.HashBiMap;
import com.tridevmc.compound.core.reflect.WrappedField;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EnumLightType;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.LogicalSide;

import java.util.*;

public abstract class MobileChunk implements IWorld {

    public static final WrappedField<IBlockState> TILE_BLOCK_STATE = WrappedField.create(TileEntity.class, new String[]{"cachedBlockState", "field_195045_e"});

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
        entityMovingWorld = entitymovingWorld;
        blockStorageMap = new HashMap<>(1);
        chunkTileEntityMap = new HashMap<>(2);
        updatableTiles = new ArrayList<>();
        boundingBoxes = HashBiMap.create();
        chunkBoundingBoxes = HashBiMap.create();
        movingWorldTileEntities = new ArrayList<>();
        marker = null;

        isChunkLoaded = false;
        isModified = false;

        boundsInit = false;
        minBounds = new BlockPos(-1, -1, -1);
        maxBounds = new BlockPos(-1, -1, -1);

        blockCount = 0;

        creationSpotBiome = Biomes.OCEAN; // Ocean biome id.
    }

    public FakeWorld getFakeWorld() {
        if (fakeWorld != null) return fakeWorld;
        fakeWorld = FakeWorld.getFakeWorld(this);
        return fakeWorld;
    }

    public EntityMovingWorld getEntityMovingWorld() {
        return entityMovingWorld;
    }

    public BlockPos getBlockPosFromBounds(AxisAlignedBB bb) {
        return boundingBoxes.inverse().get(bb);
    }

    public Vec3d getWorldPosForChunkPos(BlockPos pos) {
        Vec3d movingWorldPos = new Vec3d(entityMovingWorld.posX, entityMovingWorld.posY, entityMovingWorld.posZ);
        movingWorldPos = movingWorldPos.subtract((double) maxX() / 2, (double) maxY() / 2, (double) maxZ() / 2);
        Vec3d returnPos = new Vec3d(pos.getX(), pos.getY(), pos.getZ());
        returnPos.add(movingWorldPos);
        return returnPos;
    }

    public Vec3d getWorldPosForChunkPos(Vec3d vec) {
        Vec3d movingWorldPos = new Vec3d(entityMovingWorld.posX, entityMovingWorld.posY, entityMovingWorld.posZ);
        movingWorldPos = movingWorldPos.subtract((double) maxX() / 2, (double) maxY() / 2, (double) maxZ() / 2);
        Vec3d returnPos = new Vec3d(vec.x, vec.y, vec.z);
        returnPos.add(movingWorldPos);
        return returnPos;
    }


    public Vec3d getChunkPosForWorldPos(Vec3d pos) {
        Vec3d movingWorldPos = new Vec3d(entityMovingWorld.posX, entityMovingWorld.posY, entityMovingWorld.posZ);
        movingWorldPos = movingWorldPos.subtract((double) maxX() / 2, (double) maxY() / 2, (double) maxZ() / 2);
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
        minVec = getChunkPosForWorldPos(minVec);
        maxVec = getChunkPosForWorldPos(maxVec);

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
        return blockStorageMap.get(shiftToStorageMapPos(pos));
    }

    public ChunkSection getBlockStorageOrCreate(BlockPos blockPos) {
        BlockPos shiftedPos = shiftToStorageMapPos(blockPos);
        ChunkSection storage = blockStorageMap.get(shiftedPos);
        if (storage != null) return storage;
        storage = new ChunkSection(shiftedPos.getY(), false);
        blockStorageMap.put(shiftedPos, storage);
        return storage;
    }

    public int getBlockCount() {
        return blockCount;
    }

    public float getCenterX() {
        return (minBounds.getX() + maxBounds.getX()) / 2F;
    }

    public float getCenterY() {
        return (minBounds.getY() + maxBounds.getY()) / 2F;
    }

    public float getCenterZ() {
        return (minBounds.getZ() + maxBounds.getZ()) / 2F;
    }

    public int minX() {
        return minBounds.getX();
    }

    public int maxX() {
        return maxBounds.getX();
    }

    public int minY() {
        return minBounds.getY();
    }

    public int maxY() {
        return maxBounds.getY();
    }

    public int minZ() {
        return minBounds.getZ();
    }

    public int maxZ() {
        return maxBounds.getZ();
    }

    public void setCreationSpotBiome(Biome biome) {
        this.creationSpotBiome = biome;
    }

    public Biome getCreationSpotBiome() {
        return this.creationSpotBiome;
    }

    public boolean addBlockWithState(BlockPos pos, IBlockState state) {
        if (state == null) return false;

        ChunkSection storage = getBlockStorageOrCreate(pos);
        BlockPos internalStoragePos = shiftToInternalStoragePos(pos);

        IBlockState currentState = storage.get(internalStoragePos.getX(), internalStoragePos.getY(), internalStoragePos.getZ());
        MovingWorldMod.LOG.debug(String.format("Adding block with state: %s, at position %s in a mobile chunk. \n The block id is: %s, and the metadata is: %s", state, pos));
        if (currentState.equals(state)) {
            return false;
        }

        storage.set(internalStoragePos.getX(), internalStoragePos.getY(), internalStoragePos.getZ(), state);

        if (boundsInit) {
            int minX = Math.min(minBounds.getX(), pos.getX());
            int minY = Math.min(minBounds.getY(), pos.getY());
            int minZ = Math.min(minBounds.getZ(), pos.getZ());
            int maxX = Math.max(maxBounds.getX(), pos.getX() + 1);
            int maxY = Math.max(maxBounds.getY(), pos.getY() + 1);
            int maxZ = Math.max(maxBounds.getZ(), pos.getZ() + 1);

            minBounds = new BlockPos(minX, minY, minZ);
            maxBounds = new BlockPos(maxX, maxY, maxZ);
        } else {
            boundsInit = true;
            int minX = pos.getX();
            int minY = pos.getY();
            int minZ = pos.getZ();
            int maxX = pos.getX() + 1;
            int maxY = pos.getY() + 1;
            int maxZ = pos.getZ() + 1;

            minBounds = new BlockPos(minX, minY, minZ);
            maxBounds = new BlockPos(maxX, maxY, maxZ);
        }

        blockCount++;
        setChunkModified();

        TileEntity tileentity;
        if (state.hasTileEntity()) {
            tileentity = getTileEntity(pos);

            if (tileentity == null) {
                setTileEntity(pos, tileentity);
            } else {
                tileentity.updateContainingBlockInfo();
                TILE_BLOCK_STATE.setValue(tileentity, state);
            }
        }

        return true;
    }

    public void calculateBounds() {
        for (int i = minX(); i < maxX(); i++) {
            for (int j = minY(); j < maxY(); j++) {
                for (int k = minZ(); k < maxZ(); k++) {
                    BlockPos pos = new BlockPos(i, j, k);
                    calculateBlockBounds(pos);
                }
            }
        }
    }

    public AxisAlignedBB calculateBlockBounds(BlockPos pos) {
        IBlockState state = getBlockState(pos);
        if (state == null || (state.getMaterial().equals(Material.AIR))) {
            return null;
        }

        if (!state.getBlock().isCollidable())
            return null;

        AxisAlignedBB axisAlignedBB = this.getBlockState(pos).getCollisionShape(this.getFakeWorld(), pos).getBoundingBox();
        chunkBoundingBoxes.put(pos, axisAlignedBB);

        double maxDX = (double) maxX();
        double maxDY = (double) maxY();
        double maxDZ = (double) maxZ();

        maxDX = maxDX / 2 * -1;
        maxDY = maxDY / 2 * -1;
        maxDZ = maxDZ / 2 * -1;

        axisAlignedBB = axisAlignedBB.offset(entityMovingWorld.posX + maxDX - 0.5, entityMovingWorld.posY + maxDY, entityMovingWorld.posZ + maxDZ - 0.5);
        boundingBoxes.put(pos, axisAlignedBB);

        return axisAlignedBB;
    }

    public List<AxisAlignedBB> getBoxes() {
        ArrayList<AxisAlignedBB> boxes = new ArrayList<>();
        boxes.addAll(boundingBoxes.values());

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
            for (AxisAlignedBB axisAlignedBB : boundingBoxes.values()) {
                if (axisAlignedBB.intersects(boxUnion)) {
                    axisAlignedBBs.add(axisAlignedBB);
                }
            }
        } else {
            for (AxisAlignedBB axisAlignedBB : chunkBoundingBoxes.values()) {
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
            for (AxisAlignedBB axisAlignedBB : boundingBoxes.values()) {
                if (axisAlignedBB.intersects(box)) {
                    axisAlignedBBs.add(axisAlignedBB);
                }
            }
        } else {
            for (AxisAlignedBB axisAlignedBB : chunkBoundingBoxes.values()) {
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

        for (AxisAlignedBB bb : chunkBoundingBoxes.values()) {
            if (bb != null) {
                BlockPos offset = chunkBoundingBoxes.inverse().get(bb);
                float rotationRadians = (float) Math.toRadians(rotationYaw);

                AxisAlignedBB axisAlignedBB = bb;
                BlockPos pos = chunkBoundingBoxes.inverse().get(bb);

                double maxDX = (double) maxX();
                double maxDY = (double) maxY();
                double maxDZ = (double) maxZ();

                maxDX = maxDX / 2 * -1;
                maxDY = maxDY / 2 * -1 + 1;
                maxDZ = maxDZ / 2 * -1;


                axisAlignedBB = AABBRotator.rotateAABBAroundY(axisAlignedBB, offset.getX(), offset.getZ(), rotationRadians);
                Vec3dMod vec3 = new Vec3dMod(maxDX, maxDY, maxDZ).rotateAroundY(rotationRadians);
                axisAlignedBB = axisAlignedBB.offset(entityMovingWorld.posX + vec3.x, entityMovingWorld.posY + vec3.y, entityMovingWorld.posZ + vec3.z);

                newBoundingBoxes.put(pos, axisAlignedBB);
            }
        }

        this.boundingBoxes = newBoundingBoxes;
    }

    public boolean setBlockState(BlockPos pos, IBlockState state) {
        ChunkSection storage = getBlockStorage(pos);
        if (storage == null) return addBlockWithState(pos, state);

        IBlockState checkState = getBlockState(pos);
        if (checkState.getBlock().equals(state.getBlock())) {
            return false;
        }
        BlockPos internalStoragePos = shiftToInternalStoragePos(pos);

        if (storage.get(internalStoragePos.getX(), internalStoragePos.getY(), internalStoragePos.getZ()) == null) {
            blockCount++;
        }

        storage.set(internalStoragePos.getX(), internalStoragePos.getY(), internalStoragePos.getZ(), state);
        state = storage.get(internalStoragePos.getX(), internalStoragePos.getY(), internalStoragePos.getZ());

        if (state.hasTileEntity()) {
            TileEntity tileentity = getTileEntity(pos);

            if (tileentity != null) {
                tileentity.updateContainingBlockInfo();
                TILE_BLOCK_STATE.setValue(tileentity, state);
            }
        }

        if (boundsInit) {
            int minX = Math.min(minBounds.getX(), pos.getX());
            int minY = Math.min(minBounds.getY(), pos.getY());
            int minZ = Math.min(minBounds.getZ(), pos.getZ());
            int maxX = Math.max(maxBounds.getX(), pos.getX() + 1);
            int maxY = Math.max(maxBounds.getY(), pos.getY() + 1);
            int maxZ = Math.max(maxBounds.getZ(), pos.getZ() + 1);

            minBounds = new BlockPos(minX, minY, minZ);
            maxBounds = new BlockPos(maxX, maxY, maxZ);
        } else {
            boundsInit = true;
            int minX = pos.getX();
            int minY = pos.getY();
            int minZ = pos.getZ();
            int maxX = pos.getX() + 1;
            int maxY = pos.getY() + 1;
            int maxZ = pos.getZ() + 1;

            minBounds = new BlockPos(minX, minY, minZ);
            maxBounds = new BlockPos(maxX, maxY, maxZ);
        }

        setChunkModified();
        return true;
    }

    public boolean setBlockAsFilledAir(BlockPos pos) {
        ChunkSection storage = getBlockStorage(pos);
        if (storage == null) return true;

        IBlockState state = getBlockState(pos);
        if (Objects.equals(state.getBlock(), Blocks.AIR)) {
            return true;
        }
        if (state.getMaterial().equals(Material.AIR)) {
            BlockPos internalStoragePos = shiftToInternalStoragePos(pos);
            storage.set(internalStoragePos.getX(), internalStoragePos.getY(), internalStoragePos.getZ(), Blocks.AIR.getDefaultState());
            return true;
        }
        return false;
    }

    /**
     * Gets the TileEntity for a given block in this chunk
     */
    @Override
    public TileEntity getTileEntity(BlockPos pos) {
        TileEntity tileentity = chunkTileEntityMap.get(pos);

        if (tileentity == null) {
            IBlockState blockState = getBlockState(pos);
            Block block = blockState.getBlock();

            if (block == null || !block.hasTileEntity(blockState)) {
                return null;
            }

            tileentity = block.createTileEntity(blockState, world);
            setTileEntity(pos, tileentity);

            tileentity = chunkTileEntityMap.get(pos);
        }

        return tileentity;
    }

    public void setTileEntity(BlockPos pos, TileEntity tileentity) {
        if (tileentity == null) {
            removeChunkBlockTileEntity(pos);
            return;
        }

        setChunkBlockTileEntity(pos, tileentity);
    }

    /**
     * Sets the TileEntity for a given block in this chunk
     */
    private void setChunkBlockTileEntity(BlockPos pos, TileEntity newTile) {
        BlockPos chunkPosition = new BlockPos(pos.getX(), pos.getY(), pos.getZ());
        newTile.setPos(pos);
        newTile.setWorld(getFakeWorld());

        IBlockState blockState = getBlockState(pos);
        if (blockState != null && blockState.hasTileEntity()) {
            if (chunkTileEntityMap.containsKey(chunkPosition)) {
                chunkTileEntityMap.get(chunkPosition).remove(); //RIP
            }

            TILE_BLOCK_STATE.setValue(newTile, blockState);
            chunkTileEntityMap.put(chunkPosition, newTile);

            if (newTile instanceof IMovingTile) {
                if (!movingWorldTileEntities.contains(newTile))
                    movingWorldTileEntities.add((IMovingTile) newTile);
                ((IMovingTile) newTile).setParentMovingWorld(entityMovingWorld, chunkPosition);
            } else if (newTile instanceof ITickable && MovingWorldMod.INSTANCE.getNetworkConfig().isTileUpdatable(newTile.getClass())) {
                updatableTiles.add(newTile);
            }
        }
    }

    /**
     * Removes the TileEntity for a given block in this chunk
     */
    public void removeChunkBlockTileEntity(BlockPos pos) {
        BlockPos chunkPosition = new BlockPos(pos.getX(), pos.getY(), pos.getZ());
        if (isChunkLoaded) {
            TileEntity tileentity = chunkTileEntityMap.remove(chunkPosition);
            if (tileentity != null) {
                if (tileentity instanceof IMovingTile) {
                    if (!movingWorldTileEntities.contains(tileentity))
                        movingWorldTileEntities.add((IMovingTile) tileentity);
                    ((IMovingTile) tileentity).setParentMovingWorld(null, pos);
                }
                if (tileentity instanceof ITickable && MovingWorldMod.INSTANCE.getNetworkConfig().isTileUpdatable(tileentity.getClass())) {
                    updatableTiles.remove(tileentity);
                }

                tileentity.remove();
            }
        }
    }

    /**
     * Called when this Chunk is loaded by the ChunkProvider
     */
    public void onChunkLoad() {
        isChunkLoaded = true;
        world.addTileEntities(chunkTileEntityMap.values());
    }

    /**
     * Called when this Chunk is unloaded by the ChunkProvider
     */
    public void onChunkUnload() {
        isChunkLoaded = false;
    }

    public void setChunkModified() {
        isModified = true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public int getNeighborAwareLightSubtracted(BlockPos pos, int amount) {
        int lv = EnumLightType.SKY.defaultLightValue;
        return lv << 20 | amount << 4;
    }

    @Override
    public boolean isAirBlock(BlockPos pos) {
        IBlockState state = getBlockState(pos);
        return state == null || state.getMaterial().equals(Material.AIR);
    }

    public boolean isBlockTakingWaterVolume(BlockPos pos) {
        IBlockState blockState = getBlockState(pos);
        return !blockState.getMaterial().equals(Material.AIR);
    }

    public int getHeight() {
        return CHUNK_SIZE;
    }

    @Override
    public IBlockState getBlockState(BlockPos pos) {
        BlockPos internalStoragePos = shiftToInternalStoragePos(pos);
        ChunkSection storage = getBlockStorage(pos);
        IBlockState state = storage != null ?
                storage.get(internalStoragePos.getX(), internalStoragePos.getY(), internalStoragePos.getZ()) : null;
        if (state == null || storage == null)
            return Blocks.AIR.getDefaultState();
        return state;
    }

    @Override
    public Biome getBiome(BlockPos pos) {
        return creationSpotBiome;
    }

    @Override
    public int getStrongPower(BlockPos pos, EnumFacing direction) {
        return 0;
    }

    public final int getMemoryUsage() {
        return 2 + blockCount * 9; // (3 bytes + 2 bytes (short) + 4 bytes (int) = 9 bytes per block) + 2 bytes (short)
    }

    public boolean needsCustomCollision(AxisAlignedBB axisAlignedBB) {

        boolean retVal = false;

        for (AxisAlignedBB bb : boundingBoxes.values()) {
            if (bbContainsBB(axisAlignedBB, bb)) {
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
        setChunkModified();
    }
}
