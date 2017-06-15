package com.elytradev.movingworld.common.util;


import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/**
 * Adds rotateRoll.
 */

public class Vec3dMod extends Vec3d {

    public Vec3dMod(Vec3d vec3) {
        super(vec3.x, vec3.y, vec3.z);
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
        return new Vec3dMod(x, this.y, this.z);
    }

    public Vec3dMod setY(double y) {
        return new Vec3dMod(this.x, y, this.z);
    }

    public Vec3dMod setZ(double z) {
        return new Vec3dMod(this.x, this.y, z);
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
        double var4 = this.x * (double) var2 + this.y * (double) var3;
        double var6 = this.y * (double) var2 - this.x * (double) var3;
        double var8 = this.z;
        return new Vec3dMod(var4, var6, var8);
    }

    // Reimplemented from 1.7 for my sanity.

    /**
     * Rotates the vector around the x axis by the specified angle.
     */
    public Vec3dMod rotateAroundX(float angle) {
        float f1 = MathHelper.cos(angle);
        float f2 = MathHelper.sin(angle);
        double d0 = this.x;
        double d1 = this.y * (double) f1 + this.z * (double) f2;
        double d2 = this.z * (double) f1 - this.y * (double) f2;
        return this.makeNewVec(d0, d1, d2);
    }

    /**
     * Rotates the vector around the y axis by the specified angle.
     */
    public Vec3dMod rotateAroundY(float angle) {
        float f1 = MathHelper.cos(angle);
        float f2 = MathHelper.sin(angle);
        double d0 = this.x * (double) f1 + this.z * (double) f2;
        double d1 = this.y;
        double d2 = this.z * (double) f1 - this.x * (double) f2;
        return this.makeNewVec(d0, d1, d2);
    }

    /**
     * Rotates the vector around the z axis by the specified angle.
     */
    public Vec3dMod rotateAroundZ(float angle) {
        float f1 = MathHelper.cos(angle);
        float f2 = MathHelper.sin(angle);
        double d0 = this.x * (double) f1 + this.y * (double) f2;
        double d1 = this.y * (double) f1 - this.x * (double) f2;
        double d2 = this.z;
        return this.makeNewVec(d0, d1, d2);
    }

}
