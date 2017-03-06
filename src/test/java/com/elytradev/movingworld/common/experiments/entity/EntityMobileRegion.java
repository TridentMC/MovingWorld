package com.elytradev.movingworld.common.experiments.entity;

import com.elytradev.movingworld.client.experiments.MobileRegionWorldClient;
import com.elytradev.movingworld.common.experiments.MovingWorldExperimentsMod;
import com.elytradev.movingworld.common.experiments.network.messages.client.MessageRequestData;
import com.elytradev.movingworld.common.experiments.region.MobileRegion;
import com.elytradev.movingworld.common.experiments.world.MobileRegionWorldServer;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings;
import net.minecraftforge.common.ForgeChunkManager;
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
    public ForgeChunkManager.Ticket chunkTicket;
    private AxisAlignedBB boundingBox;

    private MessageRequestData requestedData;
    private boolean requestData = true;
    private boolean receivedData = false;

    public EntityMobileRegion(World worldIn, MobileRegion region, AxisAlignedBB bb) {
        super(worldIn);

        this.region = region;

        if (bb != null) {
            boundingBox = bb;
        }
        if (world.isRemote) {
            initClient();
        } else {
            initCommon();
        }
    }

    public EntityMobileRegion(World worldIn) {
        this(worldIn, null, null);
    }

    @Override
    public void fall(float distance, float damageMultiplier) {
        // Nah.
    }

    @Override
    public void setPosition(double x, double y, double z) {
        super.setPosition(x, y, z);

        if (region == null)
            return;

        region.x = x;
        region.y = y;
        region.z = z;
    }

    @Override
    protected void entityInit() {
        // Nah.
    }

    @Override
    public AxisAlignedBB getEntityBoundingBox() {
        return boundingBox;
    }

    protected void initCommon() {
        this.mobileRegionWorld = new MobileRegionWorldServer(getParentWorld().getMinecraftServer(),
                region.dimension, getParentWorld().profiler, world, getParentWorld(), region);

        chunkTicket = ForgeChunkManager.requestTicket(MovingWorldExperimentsMod.instance, getParentWorld(), ForgeChunkManager.Type.NORMAL);

        for (int cX = region.regionMin.chunkXPos; cX < region.regionMax.chunkXPos; cX++) {
            for (int cZ = region.regionMin.chunkZPos; cZ < region.regionMax.chunkZPos; cZ++) {
                ForgeChunkManager.forceChunk(chunkTicket, new ChunkPos(cX, cZ));
            }
        }

        chunkTicket.getModData().setTag("Region", region.writeToCompound());

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
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public void onEntityUpdate() {
        super.onEntityUpdate();
        if (region == null || world == null)
            return;

        if (world.isRemote && requestData) {
            this.setupClientForData();
            requestData = false;
        }

        if (world.getTotalWorldTime() % 15 == 0) {
            System.out.println(getEntityBoundingBox());
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
        region = MobileRegion.getRegionFor(compound.getCompoundTag("Region"));

        if (mobileRegionWorld == null) {
            if (!world.isRemote)
                initCommon();
            else
                initClient();
        }
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        compound.setTag("Region", region.writeToCompound());
    }

    @Override
    public void writeSpawnData(ByteBuf buffer) {
        ByteBufUtils.writeTag(buffer, region.writeToCompound());

        // Write initial bounding box.
        AxisAlignedBB bb = getEntityBoundingBox();
        buffer.writeDouble(bb.minX);
        buffer.writeDouble(bb.minY);
        buffer.writeDouble(bb.minZ);

        buffer.writeDouble(bb.maxX);
        buffer.writeDouble(bb.maxY);
        buffer.writeDouble(bb.maxZ);
    }

    @Override
    public void readSpawnData(ByteBuf additionalData) {
        NBTTagCompound regionCompound = ByteBufUtils.readTag(additionalData);

        region = MobileRegion.getRegionFor(regionCompound);

        double minX = additionalData.readDouble();
        double minY = additionalData.readDouble();
        double minZ = additionalData.readDouble();

        double maxX = additionalData.readDouble();
        double maxY = additionalData.readDouble();
        double maxZ = additionalData.readDouble();

        boundingBox = new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public World getMobileRegionWorld() {
        return mobileRegionWorld;
    }

    @Override
    public void addTrackingPlayer(EntityPlayerMP player) {
        super.addTrackingPlayer(player);

        for (int cX = region.regionMin.chunkXPos; cX < region.regionMax.chunkXPos; cX++) {
            for (int cZ = region.regionMin.chunkZPos; cZ < region.regionMax.chunkZPos; cZ++) {
                WorldServer worldServer = ((WorldServer) getParentWorld());
                worldServer.playerChunkMap.getOrCreateEntry(cX, cZ).addPlayer(player);
            }
        }
    }

    @Override
    public void removeTrackingPlayer(EntityPlayerMP player) {
        super.removeTrackingPlayer(player);

        for (int cX = region.regionMin.chunkXPos; cX < region.regionMax.chunkXPos; cX++) {
            for (int cZ = region.regionMin.chunkZPos; cZ < region.regionMax.chunkZPos; cZ++) {
                WorldServer worldServer = ((WorldServer) getParentWorld());
                worldServer.playerChunkMap.getEntry(cX, cZ).removePlayer(player);
            }
        }
    }
}
