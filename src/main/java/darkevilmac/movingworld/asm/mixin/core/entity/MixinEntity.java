package darkevilmac.movingworld.asm.mixin.core.entity;

import darkevilmac.movingworld.entity.ISelectableEntity;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Entity.class)
public abstract class MixinEntity implements ISelectableEntity {

    @Shadow
    public abstract boolean canBeCollidedWith();

    @Override
    public boolean canBeSelected() {
        return canBeCollidedWith();
    }

    //@Inject(method = "moveEntity(DDD)V", at = @At("HEAD"), cancellable = true)
    //public void onMoveEntity(double x, double y, double z, CallbackInfo ci) {
    //    Entity thisEntity = (Entity) (Object) this;
    //    World worldObj = thisEntity.worldObj;
//
    //    if (thisEntity instanceof EntityMovingWorld || thisEntity.noClip || !thisEntity.canBeCollidedWith())
    //        return;
//
    //    List<EntityMovingWorld> movingWorldsInBox = worldObj.getEntitiesWithinAABB(EntityMovingWorld.class, thisEntity.getEntityBoundingBox());
//
    //    if (movingWorldsInBox != null && !movingWorldsInBox.isEmpty()) {
    //        boolean doCancel = false;
    //        for (EntityMovingWorld movingWorld : movingWorldsInBox) {
    //            MobileChunk chunk = movingWorld.getMovingWorldChunk();
//
    //            doCancel = ChunkCollisionHelper.onEntityMove(thisEntity, chunk, new Vec3(x, y, z));
    //        }
    //        if (doCancel)
    //            ci.cancel();
    //    }
    //}
}
