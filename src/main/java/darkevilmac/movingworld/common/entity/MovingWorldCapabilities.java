package darkevilmac.movingworld.common.entity;

import darkevilmac.movingworld.common.util.MaterialDensity;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

public abstract class MovingWorldCapabilities {

    private final EntityMovingWorld movingWorld;
    private float mass;
    private int blockCount;
    private boolean autoCalcMass;
    private float speedLimit;
    private float bankingMultiplier;
    private boolean canFly;

    public MovingWorldCapabilities(EntityMovingWorld movingWorld, boolean autoCalcMass) {
        this.movingWorld = movingWorld;
        this.autoCalcMass = autoCalcMass;
        clear();
    }

    public void updateMovingWorldEntities() {
    }

    public int getBlockCount() {
        return blockCount;
    }

    public void onChunkBlockAdded(IBlockState blockState, BlockPos pos) {
        System.out.println("OnChunkBlockAdded");
        blockCount++;
        if (autoCalcMass)
            mass += MaterialDensity.getDensity(blockState.getBlock());
    }

    public void postBlockAdding() {
    }

    public abstract boolean mountEntity(Entity entity);

    public void clearBlockCount() {
        blockCount = 0;
    }

    public void clear() {
        clearBlockCount();
        mass = 0;
    }

    public float getMass() {
        return mass;
    }

    public void setMass(float mass) {
        this.mass = mass;
    }

    public float getSpeedLimit() {
        return speedLimit;
    }

    public void setSpeedLimit(float speedLimit) {
        this.speedLimit = speedLimit;
    }

    public float getBankingMultiplier() {
        return bankingMultiplier;
    }

    public void setBankingMultiplier(float bankingMultiplier) {
        this.bankingMultiplier = bankingMultiplier;
    }

    public boolean canFly() {
        return canFly;
    }

    public void setCanFly(boolean canFly) {
        this.canFly = canFly;
    }

    public void readFromNBT(NBTTagCompound tag) {
    }

    public void writeToNBT(NBTTagCompound tag) {
    }

}
