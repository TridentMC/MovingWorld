package io.github.elytra.movingworld.common.util;


import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/**
 * Adds rotateRoll.
 */

public class Vec3dMod extends Vec3d {

    public Vec3dMod(Vec3d vec3) {
        super(vec3.xCoord, vec3.yCoord, vec3.zCoord);
    }

    public Vec3dMod(BlockPos pos) {
        super(pos.getX(), pos.getY(), pos.getZ());
    }

    public Vec3dMod(double x, double y, double z) {
        super(x, y, z);
    }

    public static Vec3dMod getOrigin() {
        return new Vec3dMod(0, 0, 0);
    }

    public Vec3dMod setX(double x) {
        return new Vec3dMod(x, this.yCoord, this.zCoord);
    }

    public Vec3dMod setY(double y) {
        return new Vec3dMod(this.xCoord, y, this.zCoord);
    }

    public Vec3dMod setZ(double z) {
        return new Vec3dMod(this.xCoord, this.yCoord, z);
    }

    public Vec3dMod makeNewVec(double x, double y, double z) {
        return new Vec3dMod(x, y, z);
    }

    /**
     * Rotate around the X axis
     */
    @Deprecated
    public Vec3dMod rotatePitch(float pitch) {
        return new Vec3dMod(super.rotatePitch(pitch));
    }

    /**
     * Rotate around the Y axis
     */
    @Deprecated
    public Vec3dMod rotateYaw(float yaw) {
        return new Vec3dMod(super.rotateYaw(yaw));
    }

    /**
     * Rotate around the Z axis.
     */
    @Deprecated
    public Vec3dMod rotateRoll(float roll) {
        float var2 = MathHelper.cos(roll);
        float var3 = MathHelper.sin(roll);
        double var4 = this.xCoord * (double) var2 + this.yCoord * (double) var3;
        double var6 = this.yCoord * (double) var2 - this.xCoord * (double) var3;
        double var8 = this.zCoord;
        return new Vec3dMod(var4, var6, var8);
    }

    // Reimplemented from 1.7 for my sanity.

    /**
     * Rotates the vector around the x axis by the specified angle.
     */
    public Vec3dMod rotateAroundX(float p_72440_1_) {
        float f1 = MathHelper.cos(p_72440_1_);
        float f2 = MathHelper.sin(p_72440_1_);
        double d0 = this.xCoord;
        double d1 = this.yCoord * (double) f1 + this.zCoord * (double) f2;
        double d2 = this.zCoord * (double) f1 - this.yCoord * (double) f2;
        return this.makeNewVec(d0, d1, d2);
    }

    /**
     * Rotates the vector around the y axis by the specified angle.
     */
    public Vec3dMod rotateAroundY(float p_72442_1_) {
        float f1 = MathHelper.cos(p_72442_1_);
        float f2 = MathHelper.sin(p_72442_1_);
        double d0 = this.xCoord * (double) f1 + this.zCoord * (double) f2;
        double d1 = this.yCoord;
        double d2 = this.zCoord * (double) f1 - this.xCoord * (double) f2;
        return this.makeNewVec(d0, d1, d2);
    }

    /**
     * Rotates the vector around the z axis by the specified angle.
     */
    public Vec3dMod rotateAroundZ(float p_72446_1_) {
        float f1 = MathHelper.cos(p_72446_1_);
        float f2 = MathHelper.sin(p_72446_1_);
        double d0 = this.xCoord * (double) f1 + this.yCoord * (double) f2;
        double d1 = this.yCoord * (double) f1 - this.xCoord * (double) f2;
        double d2 = this.zCoord;
        return this.makeNewVec(d0, d1, d2);
    }

}
