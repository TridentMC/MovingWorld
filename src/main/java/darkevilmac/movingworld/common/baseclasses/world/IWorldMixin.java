package darkevilmac.movingworld.common.baseclasses.world;

import darkevilmac.movingworld.common.core.IMovingWorld;
import darkevilmac.movingworld.common.core.assembly.BlockMap;

import java.util.List;
import java.util.UUID;

public interface IWorldMixin {

    boolean isMovingWorld();

    void createMovingWorld(UUID uuid, BlockMap contents);

    void createMovingWorld(BlockMap contents);

    /**
     * Uses the uuid provided to get NBT data of a blockmap.
     *
     * @return if that uuid was a valid tag.
     */
    boolean createMovingWorldFromUUID(UUID uuid);

    List<IMovingWorld> getMovingWorlds();

}
