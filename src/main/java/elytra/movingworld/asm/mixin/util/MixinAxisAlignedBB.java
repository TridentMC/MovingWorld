package elytra.movingworld.asm.mixin.util;


import elytra.movingworld.common.baseclasses.util.IAxisAlignedBBMixin;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.AxisAlignedBB;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AxisAlignedBB.class)
public class MixinAxisAlignedBB implements IAxisAlignedBBMixin {

    private AxisAlignedBB thisAABB() {
        return (AxisAlignedBB) ((Object) this);
    }

    @Override
    public AxisAlignedBB rotate(int angle) {
        return rotate(angle, Axis.Y);
    }

    @Override
    public AxisAlignedBB rotate(int angle, Axis axis) {
        // Code from https://github.com/Ordinastie/MalisisCore/blob/4bd0976d451fa2e52f42b599b754dd1a34d71ff9/src/main/java/net/malisis/core/util/AABBUtils.java
        // So it's under MIT as a note.

        AxisAlignedBB aabb = thisAABB();

        if (aabb == null || angle == 0 || axis == null)
            return aabb;

        int a = -angle & 3;
        int s = (int) Math.sin(a);
        int c = (int) Math.cos(a);

        aabb = aabb.offset(-0.5F, -0.5F, -0.5F);

        double minX = aabb.minX;
        double minY = aabb.minY;
        double minZ = aabb.minZ;
        double maxX = aabb.maxX;
        double maxY = aabb.maxY;
        double maxZ = aabb.maxZ;

        if (axis == Axis.X) {
            minY = (aabb.minY * c) - (aabb.minZ * s);
            maxY = (aabb.maxY * c) - (aabb.maxZ * s);
            minZ = (aabb.minY * s) + (aabb.minZ * c);
            maxZ = (aabb.maxY * s) + (aabb.maxZ * c);

        }
        if (axis == Axis.Y) {
            minX = (aabb.minX * c) - (aabb.minZ * s);
            maxX = (aabb.maxX * c) - (aabb.maxZ * s);
            minZ = (aabb.minX * s) + (aabb.minZ * c);
            maxZ = (aabb.maxX * s) + (aabb.maxZ * c);
        }

        if (axis == Axis.Z) {
            minX = (aabb.minX * c) - (aabb.minY * s);
            maxX = (aabb.maxX * c) - (aabb.maxY * s);
            minY = (aabb.minX * s) + (aabb.minY * c);
            maxY = (aabb.maxX * s) + (aabb.maxY * c);
        }

        aabb = new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
        aabb = aabb.offset(0.5F, 0.5F, 0.5F);

        return aabb;
    }

    public AxisAlignedBB readFromNBT(NBTTagCompound tagCompound, String key) {
        NBTTagCompound bbCompound = (NBTTagCompound) tagCompound.getTag(key + "AxisAlignedBB");

        double minX = bbCompound.getDouble("minX");
        double minY = bbCompound.getDouble("minY");
        double minZ = bbCompound.getDouble("minZ");
        double maxX = bbCompound.getDouble("maxX");
        double maxY = bbCompound.getDouble("maxY");
        double maxZ = bbCompound.getDouble("maxZ");

        return new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public void writeToNBT(NBTTagCompound tagCompound, String key) {
        NBTTagCompound bbCompound = new NBTTagCompound();

        bbCompound.setDouble("minX", thisAABB().minX);
        bbCompound.setDouble("minY", thisAABB().minY);
        bbCompound.setDouble("minZ", thisAABB().minZ);

        bbCompound.setDouble("maxX", thisAABB().maxX);
        bbCompound.setDouble("maxY", thisAABB().maxY);
        bbCompound.setDouble("maxZ", thisAABB().maxZ);

        tagCompound.setTag(key + "AxisAlignedBB", bbCompound);
    }

}
