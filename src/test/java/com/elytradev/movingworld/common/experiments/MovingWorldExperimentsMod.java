package com.elytradev.movingworld.common.experiments;

import com.google.common.collect.Maps;
import net.minecraft.world.DimensionType;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Map;

/**
 * Created by darkevilmac on 2/9/2017.
 */
@Mod(modid = "movingworld-experiments", name = "MovingWorld Experiments", version = "-1")
public class MovingWorldExperimentsMod {

    public static Map<Integer, Integer> registeredDimensions = Maps.newHashMap();

    public static int startingDimID = 50;
    public static int activeDimID = startingDimID;

    @Mod.EventHandler
    public void onPreInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Mod.EventHandler
    public void onInit(FMLInitializationEvent event) {

    }

    @Mod.EventHandler
    public void onPostInit(FMLPostInitializationEvent event) {

    }

    @Mod.EventHandler
    public void onGameExit(FMLServerStoppedEvent e) {
        registeredDimensions.forEach((parent, child) -> DimensionManager.unregisterDimension(child));
        registeredDimensions.clear();

        activeDimID = startingDimID;
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load loadEvent) {
        Integer loadedDimensionID = loadEvent.getWorld().provider.getDimension();

        if (registeredDimensions.containsKey(loadedDimensionID) || registeredDimensions.containsValue(loadedDimensionID)) {
            return;
        }

        try {
            registeredDimensions.put(loadedDimensionID, activeDimID);
            DimensionManager.registerDimension(activeDimID,
                    DimensionType.register("MovingWorld|P" + loadedDimensionID + "|C" + activeDimID,
                    "movingworld", activeDimID, MovingWorldProvider.class, true));
            DimensionManager.initDimension(activeDimID);
            activeDimID++;
        } catch (Exception e) {
            System.out.println("Everything went fine don't worry it's good.");
            e.printStackTrace();
        }
    }
}
