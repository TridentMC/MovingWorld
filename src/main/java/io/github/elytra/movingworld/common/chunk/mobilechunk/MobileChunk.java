package io.github.elytra.movingworld.common.chunk.mobilechunk;

import com.google.common.collect.HashBiMap;
import io.github.elytra.movingworld.MovingWorldMod;
import io.github.elytra.movingworld.api.IMovingWorldTileEntity;
import io.github.elytra.movingworld.common.chunk.LocatedBlock;
import io.github.elytra.movingworld.common.chunk.mobilechunk.world.FakeWorld;
import io.github.elytra.movingworld.common.entity.EntityMovingWorld;
import io.github.elytra.movingworld.common.util.AABBRotator;
import io.github.elytra.movingworld.common.util.Vec3dMod;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class MobileChunk implements IBlockAccess {
    public static final int CHUNK_SIZE = 16;
    public static final int CHUNK_MEMORY_USING = CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE * (4 + 2);    //(16*16*16 shorts and ints)

    public final World worldObj;
    protected final EntityMovingWorld entityMovingWorld;
    public Map<BlockPos, TileEntity> chunkTileEntityMap;
    public List<TileEntity> updatableTiles;
    public boolean isChunkLoaded;
    public boolean isModified;
    public LocatedBlock marker;
    public ArrayList<IMovingWorldTileEntity> movingWorldTileEntities;
    private Map<BlockPos, ExtendedBlockStorage> blockStorageMap;
    private boolean boundsInit;
    private BlockPos minBounds;
    private BlockPos maxBounds;
    private int blockCount;
    private Biome creationSpotBiome;
    private HashBiMap<BlockPos, AxisAlignedBB> boundingBoxes;
    private HashBiMap<BlockPos, AxisAlignedBB> chunkBoundingBoxes;
    private FakeWorld fakeWorld;

    public MobileChunk(World world, EntityMovingWorld entitymovingWorld) {
        worldObj = world;
        entityMovingWorld = entitymovingWorld;
        blockStorageMap = new HashMap<BlockPos, ExtendedBlockStorage>(1);
        chunkTileEntityMap = new HashMap<BlockPos, TileEntity>(2);
        updatableTiles = new ArrayList<>();
        boundingBoxes = HashBiMap.create();
        chunkBoundingBoxes = HashBiMap.create();
        movingWorldTileEntities = new ArrayList<IMovingWorldTileEntity>();
        marker = null;

        isChunkLoaded = false;
        isModified = false;

        boundsInit = false;
        minBounds = new BlockPos(-1, -1, -1);
        maxBounds = new BlockPos(-1, -1, -1);

        blockCount = 0;

        creationSpotBiome = Biome.getBiome(0); // Ocean biome id.
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
        movingWorldPos = movingWorldPos.subtract(new Double(maxX()) / 2, new Double(maxY()) / 2, new Double(maxZ()) / 2);
        Vec3d returnPos = new Vec3d(pos.getX(), pos.getY(), pos.getZ());
        returnPos.add(movingWorldPos);
        return returnPos;
    }

    public Vec3d getWorldPosForChunkPos(Vec3d vec) {
        Vec3d movingWorldPos = new Vec3d(entityMovingWorld.posX, entityMovingWorld.posY, entityMovingWorld.posZ);
        movingWorldPos = movingWorldPos.subtract(new Double(maxX()) / 2, new Double(maxY()) / 2, new Double(maxZ()) / 2);
        Vec3d returnPos = new Vec3d(vec.xCoord, vec.yCoord, vec.zCoord);
        returnPos.add(movingWorldPos);
        return returnPos;
    }


    public Vec3d getChunkPosForWorldPos(Vec3d pos) {
        Vec3d movingWorldPos = new Vec3d(entityMovingWorld.posX, entityMovingWorld.posY, entityMovingWorld.posZ);
        movingWorldPos = movingWorldPos.subtract(new Double(maxX()) / 2, new Double(maxY()) / 2, new Double(maxZ()) / 2);
        Vec3d returnPos = new Vec3d(pos.xCoord, pos.yCoord, pos.zCoord);
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

        axisAlignedBB = new AxisAlignedBB(minVec.xCoord, minVec.yCoord, minVec.zCoord, maxVec.xCoord, maxVec.yCoord, maxVec.zCoord);

        return axisAlignedBB;
    }

    public ExtendedBlockStorage getBlockStorage(BlockPos pos) {
        ExtendedBlockStorage storage = blockStorageMap.get(new BlockPos(pos.getX() >> 4, pos.getY() >> 4, pos.getZ() >> 4));
        return storage;
    }

    public ExtendedBlockStorage getBlockStorageOrCreate(BlockPos pos) {
        pos = new BlockPos(pos.getX() >> 4, pos.getY() >> 4, pos.getZ() >> 4);
        ExtendedBlockStorage storage = blockStorageMap.get(pos);
        if (storage != null) return storage;
        storage = new ExtendedBlockStorage(pos.getY(), false);
        blockStorageMap.put(pos, storage);
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

    public void setCreationSpotBiomeGen(Biome biomeGen) {
        creationSpotBiome = biomeGen;
    }

    public boolean addBlockWithState(BlockPos pos, IBlockState state) {
        if (state == null) return false;

        Block block = state.getBlock();
        int id = Block.getIdFromBlock(block);
        int meta = block.getMetaFromState(state);

        if (block == null) return false;

        ExtendedBlockStorage storage = getBlockStorageOrCreate(pos);
        int i = pos.getX() & 15;
        int j = pos.getY() & 15;
        int k = pos.getZ() & 15;

        IBlockState currentState = storage.get(i, j, k);
        Block currentBlock = currentState.getBlock();
        int currentID = Block.getIdFromBlock(currentBlock);
        int currentMeta = currentBlock.getMetaFromState(currentState);
        MovingWorldMod.logger.info(String.format("Adding block with state: %s, at position %s in a mobile chunk. \n The block id is: %s, and the metadata is: %s", state, pos, id, meta));
        MovingWorldMod.logger.info(String.format("The current state of the block is %s, the id is %s and the meta is %s", currentState, currentID, currentMeta));
        if (currentID == id && currentMeta == meta) {
            return false;
        }

        storage.set(i, j, k, state);

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
        if (block.hasTileEntity(state)) {
            tileentity = getTileEntity(pos);

            if (tileentity == null) {
                setTileEntity(pos, tileentity);
            } else {
                tileentity.updateContainingBlockInfo();
                tileentity.blockType = block;
                tileentity.blockMetadata = meta;
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

        if (!state.getBlock().isCollidable() ||
                (state.getBlock().isCollidable() && state.getCollisionBoundingBox(this.getFakeWorld(), pos) == null))
            return null;

        AxisAlignedBB axisAlignedBB = this.getBlockState(pos).getCollisionBoundingBox(this.getFakeWorld(), pos);
        chunkBoundingBoxes.put(pos, axisAlignedBB);

        double maxDX = new Double(maxX());
        double maxDY = new Double(maxY());
        double maxDZ = new Double(maxZ());

        maxDX = maxDX / 2 * -1;
        maxDY = maxDY / 2 * -1;
        maxDZ = maxDZ / 2 * -1;

        axisAlignedBB = axisAlignedBB.offset(entityMovingWorld.posX + maxDX - 0.5, entityMovingWorld.posY + maxDY, entityMovingWorld.posZ + maxDZ - 0.5);
        boundingBoxes.put(pos, axisAlignedBB);

        return axisAlignedBB;
    }

    public List<AxisAlignedBB> getBoxes() {
        ArrayList<AxisAlignedBB> boxes = new ArrayList<AxisAlignedBB>();
        boxes.addAll(boundingBoxes.values());

        return boxes;
    }

    /**
     * Based off of the implementation in net.minecraft.world.World
     *
     * @return applicable bounding boxes with applicable position.
     */
    public List getCollidingBoundingBoxes(boolean chunkPos, AxisAlignedBB startBox, AxisAlignedBB endBox) {
        ArrayList<AxisAlignedBB> axisAlignedBBs = new ArrayList<AxisAlignedBB>();

        AxisAlignedBB boxUnion = startBox.union(endBox);

        if (!chunkPos) {
            for (AxisAlignedBB axisAlignedBB : boundingBoxes.values()) {
                if (axisAlignedBB.intersectsWith(boxUnion)) {
                    axisAlignedBBs.add(axisAlignedBB);
                }
            }
        } else {
            for (AxisAlignedBB axisAlignedBB : chunkBoundingBoxes.values()) {
                if (axisAlignedBB.intersectsWith(boxUnion)) {
                    axisAlignedBBs.add(axisAlignedBB);
                }
            }
        }

        return axisAlignedBBs;
    }

    public List getCollidingBoundingBoxes(boolean chunkPos, AxisAlignedBB box) {
        ArrayList<AxisAlignedBB> axisAlignedBBs = new ArrayList<AxisAlignedBB>();

        if (!chunkPos) {
            for (AxisAlignedBB axisAlignedBB : boundingBoxes.values()) {
                if (axisAlignedBB.intersectsWith(box)) {
                    axisAlignedBBs.add(axisAlignedBB);
                }
            }
        } else {
            for (AxisAlignedBB axisAlignedBB : chunkBoundingBoxes.values()) {
                if (axisAlignedBB.intersectsWith(box)) {
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

                double maxDX = new Double(maxX());
                double maxDY = new Double(maxY());
                double maxDZ = new Double(maxZ());

                maxDX = maxDX / 2 * -1;
                maxDY = maxDY / 2 * -1 + 1;
                maxDZ = maxDZ / 2 * -1;


                axisAlignedBB = AABBRotator.rotateAABBAroundY(axisAlignedBB, offset.getX(), offset.getZ(), rotationRadians);
                Vec3dMod vec3 = new Vec3dMod(maxDX, maxDY, maxDZ).rotateAroundY(rotationRadians);
                axisAlignedBB = axisAlignedBB.offset(entityMovingWorld.posX + vec3.xCoord, entityMovingWorld.posY + vec3.yCoord, entityMovingWorld.posZ + vec3.zCoord);

                newBoundingBoxes.put(pos, axisAlignedBB);
            }
        }

        this.boundingBoxes = newBoundingBoxes;
    }

    public boolean setBlockState(BlockPos pos, IBlockState state) {
        ExtendedBlockStorage storage = getBlockStorage(pos);
        if (storage == null) return addBlockWithState(pos, state);

        IBlockState checkState = getBlockState(new BlockPos(pos.getX(), pos.getY() & 15, pos.getZ()));
        if (checkState.getBlock().equals(state.getBlock()) && checkState.getBlock().getMetaFromState(checkState) == state.getBlock().getMetaFromState(state)) {
            return false;
        }
        if(storage.get(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15) == null){
            blockCount++;
        }

        storage.set(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15, state);
        state = storage.get(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15);
        Block block = state.getBlock();

        if (block != null && block.hasTileEntity(state)) {
            TileEntity tileentity = getTileEntity(pos);

            if (tileentity != null) {
                tileentity.updateContainingBlockInfo();
                tileentity.blockMetadata = block.getMetaFromState(state);
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
        ExtendedBlockStorage storage = getBlockStorage(pos);
        if (storage == null) return true;

        IBlockState state = getBlockState(new BlockPos(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15));
        Block block = state.getBlock();
        if (block == Blocks.AIR && block.getMetaFromState(state) == 1) {
            return true;
        }
        if (block == null || state.getMaterial().equals(Material.AIR)) {
            storage.set(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15, Blocks.AIR.getDefaultState());
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

            tileentity = block.createTileEntity(worldObj, blockState);
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
    private void setChunkBlockTileEntity(BlockPos pos, TileEntity tileentity) {
        BlockPos chunkPosition = new BlockPos(pos.getX(), pos.getY(), pos.getZ());
        tileentity.setPos(pos);
        tileentity.setWorldObj(getFakeWorld());

        IBlockState blockState = getBlockState(pos);
        Block block = blockState.getBlock();
        if (block != null && block.hasTileEntity(blockState)) {
            tileentity.blockMetadata = block.getMetaFromState(blockState);
            tileentity.invalidate();
            chunkTileEntityMap.put(chunkPosition, tileentity);

            if (tileentity instanceof IMovingWorldTileEntity) {
                if (!movingWorldTileEntities.contains(tileentity))
                    movingWorldTileEntities.add((IMovingWorldTileEntity) tileentity);
                ((IMovingWorldTileEntity) tileentity).setParentMovingWorld(chunkPosition, entityMovingWorld);
            } else if (tileentity instanceof ITickable && MovingWorldMod.instance.getNetworkConfig().isTileUpdatable(tileentity.getClass())) {
                updatableTiles.add(tileentity);
            }
        }
    }

    /**
     * Adds a TileEntity to a chunk
     */
    public void addTileEntity(TileEntity tileentity) {
        setChunkBlockTileEntity(tileentity.getPos(), tileentity);
    }

    /**
     * Removes the TileEntity for a given block in this chunk
     */
    public void removeChunkBlockTileEntity(BlockPos pos) {
        BlockPos chunkPosition = new BlockPos(pos.getX(), pos.getY(), pos.getZ());
        if (isChunkLoaded) {
            TileEntity tileentity = chunkTileEntityMap.remove(chunkPosition);
            if (tileentity != null) {
                if (tileentity instanceof IMovingWorldTileEntity) {
                    if (!movingWorldTileEntities.contains(tileentity))
                        movingWorldTileEntities.add((IMovingWorldTileEntity) tileentity);
                    ((IMovingWorldTileEntity) tileentity).setParentMovingWorld(pos, null);
                }
                if (tileentity instanceof ITickable && MovingWorldMod.instance.getNetworkConfig().isTileUpdatable(tileentity.getClass())) {
                    updatableTiles.remove(tileentity);
                }

                tileentity.invalidate();
            }
        }
    }

    /**
     * Called when this Chunk is loaded by the ChunkProvider
     */
    public void onChunkLoad() {
        isChunkLoaded = true;
        worldObj.addTileEntities(chunkTileEntityMap.values());
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
    @SideOnly(Side.CLIENT)
    public int getCombinedLight(BlockPos pos, int l) {
        int lv = EnumSkyBlock.SKY.defaultLightValue;
        return lv << 20 | l << 4;
    }

    @Override
    public boolean isAirBlock(BlockPos pos) {
        IBlockState state = getBlockState(pos);
        return state == null || state.getMaterial().equals(Material.AIR);
    }

    public boolean isBlockTakingWaterVolume(BlockPos pos) {
        IBlockState blockState = getBlockState(pos);
        Block block = blockState.getBlock();
        if (block == null || blockState.getMaterial().equals(Material.AIR)) {
            if (block.getMetaFromState(blockState) == 1) return false;
        }
        return true;
    }

    public int getHeight() {
        return CHUNK_SIZE;
    }

    @Override
    public IBlockState getBlockState(BlockPos pos) {
        ExtendedBlockStorage storage = getBlockStorage(pos);
        IBlockState state = storage != null ? storage.get(pos.getX(), pos.getY(), pos.getZ()) : null;
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

    @Override
    public WorldType getWorldType() {
        return worldObj.getWorldType();
    }

    @Override
    public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        if (x < -30000000 || z < -30000000 || x >= 30000000 || z >= 30000000) {
            return _default;
        }

        IBlockState state = getBlockState(pos);

        return state.isSideSolid(this, new BlockPos(x, y, z), side);
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

        if (container.minX < minVec.xCoord || container.minY < minVec.yCoord || container.minZ < minVec.zCoord) {
            return true;
        }
        if (container.maxX > maxVec.xCoord || container.maxY > maxVec.yCoord || container.maxZ > maxVec.zCoord) {
            return true;
        }
        return false;
    }

    public abstract Side side();

    public void markTileDirty(BlockPos pos) {
        setChunkModified();
    }
}
