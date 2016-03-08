package darkevilmac.movingworld.common.network;

import darkevilmac.movingworld.MovingWorld;
import darkevilmac.movingworld.common.entity.EntityMovingWorld;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.relauncher.Side;

public abstract class EntityMovingWorldMessage extends MovingWorldMessage {

    public EntityMovingWorld movingWorld;

    public EntityMovingWorldMessage() {
        movingWorld = null;
    }

    public EntityMovingWorldMessage(EntityMovingWorld movingWorld) {
        this.movingWorld = movingWorld;
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf buf, Side side) {
        buf.writeInt(movingWorld.getEntityId());
        buf.writeInt(movingWorld.worldObj.provider.getDimensionId());
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf buf, Side side) {
        int entityID = buf.readInt();
        int dimID = buf.readInt();
        World theWorld = DimensionManager.getWorld(dimID);
        if (theWorld == null) {
            MovingWorld.logger.warn("Unable to find dimension with ID " + dimID);
            return;
        }

        Entity entity = theWorld.getEntityByID(entityID);
        if (entity instanceof EntityMovingWorld) {
            movingWorld = (EntityMovingWorld) entity;
        } else {
            MovingWorld.logger.warn("Unable to find movingworld entity with ID " + entityID);
        }
    }
}
