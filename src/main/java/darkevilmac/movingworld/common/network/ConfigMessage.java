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
    public MainConfig config;

    public ConfigMessage() {
        config = null;
    }

    public ConfigMessage(MainConfig cfg) {
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
                String jsonCfg = builder.create().toJson(MovingWorld.instance.getNetworkConfig(), MainConfig.class);
                ByteBufUtils.writeUTF8String(buf, jsonCfg);
            } else {
                ByteBufUtils.writeUTF8String(buf, "N");
            }
        }
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf buf, EntityPlayer player, Side side) {
        if (FMLCommonHandler.instance().getSide().isClient() && !buf.toString().contains("Empty")) {
            String msg = ByteBufUtils.readUTF8String(buf);
            if (!msg.equals("N")) {
                config = new Gson().fromJson(msg, MainConfig.class);
            } else config = null;
        }
    }

    @Override
    public void handleClientSide(EntityPlayer player) {
        ((ClientProxy) MovingWorld.proxy).syncedConfig = this.config;
    }

    @Override
    public void handleServerSide(EntityPlayer player) {

    }
}
