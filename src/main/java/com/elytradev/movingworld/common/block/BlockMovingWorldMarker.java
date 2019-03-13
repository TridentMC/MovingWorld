package com.elytradev.movingworld.common.block;

import com.elytradev.movingworld.common.tile.TileMovingMarkingBlock;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.state.DirectionProperty;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class BlockMovingWorldMarker extends BlockContainer {

    public static final DirectionProperty FACING = DirectionProperty.create("facing", EnumFacing.Plane.HORIZONTAL);

    protected BlockMovingWorldMarker(Properties properties) {
        super(properties);
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase entity, ItemStack itemStack) {
        super.onBlockPlacedBy(world, pos, state, entity, itemStack);

        if (world != null && !world.isRemote && entity instanceof EntityPlayer) {
            if (world.getTileEntity(pos) != null && world.getTileEntity(pos) instanceof TileMovingMarkingBlock) {
                TileMovingMarkingBlock tile = (TileMovingMarkingBlock) world.getTileEntity(pos);
                tile.getInfo().setOwner(((EntityPlayer) entity).getGameProfile().getId());
            }
        }
    }
}
