package com.elytradev.movingworld.common.experiments;

import com.google.common.collect.HashBiMap;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.DimensionType;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.io.File;

/**
 * Created by darkevilmac on 2/9/2017.
 */
@Mod(modid = "movingworld-experiments", name = "MovingWorld Experiments", version = "-1")
public class MovingWorldExperimentsMod {

    @SidedProxy(modId = "movingworld-experiments", clientSide = "com.elytradev.movingworld.client.experiments.ClientProxy", serverSide = "com.elytradev.movingworld.common.experiments.CommonProxy")
    public CommonProxy modProxy;

    public static HashBiMap<Integer, Integer> registeredDimensions = HashBiMap.create();

    public static int startingDimID = 50;
    public static int activeDimID = startingDimID;

    @Mod.EventHandler
    public void onPreInit(FMLPreInitializationEvent e) {
        MinecraftForge.EVENT_BUS.register(this);
        GameRegistry.registerWithItem(new BlockDebug(Material.TNT, MapColor.TNT));
    }

    @Mod.EventHandler
    public void onInit(FMLInitializationEvent e) {
        modProxy.registerRenders();
    }

    @Mod.EventHandler
    public void onPostInit(FMLPostInitializationEvent e) {

    }

    @Mod.EventHandler
    public void onServerStopped(FMLServerStoppedEvent e) {
        registeredDimensions.forEach((parent, child) -> DimensionManager.unregisterDimension(child));
        registeredDimensions.clear();

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
                System.out.println("Something went wrong when saving region pools....");
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
                System.out.println("Something went wrong when loading region pools....");
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
            RegionPool.getPool(activeDimID, true);

            activeDimID++;
        } catch (Exception exception) {
            System.out.println("Everything went fine don't worry it's good.");
            exception.printStackTrace();
        }
    }
}
