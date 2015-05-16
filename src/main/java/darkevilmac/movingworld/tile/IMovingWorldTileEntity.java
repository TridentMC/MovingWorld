package darkevilmac.movingworld.tile;

import darkevilmac.movingworld.entity.EntityMovingWorld;
import net.minecraft.util.BlockPos;

public interface IMovingWorldTileEntity {

    void setParentMovingWorld(BlockPos pos, EntityMovingWorld entityMovingWorld);

    EntityMovingWorld getParentMovingWorld();

    void setParentMovingWorld(EntityMovingWorld entityMovingWorld);

}
