package darkevilmac.movingworld.common.tile;

import darkevilmac.movingworld.common.entity.EntityMovingWorld;

public interface IMovingWorldTileEntity {

    /**
     * @param entityMovingWorld moving world
     * @param x                 ,
     * @param y                 ,
     * @param z                 The original tile entity coordinates.
     */
    void setParentMovingWorld(EntityMovingWorld entityMovingWorld, int x, int y, int z);

    EntityMovingWorld getParentMovingWorld();

    void setParentMovingWorld(EntityMovingWorld entityMovingWorld);

}
