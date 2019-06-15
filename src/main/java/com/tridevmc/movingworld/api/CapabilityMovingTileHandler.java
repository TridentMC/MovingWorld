package com.tridevmc.movingworld.api;

import com.tridevmc.movingworld.common.chunk.mobilechunk.MobileChunk;
import com.tridevmc.movingworld.common.entity.EntityMovingWorld;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ServerWorld;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;

/**
 * This is pretty much a stub, I might actually use it but now isn't the time.
 */
public class CapabilityMovingTileHandler {

    //@CapabilityInject(IMovingTile.class)
    //public static Capability<IMovingTile> MOVING_TILE_CAPABILITY = null;

    private static void register() {
        CapabilityManager.INSTANCE.register(IMovingTile.class, new Capability.IStorage<IMovingTile>() {
            @Override
            public INBT writeNBT(Capability<IMovingTile> capability, IMovingTile instance, Direction side) {
                CompoundNBT compound = new CompoundNBT();

                if (instance.getChunkPos() == null || instance.getParentMovingWorld() == null) {
                    compound.putBoolean("Empty", true);
                } else {
                    compound.putBoolean("Empty", false);
                    compound.putLong("ChunkPos", instance.getChunkPos().toLong());
                    compound.putUniqueId("MovingWorldID", instance.getParentMovingWorld().getUniqueID());
                    compound.putInt("Dimension", instance.getParentMovingWorld().getEntityWorld().getDimension().getType().getId());
                }

                return compound;
            }

            @Override
            public void readNBT(Capability<IMovingTile> capability, IMovingTile instance, Direction side, INBT nbt) {
                if (nbt instanceof CompoundNBT) {
                    CompoundNBT compound = (CompoundNBT) nbt;

                    if (!compound.getBoolean("Empty")) {
                        DimensionType dimension = DimensionType.getById(compound.getInt("Dimension"));
                        MinecraftServer server = LogicalSidedProvider.INSTANCE.get(LogicalSide.SERVER);
                        ServerWorld world = server.getWorld(dimension);
                        instance.setParentMovingWorld((EntityMovingWorld) world.getEntityByUuid(compound.getUniqueId("MovingWorldID")));
                        instance.setChunkPos(BlockPos.fromLong(compound.getLong("ChunkPos")));
                    }
                }
            }
        }, () -> new IMovingTile() {

            EntityMovingWorld movingWorld = null;
            BlockPos chunkPos;

            @Override
            public void setParentMovingWorld(EntityMovingWorld movingWorld, BlockPos chunkPos) {
                this.movingWorld = movingWorld;
                this.chunkPos = chunkPos;
            }

            @Override
            public EntityMovingWorld getParentMovingWorld() {
                return movingWorld;
            }

            @Override
            public void setParentMovingWorld(EntityMovingWorld entityMovingWorld) {
                this.movingWorld = entityMovingWorld;
            }

            @Override
            public BlockPos getChunkPos() {
                return chunkPos;
            }

            @Override
            public void setChunkPos(BlockPos chunkPos) {
                this.chunkPos = chunkPos;
            }

            @Override
            public void tick(MobileChunk mobileChunk) {
                // No implementation by default.
            }
        });
    }

}
