package darkevilmac.movingworld.common.core;


import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import java.util.UUID;

public interface IMovingWorld {

    /**
     * The same as translateToWorldSpace but backwards.
     */
    BlockPos translateToBlockSpace(Vec3 worldSpace);

    /**
     * Convert a position from internal block space like 12, 32, 16 to space in the parent world, like 50.2, 48.7645, 9.54
     *
     * @param blockSpace The position in the internal blockspace.
     * @return The position converted to a Vec3 in the parent world.
     */
    Vec3 translateToWorldSpace(BlockPos blockSpace);

    /**
     * Always 0, 0, 0
     */
    BlockPos min();

    /**
     * The max coordinate of the world, the minimum starts at 0, 0, 0 so the max could be something like 12, 32, 16
     */
    BlockPos max();

    /**
     * The current position of this world in the parent world.
     *
     * @return
     */
    Vec3 worldTranslation();

    /**
     * The scale of this world, this changes how it's rendered in the world as well as how it collides with players.
     *
     * @return
     */
    Vec3 scale();

    /**
     * The current rotation of this world.
     *
     * @return
     */
    Vec3 rotation();

    /**
     * The parent world.
     */
    World parent();

    /**
     * Unique identifier for this moving world, corresponds to the directory inside the parent world's folder.
     * <p/>
     * For example: New World/SubWorlds/UUID
     *
     * @return the id.
     */
    UUID identifier();

    /**
     * Move a set amount of distance.
     *
     * @param teleport are we moving from our position a set amount of blocks or are we just teleporting to the position in space?
     * @return whether or not we hit something on the way.
     */
    boolean move(Vec3 move, boolean teleport);

    IMovingWorld setParent(World world);

    IMovingWorld setIdentifier(UUID id);

}
