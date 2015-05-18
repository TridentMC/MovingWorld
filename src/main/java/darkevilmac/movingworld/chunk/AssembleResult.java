package darkevilmac.movingworld.chunk;


import darkevilmac.movingworld.MaterialDensity;
import darkevilmac.movingworld.MovingWorld;
import darkevilmac.movingworld.entity.EntityMovingWorld;
import darkevilmac.movingworld.event.AssembleBlockEvent;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

import java.util.ArrayList;
import java.util.List;

public class AssembleResult {
    public static final int RESULT_NONE = 0, RESULT_OK = 1, RESULT_BLOCK_OVERFLOW = 2, RESULT_MISSING_MARKER = 3, RESULT_ERROR_OCCURED = 4,
            RESULT_BUSY_COMPILING = 5, RESULT_INCONSISTENT = 6, RESULT_OK_WITH_WARNINGS = 7;
    public final List<LocatedBlock> assembledBlocks = new ArrayList<LocatedBlock>();
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

        entity.setPilotSeat(movingWorldMarkingBlock.blockState.getBlock().getMetaFromState(movingWorldMarkingBlock.blockState) & 3, movingWorldMarkingBlock.blockPos);
        entity.getMovingWorldChunk().setCreationSpotBiomeGen(world.getBiomeGenForCoords(movingWorldMarkingBlock.blockPos));

        boolean flag = world.getGameRules().getGameRuleBooleanValue("doTileDrops");
        world.getGameRules().setOrCreateGameRule("doTileDrops", "false");

        try {
            TileEntity tileentity;
            BlockPos iPos;
            for (LocatedBlock lb : assembledBlocks) {
                AssembleBlockEvent event = new AssembleBlockEvent(lb);
                MinecraftForge.EVENT_BUS.post(event);
                iPos = lb.blockPos.subtract(offset);

                tileentity = lb.tileEntity;
                if (tileentity != null || lb.blockState.getBlock().hasTileEntity(lb.blockState) && (tileentity = world.getTileEntity(lb.blockPos)) != null) {
                    tileentity.validate();
                }
                if (entity.getMovingWorldChunk().setBlockIDWithState(iPos, lb.blockState)) {
                    entity.getMovingWorldChunk().setTileEntity(iPos, tileentity);
                    world.setBlockState(lb.blockPos, Blocks.air.getDefaultState(), 2);
                }
            }
            for (LocatedBlock block : assembledBlocks) {
                world.setBlockToAir(block.blockPos);
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

        entity.setLocationAndAngles(offset.getX() + entity.getMovingWorldChunk().getCenterX(), offset.getY(), offset.getZ() + entity.getMovingWorldChunk().getCenterZ(), 0F, 0F);

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
