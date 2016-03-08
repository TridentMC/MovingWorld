package darkevilmac.movingworld.common.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.FMLIndexedMessageToMessageCodec;

public class MovingWorldMessageToMessageCodec extends FMLIndexedMessageToMessageCodec<MovingWorldMessage> {

    private int index;

    public MovingWorldMessageToMessageCodec() {
        index = 1;
        addDiscriminator(ChunkBlockUpdateMessage.class);
        addDiscriminator(FarInteractMessage.class);
        addDiscriminator(MovingWorldClientActionMessage.class);
        addDiscriminator(RequestMovingWorldDataMessage.class);
        addDiscriminator(TileEntitiesMessage.class);
        addDiscriminator(ConfigMessage.class);
    }

    public FMLIndexedMessageToMessageCodec<MovingWorldMessage> addDiscriminator(Class<? extends MovingWorldMessage> type) {
        FMLIndexedMessageToMessageCodec<MovingWorldMessage> ret = super.addDiscriminator(index, type);
        index++;
        return ret;
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, MovingWorldMessage msg, ByteBuf target) throws Exception {
        msg.encodeInto(ctx, target, FMLCommonHandler.instance().getSide());
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf source, MovingWorldMessage msg) {
        msg.decodeInto(ctx, source, FMLCommonHandler.instance().getSide());
    }
}
