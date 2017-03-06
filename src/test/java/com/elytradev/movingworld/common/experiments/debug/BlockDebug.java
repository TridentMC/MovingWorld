package com.elytradev.movingworld.common.experiments.debug;

import com.elytradev.movingworld.common.experiments.entity.EntityMobileRegion;
import com.elytradev.movingworld.common.experiments.newassembly.WorldReader;
import com.elytradev.movingworld.common.experiments.region.MobileRegion;
import com.elytradev.movingworld.common.experiments.region.RegionPool;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockDebug extends Block {
    public BlockDebug(Material blockMaterialIn, MapColor blockMapColorIn) {
        super(blockMaterialIn, blockMapColorIn);
        setRegistryName("movingworld-experiments", "debug");
        setUnlocalizedName("debug");
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (worldIn == null || worldIn.isRemote || RegionPool.getPool(worldIn.provider.getDimension(), false) != null)
            return false;

        WorldReader reader = new WorldReader(pos, worldIn);

        reader.readAll();
        reader.moveToSubWorld();

        MobileRegion readerRegion = reader.out.getRegion();
        readerRegion.x = pos.getX();
        readerRegion.y = 0;
        readerRegion.z = pos.getZ();
        BlockPos shiftedMin = readerRegion.convertRegionPosToRealWorld(reader.out.getAddedRegionMin());
        BlockPos shiftedMax = readerRegion.convertRegionPosToRealWorld(reader.out.getAddedRegionMax());

        EntityMobileRegion entityMobileRegion = new EntityMobileRegion(worldIn, readerRegion, new AxisAlignedBB(shiftedMin.getX(), shiftedMin.getY(), shiftedMin.getZ(), shiftedMax.getX(), shiftedMax.getY(), shiftedMax.getZ()));
        entityMobileRegion.setPosition(pos.getX(), 0, pos.getZ());
        worldIn.spawnEntity(entityMobileRegion);

        return super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
    }
}
