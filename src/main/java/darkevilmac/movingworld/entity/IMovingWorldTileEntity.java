package darkevilmac.movingworld.entity;

public interface IMovingWorldTileEntity {
    /**
     * @param entityMovingWorld moving world
     * @param x                 ,
     * @param y                 ,
     * @param z                 The original tile entity coordinates.
     */
    public void setParentMovingWorld(EntityMovingWorld entityMovingWorld, int x, int y, int z);

    public void setParentMovingWorld(EntityMovingWorld entityMovingWorld);

    public EntityMovingWorld getParentMovingWorld();
}
