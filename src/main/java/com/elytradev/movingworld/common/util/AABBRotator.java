package com.elytradev.movingworld.common.util;


import net.minecraft.util.math.AxisAlignedBB;

public class AABBRotator {
    private static Vec3dMod vec00, vec01, vec10, vec11;
    private static Vec3dMod vec0h, vec1h, vech0, vech1;

    static {
        vec00 = new Vec3dMod(0D, 0D, 0D);
        vec01 = new Vec3dMod(0D, 0D, 0D);
        vec10 = new Vec3dMod(0D, 0D, 0D);
        vec11 = new Vec3dMod(0D, 0D, 0D);

        vec0h = new Vec3dMod(0D, 0D, 0D);
        vec1h = new Vec3dMod(0D, 0D, 0D);
        vech0 = new Vec3dMod(0D, 0D, 0D);
        vech1 = new Vec3dMod(0D, 0D, 0D);
    }

    /**
     * @param aabb The axis aligned boundingbox to rotate
     * @param ang  The angle to rotate the aabb in radians
     */
    public static AxisAlignedBB rotateAABBAroundY(AxisAlignedBB aabb, double xoff, double zoff, float ang) {
        double y0 = aabb.minY;
        double y1 = aabb.maxY;

        vec00 = vec00.setX(aabb.minX - xoff);
        vec00 = vec00.setZ(aabb.minZ - zoff);

        vec01 = vec01.setX(aabb.minX - xoff);
        vec01 = vec01.setZ(aabb.maxZ - zoff);

        vec10 = vec10.setX(aabb.maxX - xoff);
        vec10 = vec10.setZ(aabb.minZ - zoff);

        vec11 = vec11.setX(aabb.maxX - xoff);
        vec11 = vec11.setZ(aabb.maxZ - zoff);

        vec00 = vec00.rotateAroundY(ang);
        vec01 = vec01.rotateAroundY(ang);
        vec10 = vec10.rotateAroundY(ang);
        vec11 = vec11.rotateAroundY(ang);

        vec0h = vec0h.setX((vec00.xCoord + vec01.xCoord) / 2D);
        vec0h = vec0h.setZ((vec00.zCoord + vec01.zCoord) / 2D);

        vec1h = vec1h.setX((vec10.xCoord + vec11.xCoord) / 2D);
        vec1h = vec1h.setZ((vec10.zCoord + vec11.zCoord) / 2D);

        vech0 = vech0.setX((vec00.xCoord + vec10.xCoord) / 2D);
        vech0 = vech0.setZ((vec00.zCoord + vec10.zCoord) / 2D);

        vech1 = vech1.setX((vec01.xCoord + vec11.xCoord) / 2D);
        vech1 = vech1.setZ((vec01.zCoord + vec11.zCoord) / 2D);

        aabb = new AxisAlignedBB(minX(), y0, minZ(), maxX(), y1, maxZ()).offset(xoff, 0F, zoff);

        return aabb;
    }

    private static double minX() {
        return Math.min(Math.min(Math.min(vec0h.xCoord, vec1h.xCoord), vech0.xCoord), vech1.xCoord);
    }

    private static double minZ() {
        return Math.min(Math.min(Math.min(vec0h.zCoord, vec1h.zCoord), vech0.zCoord), vech1.zCoord);
    }

    private static double maxX() {
        return Math.max(Math.max(Math.max(vec0h.xCoord, vec1h.xCoord), vech0.xCoord), vech1.xCoord);
    }

    private static double maxZ() {
        return Math.max(Math.max(Math.max(vec0h.zCoord, vec1h.zCoord), vech0.zCoord), vech1.zCoord);
    }
}
