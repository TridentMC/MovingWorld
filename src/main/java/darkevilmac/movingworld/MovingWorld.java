package darkevilmac.movingworld;

import darkevilmac.movingworld.config.MainConfig;
import darkevilmac.movingworld.mrot.MetaRotations;
import darkevilmac.movingworld.network.MovingWorldMessageToMessageCodec;
import darkevilmac.movingworld.network.MovingWorldPacketHandler;
import darkevilmac.movingworld.network.NetworkUtil;
import darkevilmac.movingworld.proxy.CommonProxy;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.apache.logging.log4j.Logger;

import java.io.File;

@Mod(modid = MovingWorld.MOD_ID, name = MovingWorld.MOD_NAME, version = MovingWorld.MOD_VERSION)
public class MovingWorld {
    public static final String MOD_ID = "MovingWorld";
    public static final String MOD_VERSION = "1.7.10-1.7.12";
    public static final String MOD_NAME = "Moving World";

    @Mod.Instance(MOD_ID)
    public static MovingWorld instance;

    @SidedProxy(clientSide = "darkevilmac.movingworld.proxy.ClientProxy", serverSide = "darkevilmac.movingworld.proxy.CommonProxy")
    public static CommonProxy proxy;

    public static Logger logger;

    public MetaRotations metaRotations;
    public MainConfig mConfig;
    public NetworkUtil network;

    public MovingWorld() {
        //metaRotations = new MetaRotations();
        network = new NetworkUtil();
    }


    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        logger = e.getModLog();
        File configFolder =  new File(e.getModConfigurationDirectory(), "MovingWorld");
        File mConfigFile = new File(configFolder, "Main.cfg");
        mConfig = new MainConfig(new Configuration(mConfigFile));
        mConfig.loadAndSave();

        //metaRotations.setConfigDirectory(e.getModConfigurationDirectory());
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent e) {
        network.channels = NetworkRegistry.INSTANCE.newChannel(MOD_ID, new MovingWorldMessageToMessageCodec(), new MovingWorldPacketHandler());
        proxy.registerRenderers();
        mConfig.assemblePriorityConfig.loadAndSaveInit();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent e) {
        //metaRotations.readMetaRotationFiles();
        mConfig.assemblePriorityConfig.loadAndSavePostInit();
    }

}