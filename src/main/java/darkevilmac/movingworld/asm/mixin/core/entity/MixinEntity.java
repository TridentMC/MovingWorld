package darkevilmac.movingworld.asm.mixin.core.entity;

import darkevilmac.movingworld.entity.IMixinEntity;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Entity.class)
public abstract class MixinEntity implements IMixinEntity {

    @Shadow
    private int nextStepDistance;

    @Shadow
    public abstract boolean canBeCollidedWith();

    @Shadow
    protected abstract void resetPositionToBB();

    @Shadow
    protected abstract void updateFallState(double y, boolean onGroundIn, Block blockIn, BlockPos pos);

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
    public void updateFall(double y, boolean onGroundIn, Block blockIn, BlockPos pos) {
        updateFallState(y, onGroundIn, blockIn, pos);
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
