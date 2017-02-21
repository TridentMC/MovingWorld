package com.elytradev.movingworld.common.experiments;

import com.elytradev.movingworld.common.experiments.network.messages.server.MessageRegionData;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.ChunkPos;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Used for getting a region within a world to be used as a sub world. Prevents the need for
 * multiple loaded worlds, reducing overall load.
 */
public class RegionPool {

    // +- 29999984 is the max and min coords for a world according to the Minecraft wiki.
    public static final int startX = -(29999984 >> 4);
    public static final int startZ = -(29999984 >> 4);
    public static final int maxX = +(29999984 >> 4);
    public static final int maxZ = +(29999984 >> 4);
    public static final int regionSize = 8;
    public static final int regionBuffer = 8;

    // Pools sorted by dimension id.
    private static final HashMap<Integer, RegionPool> POOLS = new HashMap<>();

    public final HashMap<ChunkPos, MobileRegion> regions = new HashMap<>();
    public int dimension;
    private int regionCursorX = startX;
    private int regionCursorZ = startZ;

    private RegionPool(int dimension) {
        this.dimension = dimension;
    }

    private RegionPool() {
    }

    /**
     * Gets a region pool corresponding to a dimension id, creates one if needed.
     *
     * @param dimension
     * @return the found or created pool
     */
    public static RegionPool getPool(int dimension, boolean generate) {
        if (POOLS.containsKey(dimension)) {
            return POOLS.get(dimension);
        }

        if (generate) {
            POOLS.put(dimension, new RegionPool(dimension));
        } else {
            return null;
        }

        return POOLS.get(dimension);
    }


    /**
     * Writes all region pools to an NBTTagCompound
     *
     * @return
     */
    public static NBTTagCompound writeAllToCompound() {
        NBTTagCompound tagCompound = new NBTTagCompound();
        tagCompound.setIntArray("PoolIDS", POOLS.keySet().stream().mapToInt(Integer::intValue).toArray());
        POOLS.forEach((integer, regionPool) -> tagCompound.setTag("Pool#" + integer.intValue(), regionPool.writePoolToCompound()));

        return tagCompound;
    }

    /**
     * Reads all region pools from a previously generated NBTTagCompound
     *
     * @param tagCompound
     */
    public static void readAllFromCompound(NBTTagCompound tagCompound) {
        int[] poolIDS = tagCompound.getIntArray("PoolIDS");

        for (int i : poolIDS) {
            NBTTagCompound poolComound = tagCompound.getCompoundTag("Pool#" + i);
            RegionPool pool = new RegionPool();
            pool.readPoolFromCompound(poolComound);
            POOLS.put(i, pool);
        }
    }

    /**
     * Gets the next available MobileRegion in this pool and adjusts the cursor.
     *
     * @param simulate if true, don't change the result for the next call
     * @return the next available region
     */
    public MobileRegion nextRegion(boolean simulate) {
        ChunkPos regionMin = new ChunkPos(regionCursorX, regionCursorZ);
        ChunkPos regionMax = new ChunkPos(regionCursorX + regionSize, regionCursorZ + regionSize);
        MobileRegion mobileRegion = MobileRegion.getRegionFor(dimension, regionMin, regionMax);
        if (!simulate)
            regionCursorX += regionSize + regionBuffer;
        if (regionCursorX >= maxX) {
            regionCursorX = startX;
            if (!simulate)
                regionCursorZ += regionSize + regionBuffer;
        }

        if (regionCursorZ >= maxZ) {
            throw new RegionOverflowException();
        }
        if (!simulate) {
            for (int chunkX = regionMin.chunkXPos; chunkX <= regionMax.chunkXPos; chunkX++) {
                for (int chunkZ = regionMin.chunkZPos; chunkZ <= regionMax.chunkZPos; chunkZ++) {
                    regions.put(new ChunkPos(chunkX, chunkZ), mobileRegion);
                }
            }

            // Send new pool data to clients.
            new MessageRegionData(dimension, mobileRegion.writeToCompound()).sendToEveryone();
        }

        return mobileRegion;
    }

    /**
     * Adds a region to the pool if there's no regions matching it already present.
     *
     * @param addRegion the region to add.
     * @return the region that is already present if found, or the region added.
     */
    public MobileRegion addRegionIfNotPresent(MobileRegion addRegion) {
        MobileRegion region = addRegion;

        if (!regions.values().stream().anyMatch(testRegion -> testRegion.equals(addRegion))) {
            for (int chunkX = addRegion.regionMin.chunkXPos; chunkX <= addRegion.regionMax.chunkXPos; chunkX++) {
                for (int chunkZ = addRegion.regionMin.chunkZPos; chunkZ <= addRegion.regionMax.chunkZPos; chunkZ++) {
                    regions.put(new ChunkPos(chunkX, chunkZ), addRegion);
                }
            }

            return addRegion;
        }

        return region;
    }

    /**
     * Writes this pool to an NBTTagCompound
     *
     * @return the NBTTagCompound for this pool
     */
    public NBTTagCompound writePoolToCompound() {
        NBTTagCompound tagCompound = new NBTTagCompound();

        List<NBTTagCompound> regionCompounds = regions.entrySet().stream().map(chunkPosMobileRegionEntry -> {
            NBTTagCompound entryCompound = new NBTTagCompound();

            entryCompound.setInteger("ChunkX", chunkPosMobileRegionEntry.getKey().chunkXPos);
            entryCompound.setInteger("ChunkZ", chunkPosMobileRegionEntry.getKey().chunkZPos);
            entryCompound.setTag("MobileRegion", chunkPosMobileRegionEntry.getValue().writeToCompound());

            return entryCompound;
        }).collect(Collectors.toList());

        tagCompound.setInteger("RegionCount", regions.size());

        for (int i = 0; i < regionCompounds.size(); i++) {
            tagCompound.setTag("RegionEntry" + i, regionCompounds.get(i));
        }

        tagCompound.setInteger("DimensionID", dimension);
        tagCompound.setInteger("CursorX", regionCursorX);
        tagCompound.setInteger("CursorZ", regionCursorZ);
        return tagCompound;
    }

    /**
     * Reads the information provided into this pool.
     *
     * @param tagCompound
     */
    public void readPoolFromCompound(NBTTagCompound tagCompound) {
        dimension = tagCompound.getInteger("DimensionID");
        int regionCount = tagCompound.getInteger("RegionCount");

        for (int i = 0; i < regionCount; i++) {
            NBTTagCompound entryCompound = tagCompound.getCompoundTag("RegionEntry" + i);

            int chunkX = entryCompound.getInteger("ChunkX");
            int chunkZ = entryCompound.getInteger("ChunkZ");

            regions.put(new ChunkPos(chunkX, chunkZ), MobileRegion.getRegionFor(entryCompound.getCompoundTag("MobileRegion")));
        }

        regionCursorX = tagCompound.getInteger("CursorX");
        regionCursorZ = tagCompound.getInteger("CursorZ");
    }

}
