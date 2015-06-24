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

        double startX = entity.posX;
        double startY = entity.posY;
        double startZ = entity.posZ;

        double destX = destination.xCoord;
        double destY = destination.yCoord;
        double destZ = destination.zCoord;

        List<AxisAlignedBB> hits = mobileChunk.getCollidingBoundingBoxes(entity.getEntityBoundingBox().addCoord(destX, destY, destZ));

        return cancel;
    }
}
