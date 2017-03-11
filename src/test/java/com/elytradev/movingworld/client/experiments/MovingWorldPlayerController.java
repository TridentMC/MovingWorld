package com.elytradev.movingworld.client.experiments;

import com.elytradev.movingworld.common.experiments.EntityPlayerMPProxy;
import com.elytradev.movingworld.common.experiments.entity.EntityMobileRegion;
import com.elytradev.movingworld.common.experiments.network.messages.client.MessagePlayerDigging;
import com.elytradev.movingworld.common.experiments.network.messages.client.MessageTryUseItemOnBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCommandBlock;
import net.minecraft.block.BlockStructure;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameType;
import net.minecraft.world.World;

/**
 * Modified version of PlayerControllerMP, used to handle client input to send to the server.
 */
public class MovingWorldPlayerController {

    private BlockPos currentBlock = new BlockPos(-1, -1, -1);
    private EntityMobileRegion currentHit;
    private EntityMobileRegion lastEntityHit;

    private PlayerControllerMP realController;

    private Minecraft mc;
    private EntityPlayerSP player;
    private ItemStack currentItemHittingBlock = ItemStack.EMPTY;
    private float curBlockDamageMP;
    private float stepSoundTickCounter;
    private int blockHitDelay;
    private boolean isHittingBlock;
    private int currentPlayerItem;

    public MovingWorldPlayerController(PlayerControllerMP playerControllerMP) {
        this.realController = playerControllerMP;

        this.mc = Minecraft.getMinecraft();
        this.player = mc.player;
    }

    public static void clickBlockCreative(Minecraft mcIn, MovingWorldPlayerController inputHelper, BlockPos pos, EnumFacing facing) {
        if (!mcIn.world.extinguishFire(mcIn.player, pos, facing)) {
            inputHelper.onPlayerDestroyBlock(pos);
        }
    }

    public void setCurrentHit(EntityMobileRegion region) {
        this.lastEntityHit = this.currentHit;
        this.currentHit = region;
    }

    public GameType getCurrentGameType() {
        return realController.getCurrentGameType();
    }

    public boolean onPlayerDestroyBlock(BlockPos pos) {
        if (currentHit == null)
            return false;

        if (this.getCurrentGameType().isAdventure()) {
            if (this.getCurrentGameType() == GameType.SPECTATOR) {
                return false;
            }

            if (!this.mc.player.isAllowEdit()) {
                ItemStack itemstack = this.mc.player.getHeldItemMainhand();

                if (itemstack.isEmpty()) {
                    return false;
                }

                if (!itemstack.canDestroy(this.currentHit.getParentWorld().getBlockState(pos).getBlock())) {
                    return false;
                }
            }
        }

        ItemStack stack = mc.player.getHeldItemMainhand();
        if (!stack.isEmpty() && stack.getItem().onBlockStartBreak(stack, pos, mc.player)) {
            return false;
        }

        if (this.getCurrentGameType().isCreative() && !this.mc.player.getHeldItemMainhand().isEmpty() && this.mc.player.getHeldItemMainhand().getItem() instanceof ItemSword) {
            return false;
        } else {
            World world = currentHit.getParentWorld();
            IBlockState iblockstate = world.getBlockState(pos);
            Block block = iblockstate.getBlock();

            if ((block instanceof BlockCommandBlock || block instanceof BlockStructure) && !this.mc.player.canUseCommandBlock()) {
                return false;
            } else if (iblockstate.getMaterial() == Material.AIR) {
                return false;
            } else {
                world.playEvent(2001, pos, Block.getStateId(iblockstate));

                currentBlock = currentBlock.add(0, -(currentBlock.getY() + 1), 0);

                if (!this.getCurrentGameType().isCreative()) {
                    ItemStack itemstack1 = this.mc.player.getHeldItemMainhand();
                    ItemStack copyBeforeUse = itemstack1.copy();

                    if (!itemstack1.isEmpty()) {
                        itemstack1.onBlockDestroyed(world, iblockstate, pos, this.mc.player);

                        if (itemstack1.isEmpty()) {
                            net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem(this.mc.player, copyBeforeUse, EnumHand.MAIN_HAND);
                            this.mc.player.setHeldItem(EnumHand.MAIN_HAND, ItemStack.EMPTY);
                        }
                    }
                }

                boolean flag = block.removedByPlayer(iblockstate, world, pos, mc.player, false);

                if (flag) {
                    block.onBlockDestroyedByPlayer(world, pos, iblockstate);
                }
                return flag;
            }
        }
    }

    /**
     * Called when the player is hitting a block with an item.
     */
    public boolean clickBlock(BlockPos loc, EnumFacing face) {
        if (currentHit == null)
            return false;

        if (this.getCurrentGameType().isAdventure()) {
            if (this.getCurrentGameType() == GameType.SPECTATOR) {
                return false;
            }

            if (!this.mc.player.isAllowEdit()) {
                ItemStack itemstack = this.mc.player.getHeldItemMainhand();

                if (itemstack.isEmpty()) {
                    return false;
                }

                if (!itemstack.canDestroy(this.currentHit.getParentWorld().getBlockState(loc).getBlock())) {
                    return false;
                }
            }
        }

        boolean flag0 = !this.isHittingBlock;
        flag0 = flag0 || !this.isHittingPosition(loc);
        flag0 = flag0;

        if (!this.currentHit.getParentWorld().getWorldBorder().contains(loc)) {
            return false;
        } else {
            if (this.getCurrentGameType().isCreative()) {
                new MessagePlayerDigging(currentHit, CPacketPlayerDigging.Action.START_DESTROY_BLOCK, loc, face).sendToServer();
                if (!net.minecraftforge.common.ForgeHooks.onLeftClickBlock(this.mc.player, loc, face, net.minecraftforge.common.ForgeHooks.rayTraceEyeHitVec(this.mc.player, getBlockReachDistance() + 1)).isCanceled())
                    clickBlockCreative(this.mc, this, loc, face);
                this.blockHitDelay = 5;
            } else if (flag0) {
                if (this.isHittingBlock) {
                    new MessagePlayerDigging(currentHit, CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, currentBlock, face).sendToServer();
                }

                new MessagePlayerDigging(currentHit, CPacketPlayerDigging.Action.START_DESTROY_BLOCK, loc, face).sendToServer();

                net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock event = net.minecraftforge.common.ForgeHooks.onLeftClickBlock(this.mc.player, loc, face, net.minecraftforge.common.ForgeHooks.rayTraceEyeHitVec(this.mc.player, getBlockReachDistance() + 1));
                IBlockState iblockstate = this.currentHit.getParentWorld().getBlockState(loc);
                boolean flag = iblockstate.getMaterial() != Material.AIR;

                if (flag && this.curBlockDamageMP == 0.0F) {
                    if (event.getUseBlock() != net.minecraftforge.fml.common.eventhandler.Event.Result.DENY)
                        iblockstate.getBlock().onBlockClicked(this.currentHit.getParentWorld(), loc, this.mc.player);
                }
                if (event.getUseItem() == net.minecraftforge.fml.common.eventhandler.Event.Result.DENY) return true;
                if (flag && iblockstate.getPlayerRelativeBlockHardness(this.mc.player, this.currentHit.getMobileRegionWorld(), loc) >= 1.0F) {
                    this.onPlayerDestroyBlock(loc);
                } else {
                    this.isHittingBlock = true;
                    currentBlock = loc;
                    this.currentItemHittingBlock = this.mc.player.getHeldItemMainhand();
                    this.curBlockDamageMP = 0.0F;
                    this.stepSoundTickCounter = 0.0F;
                    this.currentHit.getParentWorld().sendBlockBreakProgress(this.mc.player.getEntityId(), this.currentBlock, (int) (this.curBlockDamageMP * 10.0F) - 1);
                }
            }

            return true;
        }
    }

    public void resetBlockRemoving() {
        if (this.isHittingBlock && (currentHit != null || lastEntityHit != null)) {
            EntityMobileRegion useRegion = currentHit != null ? currentHit : lastEntityHit;
            new MessagePlayerDigging(useRegion, CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, currentBlock, EnumFacing.DOWN).sendToServer();
            this.isHittingBlock = false;
            this.curBlockDamageMP = 0.0F;
            useRegion.getParentWorld().sendBlockBreakProgress(this.mc.player.getEntityId(), this.currentBlock, -1);
            this.mc.player.resetCooldown();
        }
    }

    public boolean onPlayerDamageBlock(BlockPos posBlock, EnumFacing directionFacing) {
        if (this.blockHitDelay > 0) {
            --this.blockHitDelay;
            return true;
        } else if (currentHit == null) {
            return false;
        } else if (this.getCurrentGameType().isCreative() && this.currentHit.getParentWorld().getWorldBorder().contains(posBlock)) {
            this.blockHitDelay = 5;
            new MessagePlayerDigging(currentHit, CPacketPlayerDigging.Action.START_DESTROY_BLOCK, posBlock, directionFacing).sendToServer();
            clickBlockCreative(this.mc, this, posBlock, directionFacing);
            return true;
        } else if (this.isHittingPosition(posBlock)) {
            IBlockState iblockstate = this.currentHit.getParentWorld().getBlockState(posBlock);
            Block block = iblockstate.getBlock();

            if (iblockstate.getMaterial() == Material.AIR) {
                this.isHittingBlock = false;
                return false;
            } else {
                this.curBlockDamageMP += iblockstate.getPlayerRelativeBlockHardness(this.mc.player, this.currentHit.getMobileRegionWorld(), posBlock);

                if (this.stepSoundTickCounter % 4.0F == 0.0F) {
                    SoundType soundtype = block.getSoundType(iblockstate, currentHit.getParentWorld(), posBlock, mc.player);
                    this.mc.getSoundHandler().playSound(new PositionedSoundRecord(soundtype.getHitSound(), SoundCategory.NEUTRAL, (soundtype.getVolume() + 1.0F) / 8.0F, soundtype.getPitch() * 0.5F, posBlock));
                }

                ++this.stepSoundTickCounter;

                if (this.curBlockDamageMP >= 1.0F) {
                    this.isHittingBlock = false;
                    new MessagePlayerDigging(currentHit, CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, posBlock, directionFacing).sendToServer();
                    this.onPlayerDestroyBlock(posBlock);
                    this.curBlockDamageMP = 0.0F;
                    this.stepSoundTickCounter = 0.0F;
                    this.blockHitDelay = 5;
                }

                if (currentHit != null && currentHit.getParentWorld() != null)
                    currentHit.getParentWorld().sendBlockBreakProgress(player.getEntityId(), currentBlock, (int) (curBlockDamageMP * 10F) - 1);
                return true;
            }
        } else {
            return this.clickBlock(posBlock, directionFacing);
        }
    }

    /**
     * player reach distance = 4F
     */
    public float getBlockReachDistance() {
        return realController.getBlockReachDistance();
    }

    private boolean isHittingPosition(BlockPos pos) {
        if (currentHit != null) {
            ItemStack itemstack = this.mc.player.getHeldItemMainhand();
            boolean flag = this.currentItemHittingBlock.isEmpty() && itemstack.isEmpty();

            if (!this.currentItemHittingBlock.isEmpty() && !itemstack.isEmpty()) {
                flag = !net.minecraftforge.client.ForgeHooksClient.shouldCauseBlockBreakReset(this.currentItemHittingBlock, itemstack);
            }

            return pos.equals(currentBlock) && flag;
        } else {
            return false;
        }
    }

    public EnumActionResult processRightClickBlock(EntityPlayerSP player, WorldClient worldIn, BlockPos stack, EnumFacing pos, Vec3d facing, EnumHand vec) {
        if (currentHit == null)
            return EnumActionResult.FAIL;

        if (!EntityPlayerSPProxy.PROXIES.containsKey(player.getGameProfile())) {
            EntityPlayerSPProxy.PROXIES.put(player.getGameProfile(), new EntityPlayerSPProxy(player, currentHit));
            worldIn.spawnEntity(EntityPlayerSPProxy.PROXIES.get(player.getGameProfile()));
        }
        EntityPlayerSPProxy playerProxy = EntityPlayerSPProxy.PROXIES.get(player.getGameProfile());
        playerProxy.setRegion(currentHit);

        ItemStack itemstack = player.getHeldItem(vec);
        float f = (float) (facing.xCoord - (double) stack.getX());
        float f1 = (float) (facing.yCoord - (double) stack.getY());
        float f2 = (float) (facing.zCoord - (double) stack.getZ());
        boolean flag = false;

        if (!this.currentHit.getParentWorld().getWorldBorder().contains(stack)) {
            return EnumActionResult.FAIL;
        } else {
            net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock event = net.minecraftforge.common.ForgeHooks
                    .onRightClickBlock(player, vec, stack, pos, net.minecraftforge.common.ForgeHooks.rayTraceEyeHitVec(player, getBlockReachDistance() + 1));
            if (event.isCanceled()) {
                // Give the server a chance to fire event as well. That way server event is not dependant on client event.
                new MessageTryUseItemOnBlock(currentHit, stack, pos, vec, f, f1, f2).sendToServer();
                return EnumActionResult.PASS;
            }
            EnumActionResult result = EnumActionResult.PASS;

            if (this.getCurrentGameType() != GameType.SPECTATOR) {
                EnumActionResult ret = itemstack.onItemUseFirst(player, worldIn, stack, vec, pos, f, f1, f2);
                if (ret != EnumActionResult.PASS) return ret;

                IBlockState iblockstate = worldIn.getBlockState(stack);
                boolean bypass = true;
                for (ItemStack s : new ItemStack[]{player.getHeldItemMainhand(), player.getHeldItemOffhand()})
                    bypass = bypass && (s.isEmpty() || s.getItem().doesSneakBypassUse(s, worldIn, stack, player));

                if ((!player.isSneaking() || bypass || event.getUseBlock() == net.minecraftforge.fml.common.eventhandler.Event.Result.ALLOW)) {
                    if (event.getUseBlock() != net.minecraftforge.fml.common.eventhandler.Event.Result.DENY)
                        flag = iblockstate.getBlock().onBlockActivated(worldIn, stack, iblockstate, playerProxy, vec, pos, f, f1, f2);
                    if (flag) result = EnumActionResult.SUCCESS;
                }

                if (!flag && itemstack.getItem() instanceof ItemBlock) {
                    ItemBlock itemblock = (ItemBlock) itemstack.getItem();

                    if (!itemblock.canPlaceBlockOnSide(worldIn, stack, pos, player, itemstack)) {
                        return EnumActionResult.FAIL;
                    }
                }
            }

            new MessageTryUseItemOnBlock(currentHit, stack, pos, vec, f, f1, f2).sendToServer();

            if (!flag && this.getCurrentGameType() != GameType.SPECTATOR || event.getUseItem() == net.minecraftforge.fml.common.eventhandler.Event.Result.ALLOW) {
                if (itemstack.isEmpty()) {
                    return EnumActionResult.PASS;
                } else if (player.getCooldownTracker().hasCooldown(itemstack.getItem())) {
                    return EnumActionResult.PASS;
                } else {
                    if (itemstack.getItem() instanceof ItemBlock && !player.canUseCommandBlock()) {
                        Block block = ((ItemBlock) itemstack.getItem()).getBlock();

                        if (block instanceof BlockCommandBlock || block instanceof BlockStructure) {
                            return EnumActionResult.FAIL;
                        }
                    }

                    if (this.getCurrentGameType().isCreative()) {
                        int i = itemstack.getMetadata();
                        int j = itemstack.getCount();
                        if (event.getUseItem() != net.minecraftforge.fml.common.eventhandler.Event.Result.DENY) {
                            EnumActionResult enumactionresult = itemstack.onItemUse(player, worldIn, stack, vec, pos, f, f1, f2);
                            itemstack.setItemDamage(i);
                            itemstack.setCount(j);
                            return enumactionresult;
                        } else return result;
                    } else {
                        ItemStack copyForUse = itemstack.copy();
                        if (event.getUseItem() != net.minecraftforge.fml.common.eventhandler.Event.Result.DENY)
                            result = itemstack.onItemUse(player, worldIn, stack, vec, pos, f, f1, f2);
                        if (itemstack.isEmpty())
                            net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem(player, copyForUse, vec);
                        return result;
                    }
                }
            } else {
                return EnumActionResult.SUCCESS;
            }
        }
    }

    /**
     * Checks if the player is not creative, used for checking if it should break a block instantly
     */
    public boolean isNotCreative() {
        return !this.getCurrentGameType().isCreative();
    }

    /**
     * returns true if player is in creative mode
     */
    public boolean isInCreativeMode() {
        return this.getCurrentGameType().isCreative();
    }

    /**
     * Return isHittingBlock
     */
    public boolean isHittingBlock() {
        return this.isHittingBlock;
    }

}
