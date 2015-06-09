package darkevilmac.movingworld.util;

import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

/**
 * Adds rotateRoll.
 */

public class Vec3Mod extends Vec3 {

    public Vec3Mod(Vec3 vec3) {
        super(vec3.xCoord, vec3.yCoord, vec3.zCoord);
    }

    public Vec3Mod(BlockPos pos) {
        super(pos.getX(), pos.getY(), pos.getZ());
    }

    public Vec3Mod(double x, double y, double z) {
        super(x, y, z);
    }

    public static Vec3Mod getOrigin() {
        return new Vec3Mod(0, 0, 0);
    }

    public Vec3Mod setX(double x) {
        return new Vec3Mod(x, this.yCoord, this.zCoord);
    }

    public Vec3Mod setY(double y) {
        return new Vec3Mod(this.xCoord, y, this.zCoord);
    }

    public Vec3Mod setZ(double z) {
        return new Vec3Mod(this.xCoord, this.yCoord, z);
    }

    public Vec3Mod makeNewVec(double x, double y, double z) {
        return new Vec3Mod(x, y, z);
    }

    /**
     * Rotate around the X axis
     *
     * @param pitch
     * @return
     */
    @Deprecated
    public Vec3Mod rotatePitch(float pitch) {
        return new Vec3Mod(super.rotatePitch(pitch));
    }

    /**
     * Rotate around the Y axis
     *
     * @param yaw
     * @return
     */
    @Deprecated
    public Vec3Mod rotateYaw(float yaw) {
        return new Vec3Mod(super.rotateYaw(yaw));
    }

    /**
     * Rotate around the Z axis.
     *
     * @param roll
     * @return
     */
    @Deprecated
    public Vec3Mod rotateRoll(float roll) {
        float var2 = MathHelper.cos(roll);
        float var3 = MathHelper.sin(roll);
        double var4 = this.xCoord * (double) var2 + this.yCoord * (double) var3;
        double var6 = this.yCoord * (double) var2 - this.xCoord * (double) var3;
        double var8 = this.zCoord;
        return new Vec3Mod(var4, var6, var8);
    }

    // Reimplemented from 1.7 for my sanity.

    /**
     * Rotates the vector around the x axis by the specified angle.
     */
    public Vec3Mod rotateAroundX(float p_72440_1_) {
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
    public Vec3Mod rotateAroundY(float p_72442_1_) {
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
    public Vec3Mod rotateAroundZ(float p_72446_1_) {
        float f1 = MathHelper.cos(p_72446_1_);
        float f2 = MathHelper.sin(p_72446_1_);
        double d0 = this.xCoord * (double) f1 + this.yCoord * (double) f2;
        double d1 = this.yCoord * (double) f1 - this.xCoord * (double) f2;
        double d2 = this.zCoord;
        return this.makeNewVec(d0, d1, d2);
    }

}
