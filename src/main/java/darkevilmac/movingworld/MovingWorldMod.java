package darkevilmac.movingworld;

import darkevilmac.movingworld.common.CommonProxy;
import darkevilmac.movingworld.common.network.MovingWorldMessageToMessageCodec;
import darkevilmac.movingworld.common.network.MovingWorldPacketHandler;
import darkevilmac.movingworld.common.network.NetworkUtil;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Mod(modid = MovingWorldMod.MOD_ID, name = MovingWorldMod.MOD_NAME, version = MovingWorldMod.MOD_VERSION, guiFactory = MovingWorldMod.MOD_GUIFACTORY)
public class MovingWorldMod {
    public static final String MOD_ID = "MovingWorld";
    public static final String MOD_VERSION = "@MOVINGWORLDVER@";
    public static final String MOD_NAME = "Moving World";
    public static final String MOD_GUIFACTORY = "darkevilmac.movingworld.client.gui.MovingWorldGUIFactory";

    @Mod.Instance(MOD_ID)
    public static MovingWorldMod instance;

    @SidedProxy(clientSide = "darkevilmac.movingworld.client.ClientProxy", serverSide = "darkevilmac.movingworld.common.CommonProxy")
    public static CommonProxy proxy;

    public static Logger logger;

    public NetworkUtil network;

    public MovingWorldMod() {
        network = new NetworkUtil();
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        logger = e.getModLog();
        File configFolder = new File(e.getModConfigurationDirectory(), "MovingWorld");
        File mConfigFile = new File(configFolder, "Main.cfg");
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent e) {
        network.channels = NetworkRegistry.INSTANCE.newChannel(MOD_ID, new MovingWorldMessageToMessageCodec(), new MovingWorldPacketHandler());
        proxy.registerRenderers();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent e) {
    }

    @SubscribeEvent
    public void onAsieChat(ServerChatEvent event) {
        if (event.username.contains("asie")) {
            List<Character> message = new ArrayList<Character>();
            for (char c : event.message.toCharArray()) {
                message.add(c);
            }
            Collections.shuffle(message);
            String newMessage = "";
            for (char c : message) {
                newMessage += c;
            }
            event.setComponent(new ChatComponentText(event.username + ": " + newMessage));
        }
    }
}