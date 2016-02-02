package darkevilmac.movingworld.common.baseclasses.world;

import darkevilmac.movingworld.common.core.IMovingWorld;
import darkevilmac.movingworld.common.core.assembly.BlockMap;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.UUID;

public interface IWorldMixin {

    void onConstruct();

    boolean isMovingWorld();

    IMovingWorld createMovingWorld(UUID uuid, BlockMap contents);

    Pair<IMovingWorld, UUID> createMovingWorld(BlockMap contents);

    /**
     * Uses the uuid provided to get NBT data of a blockmap.
     *
     * @return if that uuid was a valid tag.
     */
    boolean createMovingWorldFromUUID(UUID uuid);

    List<IMovingWorld> getMovingWorlds();

}
