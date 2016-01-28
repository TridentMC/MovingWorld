package darkevilmac.movingworld;

import darkevilmac.movingworld.common.CommonProxy;
import darkevilmac.movingworld.common.network.MovingWorldMessageToMessageCodec;
import darkevilmac.movingworld.common.network.MovingWorldPacketHandler;
import darkevilmac.movingworld.common.network.NetworkUtil;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.apache.logging.log4j.Logger;

import java.io.File;

/**
 * MovingWorld's Mod container. Nothing to see here, move along.
 */
@Mod(modid = MovingWorldMod.MOD_ID, name = MovingWorldMod.MOD_NAME, version = MovingWorldMod.MOD_VERSION)
public class MovingWorldMod {
    public static final String MOD_ID = "MovingWorld";
    public static final String MOD_VERSION = "@MOVINGWORLDVER@";
    public static final String MOD_NAME = "Moving World";

    @Mod.Instance(MOD_ID)
    public static MovingWorldMod instance;

    @SidedProxy(modId = MOD_ID, clientSide = "darkevilmac.movingworld.client.ClientProxy", serverSide = "darkevilmac.movingworld.common.CommonProxy")
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
        proxy.registerHandlers();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent e) {
        network.channels = NetworkRegistry.INSTANCE.newChannel(MOD_ID, new MovingWorldMessageToMessageCodec(), new MovingWorldPacketHandler());
        proxy.registerRenderers();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent e) {
    }
}