package com.tridevmc.movingworld.common.entity;

import com.google.common.collect.Lists;
import com.tridevmc.movingworld.MovingWorldMod;
import com.tridevmc.movingworld.api.IMovingTile;
import com.tridevmc.movingworld.common.chunk.CompressedChunkData;
import com.tridevmc.movingworld.common.chunk.LocatedBlock;
import com.tridevmc.movingworld.common.chunk.MovingWorldAssemblyInteractor;
import com.tridevmc.movingworld.common.chunk.assembly.AssembleResult;
import com.tridevmc.movingworld.common.chunk.assembly.ChunkDisassembler;
import com.tridevmc.movingworld.common.chunk.mobilechunk.MobileChunk;
import com.tridevmc.movingworld.common.chunk.mobilechunk.MobileChunkClient;
import com.tridevmc.movingworld.common.chunk.mobilechunk.MobileChunkServer;
import com.tridevmc.movingworld.common.util.MathHelperMod;
import com.tridevmc.movingworld.common.util.Vec3dMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.ITickable;
import net.minecraft.entity.*;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * All moving sections of blocks extend from this class.
 */
public abstract class EntityMovingWorld extends BoatEntity implements IEntityAdditionalSpawnData {

    public static final DataParameter<Boolean> IS_FLYING = EntityDataManager
            .createKey(EntityMovingWorld.class, DataSerializers.BOOLEAN);
    public PlayerEntity controllingPassenger;

    public float motionYaw;
    public Direction frontDirection;
    public BlockPos riderDestination;
    public Entity prevRiddenByEntity;
    public boolean disassembling = false;
    protected float groundFriction, horFriction, vertFriction;
    protected int[] layeredBlockVolumeCount;

    // Related to actual movement. We don't ever really change these variables, they're changed by classes derived from EntityMovingWorld
    protected boolean noControl;
    protected boolean syncPosWithServer;
    @OnlyIn(Dist.CLIENT)
    protected double controlPosRotationIncrements;
    @OnlyIn(Dist.CLIENT)
    protected double controlX, controlY, controlZ;
    @OnlyIn(Dist.CLIENT)
    protected double controlPitch, controlYaw;
    @OnlyIn(Dist.CLIENT)
    protected double controlVelX, controlVelY, controlVelZ;

    private EntitySize size = new EntitySize(1, 1, false);
    private NonAlignedBoundingBox realBoundingBox;
    private int disassembleTimer = 100;
    private MobileChunk mobileChunk;
    private MovingWorldInfo info;
    private ChunkDisassembler disassembler;

    public EntityMovingWorld(EntityType<? extends EntityMovingWorld> type, World world) {
        super(type, world);
        this.info = new MovingWorldInfo();
        if (world.isRemote) {
            this.initClient();
        } else {
            this.initCommon();
        }

        this.motionYaw = 0F;

        this.layeredBlockVolumeCount = null;
        this.frontDirection = Direction.NORTH;

        this.groundFriction = 0.9F;
        this.horFriction = 0.994F;
        this.vertFriction = 0.95F;

        this.prevRiddenByEntity = null;

        this.ignoreFrustumCheck = true;
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
                    BlockState blockState = world.getBlockState(new BlockPos(x, y, z));

                    if ((blockState.getMaterial() == Material.WATER
                            || blockState.getMaterial() == Material.LAVA)
                            && blockState.getBlock() instanceof FlowingFluidBlock) {
                        int j2 = blockState.get(FlowingFluidBlock.LEVEL);
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
        return this.controllingPassenger == null;
    }

    public double getControlX() {
        return this.controlX;
    }

    public double getControlY() {
        return this.controlY;
    }

    public double getControlZ() {
        return this.controlZ;
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
        return this.layeredBlockVolumeCount;
    }

    public void setLayeredBlockVolumeCount(int[] layeredBlockVolumeCount) {
        this.layeredBlockVolumeCount = layeredBlockVolumeCount;
    }

    @OnlyIn(Dist.CLIENT)
    private void initClient() {
        this.mobileChunk = new MobileChunkClient(this.world, this);
        this.initMovingWorldClient();
    }

    private void initCommon() {
        this.mobileChunk = new MobileChunkServer(this.world, this);
        this.initMovingWorldCommon();
    }

    @Override
    protected void registerData() {
        super.registerData();
        this.dataManager.register(IS_FLYING, false);
        this.initMovingWorld();
    }

    public abstract void initMovingWorld();

    public abstract void initMovingWorldClient();

    public abstract void initMovingWorldCommon();

    @Nonnull
    public MobileChunk getMobileChunk() {
        return this.mobileChunk;
    }

    /**
     * Called before returning the entity from AssembleResult.getEntity
     *
     * @see AssembleResult
     */
    public void assembleResultEntity() {
    }

    public abstract MovingWorldCapabilities getMovingWorldCapabilities();

    public abstract void setCapabilities(MovingWorldCapabilities capabilities);

    public ChunkDisassembler getDisassembler() {
        if (this.disassembler == null) {
            this.disassembler = new ChunkDisassembler(this);
        }
        return this.disassembler;
    }

    public MovingWorldInfo getInfo() {
        return this.info;
    }

    public void setInfo(MovingWorldInfo movingWorldInfo) {
        if (movingWorldInfo == null) {
            throw new NullPointerException("Cannot set null moving world info");
        }
        this.info = movingWorldInfo;
    }

    @Override
    public boolean processInitialInteract(PlayerEntity entityplayer, Hand hand) {
        return this.getHandler().processInitialInteract(entityplayer, hand);
    }

    @Override
    public void remove() {
        super.remove();
        this.mobileChunk.onChunkUnload();
        this.getMovingWorldCapabilities().clear();
    }

    @Override
    public void baseTick() {
        super.baseTick();
        if (this.mobileChunk.isModified) {
            this.mobileChunk.isModified = false;
            this.getHandler().onChunkUpdate();
        }
    }

    @Override
    public BoatEntity.Type getBoatType() {
        return Type.OAK;
    }

    public void setRotatedBoundingBox() {
        if (this.mobileChunk == null) {
            float hw = this.getWidth() / 2F;
            this.setBoundingBox(
                    new AxisAlignedBB(this.posX - hw, this.posY, this.posZ - hw, this.posX + hw, this.posY + this.size.height, this.posZ + hw));
        } else {
            AxisAlignedBB bb = new AxisAlignedBB(this.posX - this.mobileChunk.getCenterX(), this.posY, this.posZ - this.mobileChunk.getCenterZ(),
                    this.posX + this.mobileChunk.getCenterX(), this.posY + this.getHeight(), this.posZ + this.mobileChunk.getCenterZ());
            this.setBoundingBox(bb);
        }
    }

    public void setSize(float w, float h) {
        if (w != this.size.width || h != this.size.height) {
            this.size = new EntitySize(w, h, false);
            float hw = w / 2F;
            this.setBoundingBox(
                    new AxisAlignedBB(this.posX - hw, this.posY, this.posZ - hw, this.posX + hw, this.posY + this.size.height, this.posZ + hw));
        }
    }

    @Override
    public EntitySize getSize(Pose pose) {
        return this.size;
    }

    @Override
    public void move(MoverType typeIn, Vec3d pos) {
        this.setRotatedBoundingBox();
        super.move(typeIn, pos);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
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
            this.setMotion(new Vec3d(0, 0, 0));
            this.controlVelX = this.controlVelY = this.controlVelZ = 0;
        } else {
            if (this.noControl) {
                this.controlPosRotationIncrements = inc + 5;
            } else {
                double dx = x - this.posX;
                double dy = y - this.posY;
                double dz = z - this.posZ;
                double d = dx * dx + dy * dy + dz * dz;

                if (d < 0.3D) {
                    return;
                }

                this.syncPosWithServer = true;
                this.controlPosRotationIncrements = inc;
            }

            this.controlX = x;
            this.controlY = y;
            this.controlZ = z;
            this.controlYaw = yaw;
            this.controlPitch = pitch;
            this.setMotion(new Vec3d(controlVelX, controlVelY, controlVelZ));
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void setVelocity(double x, double y, double z) {
        this.setMotion(new Vec3d(x, y, z));
        this.controlVelX = x;
        this.controlVelY = y;
        this.controlVelZ = z;
    }

    @Override
    public void setBoundingBox(AxisAlignedBB bb) {
        super.setBoundingBox(bb);
        this.setRealBoundingBox(new NonAlignedBoundingBox(bb, 0, this.rotationYaw, 0));
    }

    public NonAlignedBoundingBox getRealBoundingBox() {
        return realBoundingBox;
    }

    public void setRealBoundingBox(NonAlignedBoundingBox realBoundingBox) {
        if (!Objects.equals(this.getRealBoundingBox(), realBoundingBox))
            this.realBoundingBox = realBoundingBox;
    }

    public boolean posChanged() {
        return this.posX != this.prevPosX || this.posY != this.prevPosY || this.posZ != this.prevPosZ;
    }

    @Override
    public void tick() {
        if (!this.world.isRemote) {
            this.setFlag(6, this.isGlowing());
        }

        this.baseTick();

        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        double horVel = Math.sqrt(this.getMotion().x * this.getMotion().x + this.getMotion().z * this.getMotion().z);
        if (this.world.isRemote) {
            this.spawnParticles(horVel);
        }

        if (this.world.isRemote && (this.noControl || this.syncPosWithServer)) {
            this.handleClientUpdate();
            if (this.controlPosRotationIncrements == 0) {
                this.syncPosWithServer = false;
            }
        } else {
            this.handleServerUpdate(horVel);
        }

        if (this.disassembling) {
            if (this.disassembleTimer <= 0) {
                this.disassembling = false;
                this.disassembleTimer = 100;
            } else {
                this.disassembleTimer--;
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    protected void handleClientUpdate() {
        if (this.controlPosRotationIncrements > 0) {
            double dx = this.posX + (this.controlX - this.posX) / this.controlPosRotationIncrements;
            double dy = this.posY + (this.controlY - this.posY) / this.controlPosRotationIncrements;
            double dz = this.posZ + (this.controlZ - this.posZ) / this.controlPosRotationIncrements;
            double ang = MathHelper.wrapDegrees(this.controlYaw - this.rotationYaw);
            this.rotationYaw = (float) (this.rotationYaw + ang / this.controlPosRotationIncrements);
            this.rotationPitch = (float) (this.rotationPitch
                    + (this.controlPitch - this.rotationPitch) / this.controlPosRotationIncrements);
            this.controlPosRotationIncrements--;
            this.setPosition(dx, dy, dz);
            this.setRotation(this.rotationYaw, this.rotationPitch);

            // TODO: no replacement method in 1.14?
            //this.world.tickEntity(this, false);
        } else {
            this.setPosition(this.posX + this.getMotion().x, this.posY + this.getMotion().y, this.posZ + this.getMotion().z);
            // TODO: no replacement method in 1.14?
            // this.world.tickEntity(this, false);

            if (this.onGround) {
                this.setMotion(this.getMotion().mul(this.groundFriction, this.groundFriction, this.groundFriction));
            }

            this.setMotion(this.getMotion().mul(this.horFriction, this.vertFriction, this.horFriction));
        }
        this.setRotatedBoundingBox();
    }

    protected void handleServerUpdate(double horvel) {
        if (!this.getMobileChunk().movingWorldTileEntities.isEmpty()) {
            for (IMovingTile movingWorldTileEntity : this.getMobileChunk().movingWorldTileEntities) {
                movingWorldTileEntity.tick(this.getMobileChunk());
            }
        }
        if (!this.getMobileChunk().updatableTiles.isEmpty()) {
            for (TileEntity tickable : Lists.newArrayList(this.getMobileChunk().updatableTiles)) {
                tickable.setWorld(this.mobileChunk.getFakeWorld());
                ((ITickable) tickable).tick();
                tickable.setWorld(this.mobileChunk.world);
            }
        }

        //START outer forces
        float gravity = 0.05F;
        if (!this.isFlying()) {
            this.setMotion(this.getMotion().subtract(0, gravity, 0));
        }
        //END outer forces

        this.handleControl(horvel);

        //START limit motion
        double newHorVel = Math.sqrt(this.getMotion().x * this.getMotion().x + this.getMotion().z * this.getMotion().z);
        double maxVel = this.getMovingWorldCapabilities().getSpeedLimit();
        if (newHorVel > maxVel) {
            double d = maxVel / newHorVel;
            this.setMotion(this.getMotion().mul(d, 1, d));
            newHorVel = maxVel;
        }
        this.setMotion(new Vec3d(this.getMotion().x, MathHelperMod.clamp_double(this.getMotion().y, -maxVel, maxVel), this.getMotion().z));
        //END limit motion

        if (this.onGround) {
            this.setMotion(this.getMotion().mul(this.groundFriction, this.groundFriction, this.groundFriction));
        }
        this.handleRotation();
        this.move(MoverType.SELF, this.getMotion());
        this.posY = Math.min(this.posY, this.world.getHeight());
        this.setMotion(this.getMotion().mul(this.horFriction, this.vertFriction, this.horFriction));

        this.handleServerUpdatePreRotation();
        this.setRotation(this.rotationYaw, this.rotationPitch);
        this.handleCollision(this.posX, this.posY, this.posZ);
    }

    public void handleServerUpdatePreRotation() {
    }

    private void handleRotation() {
        if (motionYaw == 0) {
            return;
        }
        //TODO: Convert to non aligned bounding boxes.

        float newRotationPitch = this.rotationPitch
                + (this.motionYaw * this.getMovingWorldCapabilities().getBankingMultiplier() - this.rotationPitch) * 0.15f;
        float newMotionYaw = motionYaw * 0.7F;
        float newRotationYaw = newMotionYaw + this.rotationYaw;

        this.rotationPitch = newRotationPitch;
        this.motionYaw = newMotionYaw;
        this.rotationYaw = newRotationYaw;
    }

    @Override
    public void updatePassenger(Entity passenger) {
        if (this.isPassenger(passenger)) {
            this.updatePassengerPosition(passenger, this.riderDestination, 1);
        }
    }

    @Override
    public boolean isPassenger(Entity entity) {
        return Objects.equals(this.controllingPassenger, entity);
    }

    @Override
    public void removePassengers() {
        this.updatePassengerPosition(this.controllingPassenger, this.riderDestination, 1);
        if (this.controllingPassenger != null) {
            this.controllingPassenger.stopRiding();
        }
    }

    public void updatePassengerPosition(Entity passenger, BlockPos riderDestination, int flags) {
        if (passenger != null && !this.disassembling) {
            int frontDir = this.frontDirection.getHorizontalIndex();

            float yaw = (float) Math.toRadians(this.rotationYaw);
            float pitch = (float) Math.toRadians(this.rotationPitch);

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

                BlockPos blockPos = new BlockPos(x1, MathHelper.floor(y1 + this.getMountedYOffset() + passenger.getYOffset()),
                        z1);
                BlockState state = this.mobileChunk.getBlockState(blockPos);
                if (state.isOpaqueCube(this.mobileChunk, blockPos)) {
                    x1 = riderDestination.getX();
                    y1 = riderDestination.getY();
                    z1 = riderDestination.getZ();
                }
            }

            double yOff = (flags & 2) == 2 ? 0d : this.getMountedYOffset();
            Vec3dMod vec = new Vec3dMod(x1 - this.mobileChunk.getCenterX() + 0.5d,
                    y1 - this.mobileChunk.minY() + yOff, z1 - this.mobileChunk.getCenterZ() + 0.5d);
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
                vec.add(0, 0.25, 0);
            }

            passenger.setPosition(this.posX + vec.x, this.posY + vec.y + passenger.getYOffset(),
                    this.posZ + vec.z);

            this.applyYawToEntity(passenger);
        }
    }

    @Override
    protected void applyYawToEntity(Entity entityToUpdate) {
    }

    @Nullable
    @Override
    public Entity getControllingPassenger() {
        return this.controllingPassenger;
    }

    @Override
    public List<Entity> getPassengers() {
        if (this.controllingPassenger != null) {
            return Lists.newArrayList(this.controllingPassenger);
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    protected void removePassenger(Entity passenger) {
        if (passenger.equals(this.controllingPassenger)) {
            this.controllingPassenger = null;
        }
    }

    @Override
    protected void addPassenger(Entity passenger) {
        if (passenger.getRidingEntity() != this) {
            throw new IllegalStateException("Use x.startRiding(y), not y.addPassenger(x)");
        } else {
            if (this.controllingPassenger == null && passenger instanceof PlayerEntity) {
                this.controllingPassenger = (PlayerEntity) passenger;
            }
        }
    }

    private boolean handleCollision(double cPosX, double cPosY, double cPosZ) {
        boolean didCollide = false;
        if (!this.world.isRemote) {
            List<Entity> list = this.world.getEntitiesWithinAABBExcludingEntity(this,
                    this.getBoundingBox().expand(0.2D, 0.0D, 0.2D));
            if (list != null && !list.isEmpty()) {
                didCollide = true;
                for (Entity entity : list) {
                    if (!Objects.equals(entity, this.getControllingPassenger()) && entity.canBePushed()) {
                        if (entity instanceof EntityMovingWorld) {
                            entity.applyEntityCollision(this);
                        } else if (entity instanceof BoatEntity) {
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
                int j1 = MathHelper.floor(cPosZ + ((((double) l) / 2) - 0.5D) * 0.8D);

                for (int k1 = 0; k1 < 2; ++k1) {
                    int l1 = MathHelper.floor(cPosY) + k1;
                    BlockState blockState = this.world.getBlockState(new BlockPos(i1, l1, j1));
                    Block block = blockState.getBlock();

                    if (block == Blocks.SNOW) {
                        this.world.destroyBlock(new BlockPos(i1, l1, j1), false);
                        this.collidedHorizontally = false;
                    } else if (block == Blocks.LILY_PAD) {
                        this.world.destroyBlock(new BlockPos(i1, l1, j1), true);
                        this.collidedHorizontally = false;
                    } else if (!blockState.isAir()) {
                        MovingWorldMod.LOG.info("Collided with " + block.getTranslationKey());
                        didCollide = true;
                    }
                }

            }
        }
        return didCollide;
    }

    @Override
    public boolean handleWaterMovement() {
        float f = this.getWidth();
        this.setSize(0, this.getHeight());
        boolean ret = super.handleWaterMovement();
        this.setSize(f, this.getHeight());
        return ret;
    }

    public boolean isFlying() {
        return this.getMovingWorldCapabilities().canFly() && this.dataManager.get(IS_FLYING);
    }

    public void setFlying(boolean isFlying) {
        this.dataManager.set(IS_FLYING, isFlying);
    }

    public abstract boolean isBraking();

    /**
     * Determines whether the entity should be pushed by fluids
     */
    @Override
    public boolean isPushedByWater() {
        return this.ticksExisted > 60;
    }

    @Override
    public boolean shouldRiderSit() {
        return true;
    }

    @Override
    public double getMountedYOffset() {
        return this.getYOffset() + 0.5D;
    }

    @Override
    protected boolean canTriggerWalking() {
        return false;
    }

    @Override
    public AxisAlignedBB getCollisionBox(Entity entity) {
        return this.getBoundingBox();
    }

    @Override
    public boolean canBePushed() {
        return !this.removed && this.getControllingPassenger() == null;
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    protected void updateFallState(double y, boolean onGroundIn, BlockState state, BlockPos pos) {
    }

    @Override
    public void fall(float distance, float damageMultiplier) {
    }

    public float getHorizontalVelocity() {
        return (float) Math.sqrt(this.getMotion().getX() * this.getMotion().getX() + this.getMotion().getZ() * this.getMotion().getZ());
    }

    public void alignToGrid(boolean doPosAdjustment) {
        this.rotationYaw = Math.round(this.rotationYaw / 90F) * 90F;
        this.rotationPitch = 0F;

        Vec3d vec = new Vec3d(-this.mobileChunk.getCenterX(), -this.mobileChunk.minY(),
                -this.mobileChunk.getCenterZ());
        vec = vec.rotateYaw((float) Math.toRadians(this.rotationYaw));

        int ix = MathHelperMod.round_double(vec.x + this.posX);
        int iy = MathHelperMod.round_double(vec.y + this.posY);
        int iz = MathHelperMod.round_double(vec.z + this.posZ);

        if (doPosAdjustment) {
            this.setMotion(new Vec3d(0, 0, 0));
            this.setPositionAndUpdate(ix - vec.x, iy - vec.y, iz - vec.z);
        }

        this.setMotion(new Vec3d(0, 0, 0));
    }

    public boolean disassemble(boolean overwrite) {
        if (this.world.isRemote) {
            return true;
        }

        this.updatePassenger(this.getControllingPassenger());

        ChunkDisassembler disassembler = this.getDisassembler();
        disassembler.overwrite = overwrite;

        if (!disassembler.canDisassemble(this.getAssemblyInteractor())) {
            return false;
        }

        AssembleResult result = disassembler.doDisassemble(this.getNewAssemblyInteractor());

        return true;
    }

    public abstract MovingWorldAssemblyInteractor getNewAssemblyInteractor();

    public void dropAsItems() {
        TileEntity tileentity;
        BlockState blockState;
        for (int i = this.mobileChunk.minX(); i < this.mobileChunk.maxX(); i++) {
            for (int j = this.mobileChunk.minY(); j < this.mobileChunk.maxY(); j++) {
                for (int k = this.mobileChunk.minZ(); k < this.mobileChunk.maxZ(); k++) {
                    tileentity = this.mobileChunk.getTileEntity(new BlockPos(i, j, k));
                    if (tileentity instanceof IInventory) {
                        IInventory inv = (IInventory) tileentity;
                        for (int it = 0; it < inv.getSizeInventory(); it++) {
                            ItemStack is = inv.getStackInSlot(it);
                            if (is != null) {
                                this.entityDropItem(is, 0F);
                            }
                        }
                    }
                    blockState = this.mobileChunk.getBlockState(new BlockPos(i, j, k));

                    if (blockState.getBlock() != Blocks.AIR) {
                        Block.spawnDrops(blockState, world, new BlockPos(MathHelper.floor(this.posX),
                                MathHelper.floor(this.posY),
                                MathHelper.floor(this.posZ)));
                    }
                }
            }
        }
    }

    protected void fillAirBlocks(Set<BlockPos> set, BlockPos pos) {
        if (pos.getX() < this.mobileChunk.minX() - 1 || pos.getX() > this.mobileChunk.maxX()
                || pos.getY() < this.mobileChunk.minY() - 1 || pos.getY() > this.mobileChunk.maxY()
                || pos.getZ() < this.mobileChunk.minZ() - 1 || pos.getZ() > this.mobileChunk.maxZ()) {
            return;
        }
        if (set.contains(pos)) {
            return;
        }

        set.add(pos);
        if (this.mobileChunk.removeBlock(pos, true)) {
            this.fillAirBlocks(set, pos.add(0, 1, 0));
            //fillAirBlocks(set, x, y - 1, z);
            this.fillAirBlocks(set, pos.add(-1, 0, 0));
            this.fillAirBlocks(set, pos.add(0, 0, -1));
            this.fillAirBlocks(set, pos.add(1, 0, 0));
            this.fillAirBlocks(set, pos.add(0, 0, 1));
        }
    }

    public void setRiderDestination(Direction frontDirection, BlockPos riderDestination) {
        this.frontDirection = frontDirection;
        this.riderDestination = riderDestination;
    }

    @Override
    protected void writeAdditional(CompoundNBT tag) {
        CompressedChunkData compressedChunkData = new CompressedChunkData(this.mobileChunk, true);
        tag.putByteArray("chunk", compressedChunkData.getBytes());
        tag.putInt("riderDestinationX", this.riderDestination.getX());
        tag.putInt("riderDestinationY", this.riderDestination.getY());
        tag.putInt("riderDestinationZ", this.riderDestination.getZ());
        tag.putInt("front", this.frontDirection.getHorizontalIndex());

        if (!this.mobileChunk.chunkTileEntityMap.isEmpty()) {
            ListNBT tileEntities = new ListNBT();
            for (TileEntity tileentity : this.mobileChunk.chunkTileEntityMap.values()) {
                CompoundNBT comp = new CompoundNBT();
                tileentity.write(comp);
                tileEntities.add(comp);
            }
            tag.put("tileent", tileEntities);
        }

        if (this.mobileChunk.marker != null) {
            CompoundNBT markerComp = new CompoundNBT();
            markerComp.putInt("markerPosX", this.mobileChunk.marker.pos.getX());
            markerComp.putInt("markerPosY", this.mobileChunk.marker.pos.getY());
            markerComp.putInt("markerPosZ", this.mobileChunk.marker.pos.getZ());
            tag.put("markerInfo", markerComp);
        }

        tag.putString("name", this.info.getName());
        if (this.info.getOwner() != null) {
            tag.putString("owner", this.info.getOwner().toString());
        }

        this.writeMovingWorldNBT(tag);
    }

    public abstract void writeMovingWorldNBT(CompoundNBT tag);

    @Override
    protected void readAdditional(CompoundNBT tag) {
        if (this.mobileChunk == null) {
            if (this.world != null) {
                if (this.world.isRemote) {
                    this.initClient();
                } else {
                    this.initCommon();
                }
            }
        }

        new CompressedChunkData(tag.getByteArray("chunk")).loadBlocks(this.mobileChunk);
        if (tag.contains("riderDestination")) {
            short s = tag.getShort("riderDestination");
            int rX = s & 0xF;
            int rY = s >>> 4 & 0xF;
            int rZ = s >>> 8 & 0xF;
            this.riderDestination = new BlockPos(rX, rY, rZ);
            this.frontDirection = Direction.NORTH;
        } else {
            int rX = tag.getInt("riderDestinationX");
            int rY = tag.getInt("riderDestinationY");
            int rZ = tag.getInt("riderDestinationZ");
            this.riderDestination = new BlockPos(rX, rY, rZ);
            this.frontDirection = Direction.byHorizontalIndex(tag.getInt("front"));
        }

        ListNBT tiles = tag.getList("tileent", 10);
        if (tiles != null) {
            for (int i = 0; i < tiles.size(); i++) {
                try {
                    CompoundNBT comp = tiles.getCompound(i);
                    TileEntity tileentity = TileEntity.create(comp);
                    this.mobileChunk.setTileEntity(tileentity.getPos(), tileentity);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if (tag.contains("markerInfo")) {
            CompoundNBT markerComp = (CompoundNBT) tag.get("markerInfo");
            BlockPos markerPos = new BlockPos(markerComp.getInt("markerPosX"),
                    markerComp.getInt("markerPosY"),
                    markerComp.getInt("markerPosZ"));

            this.mobileChunk.marker = new LocatedBlock(this.mobileChunk.getBlockState(markerPos),
                    this.mobileChunk.getTileEntity(markerPos), markerPos);
        }

        this.info = new MovingWorldInfo();
        this.info.setName(tag.getString("name"));
        if (tag.contains("owner")) {
            this.info.setOwner(UUID.fromString(tag.getString("owner")));
        }
        this.readMovingWorldNBT(tag);
    }

    public abstract void readMovingWorldNBT(CompoundNBT tag);

    @OnlyIn(Dist.CLIENT)
    public void spawnParticles(double horvel) {
    }

    @Override
    public void writeSpawnData(PacketBuffer data) {
        data.writeInt(this.riderDestination.getX());
        data.writeInt(this.riderDestination.getY());
        data.writeInt(this.riderDestination.getZ());
        data.writeInt(this.frontDirection.getHorizontalIndex());

        data.writeString(this.info.getName());

        CompressedChunkData compressedChunkData = new CompressedChunkData(this.mobileChunk, true);
        compressedChunkData.writeTo(data);

        this.writeMovingWorldSpawnData(data);
    }

    /**
     * Same as the code from the Entity Class but it doesn't spawn particles, as with larger moving worlds
     * it can cause a lot of lag.
     */
    @Override
    protected void doWaterSplashEffect() {
        float sqrtMotion = MathHelper.sqrt(
                this.getMotion().x * this.getMotion().x * 0.20000000298023224D + this.getMotion().y * this.getMotion().y
                        + this.getMotion().z * this.getMotion().z * 0.20000000298023224D) * 0.2F;
        sqrtMotion = sqrtMotion > 1.0F ? 1.0F : sqrtMotion;
        this.playSound(this.getSplashSound(), sqrtMotion,
                1.0F + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.4F);
    }

    public abstract void writeMovingWorldSpawnData(ByteBuf data);

    @Override
    public void readSpawnData(PacketBuffer data) {
        int rX = data.readInt();
        int rY = data.readInt();
        int rZ = data.readInt();
        this.riderDestination = new BlockPos(rX, rY, rZ);
        this.frontDirection = Direction.byHorizontalIndex(data.readInt());

        this.info.setName(data.readString(32767));

        CompressedChunkData chunkData = new CompressedChunkData(data);
        chunkData.loadBlocks(this.mobileChunk);

        this.mobileChunk.onChunkLoad();
        this.readMovingWorldSpawnData(data);
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