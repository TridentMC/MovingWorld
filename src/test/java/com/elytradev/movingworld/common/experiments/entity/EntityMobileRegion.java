package com.elytradev.movingworld.common.experiments.entity;

import com.elytradev.movingworld.client.experiments.MobileRegionWorldClient;
import com.elytradev.movingworld.common.experiments.region.MobileRegion;
import com.elytradev.movingworld.common.experiments.world.MobileRegionWorldServer;
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

    private boolean requestData = true;
    private boolean receivedData = false;

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
        this(worldIn, null);
    }

    @Override
    protected void entityInit() {
        // nah
    }

    protected void initCommon() {
        this.mobileRegionWorld = new MobileRegionWorldServer(getParentWorld().getMinecraftServer(),
                region.dimension, getParentWorld().profiler, world, getParentWorld(), region);

        requestData = false;
    }

    @SideOnly(Side.CLIENT)
    protected void initClient() {
    }

    public void setupClientForData() {
        if (!receivedData) {
            if (!world.isRemote)
                return;
            // generate the client world if needed.
            MovingWorldExperimentsMod.modProxy.getClientDB().addWorldForDim(region.dimension, world);
            WorldClient parentWorld = (WorldClient) MovingWorldExperimentsMod.modProxy.getClientDB().getWorldFromDim(region.dimension);

            mobileRegionWorld = new MobileRegionWorldClient(parentWorld.connection, genWorldSettings(), region.dimension, world.getDifficulty(), new Profiler(), parentWorld, region);

            receivedData = !receivedData;
        }
    }

    @Override
    public void onEntityUpdate() {
        super.onEntityUpdate();
        if (requestData) {
            new MessageRequestData(this).sendToServer();
            requestData = false;
        }

        region.x = this.posX;
        region.y = this.posY;
        region.z = this.posZ;
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
        if (world.isRemote) {
            return MovingWorldExperimentsMod.modProxy.getClientDB().getWorldFromDim(region.dimension);
        } else {
            return MovingWorldExperimentsMod.modProxy.getCommonDB().getWorldFromDim(region.dimension);
        }
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
