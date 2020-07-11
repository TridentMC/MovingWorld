package com.tridevmc.movingworld.common.entity.collision;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.tridevmc.movingworld.common.util.Vec3dMod;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A bounding box for real men, with none of that axis aligned nonsense.
 */
public class OrientedBoundingBox {

    private final AxisAlignedBB bb;
    private final float xAngle, yAngle, zAngle;

    private final List<Vec3dMod> points;
    private final List<Vec3dMod> normals;

    public OrientedBoundingBox(AxisAlignedBB bb, float xAngle, float yAngle, float zAngle) {
        this.bb = bb;
        this.xAngle = xAngle;
        this.yAngle = yAngle;
        this.zAngle = zAngle;

        this.points = Lists.newArrayList(new Vec3dMod(bb.minX, bb.minY, bb.minZ).rotate(bb.getCenter(), xAngle, yAngle, zAngle),
                new Vec3dMod(bb.maxX, bb.minY, bb.minZ).rotate(bb.getCenter(), xAngle, yAngle, zAngle),
                new Vec3dMod(bb.maxX, bb.minY, bb.maxZ).rotate(bb.getCenter(), xAngle, yAngle, zAngle),
                new Vec3dMod(bb.minX, bb.minY, bb.maxZ).rotate(bb.getCenter(), xAngle, yAngle, zAngle),
                new Vec3dMod(bb.minX, bb.maxY, bb.minZ).rotate(bb.getCenter(), xAngle, yAngle, zAngle),
                new Vec3dMod(bb.maxX, bb.maxY, bb.minZ).rotate(bb.getCenter(), xAngle, yAngle, zAngle),
                new Vec3dMod(bb.maxX, bb.maxY, bb.maxZ).rotate(bb.getCenter(), xAngle, yAngle, zAngle),
                new Vec3dMod(bb.minX, bb.maxY, bb.maxZ).rotate(bb.getCenter(), xAngle, yAngle, zAngle));

        Matrix4d matrix = new Matrix4d();
        matrix.rotX(Math.toRadians(xAngle));
        matrix.rotY(Math.toRadians(yAngle));
        matrix.rotZ(Math.toRadians(zAngle));
        this.normals = Lists.newArrayList(new Vector3d(1, 0, 0), new Vector3d(0, 1, 0), new Vector3d(0, 0, 1)).stream().map(v -> {
            matrix.transform(v);
            return new Vec3dMod(v.x, v.y, v.z);
        }).collect(Collectors.toList());
    }

    public OrientedBoundingBox(AxisAlignedBB bb) {
        this.bb = bb;
        this.xAngle = this.yAngle = this.zAngle = 0;

        this.points = Lists.newArrayList(new Vec3dMod(bb.minX, bb.minY, bb.minZ),
                new Vec3dMod(bb.maxX, bb.minY, bb.minZ),
                new Vec3dMod(bb.maxX, bb.minY, bb.maxZ),
                new Vec3dMod(bb.minX, bb.minY, bb.maxZ),
                new Vec3dMod(bb.minX, bb.maxY, bb.minZ),
                new Vec3dMod(bb.maxX, bb.maxY, bb.minZ),
                new Vec3dMod(bb.maxX, bb.maxY, bb.maxZ),
                new Vec3dMod(bb.minX, bb.maxY, bb.maxZ));

        Matrix4d matrix = new Matrix4d();
        matrix.rotX(Math.toRadians(xAngle));
        matrix.rotY(Math.toRadians(yAngle));
        matrix.rotZ(Math.toRadians(zAngle));
        this.normals = Lists.newArrayList(new Vector3d(1, 0, 0), new Vector3d(0, 1, 0), new Vector3d(0, 0, 1)).stream().map(v -> {
            matrix.transform(v);
            return new Vec3dMod(v.x, v.y, v.z);
        }).collect(Collectors.toList());
    }

    private OrientedBoundingBox(OrientedBoundingBox boundingBox, Vec3dMod offset) {
        this.bb = boundingBox.getBb();
        this.xAngle = boundingBox.getXAngle();
        this.yAngle = boundingBox.getYAngle();
        this.zAngle = boundingBox.getZAngle();

        this.points = boundingBox.getPoints().stream().map(p -> new Vec3dMod(p.add(offset))).collect(Collectors.toList());
        this.normals = boundingBox.getNormals();
    }

    public double getXSize() {
        return bb.getXSize();
    }

    public double getYSize() {
        return bb.getYSize();
    }

    public double getZSize() {
        return bb.getZSize();
    }

    public CollisionResult getCollisionResult(List<AxisAlignedBB> other, Vec3d move) {
        List<CollisionResult> collisions = other.stream().map((bb) -> this.getCollisionResult(bb, move)).collect(Collectors.toList());
        boolean willCollide = collisions.stream().anyMatch(CollisionResult::isWillCollide);
        boolean isColliding = collisions.stream().anyMatch(CollisionResult::isColliding);
        if (!willCollide && !isColliding)
            return new CollisionResult().setClampedMovement(new Vec3dMod(move)).setWillCollide(false).setColliding(false);

        List<Vec3dMod> hits = collisions.stream().map(CollisionResult::getClampedMovement).collect(Collectors.toList());
        double minX = hits.stream().min(Comparator.comparingDouble(v -> Math.abs(v.x))).map(Vec3dMod::getX).get();
        double minY = hits.stream().min(Comparator.comparingDouble(v -> Math.abs(v.y))).map(Vec3dMod::getY).get();
        double minZ = hits.stream().min(Comparator.comparingDouble(v -> Math.abs(v.z))).map(Vec3dMod::getZ).get();

        return new CollisionResult()
                .setWillCollide(willCollide)
                .setColliding(isColliding)
                .setClampedMovement(new Vec3dMod(minX, minY, minZ));
    }

    public CollisionResult getCollisionResult(AxisAlignedBB other, Vec3d move) {
        return this.getCollisionResult(new OrientedBoundingBox(other), move);
    }

    public CollisionResult getCollisionResult(OrientedBoundingBox other, Vec3d move) {
        ImmutableList<Vec3dMod> axes = this.getAxes(other);
        CollisionResult out = new CollisionResult();
        List<Tuple<Vec3dMod, Double>> collisionDistances = Lists.newArrayList();

        for (Vec3dMod axe : axes) {
            if (axe.equals(Vec3dMod.getOrigin()))
                continue;

            double[] proj = this.getPoints().stream().mapToDouble(p -> p.dotProduct(axe)).toArray();
            double[] otherProj = other.getPoints().stream().mapToDouble(p -> p.dotProduct(axe)).toArray();
            double min = Arrays.stream(proj).min().orElse(0);
            double max = Arrays.stream(proj).max().orElse(0);
            double otherMin = Arrays.stream(otherProj).min().orElse(0);
            double otherMax = Arrays.stream(otherProj).max().orElse(0);

            double dist = min < otherMin ? otherMin - max : min - otherMax;
            if (dist > 0)
                out.setColliding(false);

            double projectedVel = axe.dotProduct(move);
            min += projectedVel < 0 ? projectedVel : 0;
            max += projectedVel >= 0 ? projectedVel : 0;
            dist = min < otherMin ? otherMin - max : min - otherMax;

            if (dist > 0) out.setWillCollide(false);

            if (!out.hasFoundCollision())
                break;

            collisionDistances.add(new Tuple<>(axe, Math.abs(dist)));
        }

        if (out.isWillCollide()) {
            Tuple<Vec3dMod, Double> minDist = collisionDistances.stream().min(Comparator.comparing(Tuple::getB)).get();
            Vec3dMod movementAxis = new Vec3dMod(
                    this.getBb().getCenter().subtract(other.getBb().getCenter()).dotProduct(minDist.getA()) < 0
                            ? minDist.getA().mul(-1, -1, -1)
                            : minDist.getA());
            out.setClampedMovement(new Vec3dMod(movementAxis.mul(minDist.getB(), minDist.getB(), minDist.getB())));
        }

        return out;
    }

    public ImmutableList<Vec3dMod> getAxes(OrientedBoundingBox other) {
        return ImmutableList.copyOf(this.getNormals()
                .stream()
                .flatMap(n0 -> other.getNormals()
                        .stream()
                        .map(n0::cross))
                .collect(Collectors.toList()));
    }

    public ImmutableList<Vec3dMod> getNormals() {
        return ImmutableList.copyOf(this.normals);
    }

    public ImmutableList<Vec3dMod> getPoints() {
        return ImmutableList.copyOf(this.points);
    }

    public AxisAlignedBB getBb() {
        return bb;
    }

    public float getXAngle() {
        return xAngle;
    }

    public float getYAngle() {
        return yAngle;
    }

    public float getZAngle() {
        return zAngle;
    }

    public OrientedBoundingBox offset(Vec3dMod by) {
        return new OrientedBoundingBox(this, by);
    }

    @OnlyIn(Dist.CLIENT)
    public void drawLines(Vec3d offset) {
        Vec3d point0 = this.getPoints().get(0).add(offset);
        Vec3d point1 = this.getPoints().get(1).add(offset);
        Vec3d point2 = this.getPoints().get(2).add(offset);
        Vec3d point3 = this.getPoints().get(3).add(offset);
        Vec3d point4 = this.getPoints().get(4).add(offset);
        Vec3d point5 = this.getPoints().get(5).add(offset);
        Vec3d point6 = this.getPoints().get(6).add(offset);
        Vec3d point7 = this.getPoints().get(7).add(offset);

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

    public class CollisionResult {
        public Vec3dMod clampedMovement = Vec3dMod.getOrigin();
        public boolean isColliding = true;
        public boolean willCollide = true;

        public Vec3dMod getClampedMovement() {
            return clampedMovement;
        }

        protected CollisionResult setClampedMovement(Vec3dMod clampedMovement) {
            this.clampedMovement = clampedMovement;
            return this;
        }

        public boolean isColliding() {
            return isColliding;
        }

        protected CollisionResult setColliding(boolean colliding) {
            this.isColliding = colliding;
            return this;
        }

        public boolean isWillCollide() {
            return willCollide;
        }

        protected CollisionResult setWillCollide(boolean willCollide) {
            this.willCollide = willCollide;
            return this;
        }

        public boolean hasFoundCollision() {
            return isColliding || willCollide;
        }
    }

}
