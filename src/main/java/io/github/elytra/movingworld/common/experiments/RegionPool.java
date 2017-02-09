package io.github.elytra.movingworld.common.experiments;

import net.minecraft.util.math.ChunkPos;

import java.util.HashMap;

/**
 * Used for getting a region within a world to be used as a sub world. Prevents the need for
 * multiple loaded worlds, reducing overall load.
 */
public class RegionPool {

    // Pools sorted by dimension id.
    public static final HashMap<Integer, RegionPool> POOLS = new HashMap<>();

    public final HashMap<ChunkPos, MobileRegion> regions = new HashMap<>();

    // +- 29999984 is the max and min coords for a world according to the Minecraft wiki.
    public final int startX = -(29999984 / 16);
    public final int startZ = -(29999984 / 16);
    public final int maxX = +(29999984 / 16);
    public final int maxZ = +(29999984 / 16);

    public final int regionSize = 8;
    public final int regionBuffer = 8;

    private int regionCursorX = startX;
    private int regionCursorZ = startZ;

    public MobileRegion nextRegion() {
        ChunkPos regionMin = new ChunkPos(regionCursorX, regionCursorZ);
        ChunkPos regionMax = new ChunkPos(regionCursorX + regionSize, regionCursorZ + regionSize);
        MobileRegion mobileRegion = new MobileRegion(regionMin, regionMax);
        regionCursorX += regionSize + regionBuffer;
        if (regionCursorX >= maxX) {
            regionCursorX = startX;
            regionCursorZ += regionSize + regionBuffer;
        }

        if (regionCursorZ >= maxZ) {
            throw new RegionOverflowException();
        }
        return mobileRegion;
    }

}
