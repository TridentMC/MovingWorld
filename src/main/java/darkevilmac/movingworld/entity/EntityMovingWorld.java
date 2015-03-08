package darkevilmac.movingworld.entity;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

/**
 * All moving sections of blocks extend from this class.
 */
public class EntityMovingWorld extends Entity {

    public EntityMovingWorld(World worldObj) {
        super(worldObj);
    }

    @Override
    protected void entityInit() {

    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound p_70037_1_) {

    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound p_70014_1_) {

    }


}
