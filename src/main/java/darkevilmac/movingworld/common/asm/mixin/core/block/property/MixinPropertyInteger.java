package darkevilmac.movingworld.common.asm.mixin.core.block.property;

import darkevilmac.movingworld.common.util.RotationHelper;
import darkevilmac.movingworld.common.util.rotation.IRotationProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PropertyInteger.class)
public class MixinPropertyInteger implements IRotationProperty {
    @Override
    public IBlockState rotate(IBlockState blockState, boolean ccw) {
        PropertyInteger intProp = (PropertyInteger) (Object) this;
        int propVal = ((Integer) blockState.getValue(intProp)).intValue();

        if (intProp.getName() == "rotation" && isValidRotationProperty()) {
            for (int i = 0; i <= 3; i++) {
                propVal = RotationHelper.rotateInteger(propVal, 0, 15, ccw);
            }
            blockState = blockState.withProperty(intProp, propVal);
        }

        return blockState;
    }

    boolean isValidRotationProperty() {
        PropertyInteger intProp = (PropertyInteger) (Object) this;

        if (intProp.getAllowedValues().contains(0) && intProp.getAllowedValues().contains(15)
                && (!intProp.getAllowedValues().contains(-1) && !intProp.getAllowedValues().contains(16)))
            return true;

        return false;
    }
}
