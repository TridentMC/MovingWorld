package darkevilmac.movingworld;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import darkevilmac.movingworld.config.MainConfig;
import darkevilmac.movingworld.mrot.MetaRotations;
import org.apache.logging.log4j.core.Logger;

import java.io.File;

@Mod(modid = MovingWorld.MOD_ID, name = MovingWorld.MOD_NAME, version = MovingWorld.MOD_VERSION)
public class MovingWorld {
    public static final String MOD_ID = "MovingWorld";
    public static final String MOD_VERSION = "1.7.10-B1";
    public static final String MOD_NAME = "Moving World";

    @Mod.Instance(MOD_ID)
    public static MovingWorld instance;

    public static Logger logger;

    public MetaRotations metaRotations;
    public MainConfig mConfig;

    public MovingWorld() {
        mConfig = new MainConfig();
        metaRotations = new MetaRotations();
    }


    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        String configFolder = e.getSuggestedConfigurationFile().toString().substring(0, e.getSuggestedConfigurationFile().toString().lastIndexOf("\\") + 1);
        File mConfigFile = new File(configFolder + "\\MovingWorld\\Main.cfg");
        mConfig.initConfig(mConfigFile);

        metaRotations.setConfigDirectory(e.getModConfigurationDirectory());
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent e) {

    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent e) {
        metaRotations.readMetaRotationFiles();
    }

}