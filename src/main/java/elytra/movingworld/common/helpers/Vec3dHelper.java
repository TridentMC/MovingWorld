package elytra.movingworld.common.helpers;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class Vec3dHelper {

    // From 1.7, reimplemented for my sanity.

    /**
     * Rotates the vector around the x axis by the specified angle.
     */
    public static Vec3d rotateAroundX(Vec3d vec, float ang) {
        float f1 = MathHelper.cos(ang);
        float f2 = MathHelper.sin(ang);
        double d0 = vec.xCoord;
        double d1 = vec.yCoord * (double) f1 + vec.zCoord * (double) f2;
        double d2 = vec.zCoord * (double) f1 - vec.yCoord * (double) f2;
        return new Vec3d(d0, d1, d2);
    }

    /**
     * Rotates the vector around the y axis by the specified angle.
     */
    public static Vec3d rotateAroundY(Vec3d vec, float ang) {
        float f1 = MathHelper.cos(ang);
        float f2 = MathHelper.sin(ang);
        double d0 = vec.xCoord * (double) f1 + vec.zCoord * (double) f2;
        double d1 = vec.yCoord;
        double d2 = vec.zCoord * (double) f1 - vec.xCoord * (double) f2;
        return new Vec3d(d0, d1, d2);
    }

    /**
     * Rotates the vector around the z axis by the specified angle.
     */
    public static Vec3d rotateAroundZ(Vec3d vec, float ang) {
        float f1 = MathHelper.cos(ang);
        float f2 = MathHelper.sin(ang);
        double d0 = vec.xCoord * (double) f1 + vec.yCoord * (double) f2;
        double d1 = vec.yCoord * (double) f1 - vec.xCoord * (double) f2;
        double d2 = vec.zCoord;
        return new Vec3d(d0, d1, d2);
    }

}
