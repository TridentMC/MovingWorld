package darkevilmac.movingworld.util;

import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

/**
 * Adds rotateRoll.
 */

public class Vec3Mod extends Vec3 {

    public Vec3Mod(double x, double y, double z) {
        super(x, y, z);
    }

    public Vec3Mod rotateRoll(float roll) {
        float var2 = MathHelper.cos(roll);
        float var3 = MathHelper.sin(roll);
        double var4 = this.xCoord * (double) var2 + this.yCoord * (double) var3;
        double var6 = this.yCoord * (double) var2 - this.xCoord * (double) var3;
        double var8 = this.zCoord;
        return new Vec3Mod(var4, var6, var8);
    }

}
