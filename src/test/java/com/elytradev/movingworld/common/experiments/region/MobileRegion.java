package com.elytradev.movingworld.common.experiments.region;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class MobileRegion {

    // All known regions.
    private static List<MobileRegion> REGIONS = new ArrayList<>();
    public double x, y, z;
    public BlockPos sizeMin, sizeMax;
    public int dimension;
    public ChunkPos regionMin;
    public ChunkPos regionMax;

    private MobileRegion(int dimension, ChunkPos regionMin, ChunkPos regionMax) {
        this.dimension = dimension;
        this.regionMin = regionMin;
        this.regionMax = regionMax;

        this.sizeMin = new BlockPos(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
        this.sizeMax = new BlockPos(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
    }

    private MobileRegion(NBTTagCompound tagCompound) {
        readFromCompound(tagCompound);
    }

    /**
     * Get a region with the given information, find from list if present, if not creates a new region.
     *
     * @param dimension the id of the dimension this region is in
     * @param regionMin the minimum chunk position of the region
     * @param regionMax the maximum chunk position of the region
     * @return a region matching the given arguments
     */
    public static MobileRegion getRegionFor(int dimension, ChunkPos regionMin, ChunkPos regionMax) {
        Optional<MobileRegion> foundRegion = REGIONS.stream().filter(mobileRegion -> mobileRegion.dimension == dimension
                && mobileRegion.regionMin.equals(regionMin) && mobileRegion.regionMax.equals(regionMax)).findFirst();

        if (foundRegion.isPresent()) {
            return foundRegion.get();
        } else {
            MobileRegion region = new MobileRegion(dimension, regionMin, regionMax);
            REGIONS.add(region);

            return region;
        }
    }

    /**
     * Creates a region with information gathered from the given NBTTagCompound if it's not already present,
     * otherwise returns an already existing region.
     *
     * @param tagCompound the serialized information about the region.
     * @return the created or found region.
     */
    public static MobileRegion getRegionFor(NBTTagCompound tagCompound) {
        MobileRegion deserializedRegion = new MobileRegion(tagCompound);

        return getRegionFor(deserializedRegion.dimension, deserializedRegion.regionMin, deserializedRegion.regionMax);
    }

    /**
     * Checks if a position is within the block bounds of the region.
     *
     * @param pos the position to check.
     * @return true if within bounds, false otherwise.
     */
    public boolean isPosWithinBounds(BlockPos pos) {
        BlockPos min = minBlockPos();
        BlockPos max = maxBlockPos();

        boolean withinMin = (pos.getX() >= min.getX()) && (pos.getY() >= min.getY()) && (pos.getZ() >= min.getZ());
        boolean withinMax = (pos.getX() <= max.getX()) && (pos.getY() <= max.getY()) && (pos.getZ() <= max.getZ());

        return withinMin && withinMax;
    }

    /**
     * Checks if the given chunk coordinates are within the region.
     *
     * @param x chunk xPos
     * @param z chunk zPos
     * @return true if the chunk is within the region, false otherwise.
     */
    public boolean isChunkInRegion(int x, int z) {
        return x >= regionMin.x && x <= regionMax.x
                && z >= regionMin.z && z <= regionMax.z;
    }

    /**
     * Get the BlockPos of the minimum position of the chunk
     *
     * @return the minimum BlockPos.
     */
    public BlockPos minBlockPos() {
        return new BlockPos(regionMin.getXStart(), 0, regionMin.getZStart());
    }

    /**
     * Get the BlockPos of the center of the chunk
     *
     * @return the center BlockPos, rounded.
     */
    public BlockPos centeredBlockPos() {
        BlockPos centeredPos;

        BlockPos maxMin = new BlockPos(RegionPool.regionSize << 4, 0, RegionPool.regionSize << 4);
        maxMin = new BlockPos(Math.round(maxMin.getX() / 2), 0, Math.round(maxMin.getZ() / 2));
        centeredPos = maxMin.add(minBlockPos());

        return centeredPos;
    }

    public Vec3d centerPos() {
        Vec3d centeredPos;

        BlockPos size = new BlockPos(RegionPool.regionSize << 4, 0, RegionPool.regionSize << 4);
        centeredPos = new Vec3d(size.getX() / 2, 0, size.getZ() / 2).addVector(minBlockPos().getX(), 0, minBlockPos().getZ());

        return centeredPos;
    }

    /**
     * Get the BlockPos of the maximum position of the chunk
     *
     * @return the maximum BlockPos.
     */
    public BlockPos maxBlockPos() {
        return new BlockPos((regionMax.getXEnd()), 255, (regionMax.getZEnd()));
    }

    /**
     * Writes the min and max post to an NBTTagCompound
     *
     * @return the written compound
     */
    public NBTTagCompound writeToCompound() {
        NBTTagCompound tagCompound = new NBTTagCompound();

        tagCompound.setLong("MinPos", minBlockPos().toLong());
        tagCompound.setLong("MaxPos", maxBlockPos().toLong());
        tagCompound.setLong("SizeMin", sizeMin.toLong());
        tagCompound.setLong("SizeMax", sizeMax.toLong());
        tagCompound.setInteger("DimensionID", dimension);

        return tagCompound;
    }

    /**
     * Reads the given information into the region.
     *
     * @param tagCompound the serialized region.
     */
    public void readFromCompound(NBTTagCompound tagCompound) {
        BlockPos minBlockPos = BlockPos.fromLong(tagCompound.getLong("MinPos"));
        BlockPos maxBlockPos = BlockPos.fromLong(tagCompound.getLong("MaxPos"));

        sizeMin = BlockPos.fromLong(tagCompound.getLong("SizeMin"));
        sizeMax = BlockPos.fromLong(tagCompound.getLong("SizeMax"));

        maxBlockPos.subtract(new Vec3i(15, 0, 15));

        regionMin = new ChunkPos(minBlockPos.getX() / 16, minBlockPos.getZ() / 16);
        regionMax = new ChunkPos(maxBlockPos.getX() / 16, maxBlockPos.getZ() / 16);

        dimension = tagCompound.getInteger("DimensionID");
    }

    /**
     * Converts a given Vec3d's position to the real position in the parentWorld world.
     *
     * @param regionPos
     * @return
     */
    public Vec3d convertRegionPosToRealWorld(Vec3d regionPos) {
        Vec3d adjustedPosition = new Vec3d(regionPos.x, regionPos.y, regionPos.z);
        adjustedPosition = adjustedPosition.subtract(centerPos());
        adjustedPosition = adjustedPosition.add(new Vec3d(x, y, z));
        return adjustedPosition;
    }

    /**
     * Converts a given Vec3d's position from the real world into a position for internal region use.
     *
     * @param realWorldPos
     * @return
     */
    public Vec3d convertRealWorldPosToRegion(Vec3d realWorldPos) {
        Vec3d adjustedPosition = new Vec3d(realWorldPos.x + centerPos().x - x,
                realWorldPos.y - y,
                realWorldPos.z + centerPos().z - z);

        return adjustedPosition;
    }

    /**
     * Converts a given block position from internal positioning of the region to the equivalent position in the real world.
     *
     * @param pos the internal position to convert.
     * @return the position in the real world.
     */
    public BlockPos convertRegionPosToRealWorld(BlockPos pos) {
        Vec3d vec3DPos = new Vec3d(pos.getX(), pos.getY(), pos.getZ());
        vec3DPos = convertRegionPosToRealWorld(vec3DPos);
        return new BlockPos(Math.round(vec3DPos.x), Math.round(vec3DPos.y), Math.round(vec3DPos.z));
    }

    /**
     * Converts a given block position from the real world to the equivalent position in this region.
     *
     * @param pos the real world position to convert.
     * @return the position in this region.
     */
    public BlockPos convertRealWorldPosToRegion(BlockPos pos) {
        Vec3d vec3DPos = new Vec3d(pos.getX(), pos.getY(), pos.getZ());
        vec3DPos = convertRealWorldPosToRegion(vec3DPos);
        return new BlockPos(Math.round(vec3DPos.x), Math.round(vec3DPos.y), Math.round(vec3DPos.z));
    }

    /**
     * Converts a given bb from internal positioning of the region to the equivalent bb in the real world.
     *
     * @param regionBB the internal bb to convert.
     * @return the bb in the real world.
     */
    public AxisAlignedBB convertRegionBBToRealWorld(AxisAlignedBB regionBB) {
        Vec3d min = convertRegionPosToRealWorld(new Vec3d(regionBB.minX, regionBB.minY, regionBB.minZ));
        Vec3d max = convertRegionPosToRealWorld(new Vec3d(regionBB.maxX, regionBB.maxY, regionBB.maxZ));

        return new AxisAlignedBB(min.x, min.y, min.z, max.x, max.y, max.z);
    }

    public AxisAlignedBB size() {
        return new AxisAlignedBB(sizeMin.getX(), sizeMin.getY(), sizeMin.getZ(), sizeMax.getX(), sizeMax.getY(), sizeMax.getZ());
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z, dimension, regionMin, regionMax);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MobileRegion region = (MobileRegion) o;
        return Double.compare(region.x, x) == 0 &&
                Double.compare(region.y, y) == 0 &&
                Double.compare(region.z, z) == 0 &&
                dimension == region.dimension &&
                Objects.equals(regionMin, region.regionMin) &&
                Objects.equals(regionMax, region.regionMax);
    }
}
