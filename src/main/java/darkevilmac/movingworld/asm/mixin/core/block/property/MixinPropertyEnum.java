package darkevilmac.movingworld.asm.mixin.core.block.property;

import darkevilmac.movingworld.util.rotation.IRotationProperty;
import net.minecraft.block.BlockLever;
import net.minecraft.block.BlockLog;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PropertyEnum.class)
public class MixinPropertyEnum implements IRotationProperty {

    @Override
    public IBlockState rotateBlock(World world, BlockPos pos, boolean ccw) {
        IBlockState blockState = world.getBlockState(pos);
        IProperty propertyEnum = (IProperty) this;

        Object propertyValue = blockState.getValue(propertyEnum);

        if (propertyValue == null) return blockState;

        if (propertyValue instanceof EnumFacing) {
            EnumFacing facing = (EnumFacing) propertyValue;

            if (facing.getHorizontalIndex() != -1) {
                if (!ccw)
                    blockState = blockState.withProperty(propertyEnum, facing.rotateY());
                else
                    blockState = blockState.withProperty(propertyEnum, facing.rotateYCCW());
            }
        } else if (propertyValue instanceof EnumFacing.Axis) {
            EnumFacing.Axis axis = (EnumFacing.Axis) propertyValue;

            if (axis == EnumFacing.Axis.X)
                axis = EnumFacing.Axis.Z;
            else if (axis == EnumFacing.Axis.Z)
                axis = EnumFacing.Axis.X;

            blockState = blockState.withProperty(propertyEnum, axis);
        } else if (propertyValue instanceof BlockLog.EnumAxis) {
            BlockLog.EnumAxis axis = (BlockLog.EnumAxis) blockState.getValue(propertyEnum);

            if (axis == BlockLog.EnumAxis.X)
                axis = BlockLog.EnumAxis.Z;
            else if (axis == BlockLog.EnumAxis.Z)
                axis = BlockLog.EnumAxis.X;

            blockState = blockState.withProperty(propertyEnum, axis);
        } else if (propertyValue instanceof BlockLever.EnumOrientation) {
            BlockLever.EnumOrientation orientation = (BlockLever.EnumOrientation) blockState.getValue(propertyEnum);
            EnumFacing facing = orientation.getFacing();

            if (facing != EnumFacing.UP && facing != EnumFacing.DOWN) {
                if (!ccw)
                    facing = facing.rotateY();
                else
                    facing = facing.rotateYCCW();

                blockState = blockState.withProperty(propertyEnum,
                        BlockLever.EnumOrientation.byMetadata(BlockLever.getMetadataForFacing(facing) & 7));
            } else {
                if (orientation == BlockLever.EnumOrientation.UP_X) orientation = BlockLever.EnumOrientation.UP_Z;
                else if (orientation == BlockLever.EnumOrientation.UP_Z) orientation = BlockLever.EnumOrientation.UP_X;

                if (orientation == BlockLever.EnumOrientation.DOWN_X) orientation = BlockLever.EnumOrientation.DOWN_Z;
                else if (orientation == BlockLever.EnumOrientation.DOWN_Z)
                    orientation = BlockLever.EnumOrientation.DOWN_X;

                blockState = blockState.withProperty(propertyEnum, orientation);
            }
        }


        return blockState;
    }

}
