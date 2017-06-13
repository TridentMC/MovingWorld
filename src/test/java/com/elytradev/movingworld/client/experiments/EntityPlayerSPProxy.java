package com.elytradev.movingworld.client.experiments;

import com.elytradev.movingworld.common.experiments.MovingWorldExperimentsMod;
import com.elytradev.movingworld.common.experiments.entity.EntityMobileRegion;
import com.elytradev.movingworld.common.experiments.interact.ContainerChecks;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.MoverType;
import net.minecraft.inventory.IInventory;
import net.minecraft.stats.StatBase;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IInteractionObject;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;

/**
 * Created by darkevilmac on 3/10/2017.
 */
@SideOnly(Side.CLIENT)
public class EntityPlayerSPProxy extends EntityPlayerSP {
    private static HashMap<GameProfile, EntityPlayerSPProxy> PROXIES = new HashMap<>();
    private EntityPlayerSP parent;
    private EntityMobileRegion region;


    public static EntityPlayerSPProxy getProxyForPlayer(EntityPlayerSP realPlayer, EntityMobileRegion region, boolean checkSpawned) {
        if (PROXIES.containsKey(realPlayer.getGameProfile())) {
            EntityPlayerSPProxy proxy = PROXIES.get(realPlayer.getGameProfile());
            if (checkSpawned && region.getParentWorld().getEntityByID(proxy.getEntityId()) == null) {
                region.getParentWorld().spawnEntity(proxy);
            }
            return proxy;
        } else {
            EntityPlayerSPProxy proxy = new EntityPlayerSPProxy(realPlayer, region);
            region.getParentWorld().spawnEntity(proxy);

            PROXIES.put(realPlayer.getGameProfile(), proxy);
            return PROXIES.get(realPlayer.getGameProfile());
        }
    }

    public static boolean hasProxy(EntityPlayerSP realPlayer){
        return PROXIES.containsKey(realPlayer.getGameProfile());
    }
    
    public EntityPlayerSPProxy(EntityPlayerSP playerSP, EntityMobileRegion region) {
        super(Minecraft.getMinecraft(), region.getParentWorld(),
                playerSP.connection, playerSP.getStatFileWriter(), playerSP.func_192035_E());

        this.parent = playerSP;
        this.region = region;

        Vec3d prevPos = region.region.convertRealWorldPosToRegion(new Vec3d(parent.prevPosX, parent.prevPosY, parent.prevPosZ));
        Vec3d curPos = region.region.convertRealWorldPosToRegion(new Vec3d(parent.posX, parent.posY, parent.posZ));

        this.movementInput = parent.movementInput;
        this.inventory = parent.inventory;
        this.inventoryContainer = parent.inventoryContainer;

        this.prevPosX = prevPos.x;
        this.prevPosY = prevPos.y;
        this.prevPosZ = prevPos.z;

        this.posX = curPos.x;
        this.posY = curPos.y;
        this.posZ = curPos.z;

        this.motionX = parent.motionX;
        this.motionY = parent.motionY;
        this.motionZ = parent.motionZ;
    }

    @Override
    public void onUpdate() {
        Vec3d prevPos = region.region.convertRealWorldPosToRegion(new Vec3d(parent.prevPosX, parent.prevPosY, parent.prevPosZ));
        Vec3d curPos = region.region.convertRealWorldPosToRegion(new Vec3d(parent.posX, parent.posY, parent.posZ));

        this.inventory = parent.inventory;
        this.inventoryContainer = parent.inventoryContainer;
        this.openContainer = parent.openContainer;

        this.prevPosX = prevPos.x;
        this.prevPosY = prevPos.y;
        this.prevPosZ = prevPos.z;

        this.posX = curPos.x;
        this.posY = curPos.y;
        this.posZ = curPos.z;

        this.motionX = parent.motionX;
        this.motionY = parent.motionY;
        this.motionZ = parent.motionZ;
    }

    @Override
    public void closeScreen() {
        parent.closeScreen();
    }

    @Override
    public void addStat(StatBase stat, int amount) {
        parent.addStat(stat, amount);
    }

    @Override
    public void displayGUIChest(IInventory chestInventory) {
        parent.displayGUIChest(chestInventory);
        ContainerChecks.checkContainer(parent);
    }

    @Override
    public void displayGui(IInteractionObject guiOwner) {
        parent.displayGui(guiOwner);
        ContainerChecks.checkContainer(parent);
    }

    @Override
    public void move(MoverType type, double x, double y, double z) {
        parent.move(type, x, y, z);
    }

    @Override
    public void setPositionAndUpdate(double x, double y, double z) {
        super.setPositionAndUpdate(x, y, z);
    }

    @Override
    public void openGui(Object mod, int modGuiId, World world, int x, int y, int z) {
        ModContainer mc = FMLCommonHandler.instance().findContainerFor(mod);
        if (FMLCommonHandler.instance().getSide().equals(Side.CLIENT)) {
            Object guiContainer = NetworkRegistry.INSTANCE.getLocalGuiContainer(mc, this, modGuiId, region.getParentWorld(), x, y, z);
            FMLCommonHandler.instance().showGuiScreen(guiContainer);
        } else {
            // Something smells fucky around here.
            MovingWorldExperimentsMod.logger.warn("Proxy player for SP was not on client side.");
        }
        ContainerChecks.checkContainer(parent);
    }

    public EntityPlayerSP getParent() {
        return parent;
    }

    public EntityMobileRegion getRegion() {
        return region;
    }

    public void setRegion(EntityMobileRegion region) {
        this.region = region;
    }
}
