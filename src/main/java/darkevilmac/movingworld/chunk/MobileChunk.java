package darkevilmac.movingworld.chunk;

import darkevilmac.movingworld.entity.EntityMovingWorld;
import darkevilmac.movingworld.tile.IMovingWorldTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.Map;

public class MobileChunk implements IBlockAccess {
    public static final int CHUNK_SIZE = 16;
    public static final int CHUNK_SIZE_EXP = 4;
    public static final int CHUNK_MEMORY_USING = CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE * (4 + 2);    //(16*16*16 shorts and ints)

    public final World worldObj;
    protected final EntityMovingWorld entityMovingWorld;
    public Map<BlockPos, TileEntity> chunkTileEntityMap;
    public boolean isChunkLoaded;
    public boolean isModified;
    private Map<BlockPos, ExtendedBlockStorage> blockStorageMap;
    private boolean boundsInit;
    private int minX, minY, minZ, maxX, maxY, maxZ;
    private int blockCount;
    private BiomeGenBase creationSpotBiome;

    public MobileChunk(World world, EntityMovingWorld entitymovingWorld) {
        worldObj = world;
        entityMovingWorld = entitymovingWorld;
        blockStorageMap = new HashMap<BlockPos, ExtendedBlockStorage>(1);
        chunkTileEntityMap = new HashMap<BlockPos, TileEntity>(2);

        isChunkLoaded = false;
        isModified = false;

        boundsInit = false;
        minX = minY = minZ = maxX = maxY = maxZ = -1;
        blockCount = 0;

        creationSpotBiome = BiomeGenBase.ocean;
    }

    public ExtendedBlockStorage getBlockStorage(BlockPos pos) {
        BlockPos bPos = new BlockPos(pos.getX() >> CHUNK_SIZE_EXP, pos.getY() >> CHUNK_SIZE_EXP, pos.getZ() >> CHUNK_SIZE_EXP);
        return blockStorageMap.get(bPos);
    }

    public ExtendedBlockStorage getBlockStorageOrCreate(BlockPos pos) {
        BlockPos bPos = new BlockPos(pos.getX() >> CHUNK_SIZE_EXP, pos.getY() >> CHUNK_SIZE_EXP, pos.getZ() >> CHUNK_SIZE_EXP);
        ExtendedBlockStorage storage = blockStorageMap.get(bPos);
        if (storage != null) return storage;
        storage = new ExtendedBlockStorage(bPos.getY(), false);
        blockStorageMap.put(bPos, storage);
        return storage;
    }

    public int getBlockCount() {
        return blockCount;
    }

    public float getCenterX() {
        return (minX + maxX) / 2F;
    }

    public float getCenterY() {
        return (minY + maxY) / 2F;
    }

    public float getCenterZ() {
        return (minZ + maxZ) / 2F;
    }

    public int minX() {
        return minX;
    }

    public int maxX() {
        return maxX;
    }

    public int minY() {
        return minY;
    }

    public int maxY() {
        return maxY;
    }

    public int minZ() {
        return minZ;
    }

    public int maxZ() {
        return maxZ;
    }

    public void setCreationSpotBiomeGen(BiomeGenBase biomegenbase) {
        creationSpotBiome = biomegenbase;
    }

    public boolean setBlockIDWithState(BlockPos pos, IBlockState state) {
        if (state == null) return false;
        Block block = state.getBlock();
        if (block == null) return false;

        ExtendedBlockStorage storage = getBlockStorageOrCreate(pos);
        int i = pos.getX() & 15;
        int j = pos.getY() & 15;
        int k = pos.getZ() & 15;

        IBlockState blockState = storage.get(i, j, k);
        Block currentBlock = blockState.getBlock();
        int currentMeta = storage.getExtBlockMetadata(i, j, k);
        if (currentBlock == block && currentMeta == currentBlock.getMetaFromState(state)) {
            return false;
        }

        // storage.func_150818_a(i, j, k, block); Method gone, use set();
        //storage.setExtBlockMetadata(i, j, k, meta); Method gone, use set();
        storage.set(i, j, k, blockState);

        if (boundsInit) {
            minX = Math.min(minX, pos.getX());
            minY = Math.min(minY, pos.getY());
            minZ = Math.min(minZ, pos.getZ());
            maxX = Math.max(maxX, pos.getX() + 1);
            maxY = Math.max(maxY, pos.getY() + 1);
            maxZ = Math.max(maxZ, pos.getZ() + 1);
        } else {
            boundsInit = true;
            minX = pos.getX();
            minY = pos.getY();
            minZ = pos.getZ();
            maxX = pos.getX() + 1;
            maxY = pos.getY() + 1;
            maxZ = pos.getZ() + 1;
        }
        blockCount++;
        setChunkModified();

        TileEntity tileEntity;
        if (block.hasTileEntity(state)) {
            tileEntity = getTileEntity(pos);

            if (tileEntity == null) {
                setTileEntity(pos, tileEntity);
            }

            if (tileEntity != null) {
                tileEntity.updateContainingBlockInfo();
                tileEntity.blockType = block;
                tileEntity.blockMetadata = block.getMetaFromState(state);
            }
        }

        return true;
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
            // storage.func_150818_a(i, j, k, block); Method gone, use set();
            //storage.setExtBlockMetadata(i, j, k, meta); Method gone, use set();
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

    @Override
    public IBlockState getBlockState(BlockPos pos) {
        ExtendedBlockStorage storage = getBlockStorage(pos);
        if (storage == null) return Blocks.air.getDefaultState();
        return storage.get(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15);
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
