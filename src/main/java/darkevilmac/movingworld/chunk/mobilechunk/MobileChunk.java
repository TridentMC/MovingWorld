package darkevilmac.movingworld.chunk.mobilechunk;

import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import darkevilmac.movingworld.chunk.mobilechunk.world.FakeWorld;
import darkevilmac.movingworld.entity.EntityMovingWorld;
import darkevilmac.movingworld.tile.IMovingWorldTileEntity;
import darkevilmac.movingworld.util.AABBRotator;
import darkevilmac.movingworld.util.MathHelperMod;
import darkevilmac.movingworld.util.Vec3Mod;
import net.minecraft.block.Block;
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

import java.util.*;

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

    private HashBiMap<BlockPos, AxisAlignedBB> boundingBoxes;

    public MobileChunk(World world, EntityMovingWorld entitymovingWorld) {
        worldObj = world;
        entityMovingWorld = entitymovingWorld;
        blockStorageMap = new HashMap<BlockPos, ExtendedBlockStorage>(1);
        chunkTileEntityMap = new HashMap<BlockPos, TileEntity>(2);
        boundingBoxes = HashBiMap.create();

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

    public AxisAlignedBB calculateBlockBounds(BlockPos pos) {
        AxisAlignedBB axisAlignedBB = this.getBlockState(pos).getBlock().getCollisionBoundingBox(this.getFakeWorld(), pos, getBlockState(pos));
        axisAlignedBB = axisAlignedBB.addCoord(entityMovingWorld.posX, entityMovingWorld.posY, entityMovingWorld.posZ);

        boundingBoxes.put(pos, axisAlignedBB);

        return axisAlignedBB;
    }

    public List<AxisAlignedBB> getBoxes() {
        return Arrays.asList((AxisAlignedBB[]) boundingBoxes.values().toArray());
    }

    /**
     * Based off of the implementation in net.minecraft.world.World
     *
     * @param entityIn
     * @param entityBB bounding box with in world coordinates.
     * @return applicable bounding boxes with world position.
     */
    public List getCollidingBoundingBoxes(AxisAlignedBB entityBB) {
        ArrayList arraylist = Lists.newArrayList();
        for (AxisAlignedBB axisAlignedBB : boundingBoxes.values()) {
            if (axisAlignedBB.intersectsWith(entityBB)) arraylist.add(axisAlignedBB);
        }

        return arraylist;
    }

    /**
     * This is terrible code but it's the best I can do for now.
     *
     * @param worldPos
     * @return
     */
    public BlockPos getBlockFromWorldPos(BlockPos worldPos) {
        BlockPos chunkPos = worldPos;

        float yaw = Math.round(entityMovingWorld.rotationYaw / 90F) * 90F;
        yaw = (float) Math.toRadians(yaw);

        float ox = -getCenterX();
        float oy = -minY();
        float oz = -getCenterZ();

        Vec3Mod vec;
        BlockPos pos;
        for (int i = minX(); i < maxX(); i++) {
            for (int j = minY(); j < maxY(); j++) {
                for (int k = minZ(); k < maxZ(); k++) {
                    if (isAirBlock(new BlockPos(i, j, k))) continue;
                    Vec3Mod vecB = new Vec3Mod(i + ox, j + oy, k + oz);

                    vec = vecB;
                    vec = vec.rotateAroundY(yaw);

                    pos = new BlockPos(MathHelperMod.round_double(vec.xCoord + entityMovingWorld.posX),
                            MathHelperMod.round_double(vec.yCoord + entityMovingWorld.posY),
                            MathHelperMod.round_double(vec.zCoord + entityMovingWorld.posZ));

                    if (pos.equals(worldPos)) {
                        chunkPos = new BlockPos(MathHelperMod.round_double(vec.xCoord),
                                MathHelperMod.round_double(vec.yCoord),
                                MathHelperMod.round_double(vec.zCoord));
                    }
                }
            }
        }

        return chunkPos;
    }

    public void offsetBlockBounds(Vec3 movingWorldPos, float rotationYaw) {
        for (int i = 0; i < boundingBoxes.size(); i++) {
            BlockPos pos = (BlockPos) boundingBoxes.keySet().toArray()[i];
            IBlockState blockState = getBlockState(pos);
            Block block = blockState.getBlock();
            AxisAlignedBB blockAxisAlignedBB = block.getCollisionBoundingBox(this.getFakeWorld(), pos, getBlockState(pos));
            AxisAlignedBB axisAlignedBB;

            axisAlignedBB = AABBRotator.rotateAABBAroundY(blockAxisAlignedBB, movingWorldPos.xCoord, movingWorldPos.yCoord, (float) Math.toRadians(rotationYaw));

            double minX = axisAlignedBB.minX + movingWorldPos.xCoord;
            double minY = axisAlignedBB.minY + movingWorldPos.yCoord;
            double minZ = axisAlignedBB.minZ + movingWorldPos.zCoord;

            double maxX = axisAlignedBB.maxX + movingWorldPos.xCoord;
            double maxY = axisAlignedBB.maxY + movingWorldPos.yCoord;
            double maxZ = axisAlignedBB.maxZ + movingWorldPos.zCoord;

            axisAlignedBB = new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);

            boundingBoxes.put(pos, axisAlignedBB);
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
        return block == null || block.isAir(worldObj, pos);
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

    protected Block getBlock(BlockPos pos) {
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

}
