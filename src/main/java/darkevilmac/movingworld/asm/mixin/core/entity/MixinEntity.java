package darkevilmac.movingworld.asm.mixin.core.entity;

import darkevilmac.movingworld.chunk.ChunkCollisionHelper;
import darkevilmac.movingworld.chunk.mobilechunk.MobileChunk;
import darkevilmac.movingworld.entity.EntityMovingWorld;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Override Minecraft's move methods so we can tell it how to handle collision.
 */
@Mixin(Entity.class)
public abstract class MixinEntity {

    @Shadow
    private int nextStepDistance;

    @Shadow
    protected abstract boolean canTriggerWalking();

    @Shadow
    protected abstract void updateFallState(double y, boolean onGroundIn, Block blockIn, BlockPos pos);

    @Shadow
    protected abstract void resetPositionToBB();


    @Inject(method = "moveEntity(DDD)V", at = @At("HEAD"), cancellable = true)
    public void onMoveEntity(double x, double y, double z, CallbackInfo ci) {
        Entity thisEntity = (Entity) (Object) this;
        World worldObj = thisEntity.worldObj;

        if (thisEntity instanceof EntityMovingWorld || thisEntity.noClip || !thisEntity.canBeCollidedWith())
            return;

        List<EntityMovingWorld> movingWorldsInBox = worldObj.getEntitiesWithinAABB(EntityMovingWorld.class, thisEntity.getEntityBoundingBox());

        if (movingWorldsInBox != null && !movingWorldsInBox.isEmpty()) {
            boolean doCancel = false;
            for (EntityMovingWorld movingWorld : movingWorldsInBox) {
                MobileChunk chunk = movingWorld.getMovingWorldChunk();

                doCancel = ChunkCollisionHelper.onEntityMove(thisEntity, chunk, new Vec3(x, y, z));
            }
            if (doCancel)
                ci.cancel();
        }
    }
}
