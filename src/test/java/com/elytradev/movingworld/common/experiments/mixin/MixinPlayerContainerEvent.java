package com.elytradev.movingworld.common.experiments.mixin;

import com.elytradev.movingworld.common.experiments.interact.ContainerWrapper;
import net.minecraft.inventory.Container;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Created by darkevilmac on 3/13/2017.
 */
@Mixin(PlayerContainerEvent.class)
public class MixinPlayerContainerEvent {

    @Inject(method = "Lnet/minecraftforge/event/entity/player/PlayerContainerEvent;getContainer()Lnet/minecraft/inventory/Container;", at = @At(value = "RETURN"))
    public void getContainer(CallbackInfoReturnable<Container> cbir) {
        if (cbir != null && cbir.getReturnValue() instanceof ContainerWrapper) {
            cbir.setReturnValue(((ContainerWrapper) cbir.getReturnValue()).realContainer);
        }
    }
}
