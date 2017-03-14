package com.elytradev.movingworld.common.experiments.interact;

import com.elytradev.movingworld.common.experiments.entity.EntityMobileRegion;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.stats.StatBase;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IInteractionObject;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.HashMap;

public class EntityPlayerMPProxy extends EntityPlayerMP {

    public static HashMap<GameProfile, EntityPlayerMPProxy> PROXIES = new HashMap<>();

    private EntityPlayerMP parent;
    private EntityMobileRegion region;

    public EntityPlayerMPProxy(EntityPlayerMP playerMP, EntityMobileRegion region) {
        super(playerMP.getServer(), (WorldServer) region.getParentWorld(),
                playerMP.getGameProfile(), MWPlayerInteractionManager.MANAGERS.get(playerMP));

        this.connection = playerMP.connection;
        this.parent = playerMP;
        this.region = region;

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

        super.onUpdate();
    }

    @Override
    public void displayGui(IInteractionObject guiOwner) {
        super.displayGui(guiOwner);
        parent.displayGui(guiOwner);
    }

    @Override
    public void displayGUIChest(IInventory chestInventory) {
        super.displayGUIChest(chestInventory);
        parent.displayGUIChest(chestInventory);
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
    public void setPositionAndUpdate(double x, double y, double z) {
        super.setPositionAndUpdate(x, y, z);
    }


    @Override
    public void openGui(Object mod, int modGuiId, World world, int x, int y, int z) {
        super.openGui(mod, modGuiId, world, x, y, z);
        parent.openGui(mod, modGuiId, world, x, y, z);
    }

    @Override
    public void move(MoverType type, double x, double y, double z) {
        parent.move(type, x, y, z);
    }


}
