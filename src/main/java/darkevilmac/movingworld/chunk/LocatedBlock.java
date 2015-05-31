package darkevilmac.movingworld.chunk;

import net.minecraft.block.Block;
import net.minecraft.block.state.BlockStateBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class LocatedBlock {
    public final IBlockState blockState;
    public final TileEntity tileEntity;
    public final BlockPos blockPos;

    public LocatedBlock(IBlockState blockState, BlockPos blockPos) {
        this(blockState, null, blockPos);
    }

    public LocatedBlock(IBlockState blockState, TileEntity tileentity, BlockPos blockPos) {
        this.blockState = blockState;
        this.blockPos = blockPos;
        tileEntity = tileentity;
    }

    public LocatedBlock(NBTTagCompound comp, World world) {
        blockState = Block.getBlockById(comp.getInteger("block")).getDefaultState().getBlock().getStateFromMeta(comp.getInteger("meta"));
        blockPos = new BlockPos(comp.getInteger("x"), comp.getInteger("y"), comp.getInteger("z"));
        tileEntity = world == null ? null : world.getTileEntity(new BlockPos(blockPos.getX(), blockPos.getY(), blockPos.getZ()));
    }

    @Override
    public String toString() {
        return new StringBuilder("LocatedBlock [block=").append(blockState.getBlock()).append(", state=").append(blockState).append(", blockPos=[").append(blockPos.getX()).append(", ").append(blockPos.getY()).append(", ").append(blockPos.getZ()).append("]]").toString();
    }

    public void writeToNBT(NBTTagCompound comp) {
        comp.setShort("block", (short) Block.getIdFromBlock(blockState.getBlock()));
        comp.setInteger("meta", blockState.getBlock().getMetaFromState(blockState));
        comp.setInteger("x", blockPos.getX());
        comp.setInteger("y", blockPos.getY());
        comp.setInteger("z", blockPos.getZ());
    }
}
