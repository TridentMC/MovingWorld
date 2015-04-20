package darkevilmac.movingworld.network;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.FMLIndexedMessageToMessageCodec;
import cpw.mods.fml.common.network.NetworkRegistry;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;


public class MovingWorldChannelHandler extends FMLIndexedMessageToMessageCodec<MovingWorldMessage> {

    @Override
    public void encodeInto(ChannelHandlerContext ctx, MovingWorldMessage msg, ByteBuf target) throws Exception {
        msg.encodeInto(ctx, target);
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf source, MovingWorldMessage msg) {
        msg.decodeInto(ctx, source, ((NetHandlerPlayServer) ctx.channel().attr(NetworkRegistry.NET_HANDLER).get()).playerEntity);
    }

    @Sharable
    public class MovingWorldPacketHandler extends SimpleChannelInboundHandler<MovingWorldMessage> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, MovingWorldMessage msg) {
            try {
                INetHandler netHandler = ctx.channel().attr(NetworkRegistry.NET_HANDLER).get();
                EntityPlayer player = ((NetHandlerPlayServer) netHandler).playerEntity;

                switch (FMLCommonHandler.instance().getEffectiveSide()) {
                    case CLIENT:
                        msg.handleClientSide(player);
                        break;
                    case SERVER:
                        msg.handleServerSide(player);
                        break;
                    default:
                        break;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
