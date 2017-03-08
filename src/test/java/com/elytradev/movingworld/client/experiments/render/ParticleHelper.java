package com.elytradev.movingworld.client.experiments.render;

import com.elytradev.concrete.reflect.accessor.Accessor;
import com.elytradev.concrete.reflect.accessor.Accessors;
import com.elytradev.movingworld.common.experiments.region.MobileRegion;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.ParticleDigging;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Map;
import java.util.Random;

/**
 * Created by darkevilmac on 3/7/2017.
 */
public class ParticleHelper {

    private final static Accessor<Map<Integer, IParticleFactory>> particleTypes = Accessors.findField(ParticleManager.class, "particleTypes");
    private final static Random rand = new Random();

    public static void addBlockHitEffects(MobileRegion region, World world, BlockPos pos, EnumFacing side) {
        IBlockState iblockstate = world.getBlockState(pos);

        if (iblockstate.getRenderType() != EnumBlockRenderType.INVISIBLE) {
            float f = 0.1F;
            AxisAlignedBB axisalignedbb = iblockstate.getBoundingBox(world, pos);

            int i = region.convertRegionPosToRealWorld(pos).getX();
            int j = region.convertRegionPosToRealWorld(pos).getY();
            int k = region.convertRegionPosToRealWorld(pos).getZ();

            double d0 = (double) i + rand.nextDouble() * (axisalignedbb.maxX - axisalignedbb.minX - 0.20000000298023224D) + 0.10000000149011612D + axisalignedbb.minX;
            double d1 = (double) j + rand.nextDouble() * (axisalignedbb.maxY - axisalignedbb.minY - 0.20000000298023224D) + 0.10000000149011612D + axisalignedbb.minY;
            double d2 = (double) k + rand.nextDouble() * (axisalignedbb.maxZ - axisalignedbb.minZ - 0.20000000298023224D) + 0.10000000149011612D + axisalignedbb.minZ;

            if (side == EnumFacing.DOWN) {
                d1 = (double) j + axisalignedbb.minY - 0.10000000149011612D;
            }

            if (side == EnumFacing.UP) {
                d1 = (double) j + axisalignedbb.maxY + 0.10000000149011612D;
            }

            if (side == EnumFacing.NORTH) {
                d2 = (double) k + axisalignedbb.minZ - 0.10000000149011612D;
            }

            if (side == EnumFacing.SOUTH) {
                d2 = (double) k + axisalignedbb.maxZ + 0.10000000149011612D;
            }

            if (side == EnumFacing.WEST) {
                d0 = (double) i + axisalignedbb.minX - 0.10000000149011612D;
            }

            if (side == EnumFacing.EAST) {
                d0 = (double) i + axisalignedbb.maxX + 0.10000000149011612D;
            }

            IParticleFactory factory = particleTypes.get(Minecraft.getMinecraft().effectRenderer).get(EnumParticleTypes.BLOCK_CRACK.getParticleID());
            ParticleDigging particleDigging = (ParticleDigging) factory.createParticle(EnumParticleTypes.BLOCK_CRACK.getParticleID(), world, d0, d1, d2, 0.0D, 0.0D, 0.0D, Block.getStateId(iblockstate));
            particleDigging = particleDigging.setBlockPos(pos);
            Minecraft.getMinecraft().effectRenderer.addEffect(particleDigging.multiplyVelocity(0.2F).multipleParticleScaleBy(0.6F));
        }
    }
}
