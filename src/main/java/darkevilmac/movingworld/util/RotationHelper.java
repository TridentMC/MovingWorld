package darkevilmac.movingworld.util;

import net.minecraft.block.BlockLog;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public abstract class RotationHelper {

    public static boolean rotateArchimedesBlock(World world, BlockPos pos, EnumFacing axis) {
        if (axis == EnumFacing.UP || axis == EnumFacing.DOWN) {
            world.getBlockState(pos).getBlock().rotateBlock(world, pos, axis);
        }
        return true;
    }

    public static void rotateBlock(World world, BlockPos pos) {
        if (world != null && world.getBlockState(pos) != null && world.getBlockState(pos).getBlock() != null) {
            IBlockState blockState = world.getBlockState(pos);


            for (IProperty prop : (java.util.Set<IProperty>) blockState.getProperties().keySet()) {
                if (prop.getName().equals("facing") && prop instanceof PropertyDirection) {
                    EnumFacing facing = (EnumFacing) blockState.getValue(prop);

                    blockState = blockState.withProperty(prop, facing.rotateY());

                    break;
                } else {
                    if (prop.getName().equals("axis") && prop instanceof PropertyEnum) {
                        if (blockState.getValue(prop) instanceof EnumFacing.Axis) {
                            EnumFacing.Axis axis = (EnumFacing.Axis) blockState.getValue(prop);

                            if (axis == EnumFacing.Axis.X)
                                axis = EnumFacing.Axis.Z;
                            else if (axis == EnumFacing.Axis.Z)
                                axis = EnumFacing.Axis.X;

                            blockState = blockState.withProperty(prop, axis);

                        } else if (blockState.getValue(prop) instanceof BlockLog.EnumAxis) {
                            BlockLog.EnumAxis axis = (BlockLog.EnumAxis) blockState.getValue(prop);

                            if (axis == BlockLog.EnumAxis.X)
                                axis = BlockLog.EnumAxis.Z;
                            else if (axis == BlockLog.EnumAxis.Z)
                                axis = BlockLog.EnumAxis.X;

                            blockState = blockState.withProperty(prop, axis);

                        }
                    }
                }
            }

            world.setBlockState(pos, blockState);

        }
    }
}
