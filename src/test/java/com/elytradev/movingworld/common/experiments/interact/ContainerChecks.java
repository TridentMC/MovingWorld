package com.elytradev.movingworld.common.experiments.interact;

import com.elytradev.movingworld.common.experiments.proxy.ContainerInterceptor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.sf.cglib.proxy.Enhancer;

/**
 * Created by darkevilmac on 3/14/2017.
 */
public class ContainerChecks {

    @SideOnly(Side.CLIENT)
    public static void checkCurrentScreen(EntityPlayer player, Minecraft mc) {
        if (mc.currentScreen != null && mc.currentScreen instanceof GuiContainer) {
            if (player.openContainer != null && Enhancer.isEnhanced(player.openContainer.getClass())) {
                ((GuiContainer) mc.currentScreen).inventorySlots = player.openContainer;
            }
        }
    }

    public static void checkContainer(EntityPlayer player) {
        if (player.openContainer != null
                && player.openContainer != player.inventoryContainer
                && !Enhancer.isEnhanced(player.openContainer.getClass())) {
            System.out.println("Making container proxy... " + player.openContainer);
            player.openContainer = (Container) ContainerInterceptor.createProxy(player.openContainer);
            System.out.println("Proxy made... " + player.openContainer);
        }
    }
}
