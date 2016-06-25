package darkevilmac.movingworld.common.asm.mixin.core.entity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import darkevilmac.movingworld.common.entity.IMixinEntity;

@Mixin(Entity.class)
public abstract class MixinEntity implements IMixinEntity {

    @Shadow
    private int nextStepDistance;

    @Shadow
    public abstract boolean canBeCollidedWith();

    @Shadow
    protected abstract void resetPositionToBB();

    @Shadow
    protected abstract void updateFallState(double y, boolean onGroundIn, IBlockState state, BlockPos pos);

    @Shadow
    protected abstract boolean canTriggerWalking();

    @Override
    public boolean canBeSelected() {
        return canBeCollidedWith();
    }

    @Override
    public void resetPosToBB() {
        resetPositionToBB();
    }

    @Override
    public void updateFall(double y, boolean onGroundIn, IBlockState state, BlockPos pos) {
        updateFallState(y, onGroundIn, state, pos);
    }

    @Override
    public int getNextStepDistance() {
        return nextStepDistance;
    }

    @Override
    public void setNextStepDistance(int nextStepDistance) {
        this.nextStepDistance = nextStepDistance;
    }

    @Override
    public boolean doesTriggerWalking() {
        return this.canTriggerWalking();
    }

}
