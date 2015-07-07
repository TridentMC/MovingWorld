package darkevilmac.movingworld.common.chunk.mobilechunk;

import com.google.common.collect.HashBiMap;
import darkevilmac.movingworld.common.chunk.LocatedBlock;
import darkevilmac.movingworld.common.chunk.mobilechunk.world.FakeWorld;
import darkevilmac.movingworld.common.entity.EntityMovingWorld;
import darkevilmac.movingworld.common.tile.IMovingWorldTileEntity;
import darkevilmac.movingworld.common.util.Vec3Mod;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MobileChunk implements IBlockAccess {
    public static final int CHUNK_SIZE = 16;
    public static final int CHUNK_MEMORY_USING = CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE * (4 + 2);    //(16*16*16 shorts and ints)

    public final World worldObj;
    protected final EntityMovingWorld entityMovingWorld;
    public Map<BlockPos, TileEntity> chunkTileEntityMap;
    public boolean isChunkLoaded;
    public boolean isModified;
    private Map<BlockPos, ExtendedBlockStorage> blockStorageMap;
    private boolean boundsInit;
    private BlockPos minBounds;
    private BlockPos maxBounds;
    private int blockCount;
    private BiomeGenBase creationSpotBiome;
    public LocatedBlock marker;

    private HashBiMap<BlockPos, AxisAlignedBB> boundingBoxes;

    private HashBiMap<BlockPos, AxisAlignedBB> chunkBoundingBoxes;

    public MobileChunk(World world, EntityMovingWorld entitymovingWorld) {
        worldObj = world;
        entityMovingWorld = entitymovingWorld;
        blockStorageMap = new HashMap<BlockPos, ExtendedBlockStorage>(1);
        chunkTileEntityMap = new HashMap<BlockPos, TileEntity>(2);
        boundingBoxes = HashBiMap.create();
        chunkBoundingBoxes = HashBiMap.create();
        marker = null;

        isChunkLoaded = false;
        isModified = false;

        boundsInit = false;
        minBounds = new BlockPos(-1, -1, -1);
        maxBounds = new BlockPos(-1, -1, -1);

        blockCount = 0;

        creationSpotBiome = BiomeGenBase.ocean;
    }

    public FakeWorld getFakeWorld() {
        return FakeWorld.getFakeWorld(this);
    }

    public EntityMovingWorld getEntityMovingWorld() {
        return entityMovingWorld;
    }

    public BlockPos getBlockPosFromBounds(AxisAlignedBB bb) {
        return boundingBoxes.inverse().get(bb);
    }

    public Vec3 getWorldPosForChunkPos(BlockPos pos) {
        Vec3 movingWorldPos = new Vec3(entityMovingWorld.posX, entityMovingWorld.posY, entityMovingWorld.posZ);
        movingWorldPos = movingWorldPos.subtract(new Double(maxX()) / 2, new Double(maxY()) / 2, new Double(maxZ()) / 2);
        Vec3 returnPos = new Vec3(pos.getX(), pos.getY(), pos.getZ());
        returnPos.add(movingWorldPos);
        return returnPos;
    }

    public Vec3 getWorldPosForChunkPos(Vec3 vec) {
        Vec3 movingWorldPos = new Vec3(entityMovingWorld.posX, entityMovingWorld.posY, entityMovingWorld.posZ);
        movingWorldPos = movingWorldPos.subtract(new Double(maxX()) / 2, new Double(maxY()) / 2, new Double(maxZ()) / 2);
        Vec3 returnPos = new Vec3(vec.xCoord, vec.yCoord, vec.zCoord);
        returnPos.add(movingWorldPos);
        return returnPos;
    }


    public Vec3 getChunkPosForWorldPos(Vec3 pos) {
        Vec3 movingWorldPos = new Vec3(entityMovingWorld.posX, entityMovingWorld.posY, entityMovingWorld.posZ);
        movingWorldPos = movingWorldPos.subtract(new Double(maxX()) / 2, new Double(maxY()) / 2, new Double(maxZ()) / 2);
        Vec3 returnPos = new Vec3(pos.xCoord, pos.yCoord, pos.zCoord);
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

        Vec3 minVec = new Vec3(minX, minY, minZ);
        Vec3 maxVec = new Vec3(maxX, maxY, maxZ);
        minVec = getChunkPosForWorldPos(minVec);
        maxVec = getChunkPosForWorldPos(maxVec);

        axisAlignedBB = new AxisAlignedBB(minVec.xCoord, minVec.yCoord, minVec.zCoord, maxVec.xCoord, maxVec.yCoord, maxVec.zCoord);

        return axisAlignedBB;
    }

    public ExtendedBlockStorage getBlockStorage(BlockPos pos) {
        ExtendedBlockStorage storage = blockStorageMap.get(pos);
        return storage;
    }

    public ExtendedBlockStorage getBlockStorageOrCreate(BlockPos pos) {
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

    public void setCreationSpotBiomeGen(BiomeGenBase biomegenbase) {
        creationSpotBiome = biomegenbase;
    }

    public boolean addBlockWithState(BlockPos pos, IBlockState state) {
        if (state == null) return false;

        Block block = state.getBlock();
        int meta = block.getMetaFromState(state);

        if (block == null) return false;

        ExtendedBlockStorage storage = getBlockStorageOrCreate(pos);
        int i = pos.getX() & 15;
        int j = pos.getY() & 15;
        int k = pos.getZ() & 15;

        IBlockState currentState = storage.get(i, j, k);
        Block currentBlock = storage.getBlockByExtId(i, j, k);
        int currentMeta = storage.getExtBlockMetadata(i, j, k);
        if (currentBlock == block && currentMeta == meta) {
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
            }

            if (tileentity != null) {
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
        if (this.getBlock(pos) == null || (this.getBlock(pos) != null && this.getBlock(pos).getMaterial() == Material.air)) {
            return null;
        }

        AxisAlignedBB axisAlignedBB = this.getBlockState(pos).getBlock().getCollisionBoundingBox(this.getFakeWorld(), pos, getBlockState(pos));
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
     *
     * @param rotationYaw
     */
    public void updateBlockBounds(float rotationYaw) {
        for (AxisAlignedBB bb : chunkBoundingBoxes.values()) {
            if (bb != null) {
                BlockPos pos = chunkBoundingBoxes.inverse().get(bb);

                double maxDX = new Double(maxX());
                double maxDZ = new Double(maxZ());

                maxDX = maxDX / 2;
                maxDZ = maxDZ / 2;

                float yaw = (float) Math.toRadians(rotationYaw);

                Vec3Mod vec = new Vec3Mod(pos.getX() - maxDX, pos.getY() - minY(), pos.getZ() - maxDZ);
                vec = vec.rotateAroundY(yaw);

                bb = bb.offset(entityMovingWorld.posX + vec.xCoord, entityMovingWorld.posY + vec.yCoord, entityMovingWorld.posZ + vec.zCoord);

                boundingBoxes.put(pos, bb);
            }
        }
    }

    public boolean setBlockState(BlockPos pos, IBlockState state) {
        ExtendedBlockStorage storage = getBlockStorage(pos);
        if (storage == null) return false;

        int currentMeta = storage.getExtBlockMetadata(pos.getX(), pos.getY() & 15, pos.getZ());
        if (currentMeta == state.getBlock().getMetaFromState(state)) {
            return false;
        }

        setChunkModified();
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

        return true;
    }

    public boolean setBlockAsFilledAir(BlockPos pos) {
        ExtendedBlockStorage storage = getBlockStorage(pos);
        if (storage == null) return true;

        Block block = storage.getBlockByExtId(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15);
        int meta = storage.getExtBlockMetadata(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15);
        if (block == Blocks.air && meta == 1) {
            return true;
        }
        if (block == null || block.isAir(worldObj, pos)) {
            storage.set(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15, Blocks.air.getDefaultState());
            onSetBlockAsFilledAir(pos);
            return true;
        }
        return false;
    }

    protected void onSetBlockAsFilledAir(BlockPos pos) {
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
            return;
        }

        setChunkBlockTileEntity(pos, tileentity);
    }

    /**
     * Sets the TileEntity for a given block in this chunk
     */
    private void setChunkBlockTileEntity(BlockPos pos, TileEntity tileentity) {
        BlockPos chunkPosition = new BlockPos(pos.getX(), pos.getY(), pos.getZ());
        tileentity.setWorldObj(worldObj);
        BlockPos oPos = tileentity.getPos();
        tileentity.setPos(pos);

        IBlockState blockState = getBlockState(pos);
        Block block = blockState.getBlock();
        if (block != null && block.hasTileEntity(blockState)) {
            tileentity.blockMetadata = block.getMetaFromState(blockState);
            tileentity.invalidate();
            chunkTileEntityMap.put(chunkPosition, tileentity);

            if (tileentity instanceof IMovingWorldTileEntity) {
                ((IMovingWorldTileEntity) tileentity).setParentMovingWorld(oPos, entityMovingWorld);
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
                    ((IMovingWorldTileEntity) tileentity).setParentMovingWorld(pos, null);
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
        Block block = getBlockState(pos).getBlock();
        return block == null || block.isAir(worldObj, pos) || block.getMaterial() == Material.air;
    }

    public boolean isBlockTakingWaterVolume(BlockPos pos) {
        IBlockState blockState = getBlockState(pos);
        Block block = blockState.getBlock();
        if (block == null || block.isAir(worldObj, pos)) {
            if (block.getMetaFromState(blockState) == 1) return false;
        }
        return true;
    }

    public int getHeight() {
        return CHUNK_SIZE;
    }

    public Block getBlock(BlockPos pos) {
        ExtendedBlockStorage storage = getBlockStorage(pos);
        if (storage == null) return Blocks.air;
        return storage.getBlockByExtId(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15);
    }

    protected int getBlockMetadata(BlockPos pos) {
        ExtendedBlockStorage storage = getBlockStorage(pos);
        if (storage == null) return 0;
        return storage.getExtBlockMetadata(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15);
    }

    @Override
    public IBlockState getBlockState(BlockPos pos) {
        int meta = getBlockMetadata(pos);
        Block block = getBlock(pos);

        return block.getDefaultState().getBlock().getStateFromMeta(meta);
    }

    @Override
    public BiomeGenBase getBiomeGenForCoords(BlockPos pos) {
        return creationSpotBiome;
    }

    @Override
    public boolean extendedLevelsInChunkCache() {
        return false;
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

        Block block = getBlockState(pos).getBlock();
        return block.isSideSolid(this, new BlockPos(x, y, z), side);
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
        Vec3 minVec = new Vec3(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ);
        //Vec3 midVec = new Vec3((axisAlignedBB.maxX - axisAlignedBB.minX) / 2, (axisAlignedBB.maxY - axisAlignedBB.minY) / 2, (axisAlignedBB.maxZ - axisAlignedBB.minZ) / 2);
        //midVec = midVec.add(minVec);
        Vec3 maxVec = new Vec3(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ);

        if (container.minX < minVec.xCoord || container.minY < minVec.yCoord || container.minZ < minVec.zCoord) {
            return true;
        }
        if (container.maxX > maxVec.xCoord || container.maxY > maxVec.yCoord || container.maxZ > maxVec.zCoord) {
            return true;
        }
        return false;
    }
}
