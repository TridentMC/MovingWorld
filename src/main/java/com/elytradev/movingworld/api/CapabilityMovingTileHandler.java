package com.elytradev.movingworld.api;

import com.elytradev.movingworld.common.chunk.mobilechunk.MobileChunk;
import com.elytradev.movingworld.common.entity.EntityMovingWorld;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;

/**
 * This is pretty much a stub, I might actually use it but now isn't the time.
 */
public class CapabilityMovingTileHandler {

    //@CapabilityInject(IMovingTile.class)
    //public static Capability<IMovingTile> MOVING_TILE_CAPABILITY = null;

    private static void register() {
        CapabilityManager.INSTANCE.register(IMovingTile.class, new Capability.IStorage<IMovingTile>() {
            @Override
            public NBTBase writeNBT(Capability<IMovingTile> capability, IMovingTile instance, EnumFacing side) {
                NBTTagCompound compound = new NBTTagCompound();

                if (instance.getChunkPos() == null || instance.getParentMovingWorld() == null) {
                    compound.setBoolean("Empty", true);
                } else {
                    compound.setBoolean("Empty", false);
                    compound.setLong("ChunkPos", instance.getChunkPos().toLong());
                    compound.setUniqueId("MovingWorldID", instance.getParentMovingWorld().getPersistentID());
                    compound.setInteger("Dimension", instance.getParentMovingWorld().getEntityWorld().provider.getDimension());
                }

                return compound;
            }

            @Override
            public void readNBT(Capability<IMovingTile> capability, IMovingTile instance, EnumFacing side, NBTBase nbt) {
                if (nbt instanceof NBTTagCompound) {
                    NBTTagCompound compound = (NBTTagCompound) nbt;

                    if (!compound.getBoolean("Empty")) {
                        instance.setParentMovingWorld((EntityMovingWorld)
                                DimensionManager.getWorld(compound.getInteger("Dimension"))
                                        .getEntityFromUuid(compound.getUniqueId("MovingWorldID")));
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
