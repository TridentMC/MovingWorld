package darkevilmac.movingworld.asm.mixin.core.world;

import darkevilmac.movingworld.entity.EntityMovingWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

/**
 * Needed to prevent minecraft for getting bounding boxes.
 */

@Mixin(World.class)
public class MixinWorld {

    @Redirect(method = "getCollidingBoundingBoxes", at =
    @At(value = "INVOKE", target = "net.minecraft.world.World.getEntitiesWithinAABBExcludingEntity(Lnet/minecraft/entity/Entity;net/minecraft/util/AxisAlignedBB;)"
            + "Ljava/util/List;"))
    public List onGetEntitiesWithinAABBExcludingEntity(World world, final Entity entityIn, AxisAlignedBB bb, Entity entityMethodParameter, AxisAlignedBB bbMethodParameter) {
        List<Entity> list = world.getEntitiesWithinAABBExcludingEntity(entityIn, bb);

        if (list == null || (list != null && list.isEmpty()))
            return list;

        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) != null && list.get(i) instanceof EntityMovingWorld) {
                list.remove(i);
                i--;
            }
        }

        return list;
    }

}
