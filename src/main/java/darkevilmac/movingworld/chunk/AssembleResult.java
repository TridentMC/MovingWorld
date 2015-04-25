package darkevilmac.movingworld.chunk;


import darkevilmac.movingworld.MaterialDensity;
import darkevilmac.movingworld.MovingWorld;
import darkevilmac.movingworld.entity.EntityMovingWorld;
import darkevilmac.movingworld.event.AssembleBlockEvent;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

import java.util.ArrayList;
import java.util.List;

public class AssembleResult {
    public static final int RESULT_NONE = 0, RESULT_OK = 1, RESULT_BLOCK_OVERFLOW = 2, RESULT_MISSING_MARKER = 3, RESULT_ERROR_OCCURED = 4,
            RESULT_BUSY_COMPILING = 5, RESULT_INCONSISTENT = 6, RESULT_OK_WITH_WARNINGS = 7;
    public final List<LocatedBlock> assembledBlocks = new ArrayList<LocatedBlock>();
    public int xOffset, yOffset, zOffset;
    public MovingWorldAssemblyInteractor assemblyInteractor;
    LocatedBlock movingWorldMarkingBlock;
    int resultCode;
    int blockCount;
    int tileEntityCount;
    float mass;

    public AssembleResult(int resultCode, ByteBuf buf) {
        if (resultCode == RESULT_NONE) return;
        blockCount = buf.readInt();
        tileEntityCount = buf.readInt();
        mass = buf.readFloat();
    }

    public AssembleResult(NBTTagCompound compound, World world) {
        resultCode = compound.getByte("res");
        blockCount = compound.getInteger("blockc");
        tileEntityCount = compound.getInteger("tec");
        mass = compound.getFloat("mass");
        xOffset = compound.getInteger("xO");
        yOffset = compound.getInteger("yO");
        zOffset = compound.getInteger("zO");
        if (compound.hasKey("list")) {
            NBTTagList list = compound.getTagList("list", 10);
            for (int i = 0; i < list.tagCount(); i++) {
                NBTTagCompound comp = list.getCompoundTagAt(i);
                assembledBlocks.add(new LocatedBlock(comp, world));
            }
        }
        if (compound.hasKey("marker")) {
            NBTTagCompound comp = compound.getCompoundTag("marker");
            movingWorldMarkingBlock = new LocatedBlock(comp, world);
        }
    }

    AssembleResult() {
        clear();
    }

    void assembleBlock(LocatedBlock lb) {
        assembledBlocks.add(lb);
        blockCount = assembledBlocks.size();
        if (lb.tileEntity != null) {
            tileEntityCount++;
        }
        mass += MaterialDensity.getDensity(lb.block);
        xOffset = Math.min(xOffset, lb.coords.chunkPosX);
        yOffset = Math.min(yOffset, lb.coords.chunkPosY);
        zOffset = Math.min(zOffset, lb.coords.chunkPosZ);
    }

    public void clear() {
        resultCode = RESULT_NONE;
        movingWorldMarkingBlock = null;
        assembledBlocks.clear();
        xOffset = yOffset = zOffset = 0;
    }

    public EntityMovingWorld getEntity(World world, EntityMovingWorld entity) {
        if (!isOK()) return null;

        if (entity == null) {
            MovingWorld.logger.error("A null movingWorld was attempted!");
            return null;
        }

        entity.setPilotSeat(movingWorldMarkingBlock.blockMeta & 3, movingWorldMarkingBlock.coords.chunkPosX - xOffset, movingWorldMarkingBlock.coords.chunkPosY - yOffset, movingWorldMarkingBlock.coords.chunkPosZ - zOffset);
        entity.getMovingWorldChunk().setCreationSpotBiomeGen(world.getBiomeGenForCoords(movingWorldMarkingBlock.coords.chunkPosX, movingWorldMarkingBlock.coords.chunkPosZ));

        boolean flag = world.getGameRules().getGameRuleBooleanValue("doTileDrops");
        world.getGameRules().setOrCreateGameRule("doTileDrops", "false");

        try {
            TileEntity tileentity;
            int ix, iy, iz;
            for (LocatedBlock lb : assembledBlocks) {
                AssembleBlockEvent event = new AssembleBlockEvent(lb);
                MinecraftForge.EVENT_BUS.post(event);
                ix = lb.coords.chunkPosX - xOffset;
                iy = lb.coords.chunkPosY - yOffset;
                iz = lb.coords.chunkPosZ - zOffset;
                tileentity = lb.tileEntity;
                if (tileentity != null || lb.block.hasTileEntity(lb.blockMeta) && (tileentity = world.getTileEntity(lb.coords.chunkPosX, lb.coords.chunkPosY, lb.coords.chunkPosZ)) != null) {
                    tileentity.validate();
                }
                if (entity.getMovingWorldChunk().setBlockIDWithMetadata(ix, iy, iz, lb.block, lb.blockMeta)) {
                    entity.getMovingWorldChunk().setTileEntity(ix, iy, iz, tileentity);
                    world.setBlock(lb.coords.chunkPosX, lb.coords.chunkPosY, lb.coords.chunkPosZ, Blocks.air, 1, 2);
                }
            }
            for (LocatedBlock block : assembledBlocks) {
                world.setBlockToAir(block.coords.chunkPosX, block.coords.chunkPosY, block.coords.chunkPosZ);
            }
        } catch (Exception e) {
            resultCode = RESULT_ERROR_OCCURED;
            e.printStackTrace();
            return null;
        } finally {
            world.getGameRules().setOrCreateGameRule("doTileDrops", String.valueOf(flag));
        }

        entity.getMovingWorldChunk().setChunkModified();
        entity.getMovingWorldChunk().onChunkLoad();
        entity.setLocationAndAngles(xOffset + entity.getMovingWorldChunk().getCenterX(), yOffset, zOffset + entity.getMovingWorldChunk().getCenterZ(), 0F, 0F);
        assemblyInteractor.transferToCapabilities(entity.getCapabilities());

        return entity;
    }

    public int getCode() {
        return resultCode;
    }

    public boolean isOK() {
        return resultCode == RESULT_OK || resultCode == RESULT_OK_WITH_WARNINGS;
    }

    public LocatedBlock getShipMarker() {
        return movingWorldMarkingBlock;
    }

    public int getBlockCount() {
        return blockCount;
    }

    public int getTileEntityCount() {
        return tileEntityCount;
    }

    public float getMass() {
        return mass;
    }

    public void checkConsistent(World world) {
        boolean warn = false;
        for (LocatedBlock lb : assembledBlocks) {
            Block block = world.getBlock(lb.coords.chunkPosX, lb.coords.chunkPosY, lb.coords.chunkPosZ);
            if (block != lb.block) {
                resultCode = RESULT_INCONSISTENT;
                return;
            }
            int meta = world.getBlockMetadata(lb.coords.chunkPosX, lb.coords.chunkPosY, lb.coords.chunkPosZ);
            if (meta != lb.blockMeta) {
                warn = true;
            }
        }
        resultCode = warn ? RESULT_OK_WITH_WARNINGS : RESULT_OK;
    }

    public void writeNBTFully(NBTTagCompound compound) {
        writeNBTMetadata(compound);
        NBTTagList list = new NBTTagList();
        for (LocatedBlock lb : assembledBlocks) {
            NBTTagCompound comp = new NBTTagCompound();
            lb.writeToNBT(comp);
            list.appendTag(comp);
        }
        compound.setTag("list", list);

        if (movingWorldMarkingBlock != null) {
            NBTTagCompound comp = new NBTTagCompound();
            movingWorldMarkingBlock.writeToNBT(comp);
            compound.setTag("marker", comp);
        }
    }

    public void writeNBTMetadata(NBTTagCompound compound) {
        compound.setByte("res", (byte) getCode());
        compound.setInteger("blockc", getBlockCount());
        compound.setInteger("tec", getTileEntityCount());
        compound.setFloat("mass", getMass());
        compound.setInteger("xO", xOffset);
        compound.setInteger("yO", yOffset);
        compound.setInteger("zO", zOffset);
    }
}
