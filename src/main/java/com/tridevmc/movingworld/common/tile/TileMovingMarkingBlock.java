package com.tridevmc.movingworld.common.tile;

import com.tridevmc.movingworld.api.IMovingTile;
import com.tridevmc.movingworld.common.chunk.LocatedBlock;
import com.tridevmc.movingworld.common.chunk.MovingWorldAssemblyInteractor;
import com.tridevmc.movingworld.common.chunk.assembly.AssembleResult;
import com.tridevmc.movingworld.common.chunk.assembly.ChunkAssembler;
import com.tridevmc.movingworld.common.entity.EntityMovingWorld;
import com.tridevmc.movingworld.common.entity.MovingWorldInfo;
import com.tridevmc.movingworld.common.util.LocatedBlockList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.UUID;

public abstract class TileMovingMarkingBlock extends TileEntity implements IMovingTile {

    public LocatedBlockList removedFluidBlocks; // A list of fluid blocks that were destroyed last disassemble, used to fill back in when we reassemble.
    private AssembleResult assembleResult, prevResult;


    public TileMovingMarkingBlock(TileEntityType<?> type) {
        super(type);
        this.setParentMovingWorld(null);
        this.assembleResult = this.prevResult = null;
    }

    @Nonnull
    public abstract MovingWorldAssemblyInteractor getInteractor();

    public abstract void setInteractor(@Nonnull MovingWorldAssemblyInteractor interactor);

    @Nonnull
    public abstract MovingWorldInfo getInfo();

    public abstract void setInfo(@Nonnull MovingWorldInfo info);

    public abstract int getMaxBlocks();

    public AssembleResult getPrevAssembleResult() {
        return this.prevResult;
    }

    public void setPrevAssembleResult(AssembleResult result) {
        this.prevResult = result;
    }

    public AssembleResult getAssembleResult() {
        return this.assembleResult;
    }

    public void setAssembleResult(AssembleResult assembleResult) {
        this.assembleResult = assembleResult;
    }

    /**
     * For getting a new INSTANCE of your ship type to create.
     */
    public abstract EntityMovingWorld getMovingWorld(World worldObj);

    public boolean assembleMovingWorld(PlayerEntity player) {
        boolean returnVal = false;

        if (!this.world.isRemote) {
            this.prevResult = this.assembleResult;
            ChunkAssembler assembler = new ChunkAssembler(this.world, this.pos, this.getMaxBlocks());
            MovingWorldAssemblyInteractor interactor = this.getNewAssemblyInteractor();
            this.assembleResult = assembler.doAssemble(interactor);

            this.assembledMovingWorld(player, returnVal);
            this.setInteractor(interactor);

            StringTextComponent c;
            switch (this.assembleResult.getType()) {
                case RESULT_OK:
                    c = new StringTextComponent("Assembled " + this.getInfo().getName() + "!");
                    player.sendStatusMessage(c, true);
                    break;
                case RESULT_OK_WITH_WARNINGS:
                    returnVal = true;
                case RESULT_BLOCK_OVERFLOW:
                    c = new StringTextComponent(
                            "Cannot create moving world with more than " + this.getMaxBlocks() + " blocks");
                    player.sendStatusMessage(c, true);
                    break;
                case RESULT_MISSING_MARKER:
                    c = new StringTextComponent("Cannot create moving world with no moving world marker");
                    player.sendStatusMessage(c, true);
                    break;
                case RESULT_ERROR_OCCURED:
                    c = new StringTextComponent("An error occurred while assembling moving world. See console log for details.");
                    player.sendStatusMessage(c, true);
                    break;
                case RESULT_NONE:
                    c = new StringTextComponent("Nothing was assembled");
                    player.sendStatusMessage(c, true);
                    break;
                default:
            }
        }
        return returnVal;
    }

    public void assembledMovingWorld(PlayerEntity player, boolean returnVal) {
        //No Implementation.
    }

    public boolean mountMovingWorld(PlayerEntity player, EntityMovingWorld movingWorld) {
        if (!this.world.isRemote) {
            if (this.assembleResult != null && this.assembleResult.isOK()) {
                this.assembleResult.checkConsistent(this.world);
                this.mountedMovingWorld(player, movingWorld, MountStage.PREMSG);
                if (this.assembleResult.getType() == AssembleResult.ResultType.RESULT_INCONSISTENT) {
                    return false;
                }
                if (this.assembleResult.getType() == AssembleResult.ResultType.RESULT_OK_WITH_WARNINGS) {
                    ITextComponent c = new StringTextComponent("Moving world contains changes");
                    player.sendStatusMessage(c, true);
                }

                this.mountedMovingWorld(player, movingWorld, MountStage.PRERIDE);

                EntityMovingWorld entity = this.assembleResult.getEntity(this.world, movingWorld);
                if (entity != null) {
                    entity.setInfo(this.getInfo());
                    if (this.world.addEntity(entity)) {
                        player.startRiding(entity);
                        this.assembleResult = null;
                        return true;
                    }
                }
                this.mountedMovingWorld(player, entity, MountStage.POSTRIDE);
            }
        }
        return false;
    }

    public void undoCompilation(PlayerEntity player) {
        this.assembleResult = this.prevResult;
        this.prevResult = null;
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        CompoundNBT compound = new CompoundNBT();
        this.writeNBTForSending(compound);
        return new SUpdateTileEntityPacket(this.pos, 0, compound);
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket packet) {
        this.read(packet.getNbtCompound());
    }

    public abstract MovingWorldAssemblyInteractor getNewAssemblyInteractor();

    /**
     * Called during/after mountMovingWorld();
     *
     * @param stage can be 1, 2, or 3 this represents the stage of the method we're at. more information can be viewed
     *              at the github repo to see when your code will be executed. http://github.com/elytra/MovingWorld
     */
    public void mountedMovingWorld(PlayerEntity player, EntityMovingWorld movingWorld, MountStage stage) {
    }

    @Override
    public void read(CompoundNBT tag) {
        super.read(tag);
        this.getInfo().setName(tag.getString("name"));
        if (tag.contains("owner")) {
            this.getInfo().setOwner(UUID.fromString(tag.getString("owner")));
        }
        if (tag.contains("ship") && this.world != null) {
            int id = tag.getInt("ship");
            Entity entity = this.world.getEntityByID(id);
            if (entity instanceof EntityMovingWorld) {
                this.setParentMovingWorld((EntityMovingWorld) entity);
            }
        }
        if (tag.contains("res")) {
            this.assembleResult = new AssembleResult(tag.getCompound("res"), this.world);
            this.assembleResult.assemblyInteractor = this.getNewAssemblyInteractor().fromNBT(tag.getCompound("res"), this.world);
        }
        if (tag.contains("removedFluidCompounds")) {
            this.removedFluidBlocks = new LocatedBlockList();
            CompoundNBT removedFluidCompound = tag.getCompound("removedFluidCompounds");
            int tagIndex = 0;

            while (removedFluidCompound.contains("block#" + tagIndex)) {
                CompoundNBT lbTag = removedFluidCompound.getCompound("block#" + tagIndex);
                LocatedBlock locatedBlock = new LocatedBlock(lbTag, this.world);

                this.removedFluidBlocks.add(locatedBlock);
                tagIndex++;
            }
            tag.put("removedFluidCompounds", new CompoundNBT());
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        tag = super.write(tag);

        tag.putString("name", this.getInfo().getName());
        if (this.getInfo().getOwner() != null) {
            tag.putString("owner", this.getInfo().getOwner().toString());
        }

        tag.putString("name", this.getInfo().getName());
        if (this.getParentMovingWorld() != null && !this.getParentMovingWorld().removed) {
            tag.putInt("movingWorld", this.getParentMovingWorld().getEntityId());
        }
        if (this.assembleResult != null) {
            CompoundNBT comp = new CompoundNBT();
            this.assembleResult.writeNBTFully(comp);
            this.assembleResult.assemblyInteractor.writeNBTFully(comp);
            tag.put("res", comp);
            // Where the hell did this go in the transition to MovingWorld? Lost to the ether I suppose.
        }
        if (this.removedFluidBlocks != null && !this.removedFluidBlocks.isEmpty()) {
            CompoundNBT removedFluidCompound = new CompoundNBT();
            for (int i = 0; i < this.removedFluidBlocks.size(); i++) {
                LocatedBlock locatedBlock = this.removedFluidBlocks.get(i);
                CompoundNBT lbTag = new CompoundNBT();
                locatedBlock.writeToNBT(lbTag);

                removedFluidCompound.put("block#" + i, lbTag);
            }
            tag.put("removedFluidCompounds", removedFluidCompound);
        }

        return tag;
    }

    public void writeNBTForSending(CompoundNBT tag) {
        super.write(tag);
        tag.putString("name", this.getInfo().getName());

        if (this.getParentMovingWorld() != null && !this.getParentMovingWorld().removed) {
            tag.putInt("movingWorld", this.getParentMovingWorld().getEntityId());
        }

        if (this.assembleResult != null) {
            CompoundNBT comp = new CompoundNBT();
            this.assembleResult.writeNBTMetadata(comp);
            this.assembleResult.assemblyInteractor.writeNBTMetadata(comp);
            tag.put("res", comp);
        }
    }

    public enum MountStage {
        PREMSG, PRERIDE, POSTRIDE
    }

}
