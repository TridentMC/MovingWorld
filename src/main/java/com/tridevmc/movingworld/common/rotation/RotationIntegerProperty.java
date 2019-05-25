package com.tridevmc.movingworld.common.rotation;

import com.tridevmc.movingworld.api.rotation.IRotationProperty;
import com.tridevmc.movingworld.common.util.RotationHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.state.IProperty;
import net.minecraft.state.IntegerProperty;

import java.util.Objects;

public class RotationIntegerProperty implements IRotationProperty {

    @Override
    public IBlockState rotate(IProperty property, IBlockState blockState, boolean ccw) {
        IntegerProperty intProp = (IntegerProperty) property;
        int propVal = blockState.get(intProp);

        if (Objects.equals(intProp.getName(), "rotation") && isValidRotationProperty(property)) {
            for (int i = 0; i <= 3; i++) {
                propVal = RotationHelper.INSTANCE.rotateInteger(propVal, 0, 15, ccw);
            }
            blockState = blockState.with(intProp, propVal);
        }

        return blockState;
    }

    boolean isValidRotationProperty(IProperty property) {
        IntegerProperty intProp = (IntegerProperty) property;

        return intProp.getAllowedValues().contains(0) && intProp.getAllowedValues().contains(15)
                && (!intProp.getAllowedValues().contains(-1) && !intProp.getAllowedValues().contains(16));
    }

}
