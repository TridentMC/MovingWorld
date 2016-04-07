package darkevilmac.movingworld.common.baseclasses.util;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.EnumFacing;

public interface IAxisAlignedBBMixin {

    public AxisAlignedBB rotate(int angle);

    public AxisAlignedBB rotate(int angle, EnumFacing.Axis axis);

}
