package io.github.elytra.movingworld.common.entity;

import io.github.elytra.movingworld.MovingWorldMod;
import io.github.elytra.movingworld.common.chunk.mobilechunk.MobileChunk;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashSet;

public abstract class MovingWorldHandlerCommon {

    public MovingWorldHandlerCommon(EntityMovingWorld entityMovingWorld) {
        setMovingWorld(entityMovingWorld);
    }

    public abstract EntityMovingWorld getMovingWorld();

    public abstract void setMovingWorld(EntityMovingWorld movingWorld);

    public boolean interact(EntityPlayer player, ItemStack stack, EnumHand hand) {
        return false;
    }

    public void onChunkUpdate() {
        MobileChunk chunk = getMovingWorld().getMobileChunk();
        getMovingWorld().getCapabilities().clearBlockCount();
        for (int i = chunk.minX(); i < chunk.maxX(); i++) {
            for (int j = chunk.minY(); j < chunk.maxY(); j++) {
                for (int k = chunk.minZ(); k < chunk.maxZ(); k++) {
                    BlockPos pos = new BlockPos(i, j, k);
                    IBlockState blockState = chunk.getBlockState(pos);
                    if (blockState != null && blockState.getMaterial() != Material.AIR) {
                        getMovingWorld().getCapabilities().onChunkBlockAdded(blockState, pos);
                    }
                }
            }
        }

        getMovingWorld().setSize(Math.max(chunk.maxX() - chunk.minX(), chunk.maxZ() - chunk.minZ()), chunk.maxY() - chunk.minY());
        World.MAX_ENTITY_RADIUS = Math.max(World.MAX_ENTITY_RADIUS, Math.max(getMovingWorld().width, getMovingWorld().height) + 2F);

        try {
            getMovingWorld().fillAirBlocks(new HashSet<BlockPos>(), new BlockPos(-1, -1, -1));
        } catch (StackOverflowError e) {
            MovingWorldMod.logger.error("Failure during moving world post-initialization", e);
        }

        getMovingWorld().setLayeredBlockVolumeCount(new int[chunk.maxY() - chunk.minY()]);
        for (int y = 0; y < getMovingWorld().getLayeredBlockVolumeCount().length; y++) {
            for (int i = chunk.minX(); i < chunk.maxX(); i++) {
                for (int j = chunk.minZ(); j < chunk.maxZ(); j++) {
                    BlockPos pos = new BlockPos(i, y + chunk.minY(), j);
                    if (chunk.isBlockTakingWaterVolume(pos)) {
                        int[] layeredBlockVolCount = getMovingWorld().getLayeredBlockVolumeCount();
                        layeredBlockVolCount[y]++;
                        getMovingWorld().setLayeredBlockVolumeCount(layeredBlockVolCount);
                    }
                }
            }
        }
        getMovingWorld().isFlying = getMovingWorld().getCapabilities().canFly();
        getMovingWorld().getCapabilities().postBlockAdding();
    }
}
