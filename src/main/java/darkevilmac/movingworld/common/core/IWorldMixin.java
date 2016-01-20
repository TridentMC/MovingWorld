package darkevilmac.movingworld.common.core;

import net.minecraft.tileentity.TileEntity;

import java.util.List;

public interface IWorldMixin {

    List<TileEntity> getAddedTileEntityList();

    List<TileEntity> getTileEntitiesToBeRemoved();

    boolean isProcessingLoadedTiles();
    void setProcessingLoadedTiles(boolean flag);

    /**
     * Get's all Subworlds inside the world.
     *
     * @return
     */
    List<SubWorld> getSubWorlds();

    /**
     * Get a subworld via it's unique ID, often the same as the Entity ID.
     *
     * @return SubWorld associated with id
     */
    SubWorld getSubWorldById(String id);


}
