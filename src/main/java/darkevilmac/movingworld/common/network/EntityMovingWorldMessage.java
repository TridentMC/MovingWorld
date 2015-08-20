package darkevilmac.movingworld.common.network;

import cpw.mods.fml.relauncher.Side;
import darkevilmac.movingworld.MovingWorld;
import darkevilmac.movingworld.common.entity.EntityMovingWorld;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

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
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf buf, EntityPlayer player, Side side) {
        int entityID = buf.readInt();
        Entity entity = player.worldObj.getEntityByID(entityID);
        if (entity instanceof EntityMovingWorld) {
            movingWorld = (EntityMovingWorld) entity;
        } else {
            MovingWorld.logger.warn("Unable to find movingworld entity with ID " + entityID);
        }
    }
}
