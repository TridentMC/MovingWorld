package darkevilmac.movingworld.entity;

import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;

/**
 * Previously EntityEntityAttachment, made much more abstract. No longer contains code related to spawning parachutes,
 * this should have been done in the EntitySeat class in the first place.
 */
public class EntityMovingWorldAttachment extends Entity implements IEntityAdditionalSpawnData {
    private EntityMovingWorld movingWorld;
    private ChunkPosition pos;
    private Entity prevRiddenByEntity;

    public EntityMovingWorldAttachment(World world) {
        super(world);
        movingWorld = null;
        pos = null;
        prevRiddenByEntity = null;
        yOffset = 0f;
        setSize(0F, 0F);
    }

    public void setParentShip(EntityMovingWorld entityMovingWorld, int x, int y, int z) {
        movingWorld = entityMovingWorld;
        if (entityMovingWorld != null) {
            pos = new ChunkPosition(x, y, z);
            setLocationAndAngles(entityMovingWorld.posX, entityMovingWorld.posY, entityMovingWorld.posZ, 0F, 0F);
        }
    }

    public EntityMovingWorld getParentMovingWorld() {
        return movingWorld;
    }

    public ChunkPosition getChunkPosition() {
        return pos;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (movingWorld != null) {
            setPosition(movingWorld.posX, movingWorld.posY, movingWorld.posZ);
        }
    }

    @Override
    protected void entityInit() {
    }

    @Override
    public void updateRiderPosition() {
        if (movingWorld != null) {
            movingWorld.updateRiderPosition(riddenByEntity, pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ, 0);
        }
    }

    @Override
    public double getMountedYOffset() {
        return yOffset + 0.5d;
    }

    @Override
    public AxisAlignedBB getCollisionBox(Entity entity) {
        return null;
    }

    @Override
    public AxisAlignedBB getBoundingBox() {
        return null;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        setDead();
    }

    @Override
    public void writeSpawnData(ByteBuf data) {
        if (movingWorld == null) {
            data.writeInt(0);
            data.writeByte(0);
            data.writeByte(0);
            data.writeByte(0);
            return;
        }
        data.writeInt(movingWorld.getEntityId());
        data.writeByte(pos.chunkPosX & 0xFF);
        data.writeByte(pos.chunkPosY & 0xFF);
        data.writeByte(pos.chunkPosZ & 0xFF);
    }

    @Override
    public void readSpawnData(ByteBuf data) {
        Entity entity = worldObj.getEntityByID(data.readInt());
        if (entity instanceof EntityMovingWorld) {
            setParentShip((EntityMovingWorld) entity, data.readUnsignedByte(), data.readUnsignedByte(), data.readUnsignedByte());
        }
    }
}
