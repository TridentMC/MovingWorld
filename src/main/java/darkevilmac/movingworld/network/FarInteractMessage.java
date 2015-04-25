package darkevilmac.movingworld.network;

import cpw.mods.fml.relauncher.Side;
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
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf buf, Side side) {
        super.encodeInto(ctx, buf, side);
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf buf, EntityPlayer player, Side side) {
        super.decodeInto(ctx, buf, player, side);
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
