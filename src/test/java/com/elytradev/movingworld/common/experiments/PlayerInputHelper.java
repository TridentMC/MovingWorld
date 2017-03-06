package com.elytradev.movingworld.common.experiments;

import com.elytradev.movingworld.common.experiments.entity.EntityMobileRegion;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCommandBlock;
import net.minecraft.block.BlockStructure;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameType;
import net.minecraft.world.World;

/**
 * Created by darkevilmac on 3/2/2017.
 */
public class PlayerInputHelper {

    public static final PlayerInputHelper INSTANCE = new PlayerInputHelper();
    public Tuple<EntityMobileRegion, BlockPos> currentBlock = new Tuple<>(null, new BlockPos(-1, -1, -1));
    private Minecraft mc;
    /**
     * The Item currently being used to destroy a block
     */
    private ItemStack currentItemHittingBlock = ItemStack.EMPTY;
    /**
     * Current block damage (MP)
     */
    private float curBlockDamageMP;
    /**
     * Tick counter, when it hits 4 it resets back to 0 and plays the step sound
     */
    private float stepSoundTickCounter;
    /**
     * Delays the first damage on the block after the first click on the block
     */
    private int blockHitDelay;
    /**
     * Tells if the player is hitting a block
     */
    private boolean isHittingBlock;
    /**
     * Current game type for the player
     */
    private GameType currentGameType = GameType.SURVIVAL;
    /**
     * Index of the current item held by the player in the inventory hotbar
     */
    private int currentPlayerItem;

    public static void clickBlockCreative(Minecraft mcIn, PlayerInputHelper inputHelper, BlockPos pos, EnumFacing facing) {
        if (!mcIn.world.extinguishFire(mcIn.player, pos, facing)) {
            inputHelper.onPlayerDestroyBlock(pos);
        }
    }

    /**
     * Sets player capabilities depending on current gametype. params: player
     */
    public void setPlayerCapabilities(EntityPlayer player) {
        this.currentGameType.configurePlayerCapabilities(player.capabilities);
    }

    /**
     * None
     */
    public boolean isSpectator() {
        return this.currentGameType == GameType.SPECTATOR;
    }

    /**
     * Sets the game type for the player.
     */
    public void setGameType(GameType type) {
        this.currentGameType = type;
        this.currentGameType.configurePlayerCapabilities(this.mc.player.capabilities);
    }

    /**
     * Flips the player around.
     */
    public void flipPlayer(EntityPlayer playerIn) {
        playerIn.rotationYaw = -180.0F;
    }

    public boolean onPlayerDestroyBlock(BlockPos pos) {
        if (this.currentGameType.isAdventure()) {
            if (this.currentGameType == GameType.SPECTATOR) {
                return false;
            }

            if (!this.mc.player.isAllowEdit()) {
                ItemStack itemstack = this.mc.player.getHeldItemMainhand();

                if (itemstack.isEmpty()) {
                    return false;
                }

                if (!itemstack.canDestroy(this.mc.world.getBlockState(pos).getBlock())) {
                    return false;
                }
            }
        }

        ItemStack stack = mc.player.getHeldItemMainhand();
        if (!stack.isEmpty() && stack.getItem().onBlockStartBreak(stack, pos, mc.player)) {
            return false;
        }

        if (this.currentGameType.isCreative() && !this.mc.player.getHeldItemMainhand().isEmpty() && this.mc.player.getHeldItemMainhand().getItem() instanceof ItemSword) {
            return false;
        } else {
            World world = this.mc.world;
            IBlockState iblockstate = world.getBlockState(pos);
            Block block = iblockstate.getBlock();

            if ((block instanceof BlockCommandBlock || block instanceof BlockStructure) && !this.mc.player.canUseCommandBlock()) {
                return false;
            } else if (iblockstate.getMaterial() == Material.AIR) {
                return false;
            } else {
                world.playEvent(2001, pos, Block.getStateId(iblockstate));

                //this.currentBlock = new BlockPos(this.currentBlock.getX(), -1, this.currentBlock.getZ());

                if (!this.currentGameType.isCreative()) {
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
        if (this.currentGameType.isAdventure()) {
            if (this.currentGameType == GameType.SPECTATOR) {
                return false;
            }

            if (!this.mc.player.isAllowEdit()) {
                ItemStack itemstack = this.mc.player.getHeldItemMainhand();

                if (itemstack.isEmpty()) {
                    return false;
                }

                if (!itemstack.canDestroy(this.mc.world.getBlockState(loc).getBlock())) {
                    return false;
                }
            }
        }

        if (!this.mc.world.getWorldBorder().contains(loc)) {
            return false;
        } else {
            if (this.currentGameType.isCreative()) {
                //this.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, loc, face));
                if (!net.minecraftforge.common.ForgeHooks.onLeftClickBlock(this.mc.player, loc, face, net.minecraftforge.common.ForgeHooks.rayTraceEyeHitVec(this.mc.player, getBlockReachDistance() + 1)).isCanceled())
                    clickBlockCreative(this.mc, this, loc, face);
                this.blockHitDelay = 5;
            } else if (!this.isHittingBlock || !this.isHittingPosition(loc)) {
                if (this.isHittingBlock) {
                    //this.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, this.currentBlock, face));
                }

                //this.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, loc, face));
                net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock event = net.minecraftforge.common.ForgeHooks.onLeftClickBlock(this.mc.player, loc, face, net.minecraftforge.common.ForgeHooks.rayTraceEyeHitVec(this.mc.player, getBlockReachDistance() + 1));

                IBlockState iblockstate = this.mc.world.getBlockState(loc);
                boolean flag = iblockstate.getMaterial() != Material.AIR;

                if (flag && this.curBlockDamageMP == 0.0F) {
                    if (event.getUseBlock() != net.minecraftforge.fml.common.eventhandler.Event.Result.DENY)
                        iblockstate.getBlock().onBlockClicked(this.mc.world, loc, this.mc.player);
                }
                if (event.getUseItem() == net.minecraftforge.fml.common.eventhandler.Event.Result.DENY) return true;
                if (flag && iblockstate.getPlayerRelativeBlockHardness(this.mc.player, this.mc.player.world, loc) >= 1.0F) {
                    this.onPlayerDestroyBlock(loc);
                } else {
                    this.isHittingBlock = true;
                    //this.currentBlock = loc;
                    this.currentItemHittingBlock = this.mc.player.getHeldItemMainhand();
                    this.curBlockDamageMP = 0.0F;
                    this.stepSoundTickCounter = 0.0F;
                    this.mc.world.sendBlockBreakProgress(this.mc.player.getEntityId(), this.currentBlock.getSecond(), (int) (this.curBlockDamageMP * 10.0F) - 1);
                }
            }

            return true;
        }
    }

    /**
     * Resets current block damage and field_78778_j
     */
    public void resetBlockRemoving() {
        if (this.isHittingBlock) {
            //this.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, this.currentBlock, EnumFacing.DOWN));
            this.isHittingBlock = false;
            this.curBlockDamageMP = 0.0F;
            this.mc.world.sendBlockBreakProgress(this.mc.player.getEntityId(), this.currentBlock.getSecond(), -1);
            this.mc.player.resetCooldown();
        }
    }

    public boolean onPlayerDamageBlock(BlockPos posBlock, EnumFacing directionFacing) {
        this.syncCurrentPlayItem();

        if (this.blockHitDelay > 0) {
            --this.blockHitDelay;
            return true;
        } else if (this.currentGameType.isCreative() && this.mc.world.getWorldBorder().contains(posBlock)) {
            this.blockHitDelay = 5;
            //this.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, posBlock, directionFacing));
            clickBlockCreative(this.mc, this, posBlock, directionFacing);
            return true;
        } else if (this.isHittingPosition(posBlock)) {
            IBlockState iblockstate = this.mc.world.getBlockState(posBlock);
            Block block = iblockstate.getBlock();

            if (iblockstate.getMaterial() == Material.AIR) {
                this.isHittingBlock = false;
                return false;
            } else {
                this.curBlockDamageMP += iblockstate.getPlayerRelativeBlockHardness(this.mc.player, this.mc.player.world, posBlock);

                if (this.stepSoundTickCounter % 4.0F == 0.0F) {
                    SoundType soundtype = block.getSoundType(iblockstate, mc.world, posBlock, mc.player);
                    this.mc.getSoundHandler().playSound(new PositionedSoundRecord(soundtype.getHitSound(), SoundCategory.NEUTRAL, (soundtype.getVolume() + 1.0F) / 8.0F, soundtype.getPitch() * 0.5F, posBlock));
                }

                ++this.stepSoundTickCounter;

                if (this.curBlockDamageMP >= 1.0F) {
                    this.isHittingBlock = false;
                    //this.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, posBlock, directionFacing));
                    this.onPlayerDestroyBlock(posBlock);
                    this.curBlockDamageMP = 0.0F;
                    this.stepSoundTickCounter = 0.0F;
                    this.blockHitDelay = 5;
                }

                //this.mc.world.sendBlockBreakProgress(this.mc.player.getEntityId(), this.currentBlock, (int) (this.curBlockDamageMP * 10.0F) - 1);
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
        return this.currentGameType.isCreative() ? 5.0F : 4.5F;
    }


    private boolean isHittingPosition(BlockPos pos) {
        ItemStack itemstack = this.mc.player.getHeldItemMainhand();
        boolean flag = this.currentItemHittingBlock.isEmpty() && itemstack.isEmpty();

        if (!this.currentItemHittingBlock.isEmpty() && !itemstack.isEmpty()) {
            flag = !net.minecraftforge.client.ForgeHooksClient.shouldCauseBlockBreakReset(this.currentItemHittingBlock, itemstack);
        }

        return pos.equals(this.currentBlock) && flag;
    }

    /**
     * Syncs the current player item with the server
     */
    private void syncCurrentPlayItem() {
        int i = this.mc.player.inventory.currentItem;

        if (i != this.currentPlayerItem) {
            this.currentPlayerItem = i;
            //this.connection.sendPacket(new CPacketHeldItemChange(this.currentPlayerItem));
        }
    }

    public EnumActionResult processRightClickBlock(EntityPlayerSP player, WorldClient worldIn, BlockPos stack, EnumFacing pos, Vec3d facing, EnumHand vec) {
        this.syncCurrentPlayItem();
        ItemStack itemstack = player.getHeldItem(vec);
        float f = (float) (facing.xCoord - (double) stack.getX());
        float f1 = (float) (facing.yCoord - (double) stack.getY());
        float f2 = (float) (facing.zCoord - (double) stack.getZ());
        boolean flag = false;

        if (!this.mc.world.getWorldBorder().contains(stack)) {
            return EnumActionResult.FAIL;
        } else {
            net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock event = net.minecraftforge.common.ForgeHooks
                    .onRightClickBlock(player, vec, stack, pos, net.minecraftforge.common.ForgeHooks.rayTraceEyeHitVec(player, getBlockReachDistance() + 1));
            if (event.isCanceled()) {
                // Give the server a chance to fire event as well. That way server event is not dependant on client event.
                //this.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(stack, pos, vec, f, f1, f2));
                return EnumActionResult.PASS;
            }
            EnumActionResult result = EnumActionResult.PASS;

            if (this.currentGameType != GameType.SPECTATOR) {
                EnumActionResult ret = itemstack.onItemUseFirst(player, worldIn, stack, vec, pos, f, f1, f2);
                if (ret != EnumActionResult.PASS) return ret;

                IBlockState iblockstate = worldIn.getBlockState(stack);
                boolean bypass = true;
                for (ItemStack s : new ItemStack[]{player.getHeldItemMainhand(), player.getHeldItemOffhand()}) //TODO: Expand to more hands? player.inv.getHands()?
                    bypass = bypass && (s.isEmpty() || s.getItem().doesSneakBypassUse(s, worldIn, stack, player));

                if ((!player.isSneaking() || bypass || event.getUseBlock() == net.minecraftforge.fml.common.eventhandler.Event.Result.ALLOW)) {
                    if (event.getUseBlock() != net.minecraftforge.fml.common.eventhandler.Event.Result.DENY)
                        flag = iblockstate.getBlock().onBlockActivated(worldIn, stack, iblockstate, player, vec, pos, f, f1, f2);
                    if (flag) result = EnumActionResult.SUCCESS;
                }

                if (!flag && itemstack.getItem() instanceof ItemBlock) {
                    ItemBlock itemblock = (ItemBlock) itemstack.getItem();

                    if (!itemblock.canPlaceBlockOnSide(worldIn, stack, pos, player, itemstack)) {
                        return EnumActionResult.FAIL;
                    }
                }
            }

            //this.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(stack, pos, vec, f, f1, f2));

            if (!flag && this.currentGameType != GameType.SPECTATOR || event.getUseItem() == net.minecraftforge.fml.common.eventhandler.Event.Result.ALLOW) {
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

                    if (this.currentGameType.isCreative()) {
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

    public EnumActionResult processRightClick(EntityPlayer player, World worldIn, EnumHand stack) {
        if (this.currentGameType == GameType.SPECTATOR) {
            return EnumActionResult.PASS;
        } else {
            this.syncCurrentPlayItem();
            //this.connection.sendPacket(new CPacketPlayerTryUseItem(stack));
            ItemStack itemstack = player.getHeldItem(stack);

            if (player.getCooldownTracker().hasCooldown(itemstack.getItem())) {
                return EnumActionResult.PASS;
            } else {
                if (net.minecraftforge.common.ForgeHooks.onItemRightClick(player, stack))
                    return net.minecraft.util.EnumActionResult.PASS;
                int i = itemstack.getCount();
                ActionResult<ItemStack> actionresult = itemstack.useItemRightClick(worldIn, player, stack);
                ItemStack itemstack1 = (ItemStack) actionresult.getResult();

                if (itemstack1 != itemstack || itemstack1.getCount() != i) {
                    player.setHeldItem(stack, itemstack1);
                    if (itemstack1.isEmpty()) {
                        net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem(player, itemstack, stack);
                    }
                }

                return actionresult.getType();
            }
        }
    }

    public boolean gameIsSurvivalOrAdventure() {
        return this.currentGameType.isSurvivalOrAdventure();
    }

    /**
     * Checks if the player is not creative, used for checking if it should break a block instantly
     */
    public boolean isNotCreative() {
        return !this.currentGameType.isCreative();
    }

    /**
     * returns true if player is in creative mode
     */
    public boolean isInCreativeMode() {
        return this.currentGameType.isCreative();
    }

    /**
     * true for hitting entities far away.
     */
    public boolean extendedReach() {
        return this.currentGameType.isCreative();
    }


    public boolean isSpectatorMode() {
        return this.currentGameType == GameType.SPECTATOR;
    }

    public GameType getCurrentGameType() {
        return this.currentGameType;
    }

    /**
     * Return isHittingBlock
     */
    public boolean getIsHittingBlock() {
        return this.isHittingBlock;
    }

}
