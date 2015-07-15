package darkevilmac.movingworld.common.network;

import darkevilmac.movingworld.common.entity.EntityMovingWorld;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;

public class FarInteractMessage extends EntityMovingWorldMessage {
    public FarInteractMessage() {
        super();
    }

    public FarInteractMessage(EntityMovingWorld movingWorld) {
        super(movingWorld);
    }

    @Override
    public boolean onMainThread() {
        return false;
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
