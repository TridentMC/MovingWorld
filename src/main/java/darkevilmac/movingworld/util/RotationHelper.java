package darkevilmac.movingworld.util;

import darkevilmac.movingworld.MovingWorld;
import darkevilmac.movingworld.util.rotation.IRotationProperty;
import net.minecraft.block.BlockLog;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
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
               //else
               //if (prop.getName().equals("facing") && prop instanceof PropertyDirection) {
               //    EnumFacing facing = (EnumFacing) blockState.getValue(prop);

               //    if (facing.getHorizontalIndex() != -1) {
               //        if (!ccw)
               //            blockState = blockState.withProperty(prop, facing.rotateY());
               //        else
               //            blockState = blockState.withProperty(prop, facing.rotateYCCW());
               //    }
               //    break;
               //} else {
               //    if (prop.getName().equals("axis") && prop instanceof PropertyEnum) {
               //        if (blockState.getValue(prop) instanceof EnumFacing.Axis) {
               //            EnumFacing.Axis axis = (EnumFacing.Axis) blockState.getValue(prop);

               //            if (axis == EnumFacing.Axis.X)
               //                axis = EnumFacing.Axis.Z;
               //            else if (axis == EnumFacing.Axis.Z)
               //                axis = EnumFacing.Axis.X;

               //            blockState = blockState.withProperty(prop, axis);

               //        } else if (blockState.getValue(prop) instanceof BlockLog.EnumAxis) {
               //            BlockLog.EnumAxis axis = (BlockLog.EnumAxis) blockState.getValue(prop);

               //            if (axis == BlockLog.EnumAxis.X)
               //                axis = BlockLog.EnumAxis.Z;
               //            else if (axis == BlockLog.EnumAxis.Z)
               //                axis = BlockLog.EnumAxis.X;

               //            blockState = blockState.withProperty(prop, axis);

               //        }
               //    }
               //}
            }

            world.setBlockState(pos, blockState);
        }
    }
}
