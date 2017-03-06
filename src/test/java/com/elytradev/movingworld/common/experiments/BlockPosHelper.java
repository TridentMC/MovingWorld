package com.elytradev.movingworld.common.experiments;

import net.minecraft.util.math.BlockPos;

/**
 * Created by darkevilmac on 3/5/2017.
 */
public class BlockPosHelper {

    /**
     * Takes two positions and returns a position containing the smallest coordinates of the given two.
     * @param one
     * @param two
     * @return
     */
    public static BlockPos min(BlockPos one, BlockPos two) {
        return new BlockPos(Math.min(one.getX(), two.getX()),
                Math.min(one.getY(), two.getY()), Math.min(one.getZ(), two.getZ()));
    }

    /**
     * Takes two positions and returns a position containing the largest coordinates of the given two.
     * @param one
     * @param two
     * @return
     */
    public static BlockPos max(BlockPos one, BlockPos two) {
        return new BlockPos(Math.max(one.getX(), two.getX()),
                Math.max(one.getY(), two.getY()), Math.max(one.getZ(), two.getZ()));
    }

}
