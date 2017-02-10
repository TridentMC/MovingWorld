package com.elytradev.movingworld.common.experiments.entity;

import com.elytradev.movingworld.common.experiments.MobileRegion;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

/**
 * Experimental MobileRegion entity.
 */
public class EntityMobileRegion extends Entity {

    public MobileRegion region;

    public EntityMobileRegion(World worldIn) {
        super(worldIn);
    }

    @Override
    protected void entityInit() {

    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {

    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {

    }
}
