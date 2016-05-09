package elytra.movingworld.common.baseclasses.util;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;

public interface IAxisAlignedBBMixin {

    public AxisAlignedBB rotate(int angle);

    public AxisAlignedBB rotate(int angle, EnumFacing.Axis axis);

}
