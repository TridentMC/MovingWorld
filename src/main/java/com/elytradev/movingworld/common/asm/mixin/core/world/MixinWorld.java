package com.elytradev.movingworld.common.asm.mixin.core.world;

import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(World.class)
public class MixinWorld {
    //@Inject(method = "isValid", at = @At("RETURN"), cancellable = true)
    //public void onIsValid(BlockPos pos, CallbackInfoReturnable<Boolean> cbr) {
    //    if (RegionPool.POOLS.containsKey(getThis().provider.getDimension())) {
    //        if (cbr.getReturnValue()) {
    //            cbr.setReturnValue(RegionPool.POOLS.get(getThis().provider.getDimension()).regions
    //                    .containsKey(new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4)));
    //        }
    //    }
    //}

    private World getThis() {
        return ((World) (Object) this);
    }
}
