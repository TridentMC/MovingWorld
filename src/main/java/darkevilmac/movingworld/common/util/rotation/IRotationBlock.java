package darkevilmac.movingworld.common.util.rotation;

import darkevilmac.movingworld.common.chunk.LocatedBlock;


/**
 * The same as IRotationProperty but applies if you're using one of Minecraft's properties that aren't designed for rotation
 * like the banner's rotation integer.
 * <p/>
 * NOTE: If you're doing all the rotations in this class including your PropertyDirection rotations for whatever reason, make sure fullRotation returns true, if not return false.
 */
public interface IRotationBlock {

    /**
     * Rotates the specified block state without effecting the world.
     *
     * @param ccw counterClockwise?
     * @return The blockstate post rotation.
     */
    LocatedBlock rotate(LocatedBlock locatedBlock, boolean ccw);

    boolean fullRotation();

}
