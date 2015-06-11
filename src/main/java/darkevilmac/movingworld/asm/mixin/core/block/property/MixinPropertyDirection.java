package darkevilmac.movingworld.asm.mixin.core.block.property;

import darkevilmac.movingworld.util.rotation.IRotationProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PropertyDirection.class)
public abstract class MixinPropertyDirection implements IRotationProperty {

    @Override
    public IBlockState rotateBlock(World world, BlockPos pos, boolean ccw) {
        IBlockState blockState = world.getBlockState(pos);
        PropertyDirection propertyDirection = (PropertyDirection) (Object) this;
        EnumFacing facing = (EnumFacing) blockState.getValue(propertyDirection);

        if (facing.getHorizontalIndex() != -1) {
            if (!ccw)
                blockState = blockState.withProperty(propertyDirection, facing.rotateY());
            else
                blockState = blockState.withProperty(propertyDirection, facing.rotateYCCW());
        }

        return blockState;
    }

}
