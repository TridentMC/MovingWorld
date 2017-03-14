package com.elytradev.movingworld.common.experiments.mixin;

import com.elytradev.movingworld.client.experiments.EntityPlayerSPProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.world.IInteractionObject;
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
        EntityPlayerSPProxy.onUpdateHook((EntityPlayerSP) getThis());
    }

    @Inject(method = "Lnet/minecraft/client/entity/EntityPlayerSP;displayGUIChest(Lnet/minecraft/inventory/IInventory;)V", at = @At(value = "INVOKE"))
    public void displayGUIChestHook(IInventory chestInventory, CallbackInfo cbi) {
        if (EntityPlayerSPProxy.PROXIES.containsKey(getThis().getGameProfile())) {
            EntityPlayerSPProxy proxy = EntityPlayerSPProxy.PROXIES.get(getThis().getGameProfile());
            Container lastContainer = proxy.openContainer;
            proxy.displayGUIChestSuper(chestInventory);
            Container currentContainer = proxy.openContainer;

            proxy.validateWrapping(lastContainer, currentContainer);
        }
    }

    @Inject(method = "Lnet/minecraft/client/entity/EntityPlayerSP;displayGui(Lnet/minecraft/world/IInteractionObject;)V", at = @At(value = "INVOKE"))
    public void displayGuiHook(IInteractionObject guiOwner, CallbackInfo cbi) {
        if (EntityPlayerSPProxy.PROXIES.containsKey(getThis().getGameProfile())) {
            EntityPlayerSPProxy proxy = EntityPlayerSPProxy.PROXIES.get(getThis().getGameProfile());
            Container lastContainer = proxy.openContainer;
            proxy.displayGuiSuper(guiOwner);
            Container currentContainer = proxy.openContainer;

            proxy.validateWrapping(lastContainer, currentContainer);
        }
    }

    //@Inject(method = "Lnet/minecraft/entity/player/EntityPlayer;openGui(Ljava/lang/Object;ILnet/minecraft/world/World;III)V", at = @At(value = "INVOKE"))
    //public void openGuiHook(CallbackInfo cbi, Object mod, int modGuiId, World world, int x, int y, int z) {
    //    if (EntityPlayerSPProxy.PROXIES.containsKey(getThis().getGameProfile())) {
    //        EntityPlayerSPProxy proxy = EntityPlayerSPProxy.PROXIES.get(getThis().getGameProfile());
    //        Container lastContainer = proxy.openContainer;
    //        proxy.openGuiSuper(mod, modGuiId, world, x, y, z);
    //        Container currentContainer = proxy.openContainer;
//
    //        proxy.validateWrapping(lastContainer, currentContainer);
    //    }
    //}

    public EntityPlayer getThis() {
        return ((EntityPlayer) (Object) this);
    }


}
