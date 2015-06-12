package darkevilmac.movingworld.util;

import darkevilmac.movingworld.util.rotation.IRotationProperty;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3i;
import net.minecraft.world.World;

public class RotationHelper {

    public static void rotateBlock(World world, BlockPos pos, boolean ccw) {
        if (world != null && world.getBlockState(pos) != null && world.getBlockState(pos).getBlock() != null) {
            IBlockState blockState = world.getBlockState(pos);

            for (IProperty prop : (java.util.Set<IProperty>) blockState.getProperties().keySet()) {
                if (prop instanceof IRotationProperty) {
                    // Custom rotation property found.
                    IRotationProperty rotationProperty = (IRotationProperty) prop;
                    blockState = rotationProperty.rotateBlock(world, pos, ccw);

                    break;
                }
            }

            world.setBlockState(pos, blockState);
        }
    }

    public static Vec3i getDirectionVec(EnumFacing facing) {
        switch (facing) {
            case DOWN:
                return new Vec3i(0, -1, 0);
            case UP:
                return new Vec3i(0, 1, 0);
            case NORTH:
                return new Vec3i(0, 0, -1);
            case SOUTH:
                return new Vec3i(0, 0, 1);
            case WEST:
                return new Vec3i(-1, 0, 0);
            case EAST:
                return new Vec3i(1, 0, 0);
        }

        return null;
    }

}
