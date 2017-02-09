package io.github.elytra.movingworld.common.experiments;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;

public class MobileRegion {

    public double x, y, z;

    public ChunkPos regionMin;
    public ChunkPos regionMax;

    public MobileRegion(ChunkPos regionMin, ChunkPos regionMax) {
        this.regionMin = regionMin;
        this.regionMax = regionMax;
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
     * Converts a given Vec3d's position to the real position in the parent world.
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
        Vec3d vec3DPos = new Vec3d(pos.getX(),pos.getY(), pos.getZ());
        vec3DPos = convertRegionPosToRealWorld(vec3DPos);
        return new BlockPos(Math.round(vec3DPos.xCoord), Math.round(vec3DPos.yCoord), Math.round(vec3DPos.zCoord));
    }

    public BlockPos convertRealWorldPosToRegion(BlockPos pos) {
        Vec3d vec3DPos = new Vec3d(pos.getX(),pos.getY(), pos.getZ());
        vec3DPos = convertRealWorldPosToRegion(vec3DPos);
        return new BlockPos(Math.round(vec3DPos.xCoord), Math.round(vec3DPos.yCoord), Math.round(vec3DPos.zCoord));
    }

    public AxisAlignedBB convertRegionBBToRealWorld(AxisAlignedBB regionBB){
        Vec3d min = convertRegionPosToRealWorld(new Vec3d(regionBB.minX, regionBB.minY, regionBB.minZ));
        Vec3d max = convertRegionPosToRealWorld(new Vec3d(regionBB.maxX, regionBB.maxY, regionBB.maxZ));

        return new AxisAlignedBB(min.xCoord,min.yCoord,min.zCoord, max.xCoord,max.yCoord, max.zCoord);
    }
}
