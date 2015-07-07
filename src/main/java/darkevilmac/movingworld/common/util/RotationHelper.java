package darkevilmac.movingworld.common.util;

import darkevilmac.movingworld.common.chunk.LocatedBlock;
import darkevilmac.movingworld.common.util.rotation.IRotationBlock;
import darkevilmac.movingworld.common.util.rotation.IRotationProperty;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3i;

public class RotationHelper {

    public static LocatedBlock rotateBlock(LocatedBlock locatedBlock, boolean ccw) {
        IBlockState blockState = locatedBlock.blockState;
        if (locatedBlock != null && locatedBlock.blockState != null) {
            if (blockState.getBlock() != null && blockState.getBlock() instanceof IRotationBlock) {
                locatedBlock = ((IRotationBlock) blockState.getBlock()).rotate(locatedBlock, ccw);

                if (((IRotationBlock) blockState.getBlock()).fullRotation())
                    return locatedBlock;
            }

            for (IProperty prop : (java.util.Set<IProperty>) blockState.getProperties().keySet()) {
                if (prop instanceof IRotationProperty) {
                    // Custom rotation property found.
                    IRotationProperty rotationProperty = (IRotationProperty) prop;
                    blockState = rotationProperty.rotate(blockState, ccw);
                }
            }
        }

        return new LocatedBlock(blockState, locatedBlock.tileEntity, locatedBlock.blockPos, locatedBlock.bPosNoOffset);
    }

    public static int rotateInteger(int integer, int min, int max, boolean ccw) {
        int retVal = integer;

        if (!ccw) {
            if (retVal + 1 > max)
                retVal = min;
            else
                retVal = retVal + 1;
        } else {
            if (retVal - 1 < min)
                retVal = max;
            else
                retVal = retVal - 1;
        }

        return retVal;
    }

    public static Vec3i getDirectionVec(EnumFacing facing) {
        switch (facing) {
            case DOWN:
                return new Vec3i(0, -1, 0);
            case UP:
                return new Vec3i(0, 1, 0);
            case NORTH:
                return new Vec3i(0, 0, -1);
            case SOUTH:
                return new Vec3i(0, 0, 1);
            case WEST:
                return new Vec3i(-1, 0, 0);
            case EAST:
                return new Vec3i(1, 0, 0);
        }

        return null;
    }

}
