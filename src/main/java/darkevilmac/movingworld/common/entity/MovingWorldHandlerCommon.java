package darkevilmac.movingworld.common.entity;

import darkevilmac.movingworld.MovingWorld;
import darkevilmac.movingworld.common.chunk.mobilechunk.MobileChunk;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;

import java.util.HashSet;

public abstract class MovingWorldHandlerCommon {

    public MovingWorldHandlerCommon(EntityMovingWorld entityMovingWorld) {
        setMovingWorld(entityMovingWorld);
    }

    public abstract EntityMovingWorld getMovingWorld();

    public abstract void setMovingWorld(EntityMovingWorld movingWorld);

    public boolean interact(EntityPlayer player) {
        return false;
    }

    public void onChunkUpdate() {
        MobileChunk chunk = getMovingWorld().getMovingWorldChunk();
        getMovingWorld().getCapabilities().clearBlockCount();
        for (int i = chunk.minX(); i < chunk.maxX(); i++) {
            for (int j = chunk.minY(); j < chunk.maxY(); j++) {
                for (int k = chunk.minZ(); k < chunk.maxZ(); k++) {
                    Block block = chunk.getBlock(i, j, k);
                    if (block.getMaterial() != Material.air) {
                        getMovingWorld().getCapabilities().onChunkBlockAdded(block, chunk.getBlockMetadata(i, j, k), i, j, k);
                    }
                }
            }
        }

        getMovingWorld().setSize(Math.max(chunk.maxX() - chunk.minX(), chunk.maxZ() - chunk.minZ()), chunk.maxY() - chunk.minY());
        World.MAX_ENTITY_RADIUS = Math.max(World.MAX_ENTITY_RADIUS, Math.max(getMovingWorld().width, getMovingWorld().height) + 2F);

        try {
            getMovingWorld().fillAirBlocks(new HashSet<ChunkPosition>(), -1, -1, -1);
        } catch (StackOverflowError e) {
            MovingWorld.logger.error("Failure during moving world post-initialization", e);
        }

        getMovingWorld().setLayeredBlockVolumeCount(new int[chunk.maxY() - chunk.minY()]);
        for (int y = 0; y < getMovingWorld().getLayeredBlockVolumeCount().length; y++) {
            for (int i = chunk.minX(); i < chunk.maxX(); i++) {
                for (int j = chunk.minZ(); j < chunk.maxZ(); j++) {
                    if (chunk.isBlockTakingWaterVolume(i, y + chunk.minY(), j)) {
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
