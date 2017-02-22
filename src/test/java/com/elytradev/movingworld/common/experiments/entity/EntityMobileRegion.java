package com.elytradev.movingworld.common.experiments.entity;

import com.elytradev.movingworld.client.experiments.MobileRegionWorldClient;
import com.elytradev.movingworld.common.experiments.MobileRegion;
import com.elytradev.movingworld.common.experiments.MobileRegionWorldServer;
import com.elytradev.movingworld.common.experiments.MovingWorldExperimentsMod;
import com.elytradev.movingworld.common.experiments.network.messages.client.MessageRequestData;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.profiler.Profiler;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;
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

    public boolean receivedData = false;

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

    public void setupClientForData() {
        if (!receivedData) {
            // generate the client world if needed.
            MovingWorldExperimentsMod.modProxy.getDB().addWorldForDim(region.dimension, world);
            WorldClient parentWorld = (WorldClient) MovingWorldExperimentsMod.modProxy.getDB().getWorldFromDim(region.dimension);

            mobileRegionWorld = new MobileRegionWorldClient(parentWorld.connection, genWorldSettings(), region.dimension, world.getDifficulty(), new Profiler(), parentWorld, region);

            receivedData = !receivedData;
        }
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
        return MovingWorldExperimentsMod.modProxy.getDB().getWorldFromDim(region.dimension);
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
