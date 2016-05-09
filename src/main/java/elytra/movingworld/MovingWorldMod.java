package elytra.movingworld;

import elytra.movingworld.common.CommonProxy;
import elytra.movingworld.common.MovingWorldDimensionConfig;
import elytra.movingworld.common.core.IMovingWorld;
import elytra.movingworld.common.core.factory.CommonMovingWorldFactory;
import elytra.movingworld.common.core.world.MovingWorldManager;
import elytra.movingworld.common.network.MovingWorldMessageToMessageCodec;
import elytra.movingworld.common.network.MovingWorldPacketHandler;
import elytra.movingworld.common.network.NetworkUtil;
import elytra.movingworld.common.test.BlockMovingWorldCreator;
import net.minecraft.block.material.Material;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemBlock;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
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

    @SidedProxy(modId = MOD_ID, clientSide = "elytra.movingworld.client.ClientProxy", serverSide = "elytra.movingworld.common.CommonProxy")
    public static CommonProxy proxy;

    public static Logger logger;

    public static CommonMovingWorldFactory movingWorldFactory;
    public static MovingWorldDimensionConfig dimensionConfig;
    public NetworkUtil network;

    private File confFolder;

    public MovingWorldMod() {
        network = new NetworkUtil();
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        logger = e.getModLog();
        this.confFolder = new File(e.getModConfigurationDirectory(), "MovingWorld");

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
        String worldName = "DEDICATEDSERVER";
        if (!e.getServer().isDedicatedServer())
            worldName = e.getServer().getWorldName();

        dimensionConfig = new MovingWorldDimensionConfig(new File(confFolder, ".dim" + File.separator + worldName));

        dimensionConfig.loadDimensionManager();
    }

    @Mod.EventHandler
    public void onServerStarting(FMLServerStartingEvent evt) {
        if (inDev())
            //this command is awful im not sorry.
            evt.registerServerCommand(new CommandBase() {
                @Override
                public String getCommandName() {
                    return "tpx";
                }

                @Override
                public String getCommandUsage(ICommandSender sender) {
                    return "tpx dim";
                }

                @Override
                public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
                    if (sender != null && sender instanceof Entity && args.length > 0) {
                        ((Entity) sender).changeDimension(1);
                        ((Entity) sender).changeDimension(Integer.parseInt(args[0]));
                        if (DimensionManager.getWorld(Integer.parseInt(args[0])) != null && DimensionManager.getWorld(Integer.parseInt(args[0])) instanceof IMovingWorld) {
                            IMovingWorld world = (IMovingWorld) DimensionManager.getWorld(Integer.parseInt(args[0]));
                            ((Entity) sender).setPosition(world.coreBlock().add(0, 32, 0).getX(), world.coreBlock().add(0, 32, 0).getY(), world.coreBlock().add(0, 32, 0).getZ());
                        } else {
                            ((Entity) sender).setPosition(0, 64, 0);
                        }
                    } else {
                        throw new CommandException("thats no good");
                    }
                }
            });
    }

    public boolean inDev(){
        return MOD_VERSION.equals("@MOVINGWORLDVER@");
    }

    @Mod.EventHandler
    public void onServerStarted(FMLServerStartedEvent e) {
        if (DimensionManager.getWorld(0) == null) {
            //No overworld loaded. Fun.
            DimensionManager.initDimension(0);
        }

        for (int dim : net.minecraftforge.common.DimensionManager.getStaticDimensionIDs()) {
            WorldServer parent = DimensionManager.getWorld(dim);
            if (parent == null) {
                DimensionManager.initDimension(dim); // Make sure this world is loaded before we try and inject our worlds.
                parent = DimensionManager.getWorld(dim);
            }

            MovingWorldManager.initDims(parent);
        }
    }

    @Mod.EventHandler
    public void onServerStopEvent(FMLServerStoppedEvent e) {
        dimensionConfig.saveDimensionManager();
        MovingWorldManager.resetMovingWorldManager();
    }
}
