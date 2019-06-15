package com.tridevmc.movingworld.common.entity;

import com.tridevmc.movingworld.MovingWorldMod;
import com.tridevmc.movingworld.common.chunk.mobilechunk.MobileChunk;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashSet;

public abstract class MovingWorldHandlerCommon {

    public MovingWorldHandlerCommon(EntityMovingWorld entityMovingWorld) {
        setMovingWorld(entityMovingWorld);
    }

    public abstract EntityMovingWorld getMovingWorld();

    public abstract void setMovingWorld(EntityMovingWorld movingWorld);

    public boolean processInitialInteract(PlayerEntity player, Hand hand) {
        return !player.isSneaking();
    }

    public void onChunkUpdate() {
        EntityMovingWorld movingWorld = getMovingWorld();
        MobileChunk chunk = movingWorld.getMobileChunk();
        World world = chunk.getWorld();
        MovingWorldCapabilities capabilities = movingWorld.getMovingWorldCapabilities();
        capabilities.clearBlockCount();
        for (int i = chunk.minX(); i < chunk.maxX(); i++) {
            for (int j = chunk.minY(); j < chunk.maxY(); j++) {
                for (int k = chunk.minZ(); k < chunk.maxZ(); k++) {
                    BlockPos pos = new BlockPos(i, j, k);
                    BlockState blockState = chunk.getBlockState(pos);
                    if (blockState != null && blockState.getMaterial() != Material.AIR) {
                        capabilities.onChunkBlockAdded(blockState, pos);
                    }
                }
            }
        }

        movingWorld.setSize(Math.max(chunk.maxX() - chunk.minX(), chunk.maxZ() - chunk.minZ()), chunk.maxY() - chunk.minY());
        world.increaseMaxEntityRadius(Math.max(movingWorld.getWidth(), movingWorld.getHeight()) + 2F);
        try {
            movingWorld.fillAirBlocks(new HashSet<>(), new BlockPos(-1, -1, -1));
        } catch (StackOverflowError e) {
            MovingWorldMod.LOG.error("Failure during moving world post-initialization", e);
        }

        movingWorld.setLayeredBlockVolumeCount(new int[chunk.maxY() - chunk.minY()]);
        for (int y = 0; y < movingWorld.getLayeredBlockVolumeCount().length; y++) {
            for (int i = chunk.minX(); i < chunk.maxX(); i++) {
                for (int j = chunk.minZ(); j < chunk.maxZ(); j++) {
                    BlockPos pos = new BlockPos(i, y + chunk.minY(), j);
                    if (chunk.isBlockTakingWaterVolume(pos)) {
                        int[] layeredBlockVolCount = movingWorld.getLayeredBlockVolumeCount();
                        layeredBlockVolCount[y]++;
                        movingWorld.setLayeredBlockVolumeCount(layeredBlockVolCount);
                    }
                }
            }
        }
        movingWorld.setFlying(movingWorld.getMovingWorldCapabilities().canFly());
        movingWorld.getMovingWorldCapabilities().postBlockAdding();
    }
}
