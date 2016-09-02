package io.github.elytra.movingworld.common.entity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;

/**
 * We have a mixin, we might as well take advantage of it by making some methods accessible with a
 * cast.
 */
public interface IMixinEntity {

    boolean canBeSelected();

    void resetPosToBB();

    void updateFall(double y, boolean onGroundIn, IBlockState state, BlockPos pos);

    int getNextStepDistance();

    void setNextStepDistance(int nextStepDistance);

    boolean doesTriggerWalking();

}
