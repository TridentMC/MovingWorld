package darkevilmac.movingworld.common.test;

import darkevilmac.movingworld.MovingWorldMod;
import darkevilmac.movingworld.common.core.IMovingWorld;
import darkevilmac.movingworld.common.core.assembly.Assembler;
import darkevilmac.movingworld.common.core.assembly.BlockMap;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

/**
 * Just a basic indev block, when activated the block assembles nearby blocks and creates a movingworld.
 */

public class BlockMovingWorldCreator extends Block {
    public static PropertyBool ASSEMBLING = PropertyBool.create("assembling");

    public BlockMovingWorldCreator(Material blockMaterialIn) {
        super(blockMaterialIn);
        this.setUnlocalizedName("movingWorldCreator");
        this.setDefaultState(this.blockState.getBaseState().withProperty(ASSEMBLING, Boolean.valueOf(false)));
        this.setCreativeTab(CreativeTabs.tabRedstone);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (worldIn != null && !worldIn.isRemote) {
            System.out.println("Initializing an assembler.");
            final Assembler assembler = new Assembler(new CustomAssemblyInteractor(), worldIn, pos, !player.isSneaking());
            assembler.setAssemblyListener(new Assembler.IAssemblyListener() {
                @Override
                public void onComplete(World world, BlockPos origin, BlockMap map) {
                    if (MovingWorldMod.movingWorldFactory != null) {
                        IMovingWorld movingWorld = MovingWorldMod.movingWorldFactory.createMovingWorld(map, world);
                        if (movingWorld != null) {
                            movingWorld.move(new Vec3(origin.getX() - assembler.initialOffset.getX(), map.getMin().getY(), origin.getZ() - assembler.initialOffset.getZ()), true);
                        }
                    }

                }
            });
            worldIn.setBlockState(pos, state.withProperty(ASSEMBLING, true));

            return true;
        }

        return false;
    }

    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(ASSEMBLING, Boolean.valueOf(meta == 0));
    }

    public int getMetaFromState(IBlockState state) {
        return state.getValue(ASSEMBLING) ? 0 : 1;
    }

    protected BlockState createBlockState() {
        return new BlockState(this, new IProperty[]{ASSEMBLING});
    }
}
