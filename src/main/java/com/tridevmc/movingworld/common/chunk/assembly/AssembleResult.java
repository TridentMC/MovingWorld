package com.tridevmc.movingworld.common.chunk.assembly;


import com.tridevmc.movingworld.MovingWorldMod;
import com.tridevmc.movingworld.common.chunk.LocatedBlock;
import com.tridevmc.movingworld.common.chunk.MovingWorldAssemblyInteractor;
import com.tridevmc.movingworld.common.entity.EntityMovingWorld;
import com.tridevmc.movingworld.common.tile.TileMovingMarkingBlock;
import com.tridevmc.movingworld.common.util.LocatedBlockList;
import com.tridevmc.movingworld.common.util.MaterialDensity;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
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

    public AssembleResult(CompoundNBT tag, World world) {
        resultType = ResultType.fromByte(tag.getByte("res"));
        blockCount = tag.getInt("blockc");
        tileEntityCount = tag.getInt("tec");
        mass = tag.getFloat("mass");
        offset = new BlockPos(-tag.getInt("xO"),
                tag.getInt("yO"),
                tag.getInt("zO"));
        if (tag.contains("list")) {
            ListNBT list = tag.getList("list", 10);
            for (int i = 0; i < list.size(); i++) {
                CompoundNBT comp = list.getCompound(i);
                assembledBlocks.add(new LocatedBlock(comp, world));
            }
        }
        if (tag.contains("marker")) {
            CompoundNBT comp = tag.getCompound("marker");
            movingWorldMarkingBlock = new LocatedBlock(comp, world);
        }
    }

    AssembleResult() {
        clear();
    }

    void assembleBlock(LocatedBlock lb) {
        assembledBlocks.add(lb);
        blockCount = assembledBlocks.size();
        if (lb.tile != null) {
            tileEntityCount++;
        }
        mass += MaterialDensity.getDensity(lb.state);
        offset = new BlockPos(Math.min(offset.getX(), lb.pos.getX()),
                Math.min(offset.getY(), lb.pos.getY()),
                Math.min(offset.getZ(), lb.pos.getZ()));
    }

    public void clear() {
        resultType = ResultType.RESULT_NONE;
        movingWorldMarkingBlock = null;
        assembledBlocks.clear();
        offset = new BlockPos(BlockPos.ZERO);
    }

    public EntityMovingWorld getEntity(World world, EntityMovingWorld entity) {
        if (!isOK()) return null;

        if (entity == null) {
            MovingWorldMod.LOG.error("A null movingWorld was attempted!");
            return null;
        }

        Direction facing = assemblyInteractor.getFrontDirection(movingWorldMarkingBlock);
        BlockPos riderDestination = new BlockPos(movingWorldMarkingBlock.pos.getX() - offset.getX(), movingWorldMarkingBlock.pos.getY() - offset.getY(), movingWorldMarkingBlock.pos.getZ() - offset.getZ());

        entity.setRiderDestination(facing, riderDestination);
        entity.getMobileChunk().setCreationSpotBiome(world.getBiome(movingWorldMarkingBlock.pos));

        boolean doTileDropsInWorld = world.getGameRules().getBoolean("doTileDrops");
        MinecraftServer server = world.getServer();
        world.getGameRules().setOrCreateGameRule("doTileDrops", "false", server);

        ArrayList<LocatedBlockList> separatedLbLists = assembledBlocks.getSortedAssemblyBlocks();

        try {
            for (LocatedBlockList locatedBlockList : separatedLbLists) {
                if (locatedBlockList != null && !locatedBlockList.isEmpty()) {
                    setWorldBlocksToAir(world, entity, locatedBlockList);
                }
            }
        } catch (Exception e) {
            resultType = ResultType.RESULT_ERROR_OCCURED;
            world.getGameRules().setOrCreateGameRule("doTileDrops", String.valueOf(doTileDropsInWorld), server);
            MovingWorldMod.LOG.error("Result code: RESULT ERROR OCCURRED was reached when attempting to getEntity from assembly result. Printing stacktrace...");
            MovingWorldMod.LOG.error(e);
            e.printStackTrace();
            return null;
        }

        world.getGameRules().setOrCreateGameRule("doTileDrops", String.valueOf(doTileDropsInWorld),server);

        entity.getMobileChunk().setChunkModified();
        entity.getMobileChunk().onChunkLoad();
        entity.setLocationAndAngles(offset.getX() + entity.getMobileChunk().getCenterX(), offset.getY(), offset.getZ() + entity.getMobileChunk().getCenterZ(), 0F, 0F);
        entity.assembleResultEntity();

        return entity;
    }

    public void setWorldBlocksToAir(World world, EntityMovingWorld entityMovingWorld, LocatedBlockList locatedBlocks) {

        boolean setFluids = false;

        LocatedBlockList setAirState2 = new LocatedBlockList();

        if (movingWorldMarkingBlock != null && movingWorldMarkingBlock.tile instanceof TileMovingMarkingBlock
                && ((TileMovingMarkingBlock) movingWorldMarkingBlock.tile).removedFluidBlocks != null &&
                !((TileMovingMarkingBlock) movingWorldMarkingBlock.tile).removedFluidBlocks.isEmpty()) {

            setFluids = true;
        }

        TileEntity tileentity;
        BlockPos iPos;
        for (LocatedBlock lb : locatedBlocks) {
            iPos = new BlockPos(lb.pos.getX() - offset.getX(), lb.pos.getY() - offset.getY(), lb.pos.getZ() - offset.getZ());

            tileentity = lb.tile;
            if (tileentity != null || lb.state.getBlock().hasTileEntity(lb.state) && (tileentity = world.getTileEntity(lb.pos)) != null) {
                tileentity.validate();
            }
            if (entityMovingWorld.getMobileChunk().addBlockWithState(iPos, lb.state)) {
                if (lb.tile != null && movingWorldMarkingBlock.tile != null && lb.pos.equals(movingWorldMarkingBlock.pos)) {
                    entityMovingWorld.getMobileChunk().marker = lb;
                }
                setAirState2.add(lb);

                entityMovingWorld.getMobileChunk().setTileEntity(iPos, tileentity);
            }
        }

        for (LocatedBlock lb : setAirState2) {
            world.setBlockState(lb.pos, Blocks.AIR.getDefaultState(), 2);
            world.removeTileEntity(lb.pos);
        }

        for (LocatedBlock lb : locatedBlocks) {
            world.removeBlock(lb.pos, false);
            world.removeTileEntity(lb.pos);
        }

        if (setFluids) {
            ((TileMovingMarkingBlock) movingWorldMarkingBlock.tile).removedFluidBlocks.stream()
                    .filter(fluid -> fluid != null && world.isAirBlock(fluid.pos))
                    .forEach(fluid -> world.setBlockState(fluid.pos, fluid.state, 2));

            ((TileMovingMarkingBlock) movingWorldMarkingBlock.tile).removedFluidBlocks.stream()
                    .filter(fluid -> fluid != null && world.isAirBlock(fluid.pos))
                    .forEach(fluid -> world.setBlockState(fluid.pos, fluid.state, 3));
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
            BlockState blockState = world.getBlockState(lb.pos);
            Block block = blockState.getBlock();
            if (block != lb.state.getBlock()) {
                resultType = ResultType.RESULT_INCONSISTENT;
                return;
            }
            if (blockState != lb.state) {
                warn = true;
            }
        }
        resultType = warn ? ResultType.RESULT_OK_WITH_WARNINGS : ResultType.RESULT_OK;
    }

    public void writeNBTFully(CompoundNBT tag) {
        writeNBTMetadata(tag);
        ListNBT list = new ListNBT();
        for (LocatedBlock lb : assembledBlocks) {
            CompoundNBT comp = new CompoundNBT();
            lb.writeToNBT(comp);
            list.add(comp);
        }
        tag.put("list", list);

        if (movingWorldMarkingBlock != null) {
            CompoundNBT comp = new CompoundNBT();
            movingWorldMarkingBlock.writeToNBT(comp);
            tag.put("marker", comp);
        }
    }

    public void writeNBTMetadata(CompoundNBT tag) {
        tag.putByte("res", getType().toByte());
        tag.putInt("blockc", getBlockCount());
        tag.putInt("tec", getTileEntityCount());
        tag.putFloat("mass", getMass());
        tag.putInt("xO", offset.getX());
        tag.putInt("yO", offset.getY());
        tag.putInt("zO", offset.getZ());
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