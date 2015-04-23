package darkevilmac.movingworld.network;

import darkevilmac.movingworld.entity.EntityMovingWorld;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;

public class FarInteractMessage extends EntityMovingWorldMessage {
    public FarInteractMessage() {
        super();
    }

    public FarInteractMessage(EntityMovingWorld movingWorld) {
        super(movingWorld);
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf buf) {
        super.encodeInto(ctx, buf);
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf buf, EntityPlayer player) {
        super.decodeInto(ctx, buf, player);
    }

    @Override
    public void handleClientSide(EntityPlayer player) {
    }

    @Override
    public void handleServerSide(EntityPlayer player) {
        if (movingWorld != null) {
            player.interactWith(movingWorld);
        }
    }

}
