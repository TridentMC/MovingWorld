package darkevilmac.movingworld.config;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import darkevilmac.movingworld.MovingWorld;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraftforge.common.config.Configuration;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class MainConfig {

    public boolean iterativeAlgorithm;
    public boolean diagonalAssembly;
    public boolean useWhitelist;

    private boolean rediscoverPoweredBlocks;

    public Set<String> blockBlacklist;
    public Set<String> blockWhitelist;
    public Set<String> overwritableBlocks;

    public Set<String> highPriorityAssembly;

    private Configuration config;

    public MainConfig(Configuration config) {
        this.config = config;

        blockBlacklist = new HashSet<String>();
        blockWhitelist = new HashSet<String>();
        overwritableBlocks = new HashSet<String>();

        highPriorityAssembly = new HashSet<String>();
    }

    public void loadAndSave() {
        Block[] defaultBlockBlacklist = {(Blocks.dirt), (Blocks.grass), (Blocks.sand), (Blocks.gravel), (Blocks.clay), (Blocks.ice), (Blocks.water), (Blocks.flowing_water), (Blocks.flowing_lava), (Blocks.lava), (Blocks.snow), Blocks.snow_layer, (Blocks.waterlily), (Blocks.netherrack), (Blocks.soul_sand), Blocks.tallgrass};
        Block[] defaultBlockWhiteList = {Blocks.acacia_stairs, Blocks.activator_rail, Blocks.anvil, Blocks.bed, Blocks.birch_stairs, Blocks.bookshelf, Blocks.brewing_stand, Blocks.brick_block, Blocks.brick_stairs, Blocks.cactus, Blocks.cake, Blocks.carpet, Blocks.coal_block, Blocks.cobblestone, Blocks.cobblestone_wall, Blocks.command_block, Blocks.crafting_table, Blocks.dark_oak_stairs, Blocks.detector_rail, Blocks.diamond_block, Blocks.dispenser, Blocks.dropper, Blocks.daylight_detector, Blocks.double_stone_slab, Blocks.double_wooden_slab, Blocks.emerald_block, Blocks.enchanting_table, Blocks.end_stone, Blocks.ender_chest, Blocks.acacia_fence, Blocks.birch_fence, Blocks.dark_oak_fence, Blocks.jungle_fence, Blocks.nether_brick_fence, Blocks.oak_fence, Blocks.spruce_fence, Blocks.acacia_fence_gate, Blocks.birch_fence_gate, Blocks.dark_oak_fence_gate, Blocks.jungle_fence_gate, Blocks.oak_fence_gate, Blocks.spruce_fence_gate, Blocks.flower_pot, Blocks.furnace, Blocks.fire, Blocks.glass, Blocks.glass_pane, Blocks.glowstone, Blocks.gold_block, Blocks.golden_rail, Blocks.hardened_clay, Blocks.hay_block, Blocks.heavy_weighted_pressure_plate, Blocks.hopper, Blocks.iron_bars, Blocks.iron_block, Blocks.iron_door, Blocks.jukebox, Blocks.jungle_stairs, Blocks.ladder, Blocks.lapis_block, Blocks.lever, Blocks.light_weighted_pressure_plate, Blocks.lit_furnace, Blocks.lit_pumpkin, Blocks.lit_redstone_lamp, Blocks.log, Blocks.log2, Blocks.melon_block, Blocks.mob_spawner, Blocks.monster_egg, Blocks.mossy_cobblestone, Blocks.nether_brick, Blocks.nether_brick_fence, Blocks.nether_brick_stairs, Blocks.noteblock, Blocks.oak_stairs, Blocks.obsidian, Blocks.planks, Blocks.pumpkin, Blocks.piston, Blocks.piston_extension, Blocks.piston_head, Blocks.powered_comparator, Blocks.powered_repeater, Blocks.quartz_block, Blocks.quartz_stairs, Blocks.rail, Blocks.redstone_block, Blocks.redstone_torch, Blocks.redstone_wire, Blocks.sandstone, Blocks.sandstone_stairs, Blocks.skull, Blocks.sponge, Blocks.spruce_stairs, Blocks.stained_hardened_clay, Blocks.standing_sign, Blocks.stone_brick_stairs, Blocks.stone_button, Blocks.stone_pressure_plate, Blocks.stone_stairs, Blocks.stonebrick, Blocks.stained_glass, Blocks.stained_glass_pane, Blocks.sticky_piston, Blocks.stone_slab, Blocks.tnt, Blocks.torch, Blocks.trapdoor, Blocks.trapped_chest, Blocks.tripwire, Blocks.tripwire_hook, Blocks.unlit_redstone_torch, Blocks.unpowered_comparator, Blocks.unpowered_repeater, Blocks.wall_sign, Blocks.web, Blocks.wooden_button, Blocks.acacia_door, Blocks.birch_door, Blocks.dark_oak_door, Blocks.jungle_door, Blocks.oak_door, Blocks.spruce_door, Blocks.wooden_pressure_plate, Blocks.wool, Blocks.wooden_slab};
        Block[] defaultOverWritableBlocks = {Blocks.tallgrass, Blocks.waterlily};
        Block[] defaultHighPriorityAssemblyBlocks = {Blocks.piston, Blocks.piston_extension, Blocks.piston_head};

        String[] blockBlackListNames = new String[defaultBlockBlacklist.length];
        for (int i = 0; i < defaultBlockBlacklist.length; i++) {
            blockBlackListNames[i] = Block.blockRegistry.getNameForObject(defaultBlockBlacklist[i]).toString();
        }

        String[] blockWhiteListNames = new String[6 + defaultBlockWhiteList.length];
        for (int i = 0; i < blockWhiteListNames.length - 6; i++) {
            blockWhiteListNames[i] = Block.blockRegistry.getNameForObject(defaultBlockWhiteList[i]).toString();
        }

        String[] highPriorityAssemblyBlockNames = new String[defaultHighPriorityAssemblyBlocks.length];
        for (int i = 0; i < defaultHighPriorityAssemblyBlocks.length; i++) {
            highPriorityAssemblyBlockNames[i] = Block.blockRegistry.getNameForObject(defaultHighPriorityAssemblyBlocks[i]).toString();
        }

        blockWhiteListNames[blockWhiteListNames.length - 6] = "ArchimedesShipsPlus:marker";
        blockWhiteListNames[blockWhiteListNames.length - 5] = "ArchimedesShipsPlus:floater";
        blockWhiteListNames[blockWhiteListNames.length - 4] = "ArchimedesShipsPlus:balloon";
        blockWhiteListNames[blockWhiteListNames.length - 3] = "ArchimedesShipsPlus:gauge";
        blockWhiteListNames[blockWhiteListNames.length - 2] = "ArchimedesShipsPlus:seat";
        blockWhiteListNames[blockWhiteListNames.length - 1] = "ArchimedesShipsPlus:engine";

        String[] overWritableBlockNames = new String[defaultOverWritableBlocks.length];
        for (int i = 0; i < defaultOverWritableBlocks.length; i++) {
            overWritableBlockNames[i] = Block.blockRegistry.getNameForObject(defaultOverWritableBlocks[i]).toString();
        }
        config.load();
        iterativeAlgorithm = config.get(Configuration.CATEGORY_GENERAL, "Use Iterative Algorithm", false).getBoolean();
        diagonalAssembly = config.get(Configuration.CATEGORY_GENERAL, "Assemble Diagonal Blocks NOTE:Can be overridden by mods!", false).getBoolean();
        useWhitelist = config.get("mobile_chunk", "use_whitelist", false, "Switch this property to select the block restriction list to use. 'true' for the 'allowed_blocks' whitelist, 'false' for the 'forbidden_blocks' blacklist.").getBoolean(false);
        rediscoverPoweredBlocks = config.get(Configuration.CATEGORY_GENERAL, "Rediscover powered blocks on next restart?", true).getBoolean();

        String[] forbiddenBlocks = config.get("mobile_chunk", "forbidden_blocks", blockBlackListNames, "A list of blocks that will not be added to a Moving World.").getStringList();
        String[] allowedBlocks = config.get("mobile_chunk", "allowed_blocks", blockWhiteListNames, "A list of blocks that are allowed on a Moving World.").getStringList();
        String[] overwritableBlocks = config.get("mobile_chunk", "overwritable_blocks", overWritableBlockNames, "A list of blocks that may be overwritten when decompiling a Moving World.").getStringList();
        String[] highPriorityAssemblyBlocks = config.get("mobile_chunk", "highpriorityassembly_blocks", highPriorityAssemblyBlockNames, "A list of blocks that should be set to air first, and then placed last when disassembled.").getStringList();

        Collections.addAll(blockBlacklist, forbiddenBlocks);
        Collections.addAll(blockWhitelist, allowedBlocks);
        Collections.addAll(this.overwritableBlocks, overwritableBlocks);
        Collections.addAll(highPriorityAssembly, highPriorityAssemblyBlocks);
    }

    public void discoverPoweredBlocks() {
        if (rediscoverPoweredBlocks) {
            ArrayList<String> poweredBlockNames = new ArrayList<String>();
            ArrayList<Block> allBlocks = Lists.newArrayList(Block.blockRegistry.iterator());

            for (Block checkBlock : allBlocks) {
                IBlockState state = checkBlock.getDefaultState();
                for (IProperty prop : (java.util.Set<IProperty>) state.getProperties().keySet()) {
                    if (prop.getName().equals("powered")) {
                        String poweredBlockName = Block.blockRegistry.getNameForObject(checkBlock).toString();
                        poweredBlockNames.add(poweredBlockName);
                        MovingWorld.logger.info("Found powered block with name: " + poweredBlockName);
                    }
                }
            }

            String[] discoveredPoweredBlockNames = new String[poweredBlockNames.size()];
            for (int i = 0; i < poweredBlockNames.size(); i++) {
                discoveredPoweredBlockNames[i] = poweredBlockNames.get(i);
            }

            Block[] defaultHighPriorityAssemblyBlocks = {Blocks.piston, Blocks.piston_extension, Blocks.piston_head};

            String[] defaultHighPriorityAssemblyBlockNames = new String[defaultHighPriorityAssemblyBlocks.length];
            for (int i = 0; i < defaultHighPriorityAssemblyBlocks.length; i++) {
                defaultHighPriorityAssemblyBlockNames[i] = Block.blockRegistry.getNameForObject(defaultHighPriorityAssemblyBlocks[i]).toString();
            }

            config.get("mobile_chunk", "highpriorityassembly_blocks", defaultHighPriorityAssemblyBlockNames, "A list of blocks that should be set to air first, and then placed last when disassembled.").set(ArrayUtils.addAll(defaultHighPriorityAssemblyBlockNames, discoveredPoweredBlockNames));
            config.get(Configuration.CATEGORY_GENERAL, "Rediscover powered blocks on next restart?", true).set(false);

            highPriorityAssembly = Sets.newHashSet(ArrayUtils.addAll(defaultHighPriorityAssemblyBlockNames, discoveredPoweredBlockNames));
        }

        config.save();
    }

    public boolean isBlockAllowed(Block block) {
        String id = Block.blockRegistry.getNameForObject(block).toString();
        return useWhitelist ? blockWhitelist.contains(id) : !blockBlacklist.contains(id);
    }

    public boolean canOverwriteBlock(Block block) {
        return overwritableBlocks.contains(Block.blockRegistry.getNameForObject(block));
    }

}
