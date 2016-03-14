package darkevilmac.movingworld.common.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import darkevilmac.movingworld.MovingWorld;
import darkevilmac.movingworld.client.ClientProxy;
import darkevilmac.movingworld.common.config.MainConfig;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;

public class ConfigMessage extends MovingWorldMessage {

    public MainConfig.SharedConfig config;

    public ConfigMessage() {
        config = null;
    }

    public ConfigMessage(MainConfig.SharedConfig cfg) {
        this.config = cfg;
    }

    @Override
    public boolean onMainThread() {
        return true;
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf buf, Side side) {
        if (FMLCommonHandler.instance().getSide().isServer()) {
            if (config != null) {
                GsonBuilder builder = new GsonBuilder();
                String jsonCfg = builder.create().toJson(MovingWorld.instance.getNetworkConfig().getShared(), MainConfig.SharedConfig.class);
                ByteBufUtils.writeUTF8String(buf, jsonCfg);
            } else {
                ByteBufUtils.writeUTF8String(buf, "N");
            }
        }
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf buf, Side side) {
        if (FMLCommonHandler.instance().getSide().isClient() && !buf.toString().contains("Empty")) {
            String msg = ByteBufUtils.readUTF8String(buf);
            if (!msg.equals("N")) {
                config = new Gson().fromJson(msg, MainConfig.SharedConfig.class);
            } else config = null;
        }
    }

    @Override
    public void handleClientSide(EntityPlayer player) {
        if (config != null && MovingWorld.proxy != null && MovingWorld.proxy instanceof ClientProxy) {
            ((ClientProxy) MovingWorld.proxy).syncedConfig = MovingWorld.instance.getLocalConfig();
            ((ClientProxy) MovingWorld.proxy).syncedConfig.setShared(config);
        }
    }

    @Override
    public void handleServerSide(EntityPlayer player) {

    }
}
