package com.tridevmc.movingworld.common.util;


import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;

public class Vec3dMod extends Vector3d {

    private static final Vec3dMod ORIGIN = new Vec3dMod(Vector3d.ZERO);

    public Vec3dMod(Vector3d vec3) {
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

    public Vec3dMod subMod(Vector3d sub) {
        return this.addMod(-sub.x, -sub.y, -sub.z);
    }

    public Vec3dMod subMod(double x, double y, double z) {
        return this.addMod(-x, -y, -z);
    }

    public Vec3dMod addMod(Vector3d add) {
        return this.addMod(add.x, add.y, add.z);
    }

    public Vec3dMod addMod(double x, double y, double z) {
        return new Vec3dMod(this.x + x, this.y + y, this.z + z);
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

    public Vec3dMod rotate(Vector3d origin, float xAngle, float yAngle, float zAngle) {
        if (xAngle == 0 && xAngle == yAngle && yAngle == zAngle) {
            return new Vec3dMod(this);
        }

        return new Vec3dMod(origin).subMod(this).applyRotation(xAngle, yAngle, zAngle).addMod(origin);
    }

    public Vec3dMod cross(Vector3d vec) {
        return new Vec3dMod(super.crossProduct(vec));
    }

    public Vec3dMod rotate(Direction.Axis axis, Vector3d origin, float angle) {
        if (angle == 0)
            return new Vec3dMod(this);

        angle = (float) Math.toRadians(angle);
        float xAngle = 0, yAngle = 0, zAngle = 0;
        switch (axis) {
            case X:
                xAngle = angle;
                break;
            case Y:
                yAngle = angle;
                break;
            case Z:
                zAngle = angle;
                break;
        }
        return this.rotate(origin, xAngle, yAngle, zAngle);
    }

    private Vec3dMod applyRotation(float xAngle, float yAngle, float zAngle) {
        return applyQuaternion(createQuaternion(xAngle, yAngle, zAngle));
    }

    // just a quick copy and paste from the actual Quaternion class because it's only on the client :^)
    private Quaternion createQuaternion(float xAngle, float yAngle, float zAngle) {
        xAngle *= ((float) Math.PI / 180F);
        yAngle *= ((float) Math.PI / 180F);
        zAngle *= ((float) Math.PI / 180F);

        float f = MathHelper.sin(0.5F * xAngle);
        float f1 = MathHelper.cos(0.5F * xAngle);
        float f2 = MathHelper.sin(0.5F * yAngle);
        float f3 = MathHelper.cos(0.5F * yAngle);
        float f4 = MathHelper.sin(0.5F * zAngle);
        float f5 = MathHelper.cos(0.5F * zAngle);
        float x = f * f3 * f5 + f1 * f2 * f4;
        float y = f1 * f2 * f5 - f * f3 * f4;
        float z = f * f2 * f5 + f1 * f3 * f4;
        float w = f1 * f3 * f5 - f * f2 * f4;

        return new Quaternion(x, y, z, w);
    }

    // at this point im just not in the mood for all these final classes. just let me rotate and go away.
    private Vec3dMod applyQuaternion(Quaternion quaternion) {
        float f = quaternion.getX();
        float f1 = quaternion.getY();
        float f2 = quaternion.getZ();
        float f3 = quaternion.getW();
        float f4 = 2.0F * f * f;
        float f5 = 2.0F * f1 * f1;
        float f6 = 2.0F * f2 * f2;
        float f7 = f * f1;
        float f8 = f1 * f2;
        float f9 = f2 * f;
        float f10 = f * f3;
        float f11 = f1 * f3;
        float f12 = f2 * f3;
        float m00 = 1.0F - f5 - f6;
        float m11 = 1.0F - f6 - f4;
        float m22 = 1.0F - f4 - f5;
        float m10 = 2.0F * (f7 + f12);
        float m01 = 2.0F * (f7 - f12);
        float m20 = 2.0F * (f9 - f11);
        float m02 = 2.0F * (f9 + f11);
        float m21 = 2.0F * (f8 + f10);
        float m12 = 2.0F * (f8 - f10);

        double xOut, yOut, zOut;
        xOut = m00 * this.x + m01 * this.y + m02 * this.z;
        yOut = m10 * this.x + m11 * this.y + m12 * this.z;
        zOut = m20 * this.x + m21 * this.y + m22 * this.z;
        return new Vec3dMod(xOut, yOut, zOut);
    }

}
