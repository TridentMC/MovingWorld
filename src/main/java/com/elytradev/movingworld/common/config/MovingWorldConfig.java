package com.elytradev.movingworld.common.config;

import com.google.common.collect.Lists;
import com.tridevmc.compound.config.ConfigType;
import com.tridevmc.compound.config.ConfigValue;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.config.ModConfig;

import java.util.List;

@ConfigType(ModConfig.Type.SERVER)
public class MovingWorldConfig {

    @ConfigValue(comment = "Use iterative algorithm. [Recommended]")
    public boolean iterativeAlgorithm = true;

    @ConfigValue(comment = "Assemble Diagonal Blocks. NOTE: Can be overridden by mods!")
    public boolean diagonalAssembly = false;

    @ConfigValue(comment = "Switch this property to select the block restriction list to use. 'true' for the 'blockWhitelist' whitelist, 'false' for the 'blockBlacklist' blacklist.")
    public boolean useWhitelist = false;

    @ConfigValue(comment = "Switch this property to select the tile entity restriction list to use. 'true' for the 'tileWhitelist' whitelist, 'false' for the 'tileBlacklist' blacklist.")
    public boolean useTileWhitelist = false;

    @ConfigValue(comment = "A list of blocks that will not be added to a Moving World.")
    public List<Block> blockBlacklist = Lists.newArrayList(Blocks.DIRT, Blocks.GRASS, Blocks.SAND, Blocks.GRAVEL, Blocks.CLAY, Blocks.ICE, Blocks.WATER, Blocks.LAVA, Blocks.SNOW, Blocks.SNOW, Blocks.LILY_PAD, Blocks.NETHERRACK, Blocks.SOUL_SAND, Blocks.TALL_GRASS);

    @ConfigValue(comment = "A list of blocks that are allowed on a Moving World.")
    public List<Block> blockWhitelist = Lists.newArrayList(Blocks.ACTIVATOR_RAIL, Blocks.ANVIL, Blocks.WHITE_BED, Blocks.ORANGE_BED, Blocks.MAGENTA_BED, Blocks.LIGHT_BLUE_BED, Blocks.YELLOW_BED, Blocks.LIME_BED, Blocks.PINK_BED, Blocks.GRAY_BED, Blocks.LIGHT_GRAY_BED, Blocks.CYAN_BED, Blocks.PURPLE_BED, Blocks.BLUE_BED, Blocks.BROWN_BED, Blocks.GREEN_BED, Blocks.RED_BED, Blocks.BLACK_BED, Blocks.BOOKSHELF, Blocks.BREWING_STAND, Blocks.BRICKS, Blocks.CACTUS, Blocks.CAKE, Blocks.WHITE_CARPET, Blocks.ORANGE_CARPET, Blocks.MAGENTA_CARPET, Blocks.LIGHT_BLUE_CARPET, Blocks.YELLOW_CARPET, Blocks.LIME_CARPET, Blocks.PINK_CARPET, Blocks.GRAY_CARPET, Blocks.LIGHT_GRAY_CARPET, Blocks.CYAN_CARPET, Blocks.PURPLE_CARPET, Blocks.BLUE_CARPET, Blocks.BROWN_CARPET, Blocks.GREEN_CARPET, Blocks.RED_CARPET, Blocks.COAL_BLOCK, Blocks.COBBLESTONE, Blocks.COBBLESTONE_WALL, Blocks.COMMAND_BLOCK, Blocks.CRAFTING_TABLE, Blocks.DETECTOR_RAIL, Blocks.DIAMOND_BLOCK, Blocks.DISPENSER, Blocks.DROPPER, Blocks.DAYLIGHT_DETECTOR, Blocks.OAK_SLAB, Blocks.SPRUCE_SLAB, Blocks.BIRCH_SLAB, Blocks.JUNGLE_SLAB, Blocks.ACACIA_SLAB, Blocks.DARK_OAK_SLAB, Blocks.STONE_SLAB, Blocks.SANDSTONE_SLAB, Blocks.PETRIFIED_OAK_SLAB, Blocks.COBBLESTONE_SLAB, Blocks.BRICK_SLAB, Blocks.STONE_BRICK_SLAB, Blocks.NETHER_BRICK_SLAB, Blocks.QUARTZ_SLAB, Blocks.RED_SANDSTONE_SLAB, Blocks.PURPUR_SLAB, Blocks.PRISMARINE_SLAB, Blocks.PRISMARINE_BRICK_SLAB, Blocks.DARK_PRISMARINE_SLAB, Blocks.EMERALD_BLOCK, Blocks.ENCHANTING_TABLE, Blocks.END_STONE, Blocks.ENDER_CHEST, Blocks.ACACIA_FENCE, Blocks.BIRCH_FENCE, Blocks.DARK_OAK_FENCE, Blocks.JUNGLE_FENCE, Blocks.NETHER_BRICK_FENCE, Blocks.OAK_FENCE, Blocks.SPRUCE_FENCE, Blocks.ACACIA_FENCE_GATE, Blocks.BIRCH_FENCE_GATE, Blocks.DARK_OAK_FENCE_GATE, Blocks.JUNGLE_FENCE_GATE, Blocks.OAK_FENCE_GATE, Blocks.SPRUCE_FENCE_GATE, Blocks.FLOWER_POT, Blocks.FURNACE, Blocks.FIRE, Blocks.GLASS, Blocks.GLASS_PANE, Blocks.GLOWSTONE, Blocks.GOLD_BLOCK, Blocks.POWERED_RAIL, Blocks.WHITE_GLAZED_TERRACOTTA, Blocks.ORANGE_GLAZED_TERRACOTTA, Blocks.MAGENTA_GLAZED_TERRACOTTA, Blocks.LIGHT_BLUE_GLAZED_TERRACOTTA, Blocks.YELLOW_GLAZED_TERRACOTTA, Blocks.LIME_GLAZED_TERRACOTTA, Blocks.PINK_GLAZED_TERRACOTTA, Blocks.GRAY_GLAZED_TERRACOTTA, Blocks.LIGHT_GRAY_GLAZED_TERRACOTTA, Blocks.CYAN_GLAZED_TERRACOTTA, Blocks.PURPLE_GLAZED_TERRACOTTA, Blocks.BLUE_GLAZED_TERRACOTTA, Blocks.BROWN_GLAZED_TERRACOTTA, Blocks.GREEN_GLAZED_TERRACOTTA, Blocks.RED_GLAZED_TERRACOTTA, Blocks.BLACK_GLAZED_TERRACOTTA, Blocks.HAY_BLOCK, Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE, Blocks.HOPPER, Blocks.IRON_BARS, Blocks.IRON_BLOCK, Blocks.IRON_DOOR, Blocks.JUKEBOX, Blocks.LADDER, Blocks.LAPIS_BLOCK, Blocks.LEVER, Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE, Blocks.FURNACE, Blocks.PUMPKIN, Blocks.CARVED_PUMPKIN, Blocks.REDSTONE_LAMP, Blocks.OAK_LOG, Blocks.SPRUCE_LOG, Blocks.BIRCH_LOG, Blocks.JUNGLE_LOG, Blocks.ACACIA_LOG, Blocks.DARK_OAK_LOG, Blocks.STRIPPED_OAK_LOG, Blocks.STRIPPED_SPRUCE_LOG, Blocks.STRIPPED_BIRCH_LOG, Blocks.STRIPPED_JUNGLE_LOG, Blocks.STRIPPED_ACACIA_LOG, Blocks.STRIPPED_DARK_OAK_LOG, Blocks.MELON, Blocks.SPAWNER, Blocks.INFESTED_STONE, Blocks.INFESTED_COBBLESTONE, Blocks.INFESTED_STONE_BRICKS, Blocks.INFESTED_MOSSY_STONE_BRICKS, Blocks.INFESTED_CRACKED_STONE_BRICKS, Blocks.MOSSY_COBBLESTONE, Blocks.NETHER_BRICKS, Blocks.NETHER_BRICK_FENCE, Blocks.RED_NETHER_BRICKS, Blocks.NOTE_BLOCK, Blocks.OBSIDIAN, Blocks.OAK_PLANKS, Blocks.SPRUCE_PLANKS, Blocks.BIRCH_PLANKS, Blocks.JUNGLE_PLANKS, Blocks.ACACIA_PLANKS, Blocks.DARK_OAK_PLANKS, Blocks.PUMPKIN, Blocks.STICKY_PISTON, Blocks.PISTON, Blocks.MOVING_PISTON, Blocks.PISTON_HEAD, Blocks.COMPARATOR, Blocks.REPEATER, Blocks.QUARTZ_BLOCK, Blocks.RAIL, Blocks.REDSTONE_BLOCK, Blocks.REDSTONE_TORCH, Blocks.REDSTONE_WIRE, Blocks.SANDSTONE, Blocks.RED_SANDSTONE, Blocks.SKELETON_SKULL, Blocks.SKELETON_WALL_SKULL, Blocks.WITHER_SKELETON_SKULL, Blocks.WITHER_SKELETON_WALL_SKULL, Blocks.SPONGE, Blocks.SIGN, Blocks.WALL_SIGN, Blocks.STONE_BUTTON, Blocks.STONE_PRESSURE_PLATE, Blocks.STONE_BRICKS, Blocks.WHITE_STAINED_GLASS, Blocks.ORANGE_STAINED_GLASS, Blocks.MAGENTA_STAINED_GLASS, Blocks.LIGHT_BLUE_STAINED_GLASS, Blocks.YELLOW_STAINED_GLASS, Blocks.LIME_STAINED_GLASS, Blocks.PINK_STAINED_GLASS, Blocks.GRAY_STAINED_GLASS, Blocks.LIGHT_GRAY_STAINED_GLASS, Blocks.CYAN_STAINED_GLASS, Blocks.PURPLE_STAINED_GLASS, Blocks.BLUE_STAINED_GLASS, Blocks.BROWN_STAINED_GLASS, Blocks.GREEN_STAINED_GLASS, Blocks.RED_STAINED_GLASS, Blocks.BLACK_STAINED_GLASS, Blocks.WHITE_STAINED_GLASS_PANE, Blocks.ORANGE_STAINED_GLASS_PANE, Blocks.MAGENTA_STAINED_GLASS_PANE, Blocks.LIGHT_BLUE_STAINED_GLASS_PANE, Blocks.YELLOW_STAINED_GLASS_PANE, Blocks.LIME_STAINED_GLASS_PANE, Blocks.PINK_STAINED_GLASS_PANE, Blocks.GRAY_STAINED_GLASS_PANE, Blocks.LIGHT_GRAY_STAINED_GLASS_PANE, Blocks.CYAN_STAINED_GLASS_PANE, Blocks.PURPLE_STAINED_GLASS_PANE, Blocks.BLUE_STAINED_GLASS_PANE, Blocks.BROWN_STAINED_GLASS_PANE, Blocks.GREEN_STAINED_GLASS_PANE, Blocks.RED_STAINED_GLASS_PANE, Blocks.BLACK_STAINED_GLASS_PANE, Blocks.STICKY_PISTON, Blocks.TNT, Blocks.TORCH, Blocks.WALL_TORCH, Blocks.OAK_TRAPDOOR, Blocks.SPRUCE_TRAPDOOR, Blocks.BIRCH_TRAPDOOR, Blocks.JUNGLE_TRAPDOOR, Blocks.ACACIA_TRAPDOOR, Blocks.DARK_OAK_TRAPDOOR, Blocks.TRAPPED_CHEST, Blocks.TRIPWIRE, Blocks.TRIPWIRE_HOOK, Blocks.REDSTONE_TORCH, Blocks.REDSTONE_WALL_TORCH, Blocks.WALL_SIGN, Blocks.COBWEB, Blocks.OAK_BUTTON, Blocks.SPRUCE_BUTTON, Blocks.BIRCH_BUTTON, Blocks.JUNGLE_BUTTON, Blocks.ACACIA_BUTTON, Blocks.DARK_OAK_BUTTON, Blocks.ACACIA_DOOR, Blocks.BIRCH_DOOR, Blocks.DARK_OAK_DOOR, Blocks.JUNGLE_DOOR, Blocks.OAK_DOOR, Blocks.SPRUCE_DOOR, Blocks.OAK_PRESSURE_PLATE, Blocks.SPRUCE_PRESSURE_PLATE, Blocks.BIRCH_PRESSURE_PLATE, Blocks.JUNGLE_PRESSURE_PLATE, Blocks.ACACIA_PRESSURE_PLATE, Blocks.DARK_OAK_PRESSURE_PLATE, Blocks.WHITE_WOOL, Blocks.ORANGE_WOOL, Blocks.MAGENTA_WOOL, Blocks.LIGHT_BLUE_WOOL, Blocks.YELLOW_WOOL, Blocks.LIME_WOOL, Blocks.PINK_WOOL, Blocks.GRAY_WOOL, Blocks.LIGHT_GRAY_WOOL, Blocks.CYAN_WOOL, Blocks.PURPLE_WOOL, Blocks.BLUE_WOOL, Blocks.BROWN_WOOL, Blocks.GREEN_WOOL, Blocks.RED_WOOL, Blocks.BLACK_WOOL, Blocks.OAK_STAIRS, Blocks.COBBLESTONE_STAIRS, Blocks.BRICK_STAIRS, Blocks.STONE_BRICK_STAIRS, Blocks.NETHER_BRICK_STAIRS, Blocks.SANDSTONE_STAIRS, Blocks.SPRUCE_STAIRS, Blocks.BIRCH_STAIRS, Blocks.JUNGLE_STAIRS, Blocks.QUARTZ_STAIRS, Blocks.ACACIA_STAIRS, Blocks.DARK_OAK_STAIRS, Blocks.PRISMARINE_STAIRS, Blocks.PRISMARINE_BRICK_STAIRS, Blocks.DARK_PRISMARINE_STAIRS, Blocks.RED_SANDSTONE_STAIRS, Blocks.PURPUR_STAIRS);

    @ConfigValue(comment = "A list of blocks that may be overwritten when disassembling a Moving World.")
    public List<Block> overwritableBlocks = Lists.newArrayList(Blocks.TALL_GRASS, Blocks.LILY_PAD, Blocks.SNOW);

    @ConfigValue(comment = "A list of tile entities that will not be added to a Moving World.")
    public List<TileEntityType> tileBlacklist = Lists.newArrayList();

    @ConfigValue(comment = "A list of tiles that are allowed on a Moving World.")
    public List<TileEntityType> tileWhitelist = Lists.newArrayList();

    @ConfigValue(comment = "A list of tiles that are allowed to tick while they're part of a MobileChunk.")
    public List<TileEntityType> updatableTiles = Lists.newArrayList(TileEntityType.FURNACE, TileEntityType.HOPPER, TileEntityType.BANNER, TileEntityType.ENCHANTING_TABLE, TileEntityType.DAYLIGHT_DETECTOR);

    @ConfigValue(comment = "A list of pairs of a block with a density value. Entries in this list override the 'materialDensities' list.")
    public List<BlockDensityData> blockDensities = Lists.newArrayList();

    @ConfigValue(comment = "A list of pairs of a material with a density value. The first value is the name of a block. All objects with the same material will get this density value, unless overridden.")
    public List<BlockDensityData> materialDensities = Lists.newArrayList(new BlockDensityData(Blocks.AIR, 0.0F), new BlockDensityData(Blocks.WHITE_WOOL, 0.1F));

    public boolean canOverwriteState(IBlockState state) {
        return this.overwritableBlocks.contains(state.getBlock());
    }

    public boolean isStateAllowed(IBlockState state) {
        if (this.useWhitelist) {
            return this.blockWhitelist.contains(state.getBlock());
        } else {
            return !this.blockBlacklist.contains(state.getBlock());
        }
    }

    public boolean isTileAllowed(TileEntity tile) {
        if (this.useTileWhitelist) {
            return this.tileWhitelist.contains(tile.getType());
        } else {
            return !this.tileBlacklist.contains(tile.getType());
        }
    }

    public boolean isTileUpdatable(TileEntity tile) {
        return this.updatableTiles.contains(tile.getType());
    }
}
