package io.github.elytra.movingworld.common.chunk;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class LocatedBlock {
    public static final LocatedBlock AIR = new LocatedBlock(Blocks.AIR.getDefaultState(), BlockPos.ORIGIN);

    public final IBlockState blockState;
    public final TileEntity tileEntity;
    public final BlockPos blockPos;
    public final BlockPos bPosNoOffset;

    public LocatedBlock(IBlockState blockState, BlockPos blockPos) {
        this(blockState, null, blockPos);
    }

    public LocatedBlock(IBlockState blockState, TileEntity tileentity, BlockPos blockPos) {
        this(blockState, tileentity, blockPos, null);
    }

    public LocatedBlock(IBlockState blockState, TileEntity tileentity, BlockPos blockPos, BlockPos bPosNoOffset) {
        this.blockState = blockState;
        this.blockPos = blockPos;
        this.bPosNoOffset = bPosNoOffset;
        tileEntity = tileentity;
    }

    public LocatedBlock(NBTTagCompound tag, World world) {
        blockState = Block.getBlockById(tag.getInteger("block")).getDefaultState().getBlock().getStateFromMeta(tag.getInteger("meta"));
        blockPos = new BlockPos(tag.getInteger("x"), tag.getInteger("y"), tag.getInteger("z"));
        bPosNoOffset = null;
        tileEntity = world == null ? null : world.getTileEntity(new BlockPos(blockPos.getX(), blockPos.getY(), blockPos.getZ()));
    }

    @Override
    public String toString() {
        return new StringBuilder("LocatedBlock [block=").append(blockState.getBlock()).append(", state=").append(blockState.toString()).append(", blockPos=[").append(blockPos.getX()).append(", ").append(blockPos.getY()).append(", ").append(blockPos.getZ()).append("]]").toString();
    }

    @Override
    public LocatedBlock clone() {
        return new LocatedBlock(blockState, tileEntity, blockPos, bPosNoOffset);
    }

    public String getBlockName() {
        return Block.REGISTRY.getNameForObject(blockState.getBlock()).toString();
    }

    public void writeToNBT(NBTTagCompound tag) {
        tag.setShort("block", (short) Block.getIdFromBlock(blockState.getBlock()));
        tag.setInteger("meta", blockState.getBlock().getMetaFromState(blockState));
        tag.setInteger("x", blockPos.getX());
        tag.setInteger("y", blockPos.getY());
        tag.setInteger("z", blockPos.getZ());
    }
}
