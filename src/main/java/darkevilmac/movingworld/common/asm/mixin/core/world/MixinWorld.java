package darkevilmac.movingworld.common.asm.mixin.core.world;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(World.class)
public class MixinWorld {
    @Inject(method = "getCubes(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/AxisAlignedBB;)Ljava/util/List;", at = @At("RETURN"), cancellable = true)
    public void onGetCollisionBoxes(Entity source, AxisAlignedBB within, CallbackInfoReturnable<List> cbr) {
        //TODO: Experiment with getting ship collision boxes
    }
}
