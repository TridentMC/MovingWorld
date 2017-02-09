package com.elytradev.movingworld.common.util;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;

import java.util.HashMap;
import java.util.Map;

public class MaterialDensity {
    public static final float WATER_DENSITY = 1.000F;
    public static final float DEFAULT_DENSITY = 0.34f;

    private static Map<Material, Float> materialDensityMap = new HashMap<Material, Float>();
    private static Map<String, Float> blockDensityMap = new HashMap<String, Float>();

    public static void addDensity(Material mat, float dens) {
        materialDensityMap.put(mat, Float.valueOf(dens));
    }

    public static void addDensity(Block block, float dens) {
        blockDensityMap.put(Block.REGISTRY.getNameForObject(block).toString(), Float.valueOf(dens));
    }

    public static float getDensity(IBlockState state) {
        if (state == null) return DEFAULT_DENSITY;
        Float f = blockDensityMap.get(Block.REGISTRY.getNameForObject(state.getBlock()));
        if (f != null) return f.floatValue();
        return getDensity(state.getMaterial());
    }

    public static float getDensity(Material mat) {
        Float f = materialDensityMap.get(mat);
        if (f != null) return f.floatValue();
        return DEFAULT_DENSITY;
    }
}
