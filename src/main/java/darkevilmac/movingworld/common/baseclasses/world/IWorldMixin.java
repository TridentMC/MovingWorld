package darkevilmac.movingworld.common.baseclasses.world;

import darkevilmac.movingworld.common.core.IMovingWorld;
import darkevilmac.movingworld.common.core.assembly.BlockMap;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public interface IWorldMixin {

    void onConstruct();

    boolean isMovingWorld();

    IMovingWorld createMovingWorld(Integer id, BlockMap contents);

    Pair<IMovingWorld, Integer> createMovingWorld(BlockMap contents);

    /**
     * Uses the uuid provided to get NBT data of a blockmap.
     *
     * @return if that uuid was a valid tag.
     */
    boolean createMovingWorldFromID(Integer id);

    List<IMovingWorld> getMovingWorlds();

}
