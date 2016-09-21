package io.github.elytra.movingworld.common.asm.mixin.core.world;

import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(World.class)
public class MixinWorld {
    //@Inject(method = "getCollisionBoxes(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/AxisAlignedBB;)Ljava/util/List;", at = @At("RETURN"), cancellable = true)
    //public void onGetCollisionBoxes(Entity source, AxisAlignedBB within, CallbackInfoReturnable<List> cbr) {
    //    //TODO: Experiment with getting ship collision boxes
    //}
}
