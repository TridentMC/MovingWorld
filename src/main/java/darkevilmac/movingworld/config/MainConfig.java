package darkevilmac.movingworld.config;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraftforge.common.config.Configuration;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class MainConfig {

    public boolean iterativeAlgorithm;
    public boolean diagonalAssembly;
    public boolean useWhitelist;
    public Set<String> blockBlacklist;
    public Set<String> blockWhitelist;
    public Set<String> overwritableBlocks;
    private Configuration config;

    public MainConfig(Configuration config) {
        this.config = config;

        blockBlacklist = new HashSet<String>();
        blockWhitelist = new HashSet<String>();
        overwritableBlocks = new HashSet<String>();
    }

    public void loadAndSave() {
        Block[] defaultBlockBlacklist = {(Blocks.dirt), (Blocks.grass), (Blocks.sand), (Blocks.gravel), (Blocks.clay), (Blocks.ice), (Blocks.water), (Blocks.flowing_water), (Blocks.flowing_lava), (Blocks.lava), (Blocks.snow), Blocks.snow_layer, (Blocks.waterlily), (Blocks.netherrack), (Blocks.soul_sand), Blocks.tallgrass};
        Block[] defaultBlocksWhitelist = {Blocks.acacia_stairs, Blocks.activator_rail, Blocks.anvil, Blocks.bed, Blocks.birch_stairs, Blocks.bookshelf, Blocks.brewing_stand, Blocks.brick_block, Blocks.brick_stairs, Blocks.cactus, Blocks.cake, Blocks.carpet, Blocks.coal_block, Blocks.cobblestone, Blocks.cobblestone_wall, Blocks.command_block, Blocks.crafting_table, Blocks.dark_oak_stairs, Blocks.detector_rail, Blocks.diamond_block, Blocks.dispenser, Blocks.dropper, Blocks.daylight_detector, Blocks.double_stone_slab, Blocks.double_wooden_slab, Blocks.emerald_block, Blocks.enchanting_table, Blocks.end_stone, Blocks.ender_chest, Blocks.acacia_fence, Blocks.birch_fence, Blocks.dark_oak_fence, Blocks.jungle_fence, Blocks.nether_brick_fence, Blocks.oak_fence, Blocks.spruce_fence, Blocks.acacia_fence_gate, Blocks.birch_fence_gate, Blocks.dark_oak_fence_gate, Blocks.jungle_fence_gate, Blocks.oak_fence_gate, Blocks.spruce_fence_gate, Blocks.flower_pot, Blocks.furnace, Blocks.fire, Blocks.glass, Blocks.glass_pane, Blocks.glowstone, Blocks.gold_block, Blocks.golden_rail, Blocks.hardened_clay, Blocks.hay_block, Blocks.heavy_weighted_pressure_plate, Blocks.hopper, Blocks.iron_bars, Blocks.iron_block, Blocks.iron_door, Blocks.jukebox, Blocks.jungle_stairs, Blocks.ladder, Blocks.lapis_block, Blocks.lever, Blocks.light_weighted_pressure_plate, Blocks.lit_furnace, Blocks.lit_pumpkin, Blocks.lit_redstone_lamp, Blocks.log, Blocks.log2, Blocks.melon_block, Blocks.mob_spawner, Blocks.monster_egg, Blocks.mossy_cobblestone, Blocks.nether_brick, Blocks.nether_brick_fence, Blocks.nether_brick_stairs, Blocks.noteblock, Blocks.oak_stairs, Blocks.obsidian, Blocks.planks, Blocks.pumpkin, Blocks.piston, Blocks.piston_extension, Blocks.piston_head, Blocks.powered_comparator, Blocks.powered_repeater, Blocks.quartz_block, Blocks.quartz_stairs, Blocks.rail, Blocks.redstone_block, Blocks.redstone_torch, Blocks.redstone_wire, Blocks.sandstone, Blocks.sandstone_stairs, Blocks.skull, Blocks.sponge, Blocks.spruce_stairs, Blocks.stained_hardened_clay, Blocks.standing_sign, Blocks.stone_brick_stairs, Blocks.stone_button, Blocks.stone_pressure_plate, Blocks.stone_stairs, Blocks.stonebrick, Blocks.stained_glass, Blocks.stained_glass_pane, Blocks.sticky_piston, Blocks.stone_slab, Blocks.tnt, Blocks.torch, Blocks.trapdoor, Blocks.trapped_chest, Blocks.tripwire, Blocks.tripwire_hook, Blocks.unlit_redstone_torch, Blocks.unpowered_comparator, Blocks.unpowered_repeater, Blocks.wall_sign, Blocks.web, Blocks.wooden_button, Blocks.acacia_door, Blocks.birch_door, Blocks.dark_oak_door, Blocks.jungle_door, Blocks.oak_door, Blocks.spruce_door, Blocks.wooden_pressure_plate, Blocks.wool, Blocks.wooden_slab};
        Block[] defaultOverwritableBlocks = {Blocks.tallgrass, Blocks.waterlily};

        String[] blockblacklistnames = new String[defaultBlockBlacklist.length];
        for (int i = 0; i < defaultBlockBlacklist.length; i++) {
            blockblacklistnames[i] = (String) Block.blockRegistry.getNameForObject(defaultBlockBlacklist[i]);
        }

        String[] blockwhitelistnames = new String[6 + defaultBlocksWhitelist.length];
        for (int i = 0; i < blockwhitelistnames.length - 6; i++) {
            blockwhitelistnames[i] = (String) Block.blockRegistry.getNameForObject(defaultBlocksWhitelist[i]);
        }
        blockwhitelistnames[blockwhitelistnames.length - 6] = "ArchimedesShips:marker";
        blockwhitelistnames[blockwhitelistnames.length - 5] = "ArchimedesShips:floater";
        blockwhitelistnames[blockwhitelistnames.length - 4] = "ArchimedesShips:balloon";
        blockwhitelistnames[blockwhitelistnames.length - 3] = "ArchimedesShips:gauge";
        blockwhitelistnames[blockwhitelistnames.length - 2] = "ArchimedesShips:seat";
        blockwhitelistnames[blockwhitelistnames.length - 1] = "ArchimedesShips:engine";

        String[] overwritableblocksnames = new String[defaultOverwritableBlocks.length];
        for (int i = 0; i < defaultOverwritableBlocks.length; i++) {
            overwritableblocksnames[i] = (String) Block.blockRegistry.getNameForObject(defaultOverwritableBlocks[i]);
        }
        config.load();
        iterativeAlgorithm = config.get(Configuration.CATEGORY_GENERAL, "Use Iterative Algorithm", false).getBoolean();
        diagonalAssembly = config.get(Configuration.CATEGORY_GENERAL, "Assemble Diagonal Blocks NOTE:Can be overridden by mods!", false).getBoolean();
        useWhitelist = config.get("mobile_chunk", "use_whitelist", false, "Switch this property to select the block restriction list to use. 'true' for the 'allowed_blocks' whitelist, 'false' for the 'forbidden_blocks' blacklist.").getBoolean(false);
        String[] forbiddenblocks = config.get("mobile_chunk", "forbidden_blocks", blockblacklistnames, "A list of blocks that will not be added to a Moving World.").getStringList();
        String[] allowedblocks = config.get("mobile_chunk", "allowed_blocks", blockwhitelistnames, "A list of blocks that are allowed on a Moving World.").getStringList();
        String[] overwritableblocks = config.get("mobile_chunk", "overwritable_blocks", overwritableblocksnames, "A list of blocks that may be overwritten when decompiling a Moving World.").getStringList();
        Collections.addAll(blockBlacklist, forbiddenblocks);
        Collections.addAll(blockWhitelist, allowedblocks);
        Collections.addAll(overwritableBlocks, overwritableblocks);
        config.save();
    }

    public boolean isBlockAllowed(Block block) {
        String id = (String) Block.blockRegistry.getNameForObject(block);
        return useWhitelist ? blockWhitelist.contains(id) : !blockBlacklist.contains(id);
    }

    public boolean canOverwriteBlock(Block block) {
        return overwritableBlocks.contains(Block.blockRegistry.getNameForObject(block));
    }

}
