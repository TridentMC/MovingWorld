package com.elytradev.movingworld.client.experiments;

import com.elytradev.movingworld.client.experiments.render.ParticleHelper;
import com.elytradev.movingworld.common.experiments.entity.EntityMobileRegion;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.List;
import java.util.Optional;

public class InputReader {

    public static InputReader INSTANCE;
    public static boolean enabled;

    public Minecraft mc;
    public MovingWorldPlayerController controller;

    public EntityMobileRegion currentEntityHit = null;
    public RayTraceResult currentBlockHit = new RayTraceResult(RayTraceResult.Type.MISS, new Vec3d(-1, -1, -1), null, new BlockPos(-1, -1, -1));

    private int leftClickCounter, rightClickCounter;
    private boolean performedLeftClick = false, performedRightClick = false;

    public InputReader(PlayerControllerMP playerControllerMP) {
        enabled = false;
        this.controller = new MovingWorldPlayerController(playerControllerMP);
        this.mc = Minecraft.getMinecraft();

        MinecraftForge.EVENT_BUS.register(this);
    }


    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent e) {
        if (mc.isGamePaused()
                || mc.world == null
                || e.phase == TickEvent.Phase.END) {
            return;
        }

        if (mc.playerController == null) {
            InputReader.enabled = false;
        } else {
            if (INSTANCE == null)
                INSTANCE = this;
            if (controller == null)
                InputReader.INSTANCE.controller = new MovingWorldPlayerController(mc.playerController);
            if (!enabled)
                enabled = !enabled;
        }

        if (!enabled)
            return;

        findRegionsNearPlayerCalcSelectedBlock();
        controller.setCurrentHit(currentEntityHit);

        if (this.rightClickCounter > 0) {
            --this.rightClickCounter;
        }

        if (mc.currentScreen != null) {
            this.leftClickCounter = 10000;
        }

        if (mc.currentScreen == null || mc.currentScreen.allowUserInput) {
            if (this.leftClickCounter > 0) {
                --this.leftClickCounter;
            }
        }

        this.sendClickBlockToController(this.mc.currentScreen == null && this.mc.gameSettings.keyBindAttack.isKeyDown() && this.mc.inGameHasFocus);
    }

    @SubscribeEvent
    public void parseInputs(InputEvent.MouseInputEvent e) {
        if (!enabled)
            return;

        boolean leftClickDown = mc.gameSettings.keyBindAttack.isKeyDown();
        boolean rightClickDown = mc.gameSettings.keyBindUseItem.isKeyDown();

        if (!leftClickDown)
            performedLeftClick = false;
        if (!rightClickDown)
            performedRightClick = false;

        if (mc.world != null && currentEntityHit != null) {
            if (leftClickDown && !performedLeftClick) {
                performLeftClick();
            }
            if (rightClickDown && !performedRightClick) {
                performRightClick();
            }
        }

        if (leftClickDown)
            performedLeftClick = true;
        if (rightClickDown)
            performedRightClick = true;
    }

    public void performLeftClick() {
        if (leftClickCounter > 0)
            return;

        if (currentEntityHit == null) {
            if (controller.isNotCreative()) {
                this.leftClickCounter = 10;
            }
        } else if (!mc.player.isRowingBoat()) {
            mc.player.swingArm(EnumHand.MAIN_HAND);
            if (currentBlockHit.typeOfHit == RayTraceResult.Type.BLOCK) {
                BlockPos hitBlock = currentBlockHit.getBlockPos();

                if (!currentEntityHit.getMobileRegionWorld().isAirBlock(hitBlock)) {
                    controller.clickBlock(hitBlock, currentBlockHit.sideHit);
                }
            }
        }
    }

    public void performRightClick() {
        if (!controller.isHittingBlock()) {
            this.rightClickCounter = 4;

            if (!this.mc.player.isRowingBoat()) {

                for (EnumHand enumhand : EnumHand.values()) {
                    ItemStack itemstack = this.mc.player.getHeldItem(enumhand);

                    if (currentEntityHit != null) {
                        if (currentBlockHit.typeOfHit == RayTraceResult.Type.BLOCK) {
                            BlockPos blockpos = currentBlockHit.getBlockPos();
                            if (currentEntityHit.getMobileRegionWorld().getBlockState(blockpos).getMaterial() != Material.AIR) {
                                int i = itemstack.getCount();
                                EnumActionResult enumactionresult = controller.processRightClickBlock(this.mc.player, (WorldClient) currentEntityHit.getMobileRegionWorld(),
                                        blockpos, currentBlockHit.sideHit, currentBlockHit.hitVec, enumhand);

                                if (enumactionresult == EnumActionResult.SUCCESS) {
                                    this.mc.player.swingArm(enumhand);

                                    if (!itemstack.isEmpty() && (itemstack.getCount() != i || controller.isInCreativeMode())) {
                                        mc.entityRenderer.itemRenderer.resetEquippedProgress(enumhand);
                                    }

                                    return;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void sendClickBlockToController(boolean leftClick) {
        if (!leftClick) {
            this.leftClickCounter = 0;
        }

        if (this.leftClickCounter <= 0 && !this.mc.player.isHandActive()) {
            if (leftClick && this.currentEntityHit != null && this.currentBlockHit.typeOfHit == RayTraceResult.Type.BLOCK) {
                EntityMobileRegion hitRegionEntity = currentEntityHit;
                BlockPos blockpos = this.currentBlockHit.getBlockPos();

                if (!hitRegionEntity.getMobileRegionWorld().isAirBlock(blockpos) && this.controller.onPlayerDamageBlock(blockpos, this.currentBlockHit.sideHit)) {
                    ParticleHelper.addBlockHitEffects(hitRegionEntity.region, hitRegionEntity.getParentWorld(), currentBlockHit.getBlockPos(), currentBlockHit.sideHit);
                    this.mc.player.swingArm(EnumHand.MAIN_HAND);
                }
            } else {
                this.controller.resetBlockRemoving();
            }
        }
    }

    public void findRegionsNearPlayerCalcSelectedBlock() {
        WorldClient worldClient = Minecraft.getMinecraft().world;
        Entity renderViewEntity = Minecraft.getMinecraft().getRenderViewEntity();
        float reachMultiplier = controller.getBlockReachDistance();
        if (renderViewEntity == null)
            return;
        Vec3d lookVector = renderViewEntity.getLookVec();

        Vec3d rayStart = renderViewEntity.getPositionEyes(1.0f);
        Vec3d rayEnd = rayStart.addVector(lookVector.x * reachMultiplier, lookVector.y * reachMultiplier, lookVector.z * reachMultiplier);
        AxisAlignedBB bb = new AxisAlignedBB(rayStart.x, rayStart.y, rayStart.z, rayEnd.x, rayEnd.y, rayEnd.z).grow(1);

        List<EntityMobileRegion> regionEntities = worldClient.getEntitiesWithinAABB(EntityMobileRegion.class, bb);
        Optional<Tuple<EntityMobileRegion, RayTraceResult>> result = regionEntities.stream().map(this::calcMouseOver).sorted((o1, o2) -> {
            RayTraceResult o1Trace = o1.getSecond();
            RayTraceResult o2Trace = o2.getSecond();

            double o1Distance = Double.MAX_VALUE;
            double o2Distance = Double.MAX_VALUE;

            if (o1Trace != null && o1Trace.typeOfHit == RayTraceResult.Type.BLOCK) {
                BlockPos o1PosShift = o1.getFirst().region.convertRegionPosToRealWorld(o1Trace.getBlockPos());

                o1Distance = renderViewEntity.getDistanceSq(o1PosShift);
            }

            if (o2Trace != null && o2Trace.typeOfHit == RayTraceResult.Type.BLOCK) {
                BlockPos o2PosShift = o2.getFirst().region.convertRegionPosToRealWorld(o2Trace.getBlockPos());

                o2Distance = renderViewEntity.getDistanceSq(o2PosShift);
            }

            return (int) (o1Distance - o2Distance);
        }).findFirst();

        boolean resultFound = result != null;
        resultFound = resultFound && result.isPresent() && result.get().getSecond() != null;
        resultFound = resultFound && result.get().getSecond().typeOfHit == RayTraceResult.Type.BLOCK;

        if (resultFound) {
            currentEntityHit = result.get().getFirst();
            currentBlockHit = result.get().getSecond();
        } else {
            currentEntityHit = null;
            currentBlockHit = new RayTraceResult(RayTraceResult.Type.MISS, new Vec3d(-1, -1, -1), null, new BlockPos(-1, -1, -1));
        }
    }

    private Tuple<EntityMobileRegion, RayTraceResult> calcMouseOver(EntityMobileRegion entityMobileRegion) {
        Entity renderViewEntity = Minecraft.getMinecraft().getRenderViewEntity();
        float reachMultiplier = controller.getBlockReachDistance();
        if (renderViewEntity == null)
            return null;
        Vec3d lookVector = renderViewEntity.getLookVec();

        Vec3d rayStart = renderViewEntity.getPositionEyes(1.0f);
        Vec3d rayEnd = rayStart.addVector(lookVector.x * reachMultiplier, lookVector.y * reachMultiplier, lookVector.z * reachMultiplier);

        RayTraceResult traceResult = rayTraceMovingWorld(rayStart, rayEnd, entityMobileRegion);
        return new Tuple<>(entityMobileRegion, traceResult);
    }

    private RayTraceResult rayTraceMovingWorld(Vec3d start, Vec3d end, EntityMobileRegion entityMobileRegion) {
        Vec3d regionStart = entityMobileRegion.region.convertRealWorldPosToRegion(start);
        Vec3d regionEnd = entityMobileRegion.region.convertRealWorldPosToRegion(end);

        if (entityMobileRegion.getMobileRegionWorld() != null)
            return entityMobileRegion.getMobileRegionWorld().rayTraceBlocks(regionStart, regionEnd);
        else return new RayTraceResult(RayTraceResult.Type.MISS, new Vec3d(-1, -1, -1), null, new BlockPos(-1, -1, -1));
    }

}
