package darkevilmac.movingworld.common.network.sync.request;

import darkevilmac.movingworld.common.network.MovingWorldMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;

/**
 * A client sends this when a chunk loads on their end.
 */
public class ClientChunkEventMessage extends MovingWorldMessage{
    @Override
    public boolean onMainThread() {
        return false;
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf buf, Side side) {

    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf buf, Side side) {

    }

    @Override
    public void handleClientSide(EntityPlayer player) {

    }

    @Override
    public void handleServerSide(EntityPlayer player) {

    }
}
