package darkevilmac.movingworld.common.tile;

import net.minecraft.util.math.BlockPos;

import darkevilmac.movingworld.common.chunk.mobilechunk.MobileChunk;
import darkevilmac.movingworld.common.entity.EntityMovingWorld;

public interface IMovingWorldTileEntity {

    void setParentMovingWorld(BlockPos pos, EntityMovingWorld entityMovingWorld);

    EntityMovingWorld getParentMovingWorld();

    void setParentMovingWorld(EntityMovingWorld entityMovingWorld);

    /**
     * Called each tick from the mobilechunk, I advise strongly against any major modifications to
     * the chunk.
     */
    void tick(MobileChunk mobileChunk);

}
