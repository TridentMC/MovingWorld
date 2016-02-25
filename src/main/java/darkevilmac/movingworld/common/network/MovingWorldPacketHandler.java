package darkevilmac.movingworld.common.network;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@ChannelHandler.Sharable
public class MovingWorldPacketHandler extends SimpleChannelInboundHandler<MovingWorldMessage> {
    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final MovingWorldMessage msg) throws Exception {
        final EntityPlayer player;
        switch (FMLCommonHandler.instance().getSide()) {
            case CLIENT: {
                player = this.getClientPlayer();
                if (!msg.onMainThread()) {
                    msg.handleClientSide(player);
                } else {
                    Minecraft.getMinecraft().addScheduledTask(new Runnable() {
                        @Override
                        public void run() {
                            msg.handleClientSide(player);
                        }
                    });
                }
                break;
            }
            case SERVER: {
                player = getServerPlayer(ctx);
                if (!msg.onMainThread()) {
                    msg.handleServerSide(player);
                } else {
                    if (player != null && player.worldObj != null && !player.worldObj.isRemote) {
                        final WorldServer worldServer = (WorldServer) player.worldObj;
                        worldServer.addScheduledTask(new Runnable() {
                            @Override
                            public void run() {
                                msg.handleServerSide(player);
                            }
                        });
                    }
                }
                break;
            }
        }
    }

    @SideOnly(Side.CLIENT)
    private EntityPlayer getClientPlayer() {
        return Minecraft.getMinecraft().thePlayer;
    }

    private EntityPlayer getServerPlayer(ChannelHandlerContext ctx) {
        INetHandler netHandler = ctx.channel().attr(NetworkRegistry.NET_HANDLER).get();
        return ((NetHandlerPlayServer) netHandler).playerEntity;
    }
}