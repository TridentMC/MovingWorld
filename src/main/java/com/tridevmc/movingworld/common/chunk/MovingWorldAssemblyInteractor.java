package com.tridevmc.movingworld.common.chunk;

import com.tridevmc.movingworld.MovingWorldMod;
import com.tridevmc.movingworld.common.block.BlockMovingWorldMarker;
import com.tridevmc.movingworld.common.chunk.assembly.AssembleResult;
import com.tridevmc.movingworld.common.chunk.assembly.CanAssemble;
import com.tridevmc.movingworld.common.tile.TileMovingMarkingBlock;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

/**
 * Used for storing information given by and taken by the (Dis)Assembler <p/> Mostly for use in
 * GUIs, the ship pretty much immediately forgets this thing.
 */
public class MovingWorldAssemblyInteractor {

    public MovingWorldAssemblyInteractor fromByteBuf(byte resultCode, ByteBuf byteBuf) {
        return new MovingWorldAssemblyInteractor();
    }

    public MovingWorldAssemblyInteractor fromNBT(NBTTagCompound tag, World world) {
        return new MovingWorldAssemblyInteractor();
    }

    public boolean doDiagonalAssembly() {
        return MovingWorldMod.CONFIG.diagonalAssembly;
    }

    public void toByteBuf(ByteBuf byteBuf) {
    }

    /**
     * Called when a block is assembled to your moving world.
     */
    public void blockAssembled(LocatedBlock locatedBlock) {
    }

    /**
     * Called when a block is disassembled from your moving world.
     */
    public void blockDisassembled(LocatedBlock locatedBlock) {
        if (locatedBlock.tile != null && locatedBlock.tile.getWorld() != null
                && locatedBlock.tile.getWorld().getTileEntity(locatedBlock.pos) != null)
            locatedBlock.tile.getWorld().getTileEntity(locatedBlock.pos).markDirty();
    }

    /**
     * @return returns if it is an over writable block in the config.
     */
    public boolean canOverwriteState(IBlockState state) {
        return MovingWorldMod.CONFIG.canOverwriteState(state);
    }

    /**
     * Called when a block is overwritten when a moving world is disassembled.
     */
    public void blockOverwritten(Block block) {
    }

    /**
     * Called when a block is rotated during disassembling.
     */
    public IBlockState blockRotated(IBlockState blockState, int deltarot) {
        return blockState;
    }

    /**
     * Called when a chunk assembly has finished.
     */
    public void chunkAssembled(AssembleResult assembleResult) {
    }

    /**
     * Called when a chunk disassembly has finished.
     */
    public void chunkDissasembled(AssembleResult assembleResult) {
    }

    public CanAssemble isBlockAllowed(World world, LocatedBlock lb) {
        CanAssemble canAssemble = new CanAssemble(false, false);
        IBlockState state = lb.state;

        canAssemble.justCancel = !(!state.getMaterial().equals(Material.AIR) && !state.getMaterial().isLiquid() && MovingWorldMod.CONFIG.isStateAllowed(state));
        canAssemble.justCancel = canAssemble.justCancel || !MovingWorldMod.CONFIG.isTileAllowed(lb.tile);

        return canAssemble;
    }

    public boolean isBlockMovingWorldMarker(Block block) {
        return block instanceof BlockMovingWorldMarker;
    }

    public boolean isTileMovingWorldMarker(TileEntity tile) {
        return tile instanceof TileMovingMarkingBlock;
    }

    public EnumFacing getFrontDirection(LocatedBlock marker) {
        return marker.state.get(BlockMovingWorldMarker.FACING).getOpposite();
    }

    /**
     * Recommended to call writeNBTMetadata first, then write the rest of your data.
     */
    public void writeNBTFully(NBTTagCompound tag) {
    }


    /**
     * Write metadata to NBT.
     */
    public void writeNBTMetadata(NBTTagCompound tag) {
    }

}
