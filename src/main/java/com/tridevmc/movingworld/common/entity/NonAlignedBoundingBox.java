package com.tridevmc.movingworld.common.entity;

import com.google.common.base.MoreObjects;
import com.tridevmc.movingworld.common.util.Vec3dMod;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Objects;

/**
 * A bounding box for real men, with none of that axis aligned nonsense.
 */
public class NonAlignedBoundingBox {

    private final AxisAlignedBB bb;
    private final float angle;

    private final Vec3dMod point0, point1, point2, point3, point4, point5, point6, point7;

    public NonAlignedBoundingBox(AxisAlignedBB bb, float angle) {
        this.bb = bb;
        this.angle = angle;

        this.point0 = new Vec3dMod(bb.minX, bb.minY, bb.minZ).rotate(Direction.Axis.Y, bb.getCenter(), angle);
        this.point1 = new Vec3dMod(bb.maxX, bb.minY, bb.minZ).rotate(Direction.Axis.Y, bb.getCenter(), angle);
        this.point2 = new Vec3dMod(bb.maxX, bb.minY, bb.maxZ).rotate(Direction.Axis.Y, bb.getCenter(), angle);
        this.point3 = new Vec3dMod(bb.minX, bb.minY, bb.maxZ).rotate(Direction.Axis.Y, bb.getCenter(), angle);

        this.point4 = new Vec3dMod(bb.minX, bb.maxY, bb.minZ).rotate(Direction.Axis.Y, bb.getCenter(), angle);
        this.point5 = new Vec3dMod(bb.maxX, bb.maxY, bb.minZ).rotate(Direction.Axis.Y, bb.getCenter(), angle);
        this.point6 = new Vec3dMod(bb.maxX, bb.maxY, bb.maxZ).rotate(Direction.Axis.Y, bb.getCenter(), angle);
        this.point7 = new Vec3dMod(bb.minX, bb.maxY, bb.maxZ).rotate(Direction.Axis.Y, bb.getCenter(), angle);
    }

    public NonAlignedBoundingBox(AxisAlignedBB bb) {
        this(bb, 0);
    }

    public AxisAlignedBB getBb() {
        return bb;
    }

    public float getAngle() {
        return angle;
    }

    public Vec3dMod getPoint0() {
        return point0;
    }

    public Vec3dMod getPoint1() {
        return point1;
    }

    public Vec3dMod getPoint2() {
        return point2;
    }

    public Vec3dMod getPoint3() {
        return point3;
    }

    public Vec3dMod getPoint4() {
        return point4;
    }

    public Vec3dMod getPoint5() {
        return point5;
    }

    public Vec3dMod getPoint6() {
        return point6;
    }

    public Vec3dMod getPoint7() {
        return point7;
    }

    @OnlyIn(Dist.CLIENT)
    public void drawLines(Vec3d offset) {
        Vec3d point0 = this.point0.add(offset);
        Vec3d point1 = this.point1.add(offset);
        Vec3d point2 = this.point2.add(offset);
        Vec3d point3 = this.point3.add(offset);
        Vec3d point4 = this.point4.add(offset);
        Vec3d point5 = this.point5.add(offset);
        Vec3d point6 = this.point6.add(offset);
        Vec3d point7 = this.point7.add(offset);

        float red = 1, green = 1, blue = 1, alpha = 1;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(3, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(point0.x, point0.y, point0.z).color(red, green, blue, 0.0F).endVertex();
        buffer.pos(point0.x, point0.y, point0.z).color(red, green, blue, alpha).endVertex();
        buffer.pos(point1.x, point1.y, point1.z).color(red, green, blue, alpha).endVertex();
        buffer.pos(point2.x, point2.y, point2.z).color(red, green, blue, alpha).endVertex();
        buffer.pos(point3.x, point3.y, point3.z).color(red, green, blue, alpha).endVertex();
        buffer.pos(point0.x, point0.y, point0.z).color(red, green, blue, alpha).endVertex();
        buffer.pos(point4.x, point4.y, point4.z).color(red, green, blue, alpha).endVertex();
        buffer.pos(point5.x, point5.y, point5.z).color(red, green, blue, alpha).endVertex();
        buffer.pos(point6.x, point6.y, point6.z).color(red, green, blue, alpha).endVertex();
        buffer.pos(point7.x, point7.y, point7.z).color(red, green, blue, alpha).endVertex();
        buffer.pos(point4.x, point4.y, point4.z).color(red, green, blue, alpha).endVertex();
        buffer.pos(point7.x, point7.y, point7.z).color(red, green, blue, 0.0F).endVertex();
        buffer.pos(point3.x, point3.y, point3.z).color(red, green, blue, alpha).endVertex();
        buffer.pos(point6.x, point6.y, point6.z).color(red, green, blue, 0.0F).endVertex();
        buffer.pos(point2.x, point2.y, point2.z).color(red, green, blue, alpha).endVertex();
        buffer.pos(point5.x, point5.y, point5.z).color(red, green, blue, 0.0F).endVertex();
        buffer.pos(point1.x, point1.y, point1.z).color(red, green, blue, alpha).endVertex();
        buffer.pos(point1.x, point1.y, point1.z).color(red, green, blue, 0.0F).endVertex();
        tessellator.draw();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NonAlignedBoundingBox that = (NonAlignedBoundingBox) o;
        return Float.compare(that.getAngle(), getAngle()) == 0 &&
                Objects.equals(getBb(), that.getBb());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getBb(), getAngle());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("bb", bb)
                .add("angle", angle)
                .add("point0", point0)
                .add("point1", point1)
                .add("point2", point2)
                .add("point3", point3)
                .add("point4", point4)
                .add("point5", point5)
                .add("point6", point6)
                .add("point7", point7)
                .toString();
    }
}
