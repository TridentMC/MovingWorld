package com.elytradev.movingworld.common.asm.mixin.core.block.property;

import com.elytradev.movingworld.api.rotation.IRotationProperty;
import com.elytradev.movingworld.common.util.RotationHelper;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Objects;

@Mixin(PropertyInteger.class)
public class MixinPropertyInteger implements IRotationProperty {
    @Override
    public IBlockState rotate(IBlockState blockState, boolean ccw) {
        PropertyInteger intProp = (PropertyInteger) (Object) this;
        int propVal = blockState.getValue(intProp);

        if (Objects.equals(intProp.getName(), "rotation") && isValidRotationProperty()) {
            for (int i = 0; i <= 3; i++) {
                propVal = RotationHelper.rotateInteger(propVal, 0, 15, ccw);
            }
            blockState = blockState.withProperty(intProp, propVal);
        }

        return blockState;
    }

    boolean isValidRotationProperty() {
        PropertyInteger intProp = (PropertyInteger) (Object) this;

        return intProp.getAllowedValues().contains(0) && intProp.getAllowedValues().contains(15)
                && (!intProp.getAllowedValues().contains(-1) && !intProp.getAllowedValues().contains(16));
    }
}
