package com.elytradev.movingworld.common.experiments;

import com.elytradev.concrete.reflect.invoker.Invoker;
import com.elytradev.concrete.reflect.invoker.Invokers;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import javax.annotation.Nullable;
import java.util.List;

import static com.elytradev.movingworld.common.experiments.EntityPlayerMPProxy.PROXIES;

/**
 * Created by darkevilmac on 3/9/2017.
 */
public class ContainerWrapper extends Container {

    private Container realContainer;

    private Invoker addSlotToContainer;
    private Invoker retrySlotClick;
    private Invoker mergeItemStack;
    private Invoker resetDrag;

    public ContainerWrapper(Container realContainer) {
        this.realContainer = realContainer;

        addSlotToContainer = Invokers.findMethod(Container.class, realContainer, new String[]{"addSlotToContainer", "func_75146_a", "a"}, Slot.class);
        retrySlotClick = Invokers.findMethod(Container.class, realContainer, new String[]{"retrySlotClick", "func_75133_b", "a"}, int.class, int.class, boolean.class, EntityPlayer.class);
        mergeItemStack = Invokers.findMethod(Container.class, realContainer, new String[]{"mergeItemStack", "func_75135_a", "a"}, ItemStack.class, int.class, int.class, boolean.class);
        resetDrag = Invokers.findMethod(Container.class, realContainer, new String[]{"resetDrag", "func_94533_d", "d"});
    }

    @Override
    protected Slot addSlotToContainer(Slot slotIn) {
        return (Slot) addSlotToContainer.invoke(realContainer, slotIn);
    }

    @Override
    public void addListener(IContainerListener listener) {
        realContainer.addListener(listener);
    }

    @Override
    public NonNullList<ItemStack> getInventory() {
        return realContainer.getInventory();
    }

    @Override
    public void removeListener(IContainerListener listener) {
        realContainer.removeListener(listener);
    }

    @Override
    public void detectAndSendChanges() {
        realContainer.detectAndSendChanges();
    }

    @Override
    public boolean enchantItem(EntityPlayer playerIn, int id) {
        return realContainer.enchantItem(playerIn, id);
    }

    @Nullable
    @Override
    public Slot getSlotFromInventory(IInventory inv, int slotIn) {
        return realContainer.getSlotFromInventory(inv, slotIn);
    }

    @Override
    public Slot getSlot(int slotId) {
        return realContainer.getSlot(slotId);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        return realContainer.transferStackInSlot(playerIn, index);
    }

    @Override
    public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player) {
        return realContainer.slotClick(slotId, dragType, clickTypeIn, player);
    }

    @Override
    public boolean canMergeSlot(ItemStack stack, Slot slotIn) {
        return realContainer.canMergeSlot(stack, slotIn);
    }

    @Override
    protected void retrySlotClick(int slotId, int clickedButton, boolean mode, EntityPlayer playerIn) {
        retrySlotClick.invoke(realContainer, slotId, clickedButton, mode, playerIn);
    }

    @Override
    public void onContainerClosed(EntityPlayer playerIn) {
        realContainer.onContainerClosed(playerIn);
    }

    @Override
    public void onCraftMatrixChanged(IInventory inventoryIn) {
        realContainer.onCraftMatrixChanged(inventoryIn);
    }

    @Override
    public void putStackInSlot(int slotID, ItemStack stack) {
        realContainer.putStackInSlot(slotID, stack);
    }

    @Override
    public void setAll(List<ItemStack> p_190896_1_) {
        realContainer.setAll(p_190896_1_);
    }

    @Override
    public void updateProgressBar(int id, int data) {
        realContainer.updateProgressBar(id, data);
    }

    @Override
    public short getNextTransactionID(InventoryPlayer invPlayer) {
        return realContainer.getNextTransactionID(invPlayer);
    }

    @Override
    public boolean getCanCraft(EntityPlayer player) {
        return realContainer.getCanCraft(player);
    }

    @Override
    public void setCanCraft(EntityPlayer player, boolean canCraft) {
        realContainer.setCanCraft(player, canCraft);
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        if(playerIn instanceof EntityPlayerMP && PROXIES.containsKey(playerIn.getGameProfile())){
            return realContainer.canInteractWith(PROXIES.get(playerIn.getGameProfile()));
        } else{

        }

        return realContainer.canInteractWith(playerIn);
    }

    @Override
    protected boolean mergeItemStack(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection) {
        return (boolean) mergeItemStack.invoke(realContainer, stack, startIndex, endIndex, reverseDirection);
    }

    @Override
    protected void resetDrag() {
        resetDrag.invoke(realContainer);
    }

    @Override
    public boolean canDragIntoSlot(Slot slotIn) {
        return realContainer.canDragIntoSlot(slotIn);
    }
}
