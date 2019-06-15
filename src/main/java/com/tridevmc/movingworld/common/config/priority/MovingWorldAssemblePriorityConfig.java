package com.tridevmc.movingworld.common.config.priority;

import com.google.common.collect.Lists;
import com.tridevmc.compound.config.ConfigType;
import com.tridevmc.compound.config.ConfigValue;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.stream.Collectors;

@ConfigType(ModConfig.Type.SERVER)
public class MovingWorldAssemblePriorityConfig {

    public MovingWorldAssemblePriorityConfig() {
        List<Block> poweredBlocks = this.getPoweredBlocks();
        this.highPriorityAssemblyBlocks.addAll(poweredBlocks);
        this.lowPriorityDisassemblyBlocks.addAll(poweredBlocks);
    }

    private List<Block> getPoweredBlocks() {
        return ForgeRegistries.BLOCKS.getValues().stream()
                .filter((b) -> b.getDefaultState().getProperties().stream()
                        .anyMatch((p) -> p.getName().equals("powered")))
                .collect(Collectors.toList());
    }

    @ConfigValue(comment = "A list of blocks that should be removed from the world last during assembly.")
    public List<Block> lowPriorityAssemblyBlocks = Lists.newArrayList(Blocks.REDSTONE_WIRE, Blocks.OBSIDIAN);

    @ConfigValue(comment = "A list of blocks that should be removed from the world first during assembly.")
    public List<Block> highPriorityAssemblyBlocks = Lists.newArrayList(Blocks.NETHER_PORTAL, Blocks.PISTON, Blocks.PISTON_HEAD, Blocks.STICKY_PISTON, Blocks.MOVING_PISTON);

    @ConfigValue(comment = "A list of blocks that should be set to the world last during disassembly.")
    public List<Block> lowPriorityDisassemblyBlocks = Lists.newArrayList(Blocks.REDSTONE_WIRE, Blocks.NETHER_PORTAL);

    @ConfigValue(comment = "A list of blocks that should be set to the world first during disassembly.")
    public List<Block> highPriorityDisassemblyBlocks = Lists.newArrayList(Blocks.NETHER_PORTAL, Blocks.PISTON, Blocks.PISTON_HEAD, Blocks.STICKY_PISTON, Blocks.MOVING_PISTON);

}
