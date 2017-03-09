package com.elytradev.movingworld.common.experiments;

import com.elytradev.movingworld.client.experiments.InputReader;
import com.elytradev.movingworld.client.experiments.MovingWorldClientDatabase;
import com.elytradev.movingworld.common.experiments.debug.BlockDebug;
import com.elytradev.movingworld.common.experiments.entity.EntityMobileRegion;
import com.elytradev.movingworld.common.experiments.network.MovingWorldExperimentsNetworking;
import com.elytradev.movingworld.common.experiments.network.messages.server.MessageDimensionPoolData;
import com.elytradev.movingworld.common.experiments.network.messages.server.MessageFullPoolData;
import com.elytradev.movingworld.common.experiments.region.MobileRegion;
import com.elytradev.movingworld.common.experiments.region.RegionPool;
import com.elytradev.movingworld.common.experiments.world.MovingWorldProvider;
import com.google.common.collect.HashBiMap;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;

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

    public static HashBiMap<Integer, Integer> registeredDimensions = HashBiMap.create();

    public static int startingDimID = 50;
    public static int activeDimID = startingDimID;

    @Mod.EventHandler
    public void onPreInit(FMLPreInitializationEvent e) {
        logger = e.getModLog();
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(MWPlayerInteractionManager.class);
        modProxy.setupDBS();
        modProxy.registerRenders();

        ForgeChunkManager.setForcedChunkLoadingCallback(instance, (tickets, world) -> {
            for (ForgeChunkManager.Ticket t : tickets) {
                MobileRegion region = MobileRegion.getRegionFor(t.getModData().getCompoundTag("Region"));

                for (int cX = region.regionMin.chunkXPos; cX < region.regionMax.chunkXPos; cX++) {
                    for (int cZ = region.regionMin.chunkZPos; cZ < region.regionMax.chunkZPos; cZ++) {
                        ForgeChunkManager.forceChunk(t, new ChunkPos(cX, cZ));
                    }
                }
            }
        });
        //noinspection deprecation
        GameRegistry.registerWithItem(new BlockDebug(Material.TNT, MapColor.TNT));
    }

    @Mod.EventHandler
    public void onInit(FMLInitializationEvent e) {
        MovingWorldExperimentsNetworking.init();
        EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID, "mobileregion"), EntityMobileRegion.class, "mobileregion", 1, this, 64, 5, true);
    }

    @Mod.EventHandler
    public void onPostInit(FMLPostInitializationEvent e) {

    }

    @Mod.EventHandler
    public void onServerStopped(FMLServerStoppedEvent e) {
        registeredDimensions.forEach((parent, child) -> DimensionManager.unregisterDimension(child));
        registeredDimensions = HashBiMap.create();

        if (e.getSide() == Side.CLIENT) {
            ((MovingWorldClientDatabase) modProxy.getClientDB()).worlds.clear();
        }

        activeDimID = startingDimID;
    }

    @Mod.EventHandler
    public void onServerStopping(FMLServerStoppingEvent e) {
        File saveDir = DimensionManager.getCurrentSaveRootDirectory();

        if (saveDir != null) {
            try {
                NBTTagCompound poolCompound = RegionPool.writeAllToCompound();
                File regionPool = new File(saveDir, "movingworld-regionpools.dat");
                if (regionPool.exists()) {
                    regionPool.renameTo(new File(saveDir, "movingworld-regionpools-old.dat"));
                }
                regionPool = new File(saveDir, "movingworld-regionpools.dat");
                CompressedStreamTools.write(poolCompound, regionPool);
            } catch (Exception exception) {
                logger.error("Something went wrong when saving region pools....");
                exception.printStackTrace();
            }
        }
    }

    @Mod.EventHandler
    public void onServerStarted(FMLServerStartedEvent e) {
        File saveDir = DimensionManager.getCurrentSaveRootDirectory();

        if (saveDir != null) {
            try {
                File regionPool = new File(saveDir, "movingworld-regionpools.dat");
                if (regionPool.exists()) {

                    NBTTagCompound poolCompound = CompressedStreamTools.read(regionPool);
                    RegionPool.readAllFromCompound(poolCompound);
                }
            } catch (Exception exception) {
                logger.error("Something went wrong when loading region pools...");
                exception.printStackTrace();
            }
        }
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load e) {
        Integer loadedDimensionID = e.getWorld().provider.getDimension();

        if (registeredDimensions.containsKey(loadedDimensionID)
                || registeredDimensions.containsValue(loadedDimensionID)) {
            return;
        }

        try {
            registeredDimensions.put(loadedDimensionID, activeDimID);
            DimensionManager.registerDimension(activeDimID,
                    DimensionType.register("MovingWorld|P" + loadedDimensionID + "|C" + activeDimID,
                            "movingworld", activeDimID, MovingWorldProvider.class, true));
            DimensionManager.initDimension(activeDimID);
            WorldServer worldServer = DimensionManager.getWorld(activeDimID);
            Field playerChunkMap = ReflectionHelper.findField(WorldServer.class, "playerChunkMap", "field_73063_M");
            playerChunkMap.setAccessible(true);
            playerChunkMap.set(worldServer, new MWPlayerChunkMap(worldServer));
            RegionPool.getPool(activeDimID, true);
            worldServer.addEventListener(new BoundingBoxWorldListener());
            System.out.println(modProxy.getCommonDB().getWorldFromDim(activeDimID));
            activeDimID++;
        } catch (Exception exception) {
            logger.error("Everything went fine don't worry it's good.");
            exception.printStackTrace();
        }
    }

    @SubscribeEvent
    public void onConnection(PlayerEvent.PlayerLoggedInEvent e) {
        if (!e.isCanceled() && e.player != null && !e.player.world.isRemote) {
            new MessageFullPoolData(RegionPool.writeAllToCompound()).sendTo(e.player);
        }
    }

    @SubscribeEvent
    public void onDimChange(PlayerEvent.PlayerChangedDimensionEvent e) {
        if (!e.isCanceled() && e.player != null && !e.player.world.isRemote) {
            new MessageDimensionPoolData(e.toDim, RegionPool.getPool(e.toDim, true).writePoolToCompound()).sendTo(e.player);
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent e) {
        if (e.phase == TickEvent.Phase.START) {
            Minecraft mc = Minecraft.getMinecraft();
            MovingWorldClientDatabase cDB = (MovingWorldClientDatabase) modProxy.getClientDB();

            if (mc.isGamePaused() || mc.world == null
                    || mc.player == null) {
                if ((mc.world == null
                        || mc.player == null
                        || mc.playerController == null) && InputReader.INSTANCE != null) {
                    MinecraftForge.EVENT_BUS.unregister(InputReader.INSTANCE);
                    InputReader.INSTANCE = null;
                }

                return;
            }

            if (InputReader.INSTANCE == null) {
                InputReader.INSTANCE = new InputReader(mc.playerController);
            }

            for (HashMap.Entry<Integer, WorldClient> mapEntry : cDB.worlds.entrySet()) {
                mapEntry.getValue().updateEntities();
                mapEntry.getValue().tick();
            }
        }
    }
}
