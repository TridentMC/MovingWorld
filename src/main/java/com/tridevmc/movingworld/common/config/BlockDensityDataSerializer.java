package com.tridevmc.movingworld.common.config;

import com.tridevmc.compound.config.IConfigObjectSerializer;
import com.tridevmc.compound.config.RegisteredConfigObjectSerializer;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;

@RegisteredConfigObjectSerializer("movingworld")
public class BlockDensityDataSerializer implements IConfigObjectSerializer<BlockDensityData> {
    @Override
    public String toString(Class aClass, BlockDensityData o) {
        if (o == null)
            return "null";

        return String.format("%s=%s", ForgeRegistries.BLOCKS.getKey(o.getBlock()).toString(), o.getDensity());
    }

    @Override
    public BlockDensityData fromString(Class aClass, String s) {
        if (s.equals("null"))
            return null;

        String[] data = s.split("=");
        ResourceLocation blockKey = new ResourceLocation(data[0]);
        float density = Float.valueOf(data[1]);
        IForgeRegistry<Block> blocks = ForgeRegistries.BLOCKS;
        if (blocks.containsKey(blockKey)) {
            return new BlockDensityData(blocks.getValue(blockKey), density);
        } else {
            return null;
        }
    }

    @Override
    public boolean accepts(Class aClass) {
        return aClass.equals(BlockDensityData.class);
    }
}
