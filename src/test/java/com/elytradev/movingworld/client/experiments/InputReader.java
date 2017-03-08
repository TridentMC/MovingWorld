package com.elytradev.movingworld.client.experiments;

import com.elytradev.movingworld.client.experiments.render.ParticleHelper;
import com.elytradev.movingworld.common.experiments.entity.EntityMobileRegion;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class InputReader {

    public static InputReader INSTANCE;
    public static boolean enabled;

    public Minecraft mc;
    public MovingWorldPlayerController controller;

    private int leftClickCounter, rightClickCounter;
    private boolean performedLeftClick = false, performedRightClick = false;

    public InputReader(PlayerControllerMP playerControllerMP) {
        this.enabled = false;
        this.controller = new MovingWorldPlayerController(playerControllerMP);
        this.mc = Minecraft.getMinecraft();

        MinecraftForge.EVENT_BUS.register(this);
    }


    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent e) {
        if (mc.isGamePaused()
                || mc.world == null) {
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

        controller.findRegionsNearPlayerCalcSelectedBlock();

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

        if (mc.world != null && controller.currentHit.getFirst() != null) {
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

        if (controller.currentHit.getFirst() == null) {
            if (controller.isNotCreative()) {
                this.leftClickCounter = 10;
            }
        } else if (!mc.player.isRowingBoat()) {
            mc.player.swingArm(EnumHand.MAIN_HAND);
            if (controller.currentHit.getSecond().typeOfHit == RayTraceResult.Type.BLOCK) {
                BlockPos hitBlock = controller.currentHit.getSecond().getBlockPos();

                if (!controller.currentHit.getFirst().getMobileRegionWorld().isAirBlock(hitBlock)) {
                    controller.clickBlock(hitBlock, controller.currentHit.getSecond().sideHit);
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

                    if (controller.currentHit.getFirst() != null) {
                        if (controller.currentHit.getSecond().typeOfHit == RayTraceResult.Type.BLOCK) {
                            BlockPos blockpos = controller.currentHit.getSecond().getBlockPos();
                            if (controller.currentHit.getFirst().getMobileRegionWorld().getBlockState(blockpos).getMaterial() != Material.AIR) {
                                int i = itemstack.getCount();
                                EnumActionResult enumactionresult = controller.processRightClickBlock(this.mc.player, (WorldClient) controller.currentHit.getFirst().getMobileRegionWorld(),
                                        blockpos, controller.currentHit.getSecond().sideHit, controller.currentHit.getSecond().hitVec, enumhand);

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
            if (leftClick && this.controller.currentHit.getFirst() != null && this.controller.currentHit.getSecond().typeOfHit == RayTraceResult.Type.BLOCK) {
                EntityMobileRegion hitRegionEntity = controller.currentHit.getFirst();
                BlockPos blockpos = this.controller.currentHit.getSecond().getBlockPos();

                if (!hitRegionEntity.getMobileRegionWorld().isAirBlock(blockpos) && this.controller.onPlayerDamageBlock(blockpos, this.controller.currentHit.getSecond().sideHit)) {
                    ParticleHelper.addBlockHitEffects(hitRegionEntity.region, hitRegionEntity.getParentWorld(), controller.currentHit.getSecond().getBlockPos(), controller.currentHit.getSecond().sideHit);
                    this.mc.player.swingArm(EnumHand.MAIN_HAND);
                }
            } else {
                this.controller.resetBlockRemoving();
            }
        }
    }

}
