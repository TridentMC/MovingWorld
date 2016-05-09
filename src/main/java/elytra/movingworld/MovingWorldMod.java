package elytra.movingworld;

import elytra.movingworld.common.CommonProxy;
import elytra.movingworld.common.test.BlockMovingWorldCreator;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.apache.logging.log4j.Logger;

/**
 * MovingWorld's Mod container. Nothing to see here, move along.
 */
@Mod(modid = MovingWorldMod.MOD_ID, name = MovingWorldMod.MOD_NAME, version = MovingWorldMod.MOD_VERSION)
public class MovingWorldMod {
    public static final String MOD_ID = "MovingWorld-REDUX";
    public static final String MOD_VERSION = "@MOVINGWORLDVER@";
    public static final String MOD_NAME = "Moving World REDUX";

    @Mod.Instance(MOD_ID)
    public static MovingWorldMod instance;

    @SidedProxy(modId = MOD_ID, clientSide = "elytra.movingworld.client.ClientProxy", serverSide = "elytra.movingworld.common.CommonProxy")
    public static CommonProxy proxy;
    public static Logger logger;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        logger = e.getModLog();
        proxy.registerHandlers();

        if (inDev()) {
            // In dev environment initialize some test stuffs.
            BlockMovingWorldCreator blockMovingWorldCreator = new BlockMovingWorldCreator(Material.redstoneLight);
            blockMovingWorldCreator.setRegistryName("movingworld", "movingWorldCreator");
            GameRegistry.register(blockMovingWorldCreator);
            GameRegistry.register(new ItemBlock(blockMovingWorldCreator).setRegistryName(blockMovingWorldCreator.getRegistryName()));
        }
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent e) {
        proxy.initEvent(e.getModState());
        proxy.registerRenderers();

        proxy.setupFactory();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent e) {
        proxy.initEvent(e.getModState());
    }

    public boolean inDev(){
        return MOD_VERSION.equals("@MOVINGWORLDVER@");
    }

}
