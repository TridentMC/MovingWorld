package com.elytradev.movingworld.common.experiments.mixin;

import com.elytradev.movingworld.common.experiments.interact.ContainerChecks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Created by darkevilmac on 3/10/2017.
 */
@Mixin(EntityPlayerSP.class)
public abstract class MixinEntityPlayerSP {

    @Shadow
    protected Minecraft mc;

    @Inject(method = "Lnet/minecraft/client/entity/EntityPlayerSP;onUpdate()V", at = @At(value = "INVOKE"))
    public void onUpdateHook(CallbackInfo cbi) {
        ContainerChecks.checkContainer(getThis());
        ContainerChecks.checkCurrentScreen(getThis(), mc);
    }

    public EntityPlayer getThis() {
        return ((EntityPlayer) (Object) this);
    }

}
