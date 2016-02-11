package darkevilmac.movingworld.common.core;

import darkevilmac.movingworld.MovingWorldMod;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Stores what dimensions are MovingWorlds so that we know how to load them.
 */

public class MovingWorldManager {

    /**
     * Key = Parent DimID, Val = MovingWorlds.
     */
    public static HashMap<Integer, ArrayList<Integer>> movingWorldIDS = new HashMap<Integer, ArrayList<Integer>>();

    public static void registerMovingWorld(World parent, World child) {
        registerMovingWorld(parent.provider.getDimensionId(), child.provider.getDimensionId());
    }

    public static void registerMovingWorld(int parentID, int childID) {
        if (!movingWorldIDS.containsKey(parentID)) {
            ArrayList<Integer> children = new ArrayList<Integer>();
            children.add(childID);
            movingWorldIDS.put(parentID, children);
        } else {
            ArrayList<Integer> keys = movingWorldIDS.get(parentID);
            if (!keys.contains(childID))
                keys.add(childID);
            movingWorldIDS.put(parentID, keys);
        }
    }

    /**
     * Tries to remove a MovingWorld with the parent and child specified.
     *
     * @param parent
     * @param child
     * @return true if successful, false if no parent was found to remove from.
     */
    public static boolean removeMovingWorld(World parent, World child) {
        return removeMovingWorld(parent.provider.getDimensionId(), child.provider.getDimensionId());
    }

    /**
     * Tries to remove a MovingWorld with the parent and child ids specified.
     *
     * @return true if successful, false if no parent was found to remove from.
     */
    public static boolean removeMovingWorld(int parentID, Integer childID) {
        if (movingWorldIDS.containsKey(parentID)) {
            movingWorldIDS.get(parentID).remove(childID);
            return true;
        }
        return false;
    }

    /**
     * Initializes all MovingWorlds sourced from the config file, on world load.
     */
    public static void initDims(World parent) {
        if (!movingWorldIDS.containsKey(parent.provider.getDimensionId()))
            return;

        for (Integer child : movingWorldIDS.get(parent.provider.getDimensionId())) {
            MovingWorldMod.movingWorldFactory.loadMovingWorld(parent, child);
        }
    }
}
