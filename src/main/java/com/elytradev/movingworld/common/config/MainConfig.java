package com.elytradev.movingworld.common.config;

import com.elytradev.movingworld.MovingWorldMod;
import com.elytradev.movingworld.common.config.priority.AssemblePriorityConfig;
import com.elytradev.movingworld.common.util.MaterialDensity;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class MainConfig {

    private Configuration config;
    private SharedConfig shared;
    private boolean allowListInsertion;

    public MainConfig(Configuration config) {
        this.config = config;
        this.shared = new SharedConfig();
        this.shared.assemblePriorityConfig = new AssemblePriorityConfig(
                new Configuration(new File(config.getConfigFile().getParentFile(), "AssemblePriority.cfg")));

        shared.blockBlacklist = new HashSet<>();
        shared.blockWhitelist = new HashSet<>();
        shared.tileBlacklist = new HashSet<>();
        shared.tileWhitelist = new HashSet<>();
        shared.overwritableBlocks = new HashSet<>();
        shared.updatableTiles = new HashSet<>();

        MinecraftForge.EVENT_BUS.register(this); // For in game config reloads.
    }

    public SharedConfig getShared() {
        return shared;
    }

    public void setShared(SharedConfig shared) {
        this.shared = shared;
    }

    public Configuration getConfig() {
        return this.config;
    }

    /**
     * A private method so I can hide it in my IDE, because it's an eye sore.
     */
    private Block[] getDefaultBlockWhiteList() {
        return new Block[]{Blocks.ACACIA_STAIRS, Blocks.ACTIVATOR_RAIL, Blocks.ANVIL, Blocks.BED, Blocks.BIRCH_STAIRS, Blocks.BOOKSHELF, Blocks.BREWING_STAND, Blocks.BRICK_BLOCK, Blocks.BRICK_STAIRS, Blocks.CACTUS, Blocks.CAKE, Blocks.CARPET, Blocks.COAL_BLOCK, Blocks.COBBLESTONE, Blocks.COBBLESTONE_WALL, Blocks.COMMAND_BLOCK, Blocks.CRAFTING_TABLE, Blocks.DARK_OAK_STAIRS, Blocks.DETECTOR_RAIL, Blocks.DIAMOND_BLOCK, Blocks.DISPENSER, Blocks.DROPPER, Blocks.DAYLIGHT_DETECTOR, Blocks.DOUBLE_STONE_SLAB, Blocks.DOUBLE_WOODEN_SLAB, Blocks.EMERALD_BLOCK, Blocks.ENCHANTING_TABLE, Blocks.END_STONE, Blocks.ENDER_CHEST, Blocks.ACACIA_FENCE, Blocks.BIRCH_FENCE, Blocks.DARK_OAK_FENCE, Blocks.JUNGLE_FENCE, Blocks.NETHER_BRICK_FENCE, Blocks.OAK_FENCE, Blocks.SPRUCE_FENCE, Blocks.ACACIA_FENCE_GATE, Blocks.BIRCH_FENCE_GATE, Blocks.DARK_OAK_FENCE_GATE, Blocks.JUNGLE_FENCE_GATE, Blocks.OAK_FENCE_GATE, Blocks.SPRUCE_FENCE_GATE, Blocks.FLOWER_POT, Blocks.FURNACE, Blocks.FIRE, Blocks.GLASS, Blocks.GLASS_PANE, Blocks.GLOWSTONE, Blocks.GOLD_BLOCK, Blocks.GOLDEN_RAIL, Blocks.HARDENED_CLAY, Blocks.HAY_BLOCK, Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE, Blocks.HOPPER, Blocks.IRON_BARS, Blocks.IRON_BLOCK, Blocks.IRON_DOOR, Blocks.JUKEBOX, Blocks.JUNGLE_STAIRS, Blocks.LADDER, Blocks.LAPIS_BLOCK, Blocks.LEVER, Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE, Blocks.LIT_FURNACE, Blocks.LIT_PUMPKIN, Blocks.LIT_REDSTONE_LAMP, Blocks.LOG, Blocks.LOG2, Blocks.MELON_BLOCK, Blocks.MOB_SPAWNER, Blocks.MONSTER_EGG, Blocks.MOSSY_COBBLESTONE, Blocks.NETHER_BRICK, Blocks.NETHER_BRICK_FENCE, Blocks.NETHER_BRICK_STAIRS, Blocks.NOTEBLOCK, Blocks.OAK_STAIRS, Blocks.OBSIDIAN, Blocks.PLANKS, Blocks.PUMPKIN, Blocks.PISTON, Blocks.PISTON_EXTENSION, Blocks.PISTON_HEAD, Blocks.POWERED_COMPARATOR, Blocks.POWERED_REPEATER, Blocks.QUARTZ_BLOCK, Blocks.QUARTZ_STAIRS, Blocks.RAIL, Blocks.REDSTONE_BLOCK, Blocks.REDSTONE_TORCH, Blocks.REDSTONE_WIRE, Blocks.SANDSTONE, Blocks.SANDSTONE_STAIRS, Blocks.RED_SANDSTONE, Blocks.RED_SANDSTONE_STAIRS, Blocks.SKULL, Blocks.SPONGE, Blocks.SPRUCE_STAIRS, Blocks.STAINED_HARDENED_CLAY, Blocks.STANDING_SIGN, Blocks.STONE_BRICK_STAIRS, Blocks.STONE_BUTTON, Blocks.STONE_PRESSURE_PLATE, Blocks.STONE_STAIRS, Blocks.STONEBRICK, Blocks.STAINED_GLASS, Blocks.STAINED_GLASS_PANE, Blocks.STICKY_PISTON, Blocks.STONE_SLAB, Blocks.STONE_SLAB2, Blocks.TNT, Blocks.TORCH, Blocks.TRAPDOOR, Blocks.TRAPPED_CHEST, Blocks.TRIPWIRE, Blocks.TRIPWIRE_HOOK, Blocks.UNLIT_REDSTONE_TORCH, Blocks.UNPOWERED_COMPARATOR, Blocks.UNPOWERED_REPEATER, Blocks.WALL_SIGN, Blocks.WEB, Blocks.WOODEN_BUTTON, Blocks.ACACIA_DOOR, Blocks.BIRCH_DOOR, Blocks.DARK_OAK_DOOR, Blocks.JUNGLE_DOOR, Blocks.OAK_DOOR, Blocks.SPRUCE_DOOR, Blocks.WOODEN_PRESSURE_PLATE, Blocks.WOOL, Blocks.WOODEN_SLAB};
    }

    /**
     * A private method so I can hide it in my IDE, because it's an eye sore.
     */
    private Block[] getDefaultBlockBlackList() {
        return new Block[]{Blocks.DIRT, Blocks.GRASS, Blocks.SAND, Blocks.GRAVEL, Blocks.CLAY, Blocks.ICE, Blocks.WATER, Blocks.FLOWING_WATER, Blocks.LAVA, Blocks.FLOWING_LAVA, Blocks.SNOW, Blocks.SNOW_LAYER, Blocks.WATERLILY, Blocks.NETHERRACK, Blocks.SOUL_SAND, Blocks.TALLGRASS};
    }

    public void loadAndSave() {
        String[] defaultMaterialDensities = {"\"minecraft:air=0.0\"", "\"minecraft:wool=0.1\""};
        String[] defaultBlockDensities = {"\"DavincisVessels:floater=0.04\"", "\"DavincisVessels:balloon=0.02\""};
        String[] defaultUpdatableTiles = {"Furnace", "Hopper", "Banner", "EnchantTable", "DLDetector"};

        Block[] defaultOverWritableBlocks = {Blocks.TALLGRASS, Blocks.WATERLILY, Blocks.SNOW_LAYER};

        String[] blockBlackListNames = new String[getDefaultBlockBlackList().length];
        for (int i = 0; i < getDefaultBlockBlackList().length; i++) {
            blockBlackListNames[i] = Block.REGISTRY.getNameForObject(getDefaultBlockBlackList()[i]).toString();
        }

        String[] blockWhiteListNames = new String[getDefaultBlockWhiteList().length];
        for (int i = 0; i < blockWhiteListNames.length; i++) {
            blockWhiteListNames[i] = Block.REGISTRY.getNameForObject(getDefaultBlockWhiteList()[i]).toString();
        }

        String[] overWritableBlockNames = new String[defaultOverWritableBlocks.length];
        for (int i = 0; i < defaultOverWritableBlocks.length; i++) {
            overWritableBlockNames[i] = Block.REGISTRY.getNameForObject(defaultOverWritableBlocks[i]).toString();
        }

        config.load();

        shared.iterativeAlgorithm = config.get(Configuration.CATEGORY_GENERAL, "Use Iterative Algorithm", true).getBoolean();
        shared.diagonalAssembly = config.get(Configuration.CATEGORY_GENERAL, "Assemble Diagonal Blocks NOTE: Can be overridden by mods!", false).getBoolean();
        shared.useWhitelist = config.get("mobile_chunk", "use_whitelist", false, "Switch this property to select the block restriction list to use. 'true' for the 'allowed_blocks' whitelist, 'false' for the 'forbidden_blocks' blacklist.").getBoolean(false);
        shared.useTileWhitelist = config.get("mobile_chunk", "use_tile_whitelist", false, "Switch this property to select the tile entity restriction list to use. 'true' for the 'allowed_tiles' whitelist, 'false' for the 'forbidden_tiles' blacklist.").getBoolean(false);
        shared.loadedBlockDensities = config.get("mobile_chunk", "block_densities", defaultBlockDensities, "A list of pairs of a block with a density value. This list overrides the 'material_densities' list.").getStringList();
        shared.loadedMaterialDensities = config.get("mobile_chunk", "material_densities", defaultMaterialDensities, "A list of pairs of a material with a density value. The first value is the name of a block. All objects with the same material will get this density value, unless overridden.").getStringList();

        allowListInsertion = config.get(Configuration.CATEGORY_GENERAL, "Allow other mods to add to the whitelist/blacklist? NOTE: Turn off if you want to remove the default blacklist/whitelist", true).getBoolean();

        String[] forbiddenBlocks = config.get("mobile_chunk", "forbidden_blocks", blockBlackListNames, "A list of blocks that will not be added to a Moving World.").getStringList();
        String[] allowedBlocks = config.get("mobile_chunk", "allowed_blocks", blockWhiteListNames, "A list of blocks that are allowed on a Moving World.").getStringList();

        String[] forbiddenTiles = config.get("mobile_chunk", "forbidden_tiles", new String[0], "A list of tile entities that will not be added to a Moving World.").getStringList();
        String[] allowedTiles = config.get("mobile_chunk", "allowed_tiles", new String[0], "A list of tile entities that are allowed on a Moving World.").getStringList();

        String[] overwritableBlocks = config.get("mobile_chunk", "overwritable_blocks", overWritableBlockNames, "A list of blocks that may be overwritten when decompiling a Moving World.").getStringList();
        String[] updatableTiles = config.get("mobile_chunk", "updatable_tiles", defaultUpdatableTiles,
                "(Currently unimplemented) A list of tiles that are allowed to tick while they're part of a MobileChunk, might cause explosive loss of data, type 2 diabetes, and cancer. Use with caution.").getStringList();

        Collections.addAll(this.shared.blockBlacklist, forbiddenBlocks);
        Collections.addAll(this.shared.blockWhitelist, allowedBlocks);
        Collections.addAll(this.shared.tileBlacklist, forbiddenTiles);
        Collections.addAll(this.shared.tileWhitelist, allowedTiles);
        Collections.addAll(this.shared.overwritableBlocks, overwritableBlocks);
        Collections.addAll(this.shared.updatableTiles, updatableTiles);

        config.save();

        this.shared.assemblePriorityConfig.loadAndSavePreInit();
    }

    public void postLoad() {
        Pattern splitpattern = Pattern.compile("=");
        for (int i = 0; i < shared.loadedBlockDensities.length; i++) {
            String s = shared.loadedBlockDensities[i];
            s = s.replace("\"", "");
            String[] pair = splitpattern.split(s);
            if (pair.length != 2) {
                MovingWorldMod.LOG.warn("Invalid key-value pair at block_densities[" + i + "]");
                continue;
            }
            String key = pair[0];
            float density;
            try {
                density = Float.parseFloat(pair[1]);
            } catch (NumberFormatException e) {
                MovingWorldMod.LOG.warn("Cannot parse value " + pair[1] + " to floating point at block_densities[" + i + "]");
                continue;
            }
            Block block = Block.getBlockFromName(key);
            if (block == null) {
                MovingWorldMod.LOG.warn("No block found for " + key + " at block_densities[" + i + "]");
                continue;
            }

            MaterialDensity.addDensity(block, density);
        }

        for (int i = 0; i < shared.loadedMaterialDensities.length; i++) {
            String s = shared.loadedMaterialDensities[i];
            s = s.replace("\"", "");
            String[] pair = splitpattern.split(s);
            if (pair.length != 2) {
                MovingWorldMod.LOG.warn("Invalid key-value pair at material_densities[" + i + "]");
                continue;
            }
            String key = pair[0];
            float density;
            try {
                density = Float.parseFloat(pair[1]);
            } catch (NumberFormatException e) {
                MovingWorldMod.LOG.warn("Cannot parse value " + pair[1] + " to floating point at material_densities[" + i + "]");
                continue;
            }
            Block block = Block.getBlockFromName(key);
            if (block == null) {
                MovingWorldMod.LOG.warn("No block found for " + key + " at material_densities[" + i + "]");
                continue;
            }

            MaterialDensity.addDensity(block.getDefaultState().getMaterial(), density);
        }
    }

    public void addBlacklistedBlock(Block block) {
        if (!allowListInsertion) return;

        String blockName = Block.REGISTRY.getNameForObject(block).toString();

        config.load();

        String[] blockBlackListNames = new String[getDefaultBlockBlackList().length];
        for (int i = 0; i < getDefaultBlockBlackList().length; i++) {
            blockBlackListNames[i] = Block.REGISTRY.getNameForObject(getDefaultBlockBlackList()[i]).toString();
        }

        Property prop = config.get("mobile_chunk", "forbidden_blocks", blockBlackListNames, "A list of blocks that will not be added to a Moving World.");

        String[] stringList = prop.getStringList();
        ArrayList<String> stringArrayList = new ArrayList<>();
        Collections.addAll(stringArrayList, stringList);

        if (!stringArrayList.contains(blockName))
            stringArrayList.add(blockName);

        String[] setVal = new String[stringArrayList.size()];
        int i = 0;
        for (String str : stringArrayList) {
            setVal[i] = str;
            i++;
        }

        prop.set(setVal);

        config.save();
    }

    public void addWhitelistedBlock(Block block) {
        if (!allowListInsertion) return;

        String blockName = Block.REGISTRY.getNameForObject(block).toString();

        config.load();

        String[] blockWhiteListNames = new String[getDefaultBlockWhiteList().length];
        for (int i = 0; i < blockWhiteListNames.length; i++) {
            blockWhiteListNames[i] = Block.REGISTRY.getNameForObject(getDefaultBlockWhiteList()[i]).toString();
        }

        Property prop = config.get("mobile_chunk", "allowed_blocks", blockWhiteListNames, "A list of blocks that are allowed on a Moving World.");

        String[] stringList = prop.getStringList();
        ArrayList<String> stringArrayList = new ArrayList<>();
        Collections.addAll(stringArrayList, stringList);
        if (!stringArrayList.contains(blockName))
            stringArrayList.add(blockName);

        String[] setVal = new String[stringArrayList.size()];
        int i = 0;
        for (String str : stringArrayList) {
            setVal[i] = str;
            i++;
        }

        prop.set(setVal);

        config.save();
    }

    public boolean isStateAllowed(IBlockState state) {
        String id = Block.REGISTRY.getNameForObject(state.getBlock()).toString();
        return shared.useWhitelist ? shared.blockWhitelist.contains(id) : !shared.blockBlacklist.contains(id);
    }

    public boolean canOverwriteState(IBlockState state) {
        return shared.overwritableBlocks.contains(Block.REGISTRY.getNameForObject(state.getBlock()));
    }

    public boolean isTileUpdatable(Class<? extends TileEntity> tileClass) {
        return false;

        //return shared.updatableTiles.contains(TileEntity.classToNameMap.get(tileClass));
    }

    public boolean isTileAllowed(TileEntity t) {
        if (t == null)
            return true;
        Class<? extends TileEntity> tileClass = t.getClass();
        String tileName = TileEntity.getKey(tileClass).toString();

        return shared.useTileWhitelist ? shared.tileWhitelist.contains(tileName) : !shared.tileBlacklist.contains(tileName);
    }

    @SubscribeEvent
    public void onConfigChange(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals(MovingWorldMod.MOD_ID)) {
            if (config.hasChanged())
                config.save();
            loadAndSave();
        }
    }

    public class SharedConfig {
        public boolean iterativeAlgorithm;
        public boolean diagonalAssembly;
        public boolean useWhitelist;
        public boolean useTileWhitelist;
        public Set<String> blockBlacklist;
        public Set<String> blockWhitelist;
        public Set<String> overwritableBlocks;
        public Set<String> tileBlacklist;
        public Set<String> tileWhitelist;
        public Set<String> updatableTiles;
        public AssemblePriorityConfig assemblePriorityConfig;

        private String[] loadedBlockDensities;
        private String[] loadedMaterialDensities;

        public NBTTagCompound serialize() {
            return new NBTTagCompound();
        }

        public SharedConfig deserialize(NBTTagCompound tag) {
            return new SharedConfig();
        }
    }


}
