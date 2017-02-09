package com.elytradev.movingworld.api.rotation;

import com.elytradev.movingworld.common.chunk.LocatedBlock;


/**
 * The same as IRotationProperty but applies if you're using one of Minecraft's properties that
 * aren't designed for rotation like the banner's rotation integer.
 */
public interface IRotationBlock {

    /**
     * Rotates the specified block state without effecting the world.
     *
     * @param ccw counterClockwise?
     * @return The blockstate post rotation.
     */
    LocatedBlock rotate(LocatedBlock locatedBlock, boolean ccw);

    /**
     * @return If you're doing all the rotations in this class including your PropertyDirection
     * rotations for whatever reason, return true, if not return false.
     */
    boolean fullRotation();

}
