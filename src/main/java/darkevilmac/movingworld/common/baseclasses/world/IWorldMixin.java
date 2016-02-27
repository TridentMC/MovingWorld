package darkevilmac.movingworld.common.baseclasses.world;

import darkevilmac.movingworld.common.core.IMovingWorld;
import darkevilmac.movingworld.common.core.assembly.BlockMap;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public interface IWorldMixin {

    boolean isMovingWorld();

    IMovingWorld createMovingWorld(Integer id, BlockMap contents);

    Pair<IMovingWorld, Integer> createMovingWorld(BlockMap contents);

    boolean loadMovingWorld(World parent, Integer id);

    List<IMovingWorld> getMovingWorlds();

}
