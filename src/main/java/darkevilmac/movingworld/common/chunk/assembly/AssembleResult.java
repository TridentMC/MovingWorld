package darkevilmac.movingworld.common.chunk.assembly;


import darkevilmac.movingworld.MovingWorld;
import darkevilmac.movingworld.common.chunk.LocatedBlock;
import darkevilmac.movingworld.common.chunk.MovingWorldAssemblyInteractor;
import darkevilmac.movingworld.common.entity.EntityMovingWorld;
import darkevilmac.movingworld.common.tile.TileMovingWorldMarkingBlock;
import darkevilmac.movingworld.common.util.LocatedBlockList;
import darkevilmac.movingworld.common.util.MaterialDensity;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import java.util.ArrayList;

public class AssembleResult {

    public static final int RESULT_NONE = 0, RESULT_OK = 1, RESULT_BLOCK_OVERFLOW = 2, RESULT_MISSING_MARKER = 3, RESULT_ERROR_OCCURED = 4,
            RESULT_BUSY_COMPILING = 5, RESULT_INCONSISTENT = 6, RESULT_OK_WITH_WARNINGS = 7;

    public final LocatedBlockList assembledBlocks = new LocatedBlockList();
    public BlockPos offset;
    public MovingWorldAssemblyInteractor assemblyInteractor;
    LocatedBlock movingWorldMarkingBlock;
    int resultCode;
    int blockCount;
    int tileEntityCount;
    float mass;

    public AssembleResult(int resultCode, ByteBuf buf) {
        this.resultCode = resultCode;
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
        offset = new BlockPos(-compound.getInteger("xO"),
                compound.getInteger("yO"),
                compound.getInteger("zO"));
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
        mass += MaterialDensity.getDensity(lb.blockState.getBlock());
        offset = new BlockPos(Math.min(offset.getX(), lb.blockPos.getX()),
                Math.min(offset.getY(), lb.blockPos.getY()),
                Math.min(offset.getZ(), lb.blockPos.getZ()));
    }

    public void clear() {
        resultCode = RESULT_NONE;
        movingWorldMarkingBlock = null;
        assembledBlocks.clear();
        offset = new BlockPos(BlockPos.ORIGIN);
    }

    public EntityMovingWorld getEntity(World world, EntityMovingWorld entity) {
        if (!isOK()) return null;

        if (entity == null) {
            MovingWorld.logger.error("A null movingWorld was attempted!");
            return null;
        }

        EnumFacing facing = assemblyInteractor.getFrontDirection(movingWorldMarkingBlock);
        BlockPos riderDestination = new BlockPos(movingWorldMarkingBlock.blockPos.getX() - offset.getX(), movingWorldMarkingBlock.blockPos.getY() - offset.getY(), movingWorldMarkingBlock.blockPos.getZ() - offset.getZ());

        entity.setRiderDestination(facing, riderDestination);
        entity.getMobileChunk().setCreationSpotBiomeGen(world.getBiomeGenForCoords(movingWorldMarkingBlock.blockPos));

        boolean doTileDropsInWorld = world.getGameRules().getGameRuleBooleanValue("doTileDrops");
        world.getGameRules().setOrCreateGameRule("doTileDrops", "false");

        ArrayList<LocatedBlockList> separatedLbLists = assembledBlocks.getSortedAssemblyBlocks();

        try {
            for (LocatedBlockList locatedBlockList : separatedLbLists) {
                if (locatedBlockList != null && !locatedBlockList.isEmpty()) {
                    setWorldBlocksToAir(world, entity, locatedBlockList);
                }
            }
        } catch (Exception e) {
            resultCode = RESULT_ERROR_OCCURED;
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

        if (movingWorldMarkingBlock != null && movingWorldMarkingBlock.tileEntity != null && movingWorldMarkingBlock.tileEntity instanceof TileMovingWorldMarkingBlock
                && ((TileMovingWorldMarkingBlock) movingWorldMarkingBlock.tileEntity).removedFluidBlocks != null &&
                !((TileMovingWorldMarkingBlock) movingWorldMarkingBlock.tileEntity).removedFluidBlocks.isEmpty()) {

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

                world.setBlockState(lb.blockPos, Blocks.air.getDefaultState(), 2);
                entityMovingWorld.getMobileChunk().setTileEntity(iPos, tileentity);
            }
        }

        for (LocatedBlock lb : locatedBlocks) {
            world.setBlockToAir(lb.blockPos);
        }

        if (setFluids) {
            for (LocatedBlock fluid : ((TileMovingWorldMarkingBlock) movingWorldMarkingBlock.tileEntity).removedFluidBlocks) {
                if (fluid != null && world.isAirBlock(fluid.blockPos)) {
                    world.setBlockState(fluid.blockPos, fluid.blockState, 2);
                }
            }

            for (LocatedBlock fluid : ((TileMovingWorldMarkingBlock) movingWorldMarkingBlock.tileEntity).removedFluidBlocks) {
                if (fluid != null && world.isAirBlock(fluid.blockPos)) {
                    world.setBlockState(fluid.blockPos, fluid.blockState, 3);
                }
            }
        }


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
            IBlockState blockState = world.getBlockState(lb.blockPos);
            Block block = blockState.getBlock();
            if (block != lb.blockState.getBlock()) {
                resultCode = RESULT_INCONSISTENT;
                return;
            }
            if (blockState != lb.blockState) {
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
        compound.setInteger("xO", offset.getX());
        compound.setInteger("yO", offset.getY());
        compound.setInteger("zO", offset.getZ());
    }
}
