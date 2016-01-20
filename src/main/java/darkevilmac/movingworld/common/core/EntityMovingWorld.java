package darkevilmac.movingworld.common.core;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class EntityMovingWorld extends Entity{

    public SubWorld subWorld;

    public EntityMovingWorld(World worldIn) {
        super(worldIn);
    }

    @Override
    protected void entityInit() {

    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound tagCompund) {

    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound tagCompound) {

    }
}
