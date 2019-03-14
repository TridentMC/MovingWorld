package com.elytradev.movingworld;

import com.elytradev.movingworld.client.ClientProxy;
import com.elytradev.movingworld.common.CommonProxy;
import com.elytradev.movingworld.common.config.MovingWorldConfig;
import com.elytradev.movingworld.common.config.priority.MovingWorldAssemblePriorityConfig;
import com.tridevmc.compound.config.CompoundConfig;
import com.tridevmc.compound.network.core.CompoundNetwork;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.elytradev.movingworld.MovingWorldMod.MOD_ID;

@Mod(MOD_ID)
public class MovingWorldMod {
    public static final String MOD_ID = "movingworld";

    public static MovingWorldMod INSTANCE;
    public static CommonProxy PROXY;
    public static Logger LOG = LogManager.getLogger("MovingWorld");

    public static MovingWorldConfig CONFIG;
    public static MovingWorldAssemblePriorityConfig ASSEMBLY_CONFIG;

    public MovingWorldMod() {
        MovingWorldMod.INSTANCE = this;
        PROXY = DistExecutor.runForDist(() -> ClientProxy::new, () -> CommonProxy::new);
        MinecraftForge.EVENT_BUS.register(PROXY);

        FMLJavaModLoadingContext loadingContext = FMLJavaModLoadingContext.get();
        loadingContext.getModEventBus().addListener(this::onSetup);
    }

    public void onSetup(FMLCommonSetupEvent e) {
        ModContainer container = ModLoadingContext.get().getActiveContainer();
        CompoundNetwork.createNetwork(container, "movingworld");

        CONFIG = CompoundConfig.of(MovingWorldConfig.class, container, "movingworld-main.toml");
        ASSEMBLY_CONFIG = CompoundConfig.of(MovingWorldAssemblePriorityConfig.class, container, "movingworld-priority.toml");
    }
}