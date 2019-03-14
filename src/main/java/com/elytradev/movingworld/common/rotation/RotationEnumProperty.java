package com.elytradev.movingworld.common.rotation;

import com.elytradev.movingworld.api.rotation.IRotationProperty;
import net.minecraft.block.BlockLever;
import net.minecraft.block.state.IBlockState;
import net.minecraft.state.IProperty;
import net.minecraft.util.EnumFacing;

public class RotationEnumProperty implements IRotationProperty {

    @Override
    public IBlockState rotate(IBlockState blockState, boolean ccw) {
        IProperty propertyEnum = (IProperty) this;
        Object propertyValue = blockState.get(propertyEnum);

        if (propertyValue instanceof EnumFacing) {
            EnumFacing facing = (EnumFacing) propertyValue;

            if (facing.getHorizontalIndex() != -1) {
                if (!ccw)
                    blockState = blockState.with(propertyEnum, facing.rotateY());
                else
                    blockState = blockState.with(propertyEnum, facing.rotateYCCW());
            }
        } else if (propertyValue instanceof EnumFacing.Axis) {
            EnumFacing.Axis axis = (EnumFacing.Axis) propertyValue;

            axis = axis == EnumFacing.Axis.X ? EnumFacing.Axis.Z : axis == EnumFacing.Axis.Z ? EnumFacing.Axis.X : axis;

            blockState = blockState.with(propertyEnum, axis);
        }

        return blockState;
    }
}
