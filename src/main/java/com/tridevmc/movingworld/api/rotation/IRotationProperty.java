package com.tridevmc.movingworld.api.rotation;

import net.minecraft.block.BlockState;
import net.minecraft.state.Property;

/**
 * If a block is for whatever reason not using one of the pre-made rotation systems Minecraft
 * provides they can make their custom property extend this so MovingWorld will know how to rotate
 * it.
 */
public interface IRotationProperty<T extends Comparable<T>> {

    /**
     * Rotates the specified block state without effecting the world.
     *
     * @param blockState the blockstate to rotate.
     * @param ccw        counterClockwise?
     * @return The blockstate post rotation.
     */
    <V extends T> BlockState rotate(Property<T> property, BlockState blockState, boolean ccw);

}
