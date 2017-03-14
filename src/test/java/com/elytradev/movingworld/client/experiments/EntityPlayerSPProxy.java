package com.elytradev.movingworld.client.experiments;

import com.elytradev.movingworld.common.experiments.interact.ContainerWrapper;
import com.elytradev.movingworld.common.experiments.entity.EntityMobileRegion;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiEnchantment;
import net.minecraft.client.gui.GuiHopper;
import net.minecraft.client.gui.GuiRepair;
import net.minecraft.client.gui.inventory.*;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.stats.StatBase;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IInteractionObject;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;

/**
 * Created by darkevilmac on 3/10/2017.
 */
@SideOnly(Side.CLIENT)
public class EntityPlayerSPProxy extends EntityPlayerSP {
    public static HashMap<GameProfile, EntityPlayerSPProxy> PROXIES = new HashMap<>();
    private EntityPlayerSP parent;
    private EntityMobileRegion region;

    public EntityPlayerSPProxy(EntityPlayerSP playerSP, EntityMobileRegion region) {
        super(Minecraft.getMinecraft(), region.getParentWorld(),
                playerSP.connection, playerSP.getStatFileWriter());

        this.parent = playerSP;
        this.region = region;

        Vec3d prevPos = region.region.convertRealWorldPosToRegion(new Vec3d(parent.prevPosX, parent.prevPosY, parent.prevPosZ));
        Vec3d curPos = region.region.convertRealWorldPosToRegion(new Vec3d(parent.posX, parent.posY, parent.posZ));

        this.movementInput = parent.movementInput;
        this.inventory = parent.inventory;
        this.inventoryContainer = parent.inventoryContainer;

        this.prevPosX = prevPos.xCoord;
        this.prevPosY = prevPos.yCoord;
        this.prevPosZ = prevPos.zCoord;

        this.posX = curPos.xCoord;
        this.posY = curPos.yCoord;
        this.posZ = curPos.zCoord;

        this.motionX = parent.motionX;
        this.motionY = parent.motionY;
        this.motionZ = parent.motionZ;
    }

    public static void onUpdateHook(EntityPlayerSP realPlayer) {
        EntityPlayer proxy = null;

        if (EntityPlayerSPProxy.PROXIES.containsKey(realPlayer.getGameProfile())) {
            proxy = EntityPlayerSPProxy.PROXIES.get(realPlayer.getGameProfile());
        }

        if (proxy != null && proxy.openContainer instanceof ContainerWrapper) {
            realPlayer.openContainer = proxy.openContainer;
        } else if (proxy != null) {
            proxy.openContainer = realPlayer.openContainer;
        }
    }

    public void setRegion(EntityMobileRegion region) {
        this.region = region;
    }

    @Override
    public void onUpdate() {
        Vec3d prevPos = region.region.convertRealWorldPosToRegion(new Vec3d(parent.prevPosX, parent.prevPosY, parent.prevPosZ));
        Vec3d curPos = region.region.convertRealWorldPosToRegion(new Vec3d(parent.posX, parent.posY, parent.posZ));

        this.inventory = parent.inventory;
        this.inventoryContainer = parent.inventoryContainer;

        this.prevPosX = prevPos.xCoord;
        this.prevPosY = prevPos.yCoord;
        this.prevPosZ = prevPos.zCoord;

        this.posX = curPos.xCoord;
        this.posY = curPos.yCoord;
        this.posZ = curPos.zCoord;

        this.motionX = parent.motionX;
        this.motionY = parent.motionY;
        this.motionZ = parent.motionZ;
    }

    @Override
    public void closeScreen() {
        super.closeScreen();
        parent.closeScreen();
    }

    @Override
    public void addStat(StatBase stat, int amount) {
        parent.addStat(stat, amount);
    }

    @Override
    public void displayGUIChest(IInventory chestInventory) {
        Container lastContainer = this.openContainer;
        super.displayGUIChest(chestInventory);
        Container currentContainer = this.openContainer;

        validateWrapping(lastContainer, currentContainer);

        parent.displayGUIChest(chestInventory);
    }

    @Override
    public void displayGui(IInteractionObject guiOwner) {
        Container lastContainer = this.openContainer;
        displayGuiSuper(guiOwner);
        Container currentContainer = this.openContainer;

        validateWrapping(lastContainer, currentContainer);
        parent.displayGui(guiOwner);
    }

    @Override
    public void move(MoverType type, double x, double y, double z) {
        parent.move(type, x, y, z);
    }

    public void displayGUIChestSuper(IInventory chestInventory) {
        String s = chestInventory instanceof IInteractionObject ? ((IInteractionObject) chestInventory).getGuiID() : "minecraft:container";

        if ("minecraft:chest".equals(s)) {
            this.mc.displayGuiScreen(new GuiChest(this.inventory, chestInventory));
        } else if ("minecraft:hopper".equals(s)) {
            this.mc.displayGuiScreen(new GuiHopper(this.inventory, chestInventory));
        } else if ("minecraft:furnace".equals(s)) {
            this.mc.displayGuiScreen(new GuiFurnace(this.inventory, chestInventory));
        } else if ("minecraft:brewing_stand".equals(s)) {
            this.mc.displayGuiScreen(new GuiBrewingStand(this.inventory, chestInventory));
        } else if ("minecraft:beacon".equals(s)) {
            this.mc.displayGuiScreen(new GuiBeacon(this.inventory, chestInventory));
        } else if (!"minecraft:dispenser".equals(s) && !"minecraft:dropper".equals(s)) {
            if ("minecraft:shulker_box".equals(s)) {
                this.mc.displayGuiScreen(new GuiShulkerBox(this.inventory, chestInventory));
            } else {
                this.mc.displayGuiScreen(new GuiChest(this.inventory, chestInventory));
            }
        } else {
            this.mc.displayGuiScreen(new GuiDispenser(this.inventory, chestInventory));
        }
    }

    public void displayGuiSuper(IInteractionObject guiOwner) {
        String s = guiOwner.getGuiID();

        if ("minecraft:crafting_table".equals(s)) {
            this.mc.displayGuiScreen(new GuiCrafting(this.inventory, this.world));
        } else if ("minecraft:enchanting_table".equals(s)) {
            this.mc.displayGuiScreen(new GuiEnchantment(this.inventory, this.world, guiOwner));
        } else if ("minecraft:anvil".equals(s)) {
            this.mc.displayGuiScreen(new GuiRepair(this.inventory, this.world));
        }
    }

    @Override
    public void setPositionAndUpdate(double x, double y, double z) {
        super.setPositionAndUpdate(x, y, z);
    }

    public void validateWrapping(Container lastContainer, Container currentContainer) {
        if (lastContainer != currentContainer && !(currentContainer instanceof ContainerPlayer)) {
            if (currentContainer instanceof ContainerWrapper)
                return;

            this.openContainer = new ContainerWrapper(this.openContainer);
        }
    }

    public void openGuiSuper(Object mod, int modGuiId, World world, int x, int y, int z) {
        net.minecraftforge.fml.common.network.internal.FMLNetworkHandler.openGui(this, mod, modGuiId, world, x, y, z);
    }

    @Override
    public void openGui(Object mod, int modGuiId, World world, int x, int y, int z) {
        Container lastContainer = this.openContainer;
        super.openGui(mod, modGuiId, world, x, y, z);
        Container currentContainer = this.openContainer;

        validateWrapping(lastContainer, currentContainer);
        parent.openGui(mod, modGuiId, world, x, y, z);
    }
}
