package darkevilmac.movingworld.util;

import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public abstract class RotationHelper {

    public static boolean rotateArchimedesBlock(World world, BlockPos pos, EnumFacing axis) {
        if (axis == EnumFacing.UP || axis == EnumFacing.DOWN) {
            world.getBlockState(pos).getBlock().rotateBlock(world, pos,axis);
        }
        return true;
    }


}
