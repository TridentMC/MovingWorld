package com.elytradev.movingworld.common.experiments;

import com.elytradev.concrete.reflect.accessor.Accessor;
import com.elytradev.concrete.reflect.accessor.Accessors;
import com.elytradev.movingworld.client.experiments.InputReader;
import com.elytradev.movingworld.client.experiments.MovingWorldClientDatabase;
import com.elytradev.movingworld.common.experiments.network.messages.server.MessageDimensionPoolData;
import com.elytradev.movingworld.common.experiments.network.messages.server.MessageFullPoolData;
import com.elytradev.movingworld.common.experiments.region.RegionPool;
import com.elytradev.movingworld.common.experiments.world.MWPlayerChunkMap;
import com.elytradev.movingworld.common.experiments.world.MWServerWorldEventHandler;
import com.elytradev.movingworld.common.experiments.world.MovingWorldProvider;
import com.google.common.collect.HashBiMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.world.DimensionType;
import net.minecraft.world.ServerWorldEventHandler;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.HashMap;

/**
 * Handles init and deinit for subworld registration, region pool io, etc.
 */
public class MovingWorldInitHandler {

    public static HashBiMap<Integer, Integer> registeredDimensions = HashBiMap.create();
    public static int startingDimID = 50;
    public static int activeDimID = startingDimID;

    private Accessor<PlayerChunkMap> playerChunkMap;


    public MovingWorldInitHandler() {
        this.playerChunkMap = Accessors.findField(WorldServer.class, "playerChunkMap", "field_73063_M");
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
            RegionPool.getPool(activeDimID, true);
            worldServer.addEventListener(new BoundingBoxWorldListener());
            MovingWorldExperimentsMod.logger.info("DB check: " + MovingWorldExperimentsMod.modProxy.getCommonDB().getWorldFromDim(activeDimID));
            activeDimID++;
        } catch (Exception exception) {
            MovingWorldExperimentsMod.logger.error("Exception on subworld registration/load ", e);
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
