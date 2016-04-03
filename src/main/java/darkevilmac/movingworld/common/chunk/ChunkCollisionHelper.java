package darkevilmac.movingworld.common.chunk;

import darkevilmac.movingworld.common.chunk.mobilechunk.MobileChunk;
import darkevilmac.movingworld.common.entity.IMixinEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockWall;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.Iterator;
import java.util.List;

public class ChunkCollisionHelper {

    /**
     * Unimplemented, for colliding with a MobileChunk
     *
     * @param entity
     * @param mobileChunk
     * @return cancel further method.
     */
    public static boolean onEntityMove(Entity entity, MobileChunk mobileChunk, double x, double y, double z) {
        boolean cancel = false;
        IMixinEntity mixinEntity = (IMixinEntity) entity;

        double d3 = mobileChunk.getChunkPosForWorldPos(new Vec3d(entity.posX, 0, 0)).xCoord;
        double d4 = mobileChunk.getChunkPosForWorldPos(new Vec3d(0, entity.posY, 0)).yCoord;
        double d5 = mobileChunk.getChunkPosForWorldPos(new Vec3d(0, 0, entity.posZ)).zCoord;

        final double entityChunkPosX = mobileChunk.getChunkPosForWorldPos(new Vec3d(entity.posX, 0, 0)).xCoord;
        final double entityChunkPosY = mobileChunk.getChunkPosForWorldPos(new Vec3d(0, entity.posY, 0)).yCoord;
        final double entityChunkPosZ = mobileChunk.getChunkPosForWorldPos(new Vec3d(0, 0, entity.posZ)).zCoord;

        double d6 = x;
        double d7 = y;
        double d8 = z;

        AxisAlignedBB entityBox = entity.getEntityBoundingBox();
        entityBox = mobileChunk.offsetWorldBBToChunkBB(entityBox);

        List list1 = mobileChunk.getCollidingBoundingBoxes(true, entityBox.addCoord(x, y, z));
        AxisAlignedBB axisalignedbb = entityBox;
        AxisAlignedBB axisalignedbb1;

        if (!list1.isEmpty()) {
            for (Iterator iterator = list1.iterator(); iterator.hasNext(); y = axisalignedbb1.calculateYOffset(entityBox, y)) {
                axisalignedbb1 = (AxisAlignedBB) iterator.next();
            }

            entityBox = entityBox.offset(0.0D, y, 0.0D);
            AxisAlignedBB axisalignedbb2;
            Iterator iterator8;

            for (iterator8 = list1.iterator(); iterator8.hasNext(); x = axisalignedbb2.calculateXOffset(entityBox, x)) {
                axisalignedbb2 = (AxisAlignedBB) iterator8.next();
            }

            entityBox = entityBox.offset(x, 0.0D, 0.0D);

            for (iterator8 = list1.iterator(); iterator8.hasNext(); z = axisalignedbb2.calculateZOffset(entityBox, z)) {
                axisalignedbb2 = (AxisAlignedBB) iterator8.next();
            }

            entityBox = entityBox.offset(0.0D, 0.0D, z);
        }
        if (entity.stepHeight > 0.0F && (d6 != x || d8 != z)) {
            double d14 = x;
            double d10 = y;
            double d11 = z;
            AxisAlignedBB axisalignedbb3 = entityBox;
            entityBox = (axisalignedbb);
            y = (double) entity.stepHeight;
            List list = mobileChunk.getCollidingBoundingBoxes(true, entityBox.addCoord(d6, y, d8));
            AxisAlignedBB axisalignedbb4 = entityBox;
            AxisAlignedBB axisalignedbb5 = axisalignedbb4.addCoord(d6, 0.0D, d8);
            double d12 = y;
            AxisAlignedBB axisalignedbb6;

            if (!list.isEmpty()) {
                for (Iterator iterator1 = list.iterator(); iterator1.hasNext(); d12 = axisalignedbb6.calculateYOffset(axisalignedbb5, d12)) {
                    axisalignedbb6 = (AxisAlignedBB) iterator1.next();
                }

                axisalignedbb4 = axisalignedbb4.offset(0.0D, d12, 0.0D);
                double d18 = d6;
                AxisAlignedBB axisalignedbb7;

                for (Iterator iterator2 = list.iterator(); iterator2.hasNext(); d18 = axisalignedbb7.calculateXOffset(axisalignedbb4, d18)) {
                    axisalignedbb7 = (AxisAlignedBB) iterator2.next();
                }

                axisalignedbb4 = axisalignedbb4.offset(d18, 0.0D, 0.0D);
                double d19 = d8;
                AxisAlignedBB axisalignedbb8;

                for (Iterator iterator3 = list.iterator(); iterator3.hasNext(); d19 = axisalignedbb8.calculateZOffset(axisalignedbb4, d19)) {
                    axisalignedbb8 = (AxisAlignedBB) iterator3.next();
                }

                axisalignedbb4 = axisalignedbb4.offset(0.0D, 0.0D, d19);
                AxisAlignedBB axisalignedbb13 = entityBox;
                double d20 = y;
                AxisAlignedBB axisalignedbb9;

                for (Iterator iterator4 = list.iterator(); iterator4.hasNext(); d20 = axisalignedbb9.calculateYOffset(axisalignedbb13, d20)) {
                    axisalignedbb9 = (AxisAlignedBB) iterator4.next();
                }

                axisalignedbb13 = axisalignedbb13.offset(0.0D, d20, 0.0D);
                double d21 = d6;
                AxisAlignedBB axisalignedbb10;

                for (Iterator iterator5 = list.iterator(); iterator5.hasNext(); d21 = axisalignedbb10.calculateXOffset(axisalignedbb13, d21)) {
                    axisalignedbb10 = (AxisAlignedBB) iterator5.next();
                }

                axisalignedbb13 = axisalignedbb13.offset(d21, 0.0D, 0.0D);
                double d22 = d8;
                AxisAlignedBB axisalignedbb11;

                for (Iterator iterator6 = list.iterator(); iterator6.hasNext(); d22 = axisalignedbb11.calculateZOffset(axisalignedbb13, d22)) {
                    axisalignedbb11 = (AxisAlignedBB) iterator6.next();
                }

                axisalignedbb13 = axisalignedbb13.offset(0.0D, 0.0D, d22);
                double d23 = d18 * d18 + d19 * d19;
                double d13 = d21 * d21 + d22 * d22;

                if (d23 > d13) {
                    x = d18;
                    z = d19;
                    entityBox = (axisalignedbb4);
                } else {
                    x = d21;
                    z = d22;
                    entityBox = (axisalignedbb13);
                }

                y = (double) (-entity.stepHeight);
                AxisAlignedBB axisalignedbb12;

                for (Iterator iterator7 = list.iterator(); iterator7.hasNext(); y = axisalignedbb12.calculateYOffset(entityBox, y)) {
                    axisalignedbb12 = (AxisAlignedBB) iterator7.next();
                }

                entityBox = (entityBox.offset(0.0D, y, 0.0D));

                if (d14 * d14 + d11 * d11 >= x * x + z * z) {
                    x = d14;
                    y = d10;
                    z = d11;
                    entityBox = (axisalignedbb3);
                }
            }
        }

        double minX = entityBox.minX + (mobileChunk.getEntityMovingWorld().posX - (mobileChunk.maxX() / 2));
        double minY = entityBox.minY + (mobileChunk.getEntityMovingWorld().posY - (mobileChunk.maxY() / 2));
        double minZ = entityBox.minZ + (mobileChunk.getEntityMovingWorld().posZ - (mobileChunk.maxZ() / 2));
        double maxX = entityBox.maxX + (mobileChunk.getEntityMovingWorld().posX - (mobileChunk.maxX() / 2));
        double maxY = entityBox.maxY + (mobileChunk.getEntityMovingWorld().posY - (mobileChunk.maxY() / 2));
        double maxZ = entityBox.maxZ + (mobileChunk.getEntityMovingWorld().posZ - (mobileChunk.maxZ() / 2));

        System.out.println(minX);
        System.out.println(minY);
        System.out.println(minZ);
        System.out.println(maxX);
        System.out.println(maxY);
        System.out.println(maxZ);
        entity.setEntityBoundingBox(new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ));

        mixinEntity.resetPosToBB();
        entity.isCollidedHorizontally = d6 != x || d8 != z;
        entity.isCollidedVertically = d7 != y;
        entity.onGround = entity.isCollidedVertically && d7 < 0.0D;
        entity.isCollided = entity.isCollidedHorizontally || entity.isCollidedVertically;
        int i = MathHelper.floor_double(entityChunkPosX);
        int j = MathHelper.floor_double(entityChunkPosY - 0.20000000298023224D);
        int k = MathHelper.floor_double(entityChunkPosZ);
        BlockPos blockPos = new BlockPos(i, j, k);
        IBlockState blockState = mobileChunk.getBlockState(blockPos);
        Block block = blockState.getBlock();

        if (blockState.getMaterial() == Material.air) {
            Block blockBelow = mobileChunk.getBlockState(blockPos.down()).getBlock();

            if (blockBelow instanceof BlockFence || blockBelow instanceof BlockWall || blockBelow instanceof BlockFenceGate) {
                block = blockBelow;
                blockPos = blockPos.down();
            }
        }

        mixinEntity.updateFall(y, entity.onGround, block, blockPos);

        if (d6 != x) {
            entity.motionX = 0.0D;
        }

        if (d8 != z) {
            entity.motionZ = 0.0D;
        }

        if (d7 != y) {
            entity.motionY = 0.0D;
        }

        if (mixinEntity.doesTriggerWalking() && entity.ridingEntity == null) {
            double d15 = entityChunkPosX - d3;
            double d16 = entityChunkPosY - d4;
            double d17 = entityChunkPosZ - d5;

            if (block != Blocks.ladder) {
                d16 = 0.0D;
            }
            entity.distanceWalkedModified = (float) ((double) entity.distanceWalkedModified + (double) MathHelper.sqrt_double(d15 * d15 + d17 * d17) * 0.6D);
            entity.distanceWalkedOnStepModified = (float) ((double) entity.distanceWalkedOnStepModified + (double) MathHelper.sqrt_double(d15 * d15 + d16 * d16 + d17 * d17) * 0.6D);

            if (entity.distanceWalkedOnStepModified > (float) mixinEntity.getNextStepDistance() && block.getMaterial() != Material.air) {
                mixinEntity.setNextStepDistance((int) entity.distanceWalkedOnStepModified + 1);
                playStepSoundForEntity(entity, block);
            }
        }

        minX = entityBox.minX + (mobileChunk.getEntityMovingWorld().posX - (mobileChunk.maxX() / 2));
        minY = entityBox.minY + (mobileChunk.getEntityMovingWorld().posY - (mobileChunk.maxY() / 2));
        minZ = entityBox.minZ + (mobileChunk.getEntityMovingWorld().posZ - (mobileChunk.maxZ() / 2));
        maxX = entityBox.maxX + (mobileChunk.getEntityMovingWorld().posX - (mobileChunk.maxX() / 2));
        maxY = entityBox.maxY + (mobileChunk.getEntityMovingWorld().posY - (mobileChunk.maxY() / 2));
        maxZ = entityBox.maxZ + (mobileChunk.getEntityMovingWorld().posZ - (mobileChunk.maxZ() / 2));
        entity.setEntityBoundingBox(new AxisAlignedBB(minX,
                minY,
                minZ,
                maxX,
                maxY,
                maxZ));
        mixinEntity.resetPosToBB();

        return cancel;
    }

    protected static void playStepSoundForEntity(Entity e, Block blockIn) {
        //nah
    }
}
