package com.tridevmc.movingworld.common.config;

import net.minecraft.block.Block;

public class BlockDensityData {

    private final Block block;
    private final float density;

    public BlockDensityData(Block block, float density) {
        this.block = block;
        this.density = density;
    }

    public Block getBlock() {
        return block;
    }

    public float getDensity() {
        return density;
    }
}
