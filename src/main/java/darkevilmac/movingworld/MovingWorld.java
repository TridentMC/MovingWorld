package darkevilmac.movingworld;

import darkevilmac.movingworld.client.ClientProxy;
import darkevilmac.movingworld.common.CommonProxy;
import darkevilmac.movingworld.common.config.MainConfig;
import darkevilmac.movingworld.common.mrot.MetaRotations;
import darkevilmac.movingworld.common.network.MovingWorldMessageToMessageCodec;
import darkevilmac.movingworld.common.network.MovingWorldPacketHandler;
import darkevilmac.movingworld.common.network.NetworkUtil;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.apache.logging.log4j.Logger;

import java.io.File;

@Mod(modid = MovingWorld.MOD_ID, name = MovingWorld.MOD_NAME, version = MovingWorld.MOD_VERSION, guiFactory = MovingWorld.MOD_GUIFACTORY)
public class MovingWorld {
    public static final String MOD_ID = "MovingWorld";
    public static final String MOD_VERSION = "@MOVINGWORLDVER@";
    public static final String MOD_NAME = "Moving World";
    public static final String MOD_GUIFACTORY = "darkevilmac.movingworld.client.gui.MovingWorldGUIFactory";

    @Mod.Instance(MOD_ID)
    public static MovingWorld instance;

    @SidedProxy(clientSide = "darkevilmac.movingworld.client.ClientProxy", serverSide = "darkevilmac.movingworld.common.CommonProxy")
    public static CommonProxy proxy;

    public static Logger logger;

    public MetaRotations metaRotations;
    private MainConfig localConfig;
    public NetworkUtil network;

    public MovingWorld() {
        network = new NetworkUtil();
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        logger = e.getModLog();
        File configFolder = new File(e.getModConfigurationDirectory(), "MovingWorld");
        File mConfigFile = new File(configFolder, "Main.cfg");
        localConfig = new MainConfig(new Configuration(mConfigFile));
        localConfig.loadAndSave();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent e) {
        localConfig.postLoad();
        network.channels = NetworkRegistry.INSTANCE.newChannel(MOD_ID, new MovingWorldMessageToMessageCodec(), new MovingWorldPacketHandler());
        proxy.registerRenderers();
        localConfig.getShared().assemblePriorityConfig.loadAndSaveInit();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent e) {
        localConfig.getShared().assemblePriorityConfig.loadAndSavePostInit();
    }

    public MainConfig getNetworkConfig() {
        if (FMLCommonHandler.instance().getSide().isClient()) {
            if (((ClientProxy) proxy).syncedConfig != null)
                return ((ClientProxy) proxy).syncedConfig;
        }
        return localConfig;
    }

    public MainConfig getLocalConfig() {
        return localConfig;
    }
}