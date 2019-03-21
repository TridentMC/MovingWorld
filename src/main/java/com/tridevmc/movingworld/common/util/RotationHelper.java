package com.tridevmc.movingworld.common.util;

import com.tridevmc.movingworld.api.rotation.IRotationBlock;
import com.tridevmc.movingworld.api.rotation.IRotationProperty;
import com.tridevmc.movingworld.common.chunk.LocatedBlock;
import com.tridevmc.movingworld.common.rotation.RotationEnumProperty;
import com.tridevmc.movingworld.common.rotation.RotationIntegerProperty;
import com.google.common.collect.Maps;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.IProperty;
import net.minecraft.state.IntegerProperty;

import java.util.Map;
import java.util.Optional;

public class RotationHelper {

    public static final RotationHelper INSTANCE = new RotationHelper();

    private final Map<Class<? extends IProperty>, IRotationProperty> rotationProperties = Maps.newHashMap();
    private final Map<Class<? extends Block>, IRotationBlock> rotationBlocks = Maps.newHashMap();

    private RotationHelper() {
        this.rotationProperties.put(EnumProperty.class, new RotationEnumProperty());
        this.rotationProperties.put(IntegerProperty.class, new RotationIntegerProperty());
    }

    public LocatedBlock rotateBlock(LocatedBlock locatedBlock, boolean ccw) {
        IBlockState blockState = locatedBlock.state;
        if (locatedBlock.state != null) {
            IRotationBlock rotationBlock = this.getRotationBlock(locatedBlock.getBlock());
            if (rotationBlock != null) {
                locatedBlock = rotationBlock.rotate(locatedBlock, ccw);

                if (rotationBlock.fullRotation())
                    return locatedBlock;
            }

            for (IProperty prop : blockState.getProperties()) {
                IRotationProperty rotationProperty = this.getRotationProperty(prop);
                if (rotationProperty != null) {
                    // Custom rotation property found.
                    blockState = rotationProperty.rotate(blockState, ccw);
                }
            }
        }

        return new LocatedBlock(blockState, locatedBlock.tile, locatedBlock.pos, locatedBlock.posNoOffset);
    }

    public void registerRotationProperty(Class<? extends IProperty> property, IRotationProperty rotationProperty) {
        this.rotationProperties.put(property, rotationProperty);
    }

    public void registerRotationBlock(Class<? extends Block> block, IRotationBlock rotationBlock) {
        this.rotationBlocks.put(block, rotationBlock);
    }

    public IRotationProperty getRotationProperty(IProperty property) {
        if (property instanceof IRotationProperty) {
            return (IRotationProperty) property;
        } else {
            IRotationProperty rotationProperty = this.rotationProperties.get(property.getClass());
            if (rotationProperty == null) {
                Optional<IRotationProperty> first = this.rotationProperties.entrySet().stream()
                        .filter(e -> e.getKey().isAssignableFrom(property.getClass()))
                        .map(Map.Entry::getValue)
                        .findFirst();

                rotationProperty = first.orElse(null);
            }

            return rotationProperty;
        }
    }

    public IRotationBlock getRotationBlock(Block block) {
        if (block instanceof IRotationBlock) {
            return (IRotationBlock) block;
        } else {
            IRotationBlock rotationBlock = this.rotationBlocks.get(block.getClass());
            if (rotationBlock == null) {
                Optional<IRotationBlock> first = this.rotationBlocks.entrySet().stream()
                        .filter(e -> e.getKey().isAssignableFrom(block.getClass()))
                        .map(Map.Entry::getValue)
                        .findFirst();

                rotationBlock = first.orElse(null);
            }

            return rotationBlock;
        }
    }

    public int rotateInteger(int integer, int min, int max, boolean ccw) {
        int result = integer;

        if (!ccw) {
            if (result + 1 > max)
                result = min;
            else
                result = result + 1;
        } else {
            if (result - 1 < min)
                result = max;
            else
                result = result - 1;
        }

        return result;
    }

}
