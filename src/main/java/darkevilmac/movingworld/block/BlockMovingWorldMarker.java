package darkevilmac.movingworld.block;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public abstract class BlockMovingWorldMarker extends BlockContainer {

    protected BlockMovingWorldMarker(Material material) {
        super(material);
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack itemStack) {
        super.onBlockPlacedBy(world, x, y, z, entity, itemStack);

        if (world != null && !world.isRemote && entity != null && entity instanceof EntityPlayer) {
            if (world.getTileEntity(x, y, z) != null && world.getTileEntity(x, y, z) instanceof TileMovingWorldMarkingBlock) {
                TileMovingWorldMarkingBlock tile = (TileMovingWorldMarkingBlock) world.getTileEntity(x, y, z);
                tile.getInfo().setOwner(((EntityPlayer) entity).getGameProfile().getId());
            }
        }
    }

    public static void onPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack itemStack) {
        if (world != null && !world.isRemote && entity != null && entity instanceof EntityPlayer) {
            if (world.getTileEntity(x, y, z) != null && world.getTileEntity(x, y, z) instanceof TileMovingWorldMarkingBlock) {
                TileMovingWorldMarkingBlock tile = (TileMovingWorldMarkingBlock) world.getTileEntity(x, y, z);
                tile.getInfo().setOwner(((EntityPlayer) entity).getGameProfile().getId());
            }
        }
    }
}
