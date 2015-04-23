package darkevilmac.movingworld.network;

import cpw.mods.fml.common.network.FMLIndexedMessageToMessageCodec;
import cpw.mods.fml.common.network.NetworkRegistry;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.world.chunk.Chunk;

public class MovingWorldMessageToMessageCodec extends FMLIndexedMessageToMessageCodec<MovingWorldMessage> {

    private int index;

    public MovingWorldMessageToMessageCodec() {
        index = 1;
        addDiscriminator(ChunkBlockUpdateMessage.class);
        addDiscriminator(FarInteractMessage.class);
        addDiscriminator(MovingWorldClientActionMessage.class);
        addDiscriminator(RequestMovingWorldDataMessage.class);
        addDiscriminator(TileEntitiesMessage.class);
    }

    public FMLIndexedMessageToMessageCodec<MovingWorldMessage> addDiscriminator(Class<? extends MovingWorldMessage> type) {
        FMLIndexedMessageToMessageCodec<MovingWorldMessage> ret = super.addDiscriminator(index, type);
        index++;
        return ret;
    }


    @Override
    public void encodeInto(ChannelHandlerContext ctx, MovingWorldMessage msg, ByteBuf target) throws Exception {
        msg.encodeInto(ctx, target);
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf source, MovingWorldMessage msg) {
        msg.decodeInto(ctx, source, ((NetHandlerPlayServer) ctx.channel().attr(NetworkRegistry.NET_HANDLER).get()).playerEntity);
    }
}
