package com.elytradev.movingworld.common.entity;

import com.elytradev.movingworld.MovingWorldMod;
import com.elytradev.movingworld.common.chunk.mobilechunk.MobileChunk;
import com.tridevmc.compound.core.reflect.WrappedField;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.extensions.IForgeWorld;

import java.util.HashSet;

public abstract class MovingWorldHandlerCommon {

    public MovingWorldHandlerCommon(EntityMovingWorld entityMovingWorld) {
        setMovingWorld(entityMovingWorld);
    }

    private static final WrappedField<Float> MAX_ENTITY_RADIUS = WrappedField.create(IForgeWorld.class, "MAX_ENTITY_RADIUS");

    public abstract EntityMovingWorld getMovingWorld();

    public abstract void setMovingWorld(EntityMovingWorld movingWorld);

    public boolean processInitialInteract(EntityPlayer player, EnumHand hand) {
        return !player.isSneaking();
    }

    public void onChunkUpdate() {
        MobileChunk chunk = getMovingWorld().getMobileChunk();
        getMovingWorld().getMovingWorldCapabilities().clearBlockCount();
        for (int i = chunk.minX(); i < chunk.maxX(); i++) {
            for (int j = chunk.minY(); j < chunk.maxY(); j++) {
                for (int k = chunk.minZ(); k < chunk.maxZ(); k++) {
                    BlockPos pos = new BlockPos(i, j, k);
                    IBlockState blockState = chunk.getBlockState(pos);
                    if (blockState != null && blockState.getMaterial() != Material.AIR) {
                        getMovingWorld().getMovingWorldCapabilities().onChunkBlockAdded(blockState, pos);
                    }
                }
            }
        }

        getMovingWorld().setSize(Math.max(chunk.maxX() - chunk.minX(), chunk.maxZ() - chunk.minZ()), chunk.maxY() - chunk.minY());
        MAX_ENTITY_RADIUS.setStaticValue((float) Math.max(IForgeWorld.MAX_ENTITY_RADIUS, Math.max(getMovingWorld().width, getMovingWorld().height) + 2F));
        try {
            getMovingWorld().fillAirBlocks(new HashSet<>(), new BlockPos(-1, -1, -1));
        } catch (StackOverflowError e) {
            MovingWorldMod.LOG.error("Failure during moving world post-initialization", e);
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
        getMovingWorld().setFlying(getMovingWorld().getMovingWorldCapabilities().canFly());
        getMovingWorld().getMovingWorldCapabilities().postBlockAdding();
    }
}
