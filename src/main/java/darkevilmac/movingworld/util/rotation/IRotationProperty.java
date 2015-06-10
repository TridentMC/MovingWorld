package darkevilmac.movingworld.util.rotation;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

/**
 * If a block is for whatever reason not using one of the pre-made rotation systems Minecraft provides they can make
 * their custom property extend this so MovingWorld will know how to rotate it.
 */
public interface IRotationProperty {

    /**
     * Rotates the specified block state without effecting the world.
     *
     * @param world world.
     * @param pos   position in world.
     * @param ccw   counterClockwise?
     * @return The blockstate of the rotated block.
     */
    IBlockState rotateBlock(World world, BlockPos pos, boolean ccw);

}
