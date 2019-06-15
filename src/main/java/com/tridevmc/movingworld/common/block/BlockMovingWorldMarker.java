package com.tridevmc.movingworld.common.block;

import com.tridevmc.movingworld.common.tile.TileMovingMarkingBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.ContainerBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.DirectionProperty;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class BlockMovingWorldMarker extends ContainerBlock {

    public static final DirectionProperty FACING = DirectionProperty.create("facing", Direction.Plane.HORIZONTAL);

    protected BlockMovingWorldMarker(Properties properties) {
        super(properties);
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity entity, ItemStack itemStack) {
        super.onBlockPlacedBy(world, pos, state, entity, itemStack);

        if (world != null && !world.isRemote && entity instanceof PlayerEntity) {
            if (world.getTileEntity(pos) != null && world.getTileEntity(pos) instanceof TileMovingMarkingBlock) {
                TileMovingMarkingBlock tile = (TileMovingMarkingBlock) world.getTileEntity(pos);
                tile.getInfo().setOwner(((PlayerEntity) entity).getGameProfile().getId());
            }
        }
    }
}
