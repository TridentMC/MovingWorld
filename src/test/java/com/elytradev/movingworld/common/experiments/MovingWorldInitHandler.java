package com.elytradev.movingworld.common.experiments;

import com.elytradev.concrete.reflect.accessor.Accessor;
import com.elytradev.concrete.reflect.accessor.Accessors;
import com.elytradev.movingworld.client.experiments.InputReader;
import com.elytradev.movingworld.client.experiments.MovingWorldClientDatabase;
import com.elytradev.movingworld.common.experiments.network.messages.server.MessageDimensionPoolData;
import com.elytradev.movingworld.common.experiments.region.RegionPool;
import com.elytradev.movingworld.common.experiments.world.MWPlayerChunkMap;
import com.elytradev.movingworld.common.experiments.world.MWServerWorldEventHandler;
import com.elytradev.movingworld.common.experiments.world.MovingWorldProvider;
import com.google.common.collect.HashBiMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.world.DimensionType;
import net.minecraft.world.ServerWorldEventHandler;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;

/**
 * Handles init and deinit for subworld registration, region pool io, etc.
 */
public class MovingWorldInitHandler {

    /**
     * ParentWorldID->SubWorldID
     */
    public static HashBiMap<Integer, Integer> registeredDimensions = HashBiMap.create();
    public static int startingDimID = 50;
    public static int activeDimID = startingDimID;

    private Accessor<PlayerChunkMap> playerChunkMap;


    public MovingWorldInitHandler() {
        this.playerChunkMap = Accessors.findField(WorldServer.class, "playerChunkMap", "field_73063_M");
    }

    @SubscribeEvent
    public void onWorldSave(WorldEvent.Save e) {
        // Save pool for dimension.

        if (!registeredDimensions.containsValue(e.getWorld().provider.getDimension()))
            return;

        int subWorldID = registeredDimensions.inverse().get(e.getWorld().provider.getDimension());
        RegionPool poolForDim = RegionPool.getPool(subWorldID, false);
        savePoolToFile(e.getWorld().provider.getDimension(), poolForDim);
    }

    private void savePoolToFile(int parentDimension, RegionPool pool) {
        File saveDir = DimensionManager.getCurrentSaveRootDirectory();

        if (saveDir == null || pool == null)
            return;

        File activePoolDir = new File(saveDir, "movingworld-pools");
        File oldPoolDir = new File(activePoolDir, "old");

        try {
            NBTTagCompound poolCompound = pool.writePoolToCompound();
            File regionPool = new File(activePoolDir, MessageFormat.format("poolD{0}.dat", parentDimension));
            if (regionPool.exists()) {
                regionPool.renameTo(new File(oldPoolDir, MessageFormat.format("poolD{0}.dat", parentDimension)));
            }
            regionPool = new File(activePoolDir, MessageFormat.format("poolD{0}.dat", parentDimension));
            CompressedStreamTools.write(poolCompound, regionPool);
        } catch (IOException e) {
            MovingWorldExperimentsMod.logger.error(MessageFormat.format("Failed to write pool data from file for dimension {0}", parentDimension));
        }
    }

    private boolean isPoolDataPresent(int parentDimension) {
        File saveDir = DimensionManager.getCurrentSaveRootDirectory();

        if (saveDir == null)
            return false;

        File activePoolDir = new File(saveDir, "movingworld-pools");

        return new File(activePoolDir, MessageFormat.format("poolD{0}.dat", parentDimension)).exists();
    }

    private void readPoolFromFile(int parentDimension, int subWorldDimension) {
        File saveDir = DimensionManager.getCurrentSaveRootDirectory();

        if (saveDir == null)
            return;

        File activePoolDir = new File(saveDir, "movingworld-pools");

        try {
            File regionPool = new File(activePoolDir, MessageFormat.format("poolD{0}.dat", parentDimension));
            NBTTagCompound poolCompound = CompressedStreamTools.read(regionPool);
            RegionPool.getPool(subWorldDimension, true).readPoolFromCompound(poolCompound);
        } catch (IOException e) {
            MovingWorldExperimentsMod.logger.error(MessageFormat.format("Failed to read pool data from file for dimension {0}", parentDimension));
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
            // Register new dimension
            registeredDimensions.put(loadedDimensionID, activeDimID);
            DimensionManager.registerDimension(activeDimID,
                    DimensionType.register("MovingWorld|P" + loadedDimensionID + "|C" + activeDimID,
                            "movingworld", activeDimID, MovingWorldProvider.class, true));
            DimensionManager.initDimension(activeDimID);
            WorldServer worldServer = DimensionManager.getWorld(activeDimID);

            // Inject set MW component alternatives.
            playerChunkMap.set(worldServer, new MWPlayerChunkMap(worldServer));
            ServerWorldEventHandler currentEventListener = (ServerWorldEventHandler) worldServer.eventListeners.stream()
                    .filter((ev) -> ev instanceof ServerWorldEventHandler).findFirst().get();
            worldServer.eventListeners.remove(currentEventListener);
            worldServer.addEventListener(new MWServerWorldEventHandler(worldServer.mcServer, worldServer));

            // Init pool, increment dimension number.
            if (isPoolDataPresent(e.getWorld().provider.getDimension()))
                readPoolFromFile(e.getWorld().provider.getDimension(), activeDimID);
            RegionPool.getPool(activeDimID, true);
            worldServer.addEventListener(new BoundingBoxWorldListener());
            MovingWorldExperimentsMod.logger.info("DB check: " + MovingWorldExperimentsMod.modProxy.getCommonDB().getWorldFromDim(activeDimID));
            activeDimID++;
        } catch (Exception exception) {
            MovingWorldExperimentsMod.logger.error("Exception on subworld registration/load ", e);
        }
    }


    @Mod.EventHandler
    public void onServerStopped(FMLServerStoppedEvent e) {
        MovingWorldInitHandler.registeredDimensions.forEach((parent, child) -> DimensionManager.unregisterDimension(child));
        MovingWorldInitHandler.registeredDimensions = HashBiMap.create();

        if (e.getSide() == Side.CLIENT) {
            ((MovingWorldClientDatabase) MovingWorldExperimentsMod.modProxy.getClientDB()).worlds.clear();
        }

        MovingWorldInitHandler.activeDimID = MovingWorldInitHandler.startingDimID;
    }

    @Mod.EventHandler
    public void onServerStopping(FMLServerStoppingEvent e) {
        registeredDimensions.forEach((parentWorld, subWorld) -> savePoolToFile(parentWorld, RegionPool.getPool(subWorld, false)));
    }

    @SubscribeEvent
    public void onConnection(PlayerEvent.PlayerLoggedInEvent e) {
        if (!e.isCanceled() && e.player != null && !e.player.world.isRemote) {
            new MessageDimensionPoolData(e.player.world.provider.getDimension(), RegionPool.getPool(e.player.world.provider.getDimension(), true).writePoolToCompound()).sendTo(e.player);
        }
    }

    @SubscribeEvent
    public void onDimChange(PlayerEvent.PlayerChangedDimensionEvent e) {
        if (!e.isCanceled() && e.player != null && !e.player.world.isRemote) {
            new MessageDimensionPoolData(e.toDim, RegionPool.getPool(e.toDim, true).writePoolToCompound()).sendTo(e.player);
        }
    }


    //TODO: This shouldn't be in the init handler.
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent e) {
        if (e.phase == TickEvent.Phase.START) {
            Minecraft mc = Minecraft.getMinecraft();
            MovingWorldClientDatabase cDB = (MovingWorldClientDatabase) MovingWorldExperimentsMod.modProxy.getClientDB();

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
