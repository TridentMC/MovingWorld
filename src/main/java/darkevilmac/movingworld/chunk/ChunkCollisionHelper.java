package darkevilmac.movingworld.chunk;

import darkevilmac.movingworld.chunk.mobilechunk.MobileChunk;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;

import java.util.List;

public class ChunkCollisionHelper {

    /**
     * @param entity
     * @param mobileChunk
     * @param destination
     * @return cancel further method.
     */
    public static boolean onEntityMove(Entity entity, MobileChunk mobileChunk, Vec3 destination) {
        boolean cancel = false;

        AxisAlignedBB entityBoundingBox = entity.getEntityBoundingBox();

        double startX = entity.posX;
        double startY = entity.posY;
        double startZ = entity.posZ;

        double destX = destination.xCoord;
        double destY = destination.yCoord;
        double destZ = destination.zCoord;

        List<AxisAlignedBB> intersectingBoxes = mobileChunk.getCollidingBoundingBoxes(entityBoundingBox);

        return cancel;
    }
}
