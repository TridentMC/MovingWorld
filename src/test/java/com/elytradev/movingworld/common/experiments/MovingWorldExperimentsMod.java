package com.elytradev.movingworld.common.experiments;

import com.elytradev.movingworld.common.experiments.entity.EntityMobileRegion;
import com.elytradev.movingworld.common.experiments.interact.MWPlayerInteractionManager;
import com.elytradev.movingworld.common.experiments.network.MovingWorldExperimentsNetworking;
import com.elytradev.movingworld.common.experiments.region.MobileRegion;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.ChunkPos;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import org.apache.logging.log4j.Logger;

/**
 * Created by darkevilmac on 2/9/2017.
 */
@Mod(modid = MovingWorldExperimentsMod.MOD_ID, name = MovingWorldExperimentsMod.MOD_NAME, version = MovingWorldExperimentsMod.MOD_VERSION)
public class MovingWorldExperimentsMod {

    public static final String MOD_ID = "movingworld-experiments";
    public static final String MOD_VERSION = "-1";
    public static final String MOD_NAME = "MovingWorld Experiments";
    public static final String NETWORK_CHANNEL_NAME = "MVW-EXP";

    @SidedProxy(modId = "movingworld-experiments", clientSide = "com.elytradev.movingworld.client.experiments.ClientProxy", serverSide = "com.elytradev.movingworld.common.experiments.CommonProxy")
    public static CommonProxy modProxy;

    @Mod.Instance(MOD_ID)
    public static MovingWorldExperimentsMod instance;
    public static Logger logger;
    public MovingWorldInitHandler initHandler;


    @Mod.EventHandler
    public void onPreInit(FMLPreInitializationEvent e) {
        logger = e.getModLog();
        initHandler = new MovingWorldInitHandler();
        MinecraftForge.EVENT_BUS.register(initHandler);
        MinecraftForge.EVENT_BUS.register(MWPlayerInteractionManager.class);
        modProxy.setupDBS();
        modProxy.registerRenders();

        ForgeChunkManager.setForcedChunkLoadingCallback(instance, (tickets, world) -> {
            for (ForgeChunkManager.Ticket t : tickets) {
                MobileRegion region = MobileRegion.getRegionFor(t.getModData().getCompoundTag("Region"));

                for (int cX = region.regionMin.x; cX < region.regionMax.x; cX++) {
                    for (int cZ = region.regionMin.z; cZ < region.regionMax.z; cZ++) {
                        ForgeChunkManager.forceChunk(t, new ChunkPos(cX, cZ));
                    }
                }
            }
        });
        //noinspection deprecation
        //TODO: Register debug item.
        //GameRegistry.registerWithItem(new BlockDebug(Material.TNT, MapColor.TNT));
    }

    @Mod.EventHandler
    public void onInit(FMLInitializationEvent e) {
        MovingWorldExperimentsNetworking.init();
        EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID, "mobileregion"), EntityMobileRegion.class, "mobileregion", 1, this, 64, 5, true);
    }

    @Mod.EventHandler
    public void onPostInit(FMLPostInitializationEvent e) {

    }


}
