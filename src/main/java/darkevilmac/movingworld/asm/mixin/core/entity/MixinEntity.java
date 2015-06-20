package darkevilmac.movingworld.asm.mixin.core.entity;

import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Override Minecraft's move methods so we can tell it how to handle collision.
 */
@Mixin(Entity.class)
public class MixinEntity {

    @Inject(method = "moveEntity(DDD)V", at = @At("INVOKE"), cancellable = true)
    public void onMoveEntity(double x, double y, double z, CallbackInfo ci) {
        Entity thisEntity = (Entity) (Object) this;
        World worldObj = thisEntity.worldObj;

        // If this entity is in noClip just let the method do it's stuff.
        if (thisEntity.noClip)
            return;

        AxisAlignedBB boundingBox = new AxisAlignedBB(thisEntity.posX - 3, thisEntity.posY - 5, thisEntity.posZ - 3,
                thisEntity.posX + 3, thisEntity.posY + 5, thisEntity.posZ + 3);
        List<Entity> movingWorldsInBox = worldObj.getEntitiesWithinAABBExcludingEntity(thisEntity, boundingBox);

        if (movingWorldsInBox != null && !movingWorldsInBox.isEmpty()) {

        }
    }

}
