package com.elytradev.movingworld.common.entity;

import com.elytradev.movingworld.MovingWorldMod;
import com.elytradev.movingworld.api.IMovingTile;
import com.elytradev.movingworld.common.chunk.ChunkIO;
import com.elytradev.movingworld.common.chunk.LocatedBlock;
import com.elytradev.movingworld.common.chunk.MovingWorldAssemblyInteractor;
import com.elytradev.movingworld.common.chunk.MovingWorldSizeOverflowException;
import com.elytradev.movingworld.common.chunk.assembly.AssembleResult;
import com.elytradev.movingworld.common.chunk.assembly.ChunkDisassembler;
import com.elytradev.movingworld.common.chunk.mobilechunk.MobileChunk;
import com.elytradev.movingworld.common.chunk.mobilechunk.MobileChunkClient;
import com.elytradev.movingworld.common.chunk.mobilechunk.MobileChunkServer;
import com.elytradev.movingworld.common.util.AABBRotator;
import com.elytradev.movingworld.common.util.MathHelperMod;
import com.elytradev.movingworld.common.util.Vec3dMod;
import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.io.*;
import java.util.*;

/**
 * All moving sections of blocks extend from this class.
 */
public abstract class EntityMovingWorld extends EntityBoat implements IEntityAdditionalSpawnData {

    public static final DataParameter<Boolean> IS_FLYING = EntityDataManager
            .createKey(EntityMovingWorld.class, DataSerializers.BOOLEAN);
    public EntityPlayer controllingPassenger;

    public float motionYaw;
    public EnumFacing frontDirection;
    public BlockPos riderDestination;
    public Entity prevRiddenByEntity;
    public boolean disassembling = false;
    protected float groundFriction, horFriction, vertFriction;
    protected int[] layeredBlockVolumeCount;
    // Related to actual movement. We don't ever really change these variables, they're changed by classes derived from EntityMovingWorld
    protected boolean noControl;
    protected boolean syncPosWithServer;
    @SideOnly(Side.CLIENT)
    protected double controlPosRotationIncrements;
    @SideOnly(Side.CLIENT)
    protected double controlX, controlY, controlZ;
    @SideOnly(Side.CLIENT)
    protected double controlPitch, controlYaw;
    @SideOnly(Side.CLIENT)
    protected double controlVelX, controlVelY, controlVelZ;
    private int disassembleTimer = 100;
    private MobileChunk mobileChunk;
    private MovingWorldInfo info;
    private ChunkDisassembler disassembler;

    public EntityMovingWorld(World world) {
        super(world);
        info = new MovingWorldInfo();
        if (world.isRemote) {
            initClient();
        } else {
            initCommon();
        }

        motionYaw = 0F;

        layeredBlockVolumeCount = null;
        frontDirection = EnumFacing.NORTH;

        groundFriction = 0.9F;
        horFriction = 0.994F;
        vertFriction = 0.95F;

        prevRiddenByEntity = null;

        ignoreFrustumCheck = true;
    }

    public static boolean isAABBInLiquidNotFall(World world, AxisAlignedBB aabb) {
        int i = MathHelper.floor(aabb.minX);
        int j = MathHelper.floor(aabb.maxX + 1D);
        int k = MathHelper.floor(aabb.minY);
        int l = MathHelper.floor(aabb.maxY + 1D);
        int i1 = MathHelper.floor(aabb.minZ);
        int j1 = MathHelper.floor(aabb.maxZ + 1D);

        for (int x = i; x < j; ++x) {
            for (int y = k; y < l; ++y) {
                for (int z = i1; z < j1; ++z) {
                    IBlockState blockState = world.getBlockState(new BlockPos(x, y, z));
                    Block block = blockState.getBlock();

                    if (block != null && (blockState.getMaterial() == Material.WATER
                            || blockState.getMaterial() == Material.LAVA)) {
                        int j2 = block.getMetaFromState(blockState);
                        double d0;

                        if (j2 < 8) {
                            d0 = y + 1 - j2 / 8.0D;

                            if (d0 >= aabb.minY) {
                                return true;
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    @Override
    protected boolean canFitPassenger(Entity passenger) {
        return controllingPassenger == null;
    }

    public double getControlX() {
        return controlX;
    }

    public double getControlY() {
        return controlY;
    }

    public double getControlZ() {
        return controlZ;
    }

    @Override
    public double getYOffset() {
        return 0.0D;
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float damage) {
        return false;
    }

    public abstract MovingWorldHandlerCommon getHandler();

    public int[] getLayeredBlockVolumeCount() {
        return layeredBlockVolumeCount;
    }

    public void setLayeredBlockVolumeCount(int[] layeredBlockVolumeCount) {
        this.layeredBlockVolumeCount = layeredBlockVolumeCount;
    }

    @SideOnly(Side.CLIENT)
    private void initClient() {
        mobileChunk = new MobileChunkClient(world, this);
        initMovingWorldClient();
    }

    private void initCommon() {
        mobileChunk = new MobileChunkServer(world, this);
        initMovingWorldCommon();
    }

    @Override
    protected void entityInit() {
        dataManager.register(IS_FLYING, false);
        initMovingWorld();
    }

    public abstract void initMovingWorld();

    public abstract void initMovingWorldClient();

    public abstract void initMovingWorldCommon();

    public MobileChunk getMobileChunk() {
        return mobileChunk;
    }

    /**
     * Called before returning the entity from AssembleResult.getEntity
     *
     * @see AssembleResult
     */
    public void assembleResultEntity() {
    }

    public abstract MovingWorldCapabilities getCapabilities();

    public abstract void setCapabilities(MovingWorldCapabilities capabilities);

    public ChunkDisassembler getDisassembler() {
        if (disassembler == null) {
            disassembler = new ChunkDisassembler(this);
        }
        return disassembler;
    }

    public MovingWorldInfo getInfo() {
        return info;
    }

    public void setInfo(MovingWorldInfo movingWorldInfo) {
        if (movingWorldInfo == null) {
            throw new NullPointerException("Cannot set null moving world info");
        }
        info = movingWorldInfo;
    }

    @Override
    public boolean processInitialInteract(EntityPlayer entityplayer, EnumHand hand) {
        return getHandler().processInitialInteract(entityplayer, hand);
    }

    @Override
    public void setDead() {
        super.setDead();
        mobileChunk.onChunkUnload();
        getCapabilities().clear();
    }

    @Override
    public void onEntityUpdate() {
        super.onEntityUpdate();
        if (mobileChunk.isModified) {
            mobileChunk.isModified = false;
            getHandler().onChunkUpdate();
        }
    }

    @Override
    public EntityBoat.Type getBoatType() {
        return Type.OAK;
    }

    public void setRotatedBoundingBox() {
        if (mobileChunk == null) {
            float hw = width / 2F;
            setEntityBoundingBox(
                    new AxisAlignedBB(posX - hw, posY, posZ - hw, posX + hw, posY + height, posZ + hw));
        } else {
            setEntityBoundingBox(
                    new AxisAlignedBB(posX - mobileChunk.getCenterX(), posY, posZ - mobileChunk.getCenterZ(),
                            posX + mobileChunk.getCenterX(), posY + height, posZ + mobileChunk.getCenterZ()));
            setEntityBoundingBox(AABBRotator.rotateAABBAroundY(getEntityBoundingBox(), posX, posZ,
                    (float) Math.toRadians(rotationYaw)));
        }
    }

    @Override
    public void setSize(float w, float h) {
        if (w != width || h != height) {
            width = w;
            height = h;
            float hw = w / 2F;
            setEntityBoundingBox(
                    new AxisAlignedBB(posX - hw, posY, posZ - hw, posX + hw, posY + height, posZ + hw));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void setPositionAndRotationDirect(double x, double y, double z, float yaw, float pitch,
                                             int inc, boolean control) {
        if (control && this.getControllingPassenger() != null) {
            this.prevPosX = this.posX = x;
            this.prevPosY = this.posY = y;
            this.prevPosZ = this.posZ = z;
            this.rotationYaw = yaw;
            this.rotationPitch = pitch;
            this.controlPosRotationIncrements = 0;
            this.setPosition(x, y, z);
            this.motionX = this.controlVelX = 0.0D;
            this.motionY = this.controlVelY = 0.0D;
            this.motionZ = this.controlVelZ = 0.0D;
        } else {
            if (noControl) {
                controlPosRotationIncrements = inc + 5;
            } else {
                double dx = x - posX;
                double dy = y - posY;
                double dz = z - posZ;
                double d = dx * dx + dy * dy + dz * dz;

                if (d < 0.3D) {
                    return;
                }

                syncPosWithServer = true;
                controlPosRotationIncrements = inc;
            }

            controlX = x;
            controlY = y;
            controlZ = z;
            controlYaw = yaw;
            controlPitch = pitch;
            motionX = controlVelX;
            motionY = controlVelY;
            motionZ = controlVelZ;
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void setVelocity(double x, double y, double z) {
        controlVelX = motionX = x;
        controlVelY = motionY = y;
        controlVelZ = motionZ = z;
    }

    public boolean posChanged() {
        return posX != prevPosX || posY != prevPosY || posZ != prevPosZ;
    }

    @Override
    public void onUpdate() {
        onEntityUpdate();

        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;

        double horvel = Math.sqrt(motionX * motionX + motionZ * motionZ);
        if (world.isRemote) {
            spawnParticles(horvel);
        }

        if (world.isRemote && (noControl || syncPosWithServer)) {
            handleClientUpdate();
            if (controlPosRotationIncrements == 0) {
                syncPosWithServer = false;
            }
        } else {
            handleServerUpdate(horvel);
        }

        if (disassembling) {
            if (disassembleTimer <= 0) {
                disassembling = false;
                disassembleTimer = 100;
            } else {
                disassembleTimer--;
            }
        }
    }

    @SideOnly(Side.CLIENT)
    protected void handleClientUpdate() {
        if (controlPosRotationIncrements > 0) {
            double dx = posX + (controlX - posX) / controlPosRotationIncrements;
            double dy = posY + (controlY - posY) / controlPosRotationIncrements;
            double dz = posZ + (controlZ - posZ) / controlPosRotationIncrements;
            double ang = MathHelper.wrapDegrees(controlYaw - rotationYaw);
            rotationYaw = (float) (rotationYaw + ang / controlPosRotationIncrements);
            rotationPitch = (float) (rotationPitch
                    + (controlPitch - rotationPitch) / controlPosRotationIncrements);
            controlPosRotationIncrements--;
            setPosition(dx, dy, dz);
            setRotation(rotationYaw, rotationPitch);
            this.world.updateEntityWithOptionalForce(this, false);
        } else {
            setPosition(posX + motionX, posY + motionY, posZ + motionZ);
            this.world.updateEntityWithOptionalForce(this, false);

            if (onGround) {
                motionX *= groundFriction;
                motionY *= groundFriction;
                motionZ *= groundFriction;
            }

            motionX *= horFriction;
            motionY *= vertFriction;
            motionZ *= horFriction;
        }
        setRotatedBoundingBox();
    }

    protected void handleServerUpdate(double horvel) {
        if (getMobileChunk() != null) {
            if (!getMobileChunk().movingWorldTileEntities.isEmpty()) {
                for (IMovingTile movingWorldTileEntity : getMobileChunk().movingWorldTileEntities) {
                    movingWorldTileEntity.tick(getMobileChunk());
                }
            }
            if (!getMobileChunk().updatableTiles.isEmpty()) {
                for (TileEntity tickable : Lists.newArrayList(getMobileChunk().updatableTiles)) {
                    tickable.setWorld(mobileChunk.getFakeWorld());
                    ((ITickable) tickable).update();
                    tickable.setWorld(mobileChunk.world);
                }
            }
        }

        //START outer forces
        float gravity = 0.05F;
        if (!isFlying()) {
            motionY -= gravity;
        }
        //END outer forces

        handleControl(horvel);

        //START limit motion
        double newhorvel = Math.sqrt(motionX * motionX + motionZ * motionZ);
        double maxvel = getCapabilities().getSpeedLimit();
        if (newhorvel > maxvel) {
            double d = maxvel / newhorvel;
            motionX *= d;
            motionZ *= d;
            newhorvel = maxvel;
        }
        motionY = MathHelperMod.clamp_double(motionY, -maxvel, maxvel);
        //END limit motion

        if (onGround) {
            motionX *= groundFriction;
            motionY *= groundFriction;
            motionZ *= groundFriction;
        }
        rotationPitch = rotationPitch
                + (motionYaw * getCapabilities().getBankingMultiplier() - rotationPitch) * 0.15f;
        motionYaw *= 0.7F;
        rotationYaw += motionYaw;
        setRotatedBoundingBox();
        move(MoverType.SELF, motionX, motionY, motionZ);
        posY = Math.min(posY, world.getHeight());
        motionX *= horFriction;
        motionY *= vertFriction;
        motionZ *= horFriction;

        handleServerUpdatePreRotation();

        setRotation(rotationYaw, rotationPitch);

        handleCollision(posX, posY, posZ);
    }

    public void handleServerUpdatePreRotation() {
        // No implementation basically just a hook for archimedes ships.

        // dis mai code i do wut i wan
    }

    @Override
    public void updatePassenger(Entity passenger) {
        if (this.isPassenger(passenger)) {
            updatePassengerPosition(passenger, riderDestination, 1);
        }
    }

    @Override
    public boolean isPassenger(Entity entity) {
        return Objects.equals(this.controllingPassenger, entity);
    }

    @Override
    public void removePassengers() {
        updatePassengerPosition(controllingPassenger, riderDestination, 1);
        if (controllingPassenger != null) {
            controllingPassenger.dismountRidingEntity();
        }
    }

    public void updatePassengerPosition(Entity passenger, BlockPos riderDestination, int flags) {
        if (passenger != null && !disassembling) {
            int frontDir = frontDirection.getHorizontalIndex();

            float yaw = (float) Math.toRadians(rotationYaw);
            float pitch = (float) Math.toRadians(rotationPitch);

            int x1 = riderDestination.getX(), y1 = riderDestination.getY(), z1 = riderDestination.getZ();
            if ((flags & 1) == 1) {
                if (frontDir == 0) {
                    z1 -= 1;
                } else if (frontDir == 1) {
                    x1 += 1;
                } else if (frontDir == 2) {
                    z1 += 1;
                } else if (frontDir == 3) {
                    x1 -= 1;
                }

                IBlockState state = mobileChunk.getBlockState(
                        new BlockPos(x1, MathHelper.floor(y1 + getMountedYOffset() + passenger.getYOffset()),
                                z1));
                if (state.isOpaqueCube()) {
                    x1 = riderDestination.getX();
                    y1 = riderDestination.getY();
                    z1 = riderDestination.getZ();
                }
            }

            double yOff = (flags & 2) == 2 ? 0d : getMountedYOffset();
            Vec3dMod vec = new Vec3dMod(x1 - mobileChunk.getCenterX() + 0.5d,
                    y1 - mobileChunk.minY() + yOff, z1 - mobileChunk.getCenterZ() + 0.5d);
            switch (frontDir) {
                case 0:
                    vec = vec.rotateAroundZ(-pitch);
                    break;
                case 1:
                    vec = vec.rotateAroundX(pitch);
                    break;
                case 2:
                    vec = vec.rotateAroundZ(pitch);
                    break;
                case 3:
                    vec = vec.rotateAroundX(-pitch);
                    break;
            }
            vec = vec.rotateAroundY(yaw);

            if ((flags & 1) == 1) {
                vec.addVector(0, 0.25, 0);
            }

            passenger.setPosition(posX + vec.x, posY + vec.y + passenger.getYOffset(),
                    posZ + vec.z);

            this.applyYawToEntity(passenger);
        }
    }

    @Override
    protected void applyYawToEntity(Entity entityToUpdate) {
    }

    @Nullable
    @Override
    public Entity getControllingPassenger() {
        return controllingPassenger;
    }

    @Override
    public List<Entity> getPassengers() {
        if (controllingPassenger != null) {
            return Lists.newArrayList(controllingPassenger);
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    protected void removePassenger(Entity passenger) {
        if (passenger.equals(controllingPassenger)) {
            controllingPassenger = null;
        }
    }

    @Override
    protected void addPassenger(Entity passenger) {
        if (passenger.getRidingEntity() != this) {
            throw new IllegalStateException("Use x.startRiding(y), not y.addPassenger(x)");
        } else {
            if (controllingPassenger == null && passenger != null
                    && passenger instanceof EntityPlayer) {
                controllingPassenger = (EntityPlayer) passenger;
            }
        }
    }

    private boolean handleCollision(double cPosX, double cPosY, double cPosZ) {
        boolean didCollide = false;
        if (!world.isRemote) {
            List<Entity> list = world.getEntitiesWithinAABBExcludingEntity(this,
                    getEntityBoundingBox().expand(0.2D, 0.0D, 0.2D));
            if (list != null && !list.isEmpty()) {
                didCollide = true;
                for (Entity entity : list) {
                    if (!Objects.equals(entity, getControllingPassenger()) && entity.canBePushed()) {
                        if (entity instanceof EntityMovingWorld) {
                            entity.applyEntityCollision(this);
                        } else if (entity instanceof EntityBoat) {
                            double d0 = cPosX - entity.posX;
                            double d1 = cPosZ - entity.posZ;
                            double d2 = MathHelper.absMax(d0, d1);

                            if (d2 >= 0.01D) {
                                d2 = MathHelper.sqrt(d2);
                                d0 /= d2;
                                d1 /= d2;
                                double d3 = 1.0D / d2;

                                if (d3 > 1.0D) {
                                    d3 = 1.0D;
                                }

                                d0 *= d3;
                                d1 *= d3;
                                d0 *= 0.05D;
                                d1 *= 0.05D;
                                d0 *= 1.0F - entity.entityCollisionReduction;
                                d1 *= 1.0F - entity.entityCollisionReduction;
                                entity.addVelocity(-d0, 0.0D, -d1);
                            }
                        }
                    }
                }
            }

            for (int l = 0; l < 4; ++l) {
                int i1 = MathHelper.floor(cPosX + ((l % 2) - 0.5D) * 0.8D);
                int j1 = MathHelper.floor(cPosZ + ((l / 2) - 0.5D) * 0.8D);

                for (int k1 = 0; k1 < 2; ++k1) {
                    int l1 = MathHelper.floor(cPosY) + k1;
                    IBlockState blockState = world.getBlockState(new BlockPos(i1, l1, j1));
                    Block block = blockState.getBlock();

                    if (block == Blocks.SNOW) {
                        world.setBlockToAir(new BlockPos(i1, l1, j1));
                        isCollidedHorizontally = false;
                    } else if (block == Blocks.WATERLILY) {
                        world.destroyBlock(new BlockPos(i1, l1, j1), true);
                        isCollidedHorizontally = false;
                    } else {
                        didCollide = true;
                    }
                }

            }
        }
        return didCollide;
    }

    @Override
    public boolean handleWaterMovement() {
        float f = width;
        width = 0F;
        boolean ret = super.handleWaterMovement();
        width = f;
        return ret;
    }

    public boolean isFlying() {
        return getCapabilities().canFly() && dataManager.get(IS_FLYING);
    }

    public void setFlying(boolean isFlying) {
        dataManager.set(IS_FLYING, isFlying);
    }

    public abstract boolean isBraking();

    /**
     * Determines whether the entity should be pushed by fluids
     */
    @Override
    public boolean isPushedByWater() {
        return ticksExisted > 60;
    }

    @Override
    public boolean shouldRiderSit() {
        return true;
    }

    @Override
    public double getMountedYOffset() {
        return getYOffset() + 0.5D;
    }

    @Override
    protected boolean canTriggerWalking() {
        return false;
    }

    @Override
    public AxisAlignedBB getCollisionBox(Entity entity) {
        return getEntityBoundingBox();
    }

    @Override
    public boolean canBePushed() {
        return !isDead && getControllingPassenger() == null;
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    protected void updateFallState(double y, boolean onGroundIn, IBlockState state, BlockPos pos) {
    }

    @Override
    public void fall(float distance, float damageMultiplier) {
    }

    public float getHorizontalVelocity() {
        return (float) Math.sqrt(motionX * motionX + motionZ * motionZ);
    }

    public void alignToGrid(boolean doPosAdjustment) {
        rotationYaw = Math.round(rotationYaw / 90F) * 90F;
        rotationPitch = 0F;

        Vec3d vec = new Vec3d(-mobileChunk.getCenterX(), -mobileChunk.minY(),
                -mobileChunk.getCenterZ());
        vec = vec.rotateYaw((float) Math.toRadians(rotationYaw));

        int ix = MathHelperMod.round_double(vec.x + posX);
        int iy = MathHelperMod.round_double(vec.y + posY);
        int iz = MathHelperMod.round_double(vec.z + posZ);

        if (doPosAdjustment) {
            setPositionAndUpdate(ix - vec.x, iy - vec.y, iz - vec.z);
        }

        motionX = motionY = motionZ = 0D;
    }

    public boolean disassemble(boolean overwrite) {
        if (world.isRemote) {
            return true;
        }

        updatePassenger(this.getControllingPassenger());

        ChunkDisassembler disassembler = getDisassembler();
        disassembler.overwrite = overwrite;

        if (!disassembler.canDisassemble(getAssemblyInteractor())) {
            return false;
        }

        AssembleResult result = disassembler.doDisassemble(getNewAssemblyInteractor());

        return true;
    }

    public abstract MovingWorldAssemblyInteractor getNewAssemblyInteractor();

    public void dropAsItems() {
        TileEntity tileentity;
        IBlockState blockState;
        for (int i = mobileChunk.minX(); i < mobileChunk.maxX(); i++) {
            for (int j = mobileChunk.minY(); j < mobileChunk.maxY(); j++) {
                for (int k = mobileChunk.minZ(); k < mobileChunk.maxZ(); k++) {
                    tileentity = mobileChunk.getTileEntity(new BlockPos(i, j, k));
                    if (tileentity instanceof IInventory) {
                        IInventory inv = (IInventory) tileentity;
                        for (int it = 0; it < inv.getSizeInventory(); it++) {
                            ItemStack is = inv.getStackInSlot(it);
                            if (is != null) {
                                entityDropItem(is, 0F);
                            }
                        }
                    }
                    blockState = mobileChunk.getBlockState(new BlockPos(i, j, k));

                    if (blockState.getBlock() != Blocks.AIR) {
                        blockState.getBlock().dropBlockAsItem(world,
                                new BlockPos(MathHelper.floor(posX), MathHelper.floor(posY),
                                        MathHelper.floor(posZ)), blockState, 0);
                    }
                }
            }
        }
    }

    protected void fillAirBlocks(Set<BlockPos> set, BlockPos pos) {
        if (pos.getX() < mobileChunk.minX() - 1 || pos.getX() > mobileChunk.maxX()
                || pos.getY() < mobileChunk.minY() - 1 || pos.getY() > mobileChunk.maxY()
                || pos.getZ() < mobileChunk.minZ() - 1 || pos.getZ() > mobileChunk.maxZ()) {
            return;
        }
        if (set.contains(pos)) {
            return;
        }

        set.add(pos);
        if (mobileChunk.setBlockAsFilledAir(pos)) {
            fillAirBlocks(set, pos.add(0, 1, 0));
            //fillAirBlocks(set, x, y - 1, z);
            fillAirBlocks(set, pos.add(-1, 0, 0));
            fillAirBlocks(set, pos.add(0, 0, -1));
            fillAirBlocks(set, pos.add(1, 0, 0));
            fillAirBlocks(set, pos.add(0, 0, 1));
        }
    }

    public void setRiderDestination(EnumFacing frontDirection, BlockPos riderDestination) {
        this.frontDirection = frontDirection;
        this.riderDestination = riderDestination;
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound tag) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(mobileChunk.getMemoryUsage());
        DataOutputStream out = new DataOutputStream(baos);
        try {
            ChunkIO.writeAll(out, mobileChunk);
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        tag.setByteArray("chunk", baos.toByteArray());
        tag.setInteger("riderDestinationX", riderDestination.getX());
        tag.setInteger("riderDestinationY", riderDestination.getY());
        tag.setInteger("riderDestinationZ", riderDestination.getZ());
        tag.setInteger("front", frontDirection.getHorizontalIndex());

        if (!mobileChunk.chunkTileEntityMap.isEmpty()) {
            NBTTagList tileEntities = new NBTTagList();
            for (TileEntity tileentity : mobileChunk.chunkTileEntityMap.values()) {
                NBTTagCompound comp = new NBTTagCompound();
                tileentity.writeToNBT(comp);
                tileEntities.appendTag(comp);
            }
            tag.setTag("tileent", tileEntities);
        }

        if (mobileChunk.marker != null) {
            NBTTagCompound markerComp = new NBTTagCompound();
            markerComp.setInteger("markerPosX", mobileChunk.marker.blockPos.getX());
            markerComp.setInteger("markerPosY", mobileChunk.marker.blockPos.getY());
            markerComp.setInteger("markerPosZ", mobileChunk.marker.blockPos.getZ());
            tag.setTag("markerInfo", markerComp);
        }

        tag.setString("name", info.getName());
        if (info.getOwner() != null) {
            tag.setString("owner", info.getOwner().toString());
        }

        writeMovingWorldNBT(tag);
    }

    public abstract void writeMovingWorldNBT(NBTTagCompound tag);

    @Override
    protected void readEntityFromNBT(NBTTagCompound tag) {
        if (mobileChunk == null) {
            if (world != null) {
                if (world.isRemote) {
                    initClient();
                } else {
                    initCommon();
                }
            }
        }

        byte[] ab = tag.getByteArray("chunk");
        ByteArrayInputStream bais = new ByteArrayInputStream(ab);
        DataInputStream in = new DataInputStream(bais);
        try {
            ChunkIO.read(in, mobileChunk);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (tag.hasKey("riderDestination")) {
            short s = tag.getShort("riderDestination");
            int rX = s & 0xF;
            int rY = s >>> 4 & 0xF;
            int rZ = s >>> 8 & 0xF;
            riderDestination = new BlockPos(rX, rY, rZ);
            frontDirection = EnumFacing.NORTH;
        } else {
            int rX = tag.getInteger("riderDestinationX");
            int rY = tag.getInteger("riderDestinationY");
            int rZ = tag.getInteger("riderDestinationZ");
            riderDestination = new BlockPos(rX, rY, rZ);
            frontDirection = EnumFacing.getHorizontal(tag.getInteger("front"));
        }

        NBTTagList tiles = tag.getTagList("tileent", 10);
        if (tiles != null) {
            for (int i = 0; i < tiles.tagCount(); i++) {
                try {
                    NBTTagCompound comp = tiles.getCompoundTagAt(i);
                    TileEntity tileentity = TileEntity.create(mobileChunk.getFakeWorld(), comp);
                    mobileChunk.setTileEntity(tileentity.getPos(), tileentity);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if (tag.hasKey("markerInfo")) {
            NBTTagCompound markerComp = (NBTTagCompound) tag.getTag("markerInfo");
            BlockPos markerPos = new BlockPos(markerComp.getInteger("markerPosX"),
                    markerComp.getInteger("markerPosY"),
                    markerComp.getInteger("markerPosZ"));

            mobileChunk.marker = new LocatedBlock(mobileChunk.getBlockState(markerPos),
                    mobileChunk.getTileEntity(markerPos), markerPos);
        }

        info = new MovingWorldInfo();
        info.setName(tag.getString("name"));
        if (tag.hasKey("owner")) {
            info.setOwner(UUID.fromString(tag.getString("owner")));
        }
        readMovingWorldNBT(tag);
    }

    public abstract void readMovingWorldNBT(NBTTagCompound tag);

    @SideOnly(Side.CLIENT)
    public void spawnParticles(double horvel) {
    }

    @Override
    public void writeSpawnData(ByteBuf data) {
        data.writeInt(riderDestination.getX());
        data.writeInt(riderDestination.getY());
        data.writeInt(riderDestination.getZ());
        data.writeInt(frontDirection.getHorizontalIndex());

        data.writeShort(info.getName().length());
        data.writeBytes(info.getName().getBytes());

        try {
            ChunkIO.writeAllCompressed(data, mobileChunk);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MovingWorldSizeOverflowException ssoe) {
            disassemble(false);
            MovingWorldMod.LOG.warn("Ship is too large to be sent");
        }
        writeMovingWorldSpawnData(data);
    }

    /**
     * Same as the code from the Entity Class but it doesn't spawn particles, as with larger ships
     * it can cause a lot of lag.
     */
    @Override
    protected void doWaterSplashEffect() {
        float sqrtMotion = MathHelper.sqrt(
                this.motionX * this.motionX * 0.20000000298023224D + this.motionY * this.motionY
                        + this.motionZ * this.motionZ * 0.20000000298023224D) * 0.2F;
        sqrtMotion = sqrtMotion > 1.0F ? 1.0F : sqrtMotion;
        this.playSound(this.getSplashSound(), sqrtMotion,
                1.0F + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.4F);
    }

    public abstract void writeMovingWorldSpawnData(ByteBuf data);

    @Override
    public void readSpawnData(ByteBuf data) {
        int rX = data.readInt();
        int rY = data.readInt();
        int rZ = data.readInt();
        riderDestination = new BlockPos(rX, rY, rZ);
        frontDirection = EnumFacing.getHorizontal(data.readInt());

        byte[] ab = new byte[data.readShort()];
        data.readBytes(ab);
        info.setName(new String(ab));
        try {
            ChunkIO.readCompressed(data, mobileChunk);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mobileChunk.onChunkLoad();
        readMovingWorldSpawnData(data);
    }

    @Override
    public boolean isInRangeToRender3d(double camX, double camY, double camZ) {
        double d0 = this.posX - camX;
        double d1 = this.posY - camY;
        double d2 = this.posZ - camZ;
        double d3 = d0 * d0 + d1 * d1 + d2 * d2;
        return this.isInRangeToRenderDist(d3);
    }

    public abstract void handleControl(double horvel);

    public abstract void readMovingWorldSpawnData(ByteBuf data);

    public abstract float getXRenderScale();

    public abstract float getYRenderScale();

    public abstract float getZRenderScale();

    public abstract MovingWorldAssemblyInteractor getAssemblyInteractor();

    public abstract void setAssemblyInteractor(MovingWorldAssemblyInteractor interactor);
}