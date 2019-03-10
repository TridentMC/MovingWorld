package com.elytradev.movingworld;

import com.elytradev.movingworld.client.ClientProxy;
import com.elytradev.movingworld.common.CommonProxy;
import com.elytradev.movingworld.common.config.MainConfig;
import com.elytradev.movingworld.common.network.MovingWorldNetworking;
import com.tridevmc.compound.network.core.CompoundNetwork;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.Logger;

import java.io.File;

@Mod(modid = MovingWorldMod.MOD_ID, name = MovingWorldMod.MOD_NAME, version = MovingWorldMod.MOD_VERSION, guiFactory = MovingWorldMod.MOD_GUIFACTORY)
public class MovingWorldMod {
    public static final String MOD_ID = "movingworld";
    public static final String MOD_VERSION = "@MOVINGWORLDVER@";
    public static final String MOD_NAME = "Moving World";
    public static final String MOD_GUIFACTORY = "com.elytradev.movingworld.client.gui.MovingWorldGUIFactory";

    @Mod.Instance(MOD_ID)
    public static MovingWorldMod INSTANCE;
    @SidedProxy(clientSide = "com.elytradev.movingworld.client.ClientProxy", serverSide = "com.elytradev.movingworld.common.CommonProxy")
    public static CommonProxy PROXY;
    public static Logger LOG;

    private MainConfig localConfig;

    public MovingWorldMod() {
        MovingWorldMod.INSTANCE = this;
        PROXY = DistExecutor.runForDist(() -> ClientProxy::new, () -> CommonProxy::new);

        FMLJavaModLoadingContext loadingContext = FMLJavaModLoadingContext.get();
        loadingContext.getModEventBus().addListener(this::onSetup);
    }

    public void onSetup(FMLCommonSetupEvent e) {
        CompoundNetwork.createNetwork("movingworld", "");
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        MinecraftForge.EVENT_BUS.register(PROXY);
        LOG = e.getModLog();
        File configFolder = new File(e.getModConfigurationDirectory(), "MovingWorld");
        File mConfigFile = new File(configFolder, "Main.cfg");
        localConfig = new MainConfig(new Configuration(mConfigFile));
        localConfig.loadAndSave();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent e) {
        localConfig.postLoad();
        CompoundNetwork.createNetwork("movingworld", "1");
        localConfig.getShared().assemblePriorityConfig.loadAndSaveInit();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent e) {
        localConfig.getShared().assemblePriorityConfig.loadAndSavePostInit();
    }

    public MainConfig getNetworkConfig() {
        if (FMLCommonHandler.instance().getSide().isClient()) {
            if (((ClientProxy) PROXY).syncedConfig != null)
                return ((ClientProxy) PROXY).syncedConfig;
        }
        return localConfig;
    }

    public MainConfig getLocalConfig() {
        return localConfig;
    }
}