package com.elytradev.movingworld.common.experiments;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MobileRegion {

    private static List<MobileRegion> REGIONS = new ArrayList<>();
    public double x, y, z;
    public int dimension;
    public ChunkPos regionMin;
    public ChunkPos regionMax;

    private MobileRegion(int dimension, ChunkPos regionMin, ChunkPos regionMax) {
        this.dimension = dimension;
        this.regionMin = regionMin;
        this.regionMax = regionMax;
    }

    private MobileRegion(NBTTagCompound tagCompound) {
        readFromCompound(tagCompound);
    }

    /**
     * Get a region with the given information, find from list if present, if not create a new region
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

    public static MobileRegion getRegionFor(NBTTagCompound tagCompound) {
        MobileRegion deserializedRegion = new MobileRegion(tagCompound);

        return getRegionFor(deserializedRegion.dimension, deserializedRegion.regionMin, deserializedRegion.regionMax);
    }

    public boolean isPosWithinBounds(BlockPos pos) {
        BlockPos min = minBlockPos();
        BlockPos max = maxBlockPos();

        boolean withinMin = (pos.getX() >= min.getX()) && (pos.getY() >= min.getY()) && (pos.getZ() >= min.getZ());
        boolean withinMax = (pos.getX() <= max.getX()) && (pos.getY() <= max.getY()) && (pos.getZ() <= max.getZ());

        return withinMin && withinMax;
    }

    public boolean isChunkInRegion(int x, int z) {
        return x >= regionMin.chunkXPos && x <= regionMax.chunkXPos
                && z >= regionMin.chunkZPos && z <= regionMax.chunkZPos;
    }

    public BlockPos minBlockPos() {
        return new BlockPos(regionMin.getXStart(), 0, regionMin.getZStart());
    }

    public BlockPos maxBlockPos() {
        return new BlockPos(regionMax.getXEnd(), 256 - 1, regionMax.getZEnd());
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
        tagCompound.setInteger("DimensionID", dimension);

        return tagCompound;
    }

    public void readFromCompound(NBTTagCompound tagCompound) {
        BlockPos minBlockPos = BlockPos.fromLong(tagCompound.getLong("MinPos"));
        BlockPos maxBlockPos = BlockPos.fromLong(tagCompound.getLong("MaxPos"));

        regionMin = new ChunkPos(minBlockPos.getX(), minBlockPos.getZ());
        regionMax = new ChunkPos(maxBlockPos.getX(), maxBlockPos.getZ());
        dimension = tagCompound.getInteger("DimensionID");
    }

    /**
     * Converts a given Vec3d's position to the real position in the parentWorld world.
     *
     * @param regionPos
     * @return
     */
    public Vec3d convertRegionPosToRealWorld(Vec3d regionPos) {
        Vec3d adjustedPosition = new Vec3d(regionPos.xCoord - regionMin.getXStart() + x,
                regionPos.yCoord + y,
                regionPos.zCoord - regionMin.getZStart() + z);

        return adjustedPosition;
    }

    /**
     * Converts a given Vec3d's position from the real world into a position for internal region use.
     *
     * @param realWorldPos
     * @return
     */
    public Vec3d convertRealWorldPosToRegion(Vec3d realWorldPos) {
        Vec3d adjustedPosition = new Vec3d(realWorldPos.xCoord + regionMin.getXStart() - x,
                realWorldPos.yCoord - y,
                realWorldPos.zCoord + regionMin.getZStart() - z);

        return adjustedPosition;
    }

    public BlockPos convertRegionPosToRealWorld(BlockPos pos) {
        Vec3d vec3DPos = new Vec3d(pos.getX(), pos.getY(), pos.getZ());
        vec3DPos = convertRegionPosToRealWorld(vec3DPos);
        return new BlockPos(Math.round(vec3DPos.xCoord), Math.round(vec3DPos.yCoord), Math.round(vec3DPos.zCoord));
    }

    public BlockPos convertRealWorldPosToRegion(BlockPos pos) {
        Vec3d vec3DPos = new Vec3d(pos.getX(), pos.getY(), pos.getZ());
        vec3DPos = convertRealWorldPosToRegion(vec3DPos);
        return new BlockPos(Math.round(vec3DPos.xCoord), Math.round(vec3DPos.yCoord), Math.round(vec3DPos.zCoord));
    }

    public AxisAlignedBB convertRegionBBToRealWorld(AxisAlignedBB regionBB) {
        Vec3d min = convertRegionPosToRealWorld(new Vec3d(regionBB.minX, regionBB.minY, regionBB.minZ));
        Vec3d max = convertRegionPosToRealWorld(new Vec3d(regionBB.maxX, regionBB.maxY, regionBB.maxZ));

        return new AxisAlignedBB(min.xCoord, min.yCoord, min.zCoord, max.xCoord, max.yCoord, max.zCoord);
    }

}
