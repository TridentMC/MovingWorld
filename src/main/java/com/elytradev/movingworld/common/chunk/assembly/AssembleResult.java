package com.elytradev.movingworld.common.chunk.assembly;


import com.elytradev.movingworld.MovingWorldMod;
import com.elytradev.movingworld.common.chunk.LocatedBlock;
import com.elytradev.movingworld.common.chunk.MovingWorldAssemblyInteractor;
import com.elytradev.movingworld.common.entity.EntityMovingWorld;
import com.elytradev.movingworld.common.tile.TileMovingMarkingBlock;
import com.elytradev.movingworld.common.util.LocatedBlockList;
import com.elytradev.movingworld.common.util.MaterialDensity;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;

public class AssembleResult {

    public final LocatedBlockList assembledBlocks = new LocatedBlockList();
    public BlockPos offset;
    public MovingWorldAssemblyInteractor assemblyInteractor;
    LocatedBlock movingWorldMarkingBlock;
    ResultType resultType;
    int blockCount;
    int tileEntityCount;
    float mass;

    public AssembleResult(ResultType resultCode, ByteBuf buf) {
        this.resultType = resultCode;
        if (resultCode == ResultType.RESULT_NONE) return;
        blockCount = buf.readInt();
        tileEntityCount = buf.readInt();
        mass = buf.readFloat();
    }

    public AssembleResult(NBTTagCompound tag, World world) {
        resultType = ResultType.fromByte(tag.getByte("res"));
        blockCount = tag.getInteger("blockc");
        tileEntityCount = tag.getInteger("tec");
        mass = tag.getFloat("mass");
        offset = new BlockPos(-tag.getInteger("xO"),
                tag.getInteger("yO"),
                tag.getInteger("zO"));
        if (tag.hasKey("list")) {
            NBTTagList list = tag.getTagList("list", 10);
            for (int i = 0; i < list.tagCount(); i++) {
                NBTTagCompound comp = list.getCompoundTagAt(i);
                assembledBlocks.add(new LocatedBlock(comp, world));
            }
        }
        if (tag.hasKey("marker")) {
            NBTTagCompound comp = tag.getCompoundTag("marker");
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
        mass += MaterialDensity.getDensity(lb.blockState);
        offset = new BlockPos(Math.min(offset.getX(), lb.blockPos.getX()),
                Math.min(offset.getY(), lb.blockPos.getY()),
                Math.min(offset.getZ(), lb.blockPos.getZ()));
    }

    public void clear() {
        resultType = ResultType.RESULT_NONE;
        movingWorldMarkingBlock = null;
        assembledBlocks.clear();
        offset = new BlockPos(BlockPos.ORIGIN);
    }

    public EntityMovingWorld getEntity(World world, EntityMovingWorld entity) {
        if (!isOK()) return null;

        if (entity == null) {
            MovingWorldMod.LOG.error("A null movingWorld was attempted!");
            return null;
        }

        EnumFacing facing = assemblyInteractor.getFrontDirection(movingWorldMarkingBlock);
        BlockPos riderDestination = new BlockPos(movingWorldMarkingBlock.blockPos.getX() - offset.getX(), movingWorldMarkingBlock.blockPos.getY() - offset.getY(), movingWorldMarkingBlock.blockPos.getZ() - offset.getZ());

        entity.setRiderDestination(facing, riderDestination);
        entity.getMobileChunk().setCreationSpotBiomeGen(world.getBiome(movingWorldMarkingBlock.blockPos));

        boolean doTileDropsInWorld = world.getGameRules().getBoolean("doTileDrops");
        world.getGameRules().setOrCreateGameRule("doTileDrops", "false");

        ArrayList<LocatedBlockList> separatedLbLists = assembledBlocks.getSortedAssemblyBlocks();

        try {
            for (LocatedBlockList locatedBlockList : separatedLbLists) {
                if (locatedBlockList != null && !locatedBlockList.isEmpty()) {
                    setWorldBlocksToAir(world, entity, locatedBlockList);
                }
            }
        } catch (Exception e) {
            resultType = ResultType.RESULT_ERROR_OCCURED;
            MovingWorldMod.LOG.error("Result code: RESULT ERROR OCCURRED was reached when attempting to getEntity from assembly result. Printing stacktrace...");
            MovingWorldMod.LOG.error(e);
            e.printStackTrace();
            return null;
        }

        world.getGameRules().setOrCreateGameRule("doTileDrops", String.valueOf(doTileDropsInWorld));

        entity.getMobileChunk().setChunkModified();
        entity.getMobileChunk().onChunkLoad();
        entity.setLocationAndAngles(offset.getX() + entity.getMobileChunk().getCenterX(), offset.getY(), offset.getZ() + entity.getMobileChunk().getCenterZ(), 0F, 0F);
        entity.assembleResultEntity();

        return entity;
    }

    public void setWorldBlocksToAir(World world, EntityMovingWorld entityMovingWorld, LocatedBlockList locatedBlocks) {

        boolean setFluids = false;

        LocatedBlockList setAirState2 = new LocatedBlockList();

        if (movingWorldMarkingBlock != null && movingWorldMarkingBlock.tileEntity != null && movingWorldMarkingBlock.tileEntity instanceof TileMovingMarkingBlock
                && ((TileMovingMarkingBlock) movingWorldMarkingBlock.tileEntity).removedFluidBlocks != null &&
                !((TileMovingMarkingBlock) movingWorldMarkingBlock.tileEntity).removedFluidBlocks.isEmpty()) {

            setFluids = true;
        }

        TileEntity tileentity;
        BlockPos iPos;
        for (LocatedBlock lb : locatedBlocks) {
            iPos = new BlockPos(lb.blockPos.getX() - offset.getX(), lb.blockPos.getY() - offset.getY(), lb.blockPos.getZ() - offset.getZ());

            tileentity = lb.tileEntity;
            if (tileentity != null || lb.blockState.getBlock().hasTileEntity(lb.blockState) && (tileentity = world.getTileEntity(lb.blockPos)) != null) {
                tileentity.validate();
            }
            if (entityMovingWorld.getMobileChunk().addBlockWithState(iPos, lb.blockState)) {
                if (lb.tileEntity != null && movingWorldMarkingBlock.tileEntity != null && lb.blockPos.equals(movingWorldMarkingBlock.blockPos)) {
                    entityMovingWorld.getMobileChunk().marker = lb;
                }
                setAirState2.add(lb);

                TileEntity tileClone = tileentity;
                entityMovingWorld.getMobileChunk().setTileEntity(iPos, tileClone);
            }
        }

        for (LocatedBlock lb : setAirState2) {
            world.removeTileEntity(lb.blockPos);
            world.setBlockState(lb.blockPos, Blocks.AIR.getDefaultState(), 2);
        }

        for (LocatedBlock lb : locatedBlocks) {
            world.removeTileEntity(lb.blockPos);
            world.setBlockToAir(lb.blockPos);
        }

        if (setFluids) {
            ((TileMovingMarkingBlock) movingWorldMarkingBlock.tileEntity).removedFluidBlocks.stream()
                    .filter(fluid -> fluid != null && world.isAirBlock(fluid.blockPos))
                    .forEach(fluid -> world.setBlockState(fluid.blockPos, fluid.blockState, 2));

            ((TileMovingMarkingBlock) movingWorldMarkingBlock.tileEntity).removedFluidBlocks.stream()
                    .filter(fluid -> fluid != null && world.isAirBlock(fluid.blockPos))
                    .forEach(fluid -> world.setBlockState(fluid.blockPos, fluid.blockState, 3));
        }


    }

    public ResultType getType() {
        return resultType;
    }

    public boolean isOK() {
        return resultType == ResultType.RESULT_OK || resultType == ResultType.RESULT_OK_WITH_WARNINGS;
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
            IBlockState blockState = world.getBlockState(lb.blockPos);
            Block block = blockState.getBlock();
            if (block != lb.blockState.getBlock()) {
                resultType = ResultType.RESULT_INCONSISTENT;
                return;
            }
            if (blockState != lb.blockState) {
                warn = true;
            }
        }
        resultType = warn ? ResultType.RESULT_OK_WITH_WARNINGS : ResultType.RESULT_OK;
    }

    public void writeNBTFully(NBTTagCompound tag) {
        writeNBTMetadata(tag);
        NBTTagList list = new NBTTagList();
        for (LocatedBlock lb : assembledBlocks) {
            NBTTagCompound comp = new NBTTagCompound();
            lb.writeToNBT(comp);
            list.appendTag(comp);
        }
        tag.setTag("list", list);

        if (movingWorldMarkingBlock != null) {
            NBTTagCompound comp = new NBTTagCompound();
            movingWorldMarkingBlock.writeToNBT(comp);
            tag.setTag("marker", comp);
        }
    }

    public void writeNBTMetadata(NBTTagCompound tag) {
        tag.setByte("res", getType().toByte());
        tag.setInteger("blockc", getBlockCount());
        tag.setInteger("tec", getTileEntityCount());
        tag.setFloat("mass", getMass());
        tag.setInteger("xO", offset.getX());
        tag.setInteger("yO", offset.getY());
        tag.setInteger("zO", offset.getZ());
    }

    public ByteBuf toByteBuf(ByteBuf buf) {
        buf.writeByte(getType().toByte());
        buf.writeInt(getBlockCount());
        buf.writeInt(getTileEntityCount());
        buf.writeFloat(getMass());
        if (assemblyInteractor != null)
            assemblyInteractor.toByteBuf(buf);

        return buf;
    }

    public enum ResultType {
        RESULT_NONE, RESULT_OK, RESULT_BLOCK_OVERFLOW, RESULT_MISSING_MARKER, RESULT_ERROR_OCCURED,
        RESULT_BUSY_COMPILING, RESULT_INCONSISTENT, RESULT_OK_WITH_WARNINGS;

        public static byte toByte(ResultType action) {
            switch (action) {
                case RESULT_OK:
                    return (byte) 1;
                case RESULT_BLOCK_OVERFLOW:
                    return (byte) 2;
                case RESULT_MISSING_MARKER:
                    return (byte) 3;
                case RESULT_ERROR_OCCURED:
                    return (byte) 4;
                case RESULT_BUSY_COMPILING:
                    return (byte) 5;
                case RESULT_INCONSISTENT:
                    return (byte) 6;
                case RESULT_OK_WITH_WARNINGS:
                    return (byte) 7;
                default:
                    return (byte) 0;
            }
        }

        public static ResultType fromByte(byte actionInt) {
            switch (actionInt) {
                case 1:
                    return RESULT_OK;
                case 2:
                    return RESULT_BLOCK_OVERFLOW;
                case 3:
                    return RESULT_MISSING_MARKER;
                case 4:
                    return RESULT_ERROR_OCCURED;
                case 5:
                    return RESULT_BUSY_COMPILING;
                case 6:
                    return RESULT_INCONSISTENT;
                case 7:
                    return RESULT_OK_WITH_WARNINGS;
                default:
                    return RESULT_NONE;
            }
        }

        public byte toByte() {
            return ResultType.toByte(this);
        }
    }

}