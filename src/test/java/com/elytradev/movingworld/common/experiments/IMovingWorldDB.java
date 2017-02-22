package com.elytradev.movingworld.common.experiments;

import net.minecraft.world.World;

/**
 * Stores known Movingworlds, primarily used on the client to help handle the need for a parent client world.
 */
public interface IMovingWorldDB {

    /**
     * Gets the world associated with the dimension provided, returns null if no such world exists.
     *
     * @param dim the dimension id of the world.
     * @return the world if present.
     */
    World getWorldFromDim(int dim);

    /**
     * Adds a world to the db if not already present, return true if added, false if was present.
     *
     * @param dim   the dimension id of the world.
     * @param world the world object.
     * @return true if added, false if already present.
     */
    boolean addWorldForDim(int dim, World world);

}