package io.github.elytra.movingworld.common.config.priority;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

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

import io.github.elytra.movingworld.MovingWorldMod;

public class AssemblePriorityConfig {

    public static String CONFIG_VERSION = "MovingWorldCFG.1.0.0";
    public String detectedVersion;

    private Configuration config;

    private Set<String> highPriorityAssembly;
    private Set<String> lowPriorityAssembly;
    private Set<String> highPriorityDisassembly;
    private Set<String> lowPriorityDisassembly;

    private boolean rediscoverPoweredBlocks;

    private Block[] defaultHighPriorityAssemblyBlocks = {Blocks.PORTAL, Blocks.PISTON_EXTENSION, Blocks.PISTON_HEAD, Blocks.STICKY_PISTON, Blocks.PISTON, Blocks.PORTAL};
    private Block[] defaultLowPriorityAssemblyBlocks = {Blocks.REDSTONE_WIRE, Blocks.OBSIDIAN};

    private Block[] defaultHighPriorityDisassemblyBlocks = {Blocks.PISTON_HEAD, Blocks.PISTON_EXTENSION};
    private Block[] defaultLowPriorityDisassemblyBlocks = {Blocks.END_PORTAL, Blocks.PORTAL, Blocks.REDSTONE_WIRE};

    private Set<String> highPriorityAssemblyBlocksToRegister;
    private Set<String> lowPriorityAssemblyBlocksToRegister;
    private Set<String> highPriorityDisassemblyBlocksToRegister;
    private Set<String> lowPriorityDisassemblyBlocksToRegister;

    public AssemblePriorityConfig(Configuration configuration) {
        this.config = configuration;

        highPriorityAssembly = new HashSet<String>();
        lowPriorityAssembly = new HashSet<String>();
        highPriorityDisassembly = new HashSet<String>();
        lowPriorityDisassembly = new HashSet<String>();

        highPriorityAssemblyBlocksToRegister = new HashSet<String>();
        lowPriorityAssemblyBlocksToRegister = new HashSet<String>();
        highPriorityDisassemblyBlocksToRegister = new HashSet<String>();
        lowPriorityDisassemblyBlocksToRegister = new HashSet<String>();
    }

    public void loadAndSavePreInit() {
        config.load();

        String[] highPriorityAssemblyBlocks = config.get("mobile_chunk", "highpriorityassembly_blocks",
                blockArrayToBlockNameArray(defaultHighPriorityAssemblyBlocks), "A list of blocks that should be set to air first.").getStringList();
        String[] lowPriorityAssemblyBlocks = config.get("mobile_chunk", "lowpriorityassembly_blocks",
                blockArrayToBlockNameArray(defaultLowPriorityAssemblyBlocks), "A list of blocks that should be set to air last.").getStringList();
        String[] highPriorityDisassemblyBlocks = config.get("mobile_chunk", "highprioritydisassembly_blocks",
                blockArrayToBlockNameArray(defaultHighPriorityDisassemblyBlocks), "A list of blocks that should be set to the world first.").getStringList();
        String[] lowPriorityDisassemblyBlocks = config.get("mobile_chunk", "lowprioritydisassembly_blocks",
                blockArrayToBlockNameArray(defaultLowPriorityDisassemblyBlocks), "A list of blocks that should be set to the world last.").getStringList();

        Collections.addAll(highPriorityAssembly, highPriorityAssemblyBlocks);
        Collections.addAll(lowPriorityAssembly, lowPriorityAssemblyBlocks);
        Collections.addAll(highPriorityDisassembly, highPriorityDisassemblyBlocks);
        Collections.addAll(lowPriorityDisassembly, lowPriorityDisassemblyBlocks);

        detectedVersion = config.get("DONT TOUCH", "CONFIG VERSION", AssemblePriorityConfig.CONFIG_VERSION).getString();
        rediscoverPoweredBlocks = config.get(Configuration.CATEGORY_GENERAL, "Rediscover powered blocks on next restart?", true).getBoolean();
        if (detectedVersion != AssemblePriorityConfig.CONFIG_VERSION) {
            config.get("DONT TOUCH", "CONFIG VERSION", AssemblePriorityConfig.CONFIG_VERSION).set(AssemblePriorityConfig.CONFIG_VERSION);
            rediscoverPoweredBlocks = true;
        }

        config.save();
    }

    public void loadAndSaveInit() {
        config.load();

        discoverPoweredBlocks();

        config.save();
    }

    public String[] blockArrayToBlockNameArray(Block[] blocks) {
        String[] retVal = new String[blocks.length];

        for (int i = 0; i < blocks.length; i++) {
            retVal[i] = Block.REGISTRY.getNameForObject(blocks[i]).toString();
        }

        return retVal;
    }

    public void loadAndSavePostInit() {
        config.load();

        if (!highPriorityAssemblyBlocksToRegister.isEmpty()) {
            highPriorityAssembly.addAll(highPriorityAssemblyBlocksToRegister);

            String[] highPriorityAssemblyNames = new String[highPriorityAssembly.size()];
            highPriorityAssemblyNames = highPriorityAssembly.toArray(highPriorityAssemblyNames);

            config.get("mobile_chunk", "highpriorityassembly_blocks",
                    blockArrayToBlockNameArray(defaultHighPriorityAssemblyBlocks), "A list of blocks that should be set to air first.").set(highPriorityAssemblyNames);

        }
        if (!lowPriorityAssemblyBlocksToRegister.isEmpty()) {
            lowPriorityAssembly.addAll(lowPriorityAssemblyBlocksToRegister);

            String[] lowPriorityAssemblyNames = new String[lowPriorityAssembly.size()];
            lowPriorityAssemblyNames = lowPriorityAssembly.toArray(lowPriorityAssemblyNames);
            config.get("mobile_chunk", "lowpriorityassembly_blocks",
                    blockArrayToBlockNameArray(defaultLowPriorityAssemblyBlocks), "A list of blocks that should be set to air last.").set(lowPriorityAssemblyNames);

        }
        if (!highPriorityDisassemblyBlocksToRegister.isEmpty()) {
            highPriorityDisassembly.addAll(highPriorityDisassemblyBlocksToRegister);

            String[] highPriorityAssemblyNames = new String[highPriorityAssembly.size()];
            highPriorityAssemblyNames = highPriorityAssembly.toArray(highPriorityAssemblyNames);
            config.get("mobile_chunk", "highprioritydisassembly_blocks",
                    blockArrayToBlockNameArray(defaultHighPriorityDisassemblyBlocks), "A list of blocks that should be set to the world first.").set(highPriorityAssemblyNames);
        }
        if (!lowPriorityAssemblyBlocksToRegister.isEmpty()) {
            lowPriorityAssembly.addAll(lowPriorityAssemblyBlocksToRegister);

            String[] lowPriorityAssemblyNames = new String[lowPriorityAssembly.size()];
            lowPriorityAssemblyNames = lowPriorityAssembly.toArray(lowPriorityAssemblyNames);
            config.get("mobile_chunk", "lowprioritydisassembly_blocks",
                    blockArrayToBlockNameArray(defaultLowPriorityDisassemblyBlocks), "A list of blocks that should be set to the world last.").set(lowPriorityAssemblyNames);

        }

        config.save();
    }

    void discoverPoweredBlocks() {
        if (rediscoverPoweredBlocks) {
            ArrayList<String> poweredBlockNames = new ArrayList<String>();
            ArrayList<Block> allBlocks = Lists.newArrayList(Block.REGISTRY.iterator());

            for (Block checkBlock : allBlocks) {
                IBlockState state = checkBlock.getDefaultState();
                for (IProperty prop : state.getProperties().keySet()) {
                    if (prop.getName().equals("powered")) {
                        String poweredBlockName = Block.REGISTRY.getNameForObject(checkBlock).toString();
                        poweredBlockNames.add(poweredBlockName);
                        MovingWorldMod.logger.info("Found powered block with name: " + poweredBlockName);
                    }
                }
            }

            String[] discoveredPoweredBlockNames = new String[poweredBlockNames.size()];
            for (int i = 0; i < poweredBlockNames.size(); i++) {
                discoveredPoweredBlockNames[i] = poweredBlockNames.get(i);
            }

            String[] defaultHighPriorityAssemblyBlockNames = new String[defaultHighPriorityAssemblyBlocks.length];
            for (int i = 0; i < defaultHighPriorityAssemblyBlocks.length; i++) {
                defaultHighPriorityAssemblyBlockNames[i] = Block.REGISTRY.getNameForObject(defaultHighPriorityAssemblyBlocks[i]).toString();
            }

            config.get("mobile_chunk", "highpriorityassembly_blocks", defaultHighPriorityAssemblyBlockNames, "A list of blocks that should be set to air first, and then placed last when disassembled.").set(ArrayUtils.addAll(defaultHighPriorityAssemblyBlockNames, discoveredPoweredBlockNames));
            config.get(Configuration.CATEGORY_GENERAL, "Rediscover powered blocks on next restart?", true).set(false);

            highPriorityAssembly = Sets.newHashSet(ArrayUtils.addAll(defaultHighPriorityAssemblyBlockNames, discoveredPoweredBlockNames));
        }
    }

    public void registerHighPriorityAssemblyBlock(Block block) {
        highPriorityAssemblyBlocksToRegister.add(Block.REGISTRY.getNameForObject(block).toString());
    }

    public void registerLowPriorityAssemblyBlock(Block block) {
        lowPriorityAssemblyBlocksToRegister.add(Block.REGISTRY.getNameForObject(block).toString());
    }

    public void registerHighPriorityDisassemblyBlock(Block block) {
        highPriorityDisassemblyBlocksToRegister.add(Block.REGISTRY.getNameForObject(block).toString());
    }

    public void registerLowPriorityDisassemblyBlock(Block block) {
        lowPriorityDisassemblyBlocksToRegister.add(Block.REGISTRY.getNameForObject(block).toString());
    }

    public Set<String> getHighPriorityAssembly() {
        return highPriorityAssembly;
    }

    public Set<String> getLowPriorityAssembly() {
        return lowPriorityAssembly;
    }

    public Set<String> getHighPriorityDisassembly() {
        return highPriorityDisassembly;
    }

    public Set<String> getLowPriorityDisassembly() {
        return lowPriorityDisassembly;
    }

}
