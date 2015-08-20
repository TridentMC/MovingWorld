package darkevilmac.movingworld.common.network;

import cpw.mods.fml.common.network.FMLEmbeddedChannel;
import cpw.mods.fml.common.network.FMLOutboundHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.relauncher.Side;
import io.netty.channel.ChannelFutureListener;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.EnumMap;

/**
 * Heavily based off of code from Buildcraft
 */

public class NetworkUtil {

    public EnumMap<Side, FMLEmbeddedChannel> channels;

    /**
     * Send this message to everyone.
     *
     * @param message The message to send
     */
    public void sendToAll(MovingWorldMessage message) {
        channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.ALL);
        channels.get(Side.SERVER).writeAndFlush(message).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    /**
     * Send this message to the specified player.
     *
     * @param message The message to send
     * @param player  The player to send it to
     */
    public void sendTo(MovingWorldMessage message, EntityPlayerMP player) {
        channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.PLAYER);
        channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(player);
        channels.get(Side.SERVER).writeAndFlush(message).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    /**
     * Send this message to everyone within a certain range of a point.
     *
     * @param message The message to send
     * @param point   The {@link NetworkRegistry.TargetPoint} around which to send
     */
    public void sendToAllAround(MovingWorldMessage message, NetworkRegistry.TargetPoint point) {
        channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.ALLAROUNDPOINT);
        channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(point);
        channels.get(Side.SERVER).writeAndFlush(message).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    /**
     * Send this message to everyone within the supplied dimension.
     *
     * @param message     The message to send
     * @param dimensionId The dimension id to target
     */
    public void sendToDimension(MovingWorldMessage message, int dimensionId) {
        channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.DIMENSION);
        channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(dimensionId);
        channels.get(Side.SERVER).writeAndFlush(message).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    /**
     * Send this message to the server.
     *
     * @param message The message to send
     */
    public void sendToServer(MovingWorldMessage message) {
        channels.get(Side.CLIENT).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.TOSERVER);
        channels.get(Side.CLIENT).writeAndFlush(message).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

}
