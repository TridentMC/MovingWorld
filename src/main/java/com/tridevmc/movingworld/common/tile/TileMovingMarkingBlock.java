package com.tridevmc.movingworld.common.tile;

import com.tridevmc.movingworld.api.IMovingTile;
import com.tridevmc.movingworld.common.chunk.LocatedBlock;
import com.tridevmc.movingworld.common.chunk.MovingWorldAssemblyInteractor;
import com.tridevmc.movingworld.common.chunk.assembly.AssembleResult;
import com.tridevmc.movingworld.common.chunk.assembly.ChunkAssembler;
import com.tridevmc.movingworld.common.entity.EntityMovingWorld;
import com.tridevmc.movingworld.common.entity.MovingWorldInfo;
import com.tridevmc.movingworld.common.util.LocatedBlockList;
import com.tridevmc.compound.core.reflect.WrappedField;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.UUID;

public abstract class TileMovingMarkingBlock extends TileEntity implements IMovingTile {

    private static final WrappedField<IBlockState> TILE_STATE = WrappedField.create(TileEntity.class, new String[]{"cachedBlockState", "field_195045_e"});

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

    public boolean assembleMovingWorld(EntityPlayer player) {
        boolean returnVal = false;

        if (!this.world.isRemote) {
            this.prevResult = this.assembleResult;
            ChunkAssembler assembler = new ChunkAssembler(this.world, this.pos, this.getMaxBlocks());
            MovingWorldAssemblyInteractor interactor = this.getNewAssemblyInteractor();
            this.assembleResult = assembler.doAssemble(interactor);

            this.assembledMovingWorld(player, returnVal);

            this.setInteractor(interactor);
            TextComponentString c;
            switch (this.assembleResult.getType()) {
                case RESULT_OK:
                    c = new TextComponentString("Assembled " + this.getInfo().getName() + "!");
                    player.sendStatusMessage(c, true);
                    break;
                case RESULT_OK_WITH_WARNINGS:
                    returnVal = true;
                case RESULT_BLOCK_OVERFLOW:
                    c = new TextComponentString(
                            "Cannot create moving world with more than " + this.getMaxBlocks() + " blocks");
                    player.sendStatusMessage(c, true);
                    break;
                case RESULT_MISSING_MARKER:
                    c = new TextComponentString("Cannot create moving world with no moving world marker");
                    player.sendStatusMessage(c, true);
                    break;
                case RESULT_ERROR_OCCURED:
                    c = new TextComponentString("An error occured while assembling moving world. See console log for details.");
                    player.sendStatusMessage(c, true);
                    break;
                case RESULT_NONE:
                    c = new TextComponentString("Nothing was assembled");
                    player.sendStatusMessage(c, true);
                    break;
                default:
            }
        }
        return returnVal;
    }

    public void assembledMovingWorld(EntityPlayer player, boolean returnVal) {
        //No Implementation.
    }

    public boolean mountMovingWorld(EntityPlayer player, EntityMovingWorld movingWorld) {
        if (!this.world.isRemote) {
            if (this.assembleResult != null && this.assembleResult.isOK()) {
                this.assembleResult.checkConsistent(this.world);
                this.mountedMovingWorld(player, movingWorld, MountStage.PREMSG);
                if (this.assembleResult.getType() == AssembleResult.ResultType.RESULT_INCONSISTENT) {
                    return false;
                }
                if (this.assembleResult.getType() == AssembleResult.ResultType.RESULT_OK_WITH_WARNINGS) {
                    ITextComponent c = new TextComponentString("Moving world contains changes");
                    player.sendStatusMessage(c, true);
                }

                this.mountedMovingWorld(player, movingWorld, MountStage.PRERIDE);

                EntityMovingWorld entity = this.assembleResult.getEntity(this.world, movingWorld);
                if (entity != null) {
                    entity.setInfo(this.getInfo());
                    if (this.world.spawnEntity(entity)) {
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

    public void undoCompilation(EntityPlayer player) {
        this.assembleResult = this.prevResult;
        this.prevResult = null;
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound compound = new NBTTagCompound();
        this.writeNBTForSending(compound);
        return new SPacketUpdateTileEntity(this.pos, 0, compound);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
        this.read(packet.getNbtCompound());
    }

    public abstract MovingWorldAssemblyInteractor getNewAssemblyInteractor();

    /**
     * Called during/after mountMovingWorld();
     *
     * @param stage can be 1, 2, or 3 this represents the stage of the method we're at. more information can be viewed
     *              at the github repo to see when your code will be executed. http://github.com/elytra/MovingWorld
     */
    public void mountedMovingWorld(EntityPlayer player, EntityMovingWorld movingWorld, MountStage stage) {
    }

    @Override
    public void read(NBTTagCompound tag) {
        super.read(tag);
        this.getInfo().setName(tag.getString("name"));
        if (tag.contains("owner")) {
            this.getInfo().setOwner(UUID.fromString(tag.getString("owner")));
        }
        TILE_STATE.set(this, Block.getStateById(tag.getInt("state")));
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
            NBTTagCompound removedFluidCompound = tag.getCompound("removedFluidCompounds");
            int tagIndex = 0;

            while (removedFluidCompound.contains("block#" + tagIndex)) {
                NBTTagCompound lbTag = removedFluidCompound.getCompound("block#" + tagIndex);
                LocatedBlock locatedBlock = new LocatedBlock(lbTag, this.world);

                this.removedFluidBlocks.add(locatedBlock);
                tagIndex++;
            }
            tag.put("removedFluidCompounds", new NBTTagCompound());
        }
    }

    @Override
    public NBTTagCompound write(NBTTagCompound tag) {
        tag = super.write(tag);

        tag.putString("name", this.getInfo().getName());
        if (this.getInfo().getOwner() != null) {
            tag.putString("owner", this.getInfo().getOwner().toString());
        }

        tag.putInt("state", Block.getStateId(TILE_STATE.get(this)));
        tag.putString("name", this.getInfo().getName());
        if (this.getParentMovingWorld() != null && !this.getParentMovingWorld().removed) {
            tag.putInt("movingWorld", this.getParentMovingWorld().getEntityId());
        }
        if (this.assembleResult != null) {
            NBTTagCompound comp = new NBTTagCompound();
            this.assembleResult.writeNBTFully(comp);
            this.assembleResult.assemblyInteractor.writeNBTFully(comp);
            tag.put("res", comp);
            // Where the hell did this go in the transition to MovingWorld? Lost to the ether I suppose.
        }
        if (this.removedFluidBlocks != null && !this.removedFluidBlocks.isEmpty()) {
            NBTTagCompound removedFluidCompound = new NBTTagCompound();
            for (int i = 0; i < this.removedFluidBlocks.size(); i++) {
                LocatedBlock locatedBlock = this.removedFluidBlocks.get(i);
                NBTTagCompound lbTag = new NBTTagCompound();
                locatedBlock.writeToNBT(lbTag);

                removedFluidCompound.put("block#" + i, lbTag);
            }
            tag.put("removedFluidCompounds", removedFluidCompound);
        }

        return tag;
    }

    public void writeNBTForSending(NBTTagCompound tag) {
        super.write(tag);
        tag.putInt("state", Block.getStateId(TILE_STATE.get(this)));
        tag.putString("name", this.getInfo().getName());

        if (this.getParentMovingWorld() != null && !this.getParentMovingWorld().removed) {
            tag.putInt("movingWorld", this.getParentMovingWorld().getEntityId());
        }

        if (this.assembleResult != null) {
            NBTTagCompound comp = new NBTTagCompound();
            this.assembleResult.writeNBTMetadata(comp);
            this.assembleResult.assemblyInteractor.writeNBTMetadata(comp);
            tag.put("res", comp);
        }
    }

    public enum MountStage {
        PREMSG, PRERIDE, POSTRIDE
    }

}
