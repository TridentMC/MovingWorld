package com.tridevmc.movingworld.common.rotation;

import com.tridevmc.movingworld.api.rotation.IRotationProperty;
import net.minecraft.block.BlockState;
import net.minecraft.state.Property;
import net.minecraft.util.Direction;
import net.minecraft.util.IStringSerializable;

public class RotationEnumProperty<T extends Enum<T> & IStringSerializable> implements IRotationProperty<T> {

    @SuppressWarnings("unchecked")
    @Override
    public <V extends T> BlockState rotate(Property<T> property, BlockState blockState, boolean ccw) {
        V propertyValue = (V) blockState.get(property);

        if (propertyValue instanceof Direction) {
            Direction direction = (Direction) propertyValue;

            if (direction.getHorizontalIndex() != -1) {
                if (!ccw)
                    blockState = blockState.with(property, (V) direction.rotateY());
                else
                    blockState = blockState.with(property, (V) direction.rotateYCCW());
            }
        } else if (propertyValue instanceof Direction.Axis) {
            Direction.Axis axis = (Direction.Axis) propertyValue;

            axis = axis == Direction.Axis.X ? Direction.Axis.Z : axis == Direction.Axis.Z ? Direction.Axis.X : axis;

            blockState = blockState.with(property, (V) axis);
        }

        return blockState;
    }
}
