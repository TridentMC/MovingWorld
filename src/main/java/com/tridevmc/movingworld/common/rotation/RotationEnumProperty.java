package com.tridevmc.movingworld.common.rotation;

import com.tridevmc.movingworld.api.rotation.IRotationProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.state.IProperty;
import net.minecraft.util.EnumFacing;

public class RotationEnumProperty implements IRotationProperty {

    @Override
    public IBlockState rotate(IProperty property, IBlockState blockState, boolean ccw) {
        Object propertyValue = blockState.get(property);

        if (propertyValue instanceof EnumFacing) {
            EnumFacing facing = (EnumFacing) propertyValue;

            if (facing.getHorizontalIndex() != -1) {
                if (!ccw)
                    blockState = blockState.with(property, facing.rotateY());
                else
                    blockState = blockState.with(property, facing.rotateYCCW());
            }
        } else if (propertyValue instanceof EnumFacing.Axis) {
            EnumFacing.Axis axis = (EnumFacing.Axis) propertyValue;

            axis = axis == EnumFacing.Axis.X ? EnumFacing.Axis.Z : axis == EnumFacing.Axis.Z ? EnumFacing.Axis.X : axis;

            blockState = blockState.with(property, axis);
        }

        return blockState;
    }
}
