package com.elytradev.movingworld.common.experiments.interact;

import com.elytradev.movingworld.common.experiments.entity.EntityMobileRegion;
import com.elytradev.movingworld.common.experiments.network.messages.server.MessageBlockChange;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockCommandBlock;
import net.minecraft.block.BlockStructure;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameType;
import net.minecraft.world.ILockableContainer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.HashMap;

/**
 * Created by darkevilmac on 3/8/2017.
 */
public class MWPlayerInteractionManager extends PlayerInteractionManager {

    public static HashMap<EntityPlayerMP, MWPlayerInteractionManager> MANAGERS = new HashMap<>();

    public EntityMobileRegion regionEntity;
    public EntityPlayerMP player;

    private double blockReachDistance = 5.0d;
    private boolean isDestroyingBlock;
    private int initialDamage;
    private BlockPos destroyPos = BlockPos.ORIGIN;
    private int curblockDamage;

    private boolean receivedFinishDiggingPacket;
    private BlockPos delayedDestroyPos = BlockPos.ORIGIN;
    private int initialBlockDamage;
    private int durabilityRemainingOnBlock = -1;


    public MWPlayerInteractionManager(EntityMobileRegion regionEntity, EntityPlayerMP sender) {
        super(regionEntity.getMobileRegionWorld());
        this.regionEntity = regionEntity;
        this.player = sender;
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent e) {
        if (MANAGERS.containsKey(e.player)) {
            MANAGERS.get(e.player).updateBlockRemoving();
        }
    }

    /**
     * Get if we are in creative game mode.
     */
    public boolean isCreative() {
        return player.interactionManager.isCreative();
    }

    public void updateBlockRemoving() {
        ++this.curblockDamage;

        if (this.receivedFinishDiggingPacket) {
            int i = this.curblockDamage - this.initialBlockDamage;
            IBlockState iblockstate = this.regionEntity.getParentWorld().getBlockState(this.delayedDestroyPos);
            Block block = iblockstate.getBlock();

            if (block.isAir(iblockstate, regionEntity.getParentWorld(), delayedDestroyPos)) {
                this.receivedFinishDiggingPacket = false;
            } else {
                float f = iblockstate.getPlayerRelativeBlockHardness(this.player, this.regionEntity.getParentWorld(), this.delayedDestroyPos) * (float) (i + 1);
                int j = (int) (f * 10.0F);

                if (j != this.durabilityRemainingOnBlock) {
                    this.regionEntity.getParentWorld().sendBlockBreakProgress(this.player.getEntityId(), this.delayedDestroyPos, j);
                    this.durabilityRemainingOnBlock = j;
                }

                if (f >= 1.0F) {
                    this.receivedFinishDiggingPacket = false;
                    this.tryHarvestBlock(this.delayedDestroyPos);
                }
            }
        } else if (this.isDestroyingBlock) {
            IBlockState iblockstate1 = this.regionEntity.getParentWorld().getBlockState(this.destroyPos);
            Block block1 = iblockstate1.getBlock();

            if (block1.isAir(iblockstate1, regionEntity.getParentWorld(), destroyPos)) {
                this.regionEntity.getParentWorld().sendBlockBreakProgress(this.player.getEntityId(), this.destroyPos, -1);
                this.durabilityRemainingOnBlock = -1;
                this.isDestroyingBlock = false;
            } else {
                int k = this.curblockDamage - this.initialDamage;
                float f1 = iblockstate1.getPlayerRelativeBlockHardness(this.player, this.regionEntity.getParentWorld(), this.destroyPos) * (float) (k + 1); // Forge: Fix network break progress using wrong position
                int l = (int) (f1 * 10.0F);

                if (l != this.durabilityRemainingOnBlock) {
                    this.regionEntity.getParentWorld().sendBlockBreakProgress(this.player.getEntityId(), this.destroyPos, l);
                    this.durabilityRemainingOnBlock = l;
                }
            }
        }
    }

    /**
     * If not creative, it calls sendBlockBreakProgress until the block is broken first. tryHarvestBlock can also be the
     * result of this call.
     */
    public void onBlockClicked(BlockPos pos, EnumFacing side) {
        net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock event = net.minecraftforge.common.ForgeHooks.onLeftClickBlock(player, pos, side, net.minecraftforge.common.ForgeHooks.rayTraceEyeHitVec(player, getBlockReachDistance() + 1));
        if (event.isCanceled()) {
            // Restore block and te data
            new MessageBlockChange(regionEntity.getParentWorld(), pos).sendTo(player);
            regionEntity.getParentWorld().notifyBlockUpdate(pos, regionEntity.getParentWorld().getBlockState(pos), regionEntity.getParentWorld().getBlockState(pos), 3);
            return;
        }

        if (this.isCreative()) {
            if (!this.regionEntity.getParentWorld().extinguishFire((EntityPlayer) null, pos, side)) {
                this.tryHarvestBlock(pos);
            }
        } else {
            IBlockState iblockstate = this.regionEntity.getParentWorld().getBlockState(pos);
            Block block = iblockstate.getBlock();

            if (this.isAdventure()) {
                if (this.isSpectator()) {
                    return;
                }

                if (!this.player.isAllowEdit()) {
                    ItemStack itemstack = this.player.getHeldItemMainhand();

                    if (itemstack.isEmpty()) {
                        return;
                    }

                    if (!itemstack.canDestroy(block)) {
                        return;
                    }
                }
            }

            this.initialDamage = this.curblockDamage;
            float f = 1.0F;

            if (!iblockstate.getBlock().isAir(iblockstate, regionEntity.getParentWorld(), pos)) {
                if (event.getUseBlock() != net.minecraftforge.fml.common.eventhandler.Event.Result.DENY) {
                    block.onBlockClicked(this.regionEntity.getParentWorld(), pos, this.player);
                    this.regionEntity.getParentWorld().extinguishFire((EntityPlayer) null, pos, side);
                } else {
                    // Restore block and te data
                    new MessageBlockChange(regionEntity.getParentWorld(), pos).sendTo(player);
                    regionEntity.getParentWorld().notifyBlockUpdate(pos, regionEntity.getParentWorld().getBlockState(pos), regionEntity.getParentWorld().getBlockState(pos), 3);
                }
                f = iblockstate.getPlayerRelativeBlockHardness(this.player, this.regionEntity.getParentWorld(), pos);
            }
            if (event.getUseItem() == net.minecraftforge.fml.common.eventhandler.Event.Result.DENY) {
                if (f >= 1.0F) {
                    // Restore block and te data
                    new MessageBlockChange(regionEntity.getParentWorld(), pos).sendTo(player);
                    regionEntity.getParentWorld().notifyBlockUpdate(pos, regionEntity.getParentWorld().getBlockState(pos), regionEntity.getParentWorld().getBlockState(pos), 3);
                }
                return;
            }

            if (!iblockstate.getBlock().isAir(iblockstate, regionEntity.getParentWorld(), pos) && f >= 1.0F) {
                this.tryHarvestBlock(pos);
            } else {
                this.isDestroyingBlock = true;
                this.destroyPos = pos;
                int i = (int) (f * 10.0F);
                this.regionEntity.getParentWorld().sendBlockBreakProgress(this.player.getEntityId(), pos, i);
                this.durabilityRemainingOnBlock = i;
            }
        }
    }

    public void blockRemoving(BlockPos pos) {
        if (pos.equals(this.destroyPos)) {
            int i = this.curblockDamage - this.initialDamage;
            IBlockState iblockstate = this.regionEntity.getParentWorld().getBlockState(pos);

            if (!iblockstate.getBlock().isAir(iblockstate, regionEntity.getParentWorld(), pos)) {
                float f = iblockstate.getPlayerRelativeBlockHardness(this.player, this.regionEntity.getParentWorld(), pos) * (float) (i + 1);

                if (f >= 0.7F) {
                    this.isDestroyingBlock = false;
                    this.regionEntity.getParentWorld().sendBlockBreakProgress(this.player.getEntityId(), pos, -1);
                    this.tryHarvestBlock(pos);
                } else if (!this.receivedFinishDiggingPacket) {
                    this.isDestroyingBlock = false;
                    this.receivedFinishDiggingPacket = true;
                    this.delayedDestroyPos = pos;
                    this.initialBlockDamage = this.initialDamage;
                }
            }
        }
    }

    /**
     * Stops the block breaking process
     */
    public void cancelDestroyingBlock() {
        this.isDestroyingBlock = false;
        this.regionEntity.getParentWorld().sendBlockBreakProgress(this.player.getEntityId(), this.destroyPos, -1);
    }

    /**
     * Attempts to harvest a block
     */
    public boolean tryHarvestBlock(BlockPos pos) {
        int exp = net.minecraftforge.common.ForgeHooks.onBlockBreakEvent(regionEntity.getParentWorld(), player.interactionManager.getGameType(), player, pos);
        if (exp == -1) {
            return false;
        } else {
            IBlockState iblockstate = this.regionEntity.getParentWorld().getBlockState(pos);
            TileEntity tileentity = this.regionEntity.getParentWorld().getTileEntity(pos);
            Block block = iblockstate.getBlock();

            if ((block instanceof BlockCommandBlock || block instanceof BlockStructure) && !this.player.canUseCommandBlock()) {
                this.regionEntity.getParentWorld().notifyBlockUpdate(pos, iblockstate, iblockstate, 3);
                return false;
            } else {
                ItemStack stack = player.getHeldItemMainhand();
                if (!stack.isEmpty() && stack.getItem().onBlockStartBreak(stack, pos, player)) return false;

                this.regionEntity.getParentWorld().playEvent(this.player, 2001, pos, Block.getStateId(iblockstate));
                boolean flag1;

                if (this.isCreative()) {
                    flag1 = this.removeBlock(pos);
                    new MessageBlockChange(regionEntity.getParentWorld(), pos).sendTo(player);
                } else {
                    ItemStack itemstack1 = this.player.getHeldItemMainhand();
                    ItemStack itemstack2 = itemstack1.isEmpty() ? ItemStack.EMPTY : itemstack1.copy();
                    boolean flag = iblockstate.getBlock().canHarvestBlock(regionEntity.getParentWorld(), pos, player);

                    if (!itemstack1.isEmpty()) {
                        itemstack1.onBlockDestroyed(this.regionEntity.getParentWorld(), iblockstate, pos, this.player);
                    }

                    flag1 = this.removeBlock(pos, flag);
                    if (flag1 && flag) {
                        iblockstate.getBlock().harvestBlock(this.regionEntity.getEntityWorld(), this.player, regionEntity.region.convertRegionPosToRealWorld(pos), iblockstate, tileentity, itemstack2);
                    }
                }

                // Drop experience
                if (!this.isCreative() && flag1 && exp > 0) {
                    iblockstate.getBlock().dropXpOnBlockBreak(this.regionEntity.getEntityWorld(), regionEntity.region.convertRegionPosToRealWorld(pos), exp);
                }
                return flag1;
            }
        }
    }

    public EnumActionResult processRightClick(EntityPlayer player, World worldIn, ItemStack stack, EnumHand hand) {
        if (isSpectator()) {
            return EnumActionResult.PASS;
        } else if (player.getCooldownTracker().hasCooldown(stack.getItem())) {
            return EnumActionResult.PASS;
        } else {
            if (net.minecraftforge.common.ForgeHooks.onItemRightClick(player, hand))
                return net.minecraft.util.EnumActionResult.PASS;
            int i = stack.getCount();
            int j = stack.getMetadata();
            ItemStack copyBeforeUse = stack.copy();
            ActionResult<ItemStack> actionresult = stack.useItemRightClick(worldIn, player, hand);
            ItemStack itemstack = (ItemStack) actionresult.getResult();

            if (itemstack == stack && itemstack.getCount() == i && itemstack.getMaxItemUseDuration() <= 0 && itemstack.getMetadata() == j) {
                return actionresult.getType();
            } else if (actionresult.getType() == EnumActionResult.FAIL && itemstack.getMaxItemUseDuration() > 0 && !player.isHandActive()) {
                return actionresult.getType();
            } else {
                player.setHeldItem(hand, itemstack);

                if (this.isCreative()) {
                    itemstack.setCount(i);

                    if (itemstack.isItemStackDamageable()) {
                        itemstack.setItemDamage(j);
                    }
                }

                if (itemstack.isEmpty()) {
                    player.setHeldItem(hand, ItemStack.EMPTY);
                    net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem(player, copyBeforeUse, hand);
                }

                if (!player.isHandActive()) {
                    ((EntityPlayerMP) player).sendContainerToPlayer(player.inventoryContainer);
                }

                return actionresult.getType();
            }
        }
    }

    public EnumActionResult processRightClickBlock(EntityPlayer player, World worldIn, ItemStack stack, EnumHand hand, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!EntityPlayerMPProxy.PROXIES.containsKey(player.getGameProfile())) {
            EntityPlayerMPProxy.PROXIES.put(player.getGameProfile(), new EntityPlayerMPProxy((EntityPlayerMP) player, regionEntity));
            worldIn.spawnEntity(EntityPlayerMPProxy.PROXIES.get(player.getGameProfile()));
        }
        EntityPlayerMPProxy playerProxy = EntityPlayerMPProxy.PROXIES.get(player.getGameProfile());
        playerProxy.setRegion(regionEntity);

        if (isSpectator()) {
            TileEntity tileentity = worldIn.getTileEntity(pos);

            if (tileentity instanceof ILockableContainer) {
                Block block1 = worldIn.getBlockState(pos).getBlock();
                ILockableContainer ilockablecontainer = (ILockableContainer) tileentity;

                if (ilockablecontainer instanceof TileEntityChest && block1 instanceof BlockChest) {
                    ilockablecontainer = ((BlockChest) block1).getLockableContainer(worldIn, pos);
                }

                if (ilockablecontainer != null) {
                    player.displayGUIChest(ilockablecontainer);
                    return EnumActionResult.SUCCESS;
                }
            } else if (tileentity instanceof IInventory) {
                player.displayGUIChest((IInventory) tileentity);
                return EnumActionResult.SUCCESS;
            }

            return EnumActionResult.PASS;
        } else {
            net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock event = net.minecraftforge.common.ForgeHooks
                    .onRightClickBlock(playerProxy, hand, pos, facing, net.minecraftforge.common.ForgeHooks.rayTraceEyeHitVec(playerProxy, getBlockReachDistance() + 1));
            if (event.isCanceled()) return EnumActionResult.PASS;

            EnumActionResult ret = stack.onItemUseFirst(playerProxy, worldIn, pos, hand, facing, hitX, hitY, hitZ);
            if (ret != EnumActionResult.PASS) return ret;

            boolean bypass = true;
            for (ItemStack s : new ItemStack[]{player.getHeldItemMainhand(), player.getHeldItemOffhand()}) //TODO: Expand to more hands? player.inv.getHands()?
                bypass = bypass && (s.isEmpty() || s.getItem().doesSneakBypassUse(s, worldIn, pos, player));
            EnumActionResult result = EnumActionResult.PASS;

            if (!player.isSneaking() || bypass || event.getUseBlock() == net.minecraftforge.fml.common.eventhandler.Event.Result.ALLOW) {
                IBlockState iblockstate = worldIn.getBlockState(pos);
                if (event.getUseBlock() != net.minecraftforge.fml.common.eventhandler.Event.Result.DENY)
                    if (iblockstate.getBlock().onBlockActivated(worldIn, pos, iblockstate, playerProxy, hand, facing, hitX, hitY, hitZ)) {
                        result = EnumActionResult.SUCCESS;
                    }
            }

            if (stack.isEmpty()) {
                return EnumActionResult.PASS;
            } else if (player.getCooldownTracker().hasCooldown(stack.getItem())) {
                return EnumActionResult.PASS;
            } else {
                if (stack.getItem() instanceof ItemBlock && !player.canUseCommandBlock()) {
                    Block block = ((ItemBlock) stack.getItem()).getBlock();

                    if (block instanceof BlockCommandBlock || block instanceof BlockStructure) {
                        return EnumActionResult.FAIL;
                    }
                }

                if (this.isCreative()) {
                    int j = stack.getMetadata();
                    int i = stack.getCount();
                    if (result != EnumActionResult.SUCCESS && event.getUseItem() != net.minecraftforge.fml.common.eventhandler.Event.Result.DENY
                            || result == EnumActionResult.SUCCESS && event.getUseItem() == net.minecraftforge.fml.common.eventhandler.Event.Result.ALLOW) {
                        EnumActionResult enumactionresult = stack.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
                        stack.setItemDamage(j);
                        stack.setCount(i);
                        return enumactionresult;
                    } else return result;
                } else {
                    if (result != EnumActionResult.SUCCESS && event.getUseItem() != net.minecraftforge.fml.common.eventhandler.Event.Result.DENY
                            || result == EnumActionResult.SUCCESS && event.getUseItem() == net.minecraftforge.fml.common.eventhandler.Event.Result.ALLOW)
                        return stack.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
                    else return result;
                }
            }
        }
    }

    public double getBlockReachDistance() {
        return blockReachDistance;
    }

    public void setBlockReachDistance(double distance) {
        blockReachDistance = distance;
    }

    /**
     * Removes a block and triggers the appropriate events
     */
    private boolean removeBlock(BlockPos pos) {
        return removeBlock(pos, false);
    }

    private boolean removeBlock(BlockPos pos, boolean canHarvest) {
        IBlockState iblockstate = this.regionEntity.getParentWorld().getBlockState(pos);
        boolean flag = iblockstate.getBlock().removedByPlayer(iblockstate, regionEntity.getParentWorld(), pos, player, canHarvest);

        if (flag) {
            iblockstate.getBlock().onBlockDestroyedByPlayer(this.regionEntity.getParentWorld(), pos, iblockstate);
        }

        return flag;
    }

    public boolean isAdventure() {
        return player.interactionManager.getGameType().isAdventure();
    }

    public boolean isSpectator() {
        return player.interactionManager.getGameType() == GameType.SPECTATOR;
    }

    public void setRegionEntity(EntityMobileRegion regionEntity) {
        this.regionEntity = regionEntity;
    }
}
