package com.tridevmc.movingworld.common.util;


import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

public class Vec3dMod extends Vec3d {

    private static final Vec3dMod ORIGIN = new Vec3dMod(Vec3d.ZERO);

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
        return ORIGIN;
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
    @Override
    @Deprecated
    public Vec3dMod rotatePitch(float pitch) {
        return new Vec3dMod(super.rotatePitch(pitch));
    }

    /**
     * Rotate around the Y axis
     */
    @Override
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
        return new Vec3dMod(var4, var6, this.z);
    }

    // Reimplemented from 1.7 for my sanity.

    /**
     * Rotates the vector around the x axis by the specified angle.
     */
    public Vec3dMod rotateAroundX(float angle) {
        float f1 = MathHelper.cos(angle);
        float f2 = MathHelper.sin(angle);
        double d1 = this.y * (double) f1 + this.z * (double) f2;
        double d2 = this.z * (double) f1 - this.y * (double) f2;
        return this.makeNewVec(this.x, d1, d2);
    }

    /**
     * Rotates the vector around the y axis by the specified angle.
     */
    public Vec3dMod rotateAroundY(float angle) {
        float f1 = MathHelper.cos(angle);
        float f2 = MathHelper.sin(angle);
        double d0 = this.x * (double) f1 + this.z * (double) f2;
        double d2 = this.z * (double) f1 - this.x * (double) f2;
        return this.makeNewVec(d0, this.y, d2);
    }

    /**
     * Rotates the vector around the z axis by the specified angle.
     */
    public Vec3dMod rotateAroundZ(float angle) {
        float f1 = MathHelper.cos(angle);
        float f2 = MathHelper.sin(angle);
        double d0 = this.x * (double) f1 + this.y * (double) f2;
        double d1 = this.y * (double) f1 - this.x * (double) f2;
        return this.makeNewVec(d0, d1, this.z);
    }

    public Vec3dMod rotate(Vec3d origin, float xAngle, float yAngle, float zAngle) {
        if (xAngle == 0 && yAngle == 0 && zAngle == 0) {
            return new Vec3dMod(this);
        }

        Matrix4d matrix = new Matrix4d();
        if (xAngle != 0) matrix.rotX(Math.toRadians(xAngle));
        if (yAngle != 0) matrix.rotY(Math.toRadians(yAngle));
        if (zAngle != 0) matrix.rotZ(Math.toRadians(zAngle));
        Vector3d vec = new Vector3d(origin.x - x, origin.y - y, origin.z - z);
        matrix.transform(vec);
        Vec3dMod out = new Vec3dMod(origin.x + vec.x, origin.y + vec.y, origin.z + vec.z);
        return out;
    }

    public Vec3dMod cross(Vec3d vec) {
        return new Vec3dMod(super.crossProduct(vec));
    }

    public Vec3dMod rotate(Direction.Axis axis, Vec3d origin, float angle) {
        if (angle == 0)
            return new Vec3dMod(this);

        angle = (float) Math.toRadians(angle);
        Matrix4d matrix = new Matrix4d();
        switch (axis) {
            case X:
                matrix.rotX(angle);
                break;
            case Y:
                matrix.rotY(angle);
                break;
            case Z:
                matrix.rotZ(angle);
                break;
        }
        Vector3d vec = new Vector3d(origin.x - x, origin.y - y, origin.z - z);
        matrix.transform(vec);
        return new Vec3dMod(origin.x + vec.x, origin.y + vec.y, origin.z + vec.z);
    }

}
