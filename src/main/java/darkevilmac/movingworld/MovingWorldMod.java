package darkevilmac.movingworld;

import darkevilmac.movingworld.common.CommonProxy;
import darkevilmac.movingworld.common.MovingWorldDimensionConfig;
import darkevilmac.movingworld.common.core.MovingWorldProvider;
import darkevilmac.movingworld.common.core.factory.CommonMovingWorldFactory;
import darkevilmac.movingworld.common.network.MovingWorldMessageToMessageCodec;
import darkevilmac.movingworld.common.network.MovingWorldPacketHandler;
import darkevilmac.movingworld.common.network.NetworkUtil;
import darkevilmac.movingworld.common.test.BlockMovingWorldCreator;
import net.minecraft.block.material.Material;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
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

    public static CommonMovingWorldFactory movingWorldFactory;

    public NetworkUtil network;

    public static MovingWorldDimensionConfig dimensionConfig;

    private File confFolder;

    public MovingWorldMod() {
        network = new NetworkUtil();
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        logger = e.getModLog();
        this.confFolder = new File(e.getModConfigurationDirectory(), "MovingWorld");

        proxy.registerHandlers();

        MovingWorldProvider.PROVIDERID = 64;
        DimensionManager.registerProviderType(64, MovingWorldProvider.class, true);

        if (MOD_VERSION.equals("@MOVINGWORLDVER@")) {
            // In dev environment initialize some test stuffs.

            GameRegistry.registerBlock(new BlockMovingWorldCreator(Material.cake), "movingWorldCreator");
        }
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent e) {
        proxy.initEvent(e.getModState());
        network.channels = NetworkRegistry.INSTANCE.newChannel(MOD_ID, new MovingWorldMessageToMessageCodec(), new MovingWorldPacketHandler());
        proxy.registerRenderers();

        proxy.setupFactory();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent e) {
        proxy.initEvent(e.getModState());
    }

    @Mod.EventHandler
    public void onServerAboutToStart(FMLServerAboutToStartEvent e) {
        dimensionConfig = new MovingWorldDimensionConfig(new File(confFolder, ".dim" + File.separator + e.getServer().getWorldName()));

        dimensionConfig.loadDimensionManager();
    }

    @Mod.EventHandler
    public void onServerStopEvent(FMLServerStoppedEvent e) {
        dimensionConfig.saveDimensionManager();
    }
}