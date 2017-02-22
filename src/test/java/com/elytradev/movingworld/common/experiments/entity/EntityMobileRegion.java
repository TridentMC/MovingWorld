package com.elytradev.movingworld.common.experiments.entity;

import com.elytradev.movingworld.common.experiments.MobileRegion;
import com.elytradev.movingworld.common.experiments.MobileRegionWorldServer;
import com.elytradev.movingworld.common.experiments.network.messages.client.MessageRequestData;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Experimental MobileRegion entity.
 */
public class EntityMobileRegion extends Entity implements IEntityAdditionalSpawnData {

    public MobileRegion region;
    public World mobileRegionWorld;

    public EntityMobileRegion(World worldIn, MobileRegion region) {
        super(worldIn);

        this.region = region;
        if (world.isRemote) {
            initClient();
        } else {
            initCommon();
        }
    }

    public EntityMobileRegion(World worldIn) {
        super(worldIn);
    }

    @Override
    protected void entityInit() {
        // nah
    }

    protected void initCommon() {
        this.mobileRegionWorld = new MobileRegionWorldServer(getParentWorld().getMinecraftServer(),
                region.dimension, getParentWorld().profiler, world, getParentWorld(), region);
    }

    @SideOnly(Side.CLIENT)
    protected void initClient() {
        new MessageRequestData(this).sendToServer();
    }

    @Override
    public void onEntityUpdate() {
        super.onEntityUpdate();
        // stub for debug output.
    }

    private WorldSettings genWorldSettings() {
        WorldSettings settings = new WorldSettings(0L,
                getParentWorld().getWorldInfo().getGameType(),
                false,
                getParentWorld().getWorldInfo().isHardcoreModeEnabled(),
                getParentWorld().getWorldType());

        return settings;
    }

    public World getParentWorld() {
        return DimensionManager.getWorld(region.dimension);
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {

    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {

    }

    @Override
    public void writeSpawnData(ByteBuf buffer) {
        ByteBufUtils.writeTag(buffer, region.writeToCompound());
    }

    @Override
    public void readSpawnData(ByteBuf additionalData) {
        NBTTagCompound regionCompound = ByteBufUtils.readTag(additionalData);

        region = MobileRegion.getRegionFor(regionCompound);
    }
}
