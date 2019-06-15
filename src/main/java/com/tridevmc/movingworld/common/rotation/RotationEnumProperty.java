package com.tridevmc.movingworld.common.rotation;

import com.tridevmc.movingworld.api.rotation.IRotationProperty;
import net.minecraft.block.BlockState;
import net.minecraft.state.IProperty;
import net.minecraft.util.Direction;

public class RotationEnumProperty implements IRotationProperty {

    @Override
    public BlockState rotate(IProperty property, BlockState blockState, boolean ccw) {
        Object propertyValue = blockState.get(property);

        if (propertyValue instanceof Direction) {
            Direction facing = (Direction) propertyValue;

            if (facing.getHorizontalIndex() != -1) {
                if (!ccw)
                    blockState = blockState.with(property, facing.rotateY());
                else
                    blockState = blockState.with(property, facing.rotateYCCW());
            }
        } else if (propertyValue instanceof Direction.Axis) {
            Direction.Axis axis = (Direction.Axis) propertyValue;

            axis = axis == Direction.Axis.X ? Direction.Axis.Z : axis == Direction.Axis.Z ? Direction.Axis.X : axis;

            blockState = blockState.with(property, axis);
        }

        return blockState;
    }
}
