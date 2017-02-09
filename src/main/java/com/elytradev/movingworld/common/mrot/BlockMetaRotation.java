package com.elytradev.movingworld.common.mrot;

import net.minecraft.block.Block;

public class BlockMetaRotation {
    public final Block block;
    public final int[] metaRotation;
    private int bitMask;

    protected BlockMetaRotation(Block block, int[] metarotation, int bitmask) {
        if (metarotation.length != 4) throw new IllegalArgumentException("ejejejej");
        this.block = block;
        metaRotation = metarotation;
        bitMask = bitmask;
    }

    public static int wrapRotationIndex(int i) {
        return i & 3;
    }

    public int getRotatedMeta(int currentmeta, int rotate) {
        int mr;
        for (int i = 0; i < metaRotation.length; i++) {
            if (metaRotation[i] == (currentmeta & bitMask)) {
                mr = (currentmeta & ~bitMask) | (metaRotation[wrapRotationIndex(i + rotate)] & bitMask);
                return mr;
            }
        }
        return currentmeta;
    }
}
