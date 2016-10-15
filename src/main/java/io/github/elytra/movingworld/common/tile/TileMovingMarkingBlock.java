package io.github.elytra.movingworld.common.tile;

import io.github.elytra.movingworld.api.IMovingTile;
import io.github.elytra.movingworld.common.chunk.LocatedBlock;
import io.github.elytra.movingworld.common.chunk.MovingWorldAssemblyInteractor;
import io.github.elytra.movingworld.common.chunk.assembly.AssembleResult;
import io.github.elytra.movingworld.common.chunk.assembly.ChunkAssembler;
import io.github.elytra.movingworld.common.entity.EntityMovingWorld;
import io.github.elytra.movingworld.common.entity.MovingWorldInfo;
import io.github.elytra.movingworld.common.util.LocatedBlockList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import java.util.UUID;

import static io.github.elytra.movingworld.common.chunk.assembly.AssembleResult.ResultType.RESULT_INCONSISTENT;
import static io.github.elytra.movingworld.common.chunk.assembly.AssembleResult.ResultType.RESULT_OK_WITH_WARNINGS;

public abstract class TileMovingMarkingBlock extends TileEntity implements IMovingTile {

    public LocatedBlockList removedFluidBlocks; // A list of fluid blocks that were destroyed last disassemble, used to fill back in when we reassemble.
    private AssembleResult assembleResult, prevResult;


    public TileMovingMarkingBlock() {
        super();
        setParentMovingWorld(null);
        assembleResult = prevResult = null;
    }

    public abstract MovingWorldAssemblyInteractor getInteractor();

    public abstract void setInteractor(MovingWorldAssemblyInteractor interactor);

    public abstract MovingWorldInfo getInfo();

    public abstract void setInfo(MovingWorldInfo info);

    public abstract int getMaxBlocks();

    public AssembleResult getPrevAssembleResult() {
        return prevResult;
    }

    public void setPrevAssembleResult(AssembleResult result) {
        prevResult = result;
    }

    public AssembleResult getAssembleResult() {
        return assembleResult;
    }

    public void setAssembleResult(AssembleResult assembleResult) {
        this.assembleResult = assembleResult;
    }

    /**
     * For getting a new instance of your ship type to create.
     */
    public abstract EntityMovingWorld getMovingWorld(World worldObj);

    public boolean assembleMovingWorld(EntityPlayer player) {
        boolean returnVal = false;

        if (!worldObj.isRemote) {
            prevResult = assembleResult;
            ChunkAssembler assembler = new ChunkAssembler(worldObj, pos, getMaxBlocks());
            MovingWorldAssemblyInteractor interactor = getNewAssemblyInteractor();
            assembleResult = assembler.doAssemble(interactor);

            assembledMovingWorld(player, returnVal);

            setInteractor(interactor);
            TextComponentString c;
            switch (assembleResult.getType()) {
                case RESULT_OK:
                    c = new TextComponentString("Assembled " + getInfo().getName() + "!");
                    player.addChatMessage(c);
                    break;
                case RESULT_OK_WITH_WARNINGS:
                    returnVal = true;
                case RESULT_BLOCK_OVERFLOW:
                    c = new TextComponentString("Cannot create moving world with more than " + getMaxBlocks() + " blocks");
                    player.addChatMessage(c);
                    break;
                case RESULT_MISSING_MARKER:
                    c = new TextComponentString("Cannot create moving world with no moving world marker");
                    player.addChatMessage(c);
                    break;
                case RESULT_ERROR_OCCURED:
                    c = new TextComponentString("An error occured while assembling moving world. See console log for details.");
                    player.addChatMessage(c);
                    break;
                case RESULT_NONE:
                    c = new TextComponentString("Nothing was assembled");
                    player.addChatMessage(c);
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
        if (!worldObj.isRemote) {
            if (assembleResult != null && assembleResult.isOK()) {
                assembleResult.checkConsistent(worldObj);
                mountedMovingWorld(player, movingWorld, MountStage.PREMSG);
                if (assembleResult.getType() == RESULT_INCONSISTENT) {
                    return false;
                }
                if (assembleResult.getType() == RESULT_OK_WITH_WARNINGS) {
                    ITextComponent c = new TextComponentString("Moving world contains changes");
                    player.addChatMessage(c);
                }

                mountedMovingWorld(player, movingWorld, MountStage.PRERIDE);

                EntityMovingWorld entity = assembleResult.getEntity(worldObj, movingWorld);
                if (entity != null) {
                    entity.setInfo(getInfo());
                    if (worldObj.spawnEntityInWorld(entity)) {
                        player.startRiding(entity);
                        assembleResult = null;
                        return true;
                    }
                }
                mountedMovingWorld(player, entity, MountStage.POSTRIDE);
            }
        }
        return false;
    }

    public void undoCompilation(EntityPlayer player) {
        assembleResult = prevResult;
        prevResult = null;
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound compound = new NBTTagCompound();
        writeNBTForSending(compound);
        return new SPacketUpdateTileEntity(pos, 0, compound);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
        readFromNBT(packet.getNbtCompound());
    }

    public abstract MovingWorldAssemblyInteractor getNewAssemblyInteractor();

    /**
     * Called during/after mountMovingWorld();
     *
     * @param stage can be 1, 2, or 3 this represents the stage of the method we're at. more
     *              information can be viewed at the github repo to see when your code will be
     *              executed. http://github.com/elytra/MovingWorld
     */
    public void mountedMovingWorld(EntityPlayer player, EntityMovingWorld movingWorld, MountStage stage) {
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        getInfo().setName(tag.getString("name"));
        if (tag.hasKey("owner")) {
            getInfo().setOwner(UUID.fromString(tag.getString("owner")));
        }
        blockMetadata = tag.getInteger("meta");
        if (tag.hasKey("ship") && worldObj != null) {
            int id = tag.getInteger("ship");
            Entity entity = worldObj.getEntityByID(id);
            if (entity instanceof EntityMovingWorld) {
                setParentMovingWorld((EntityMovingWorld) entity);
            }
        }
        if (tag.hasKey("res")) {
            assembleResult = new AssembleResult(tag.getCompoundTag("res"), worldObj);
            assembleResult.assemblyInteractor = getNewAssemblyInteractor().fromNBT(tag.getCompoundTag("res"), worldObj);
        }
        if (tag.hasKey("removedFluidCompounds")) {
            removedFluidBlocks = new LocatedBlockList();
            NBTTagCompound removedFluidCompound = tag.getCompoundTag("removedFluidCompounds");
            int tagIndex = 0;

            while (removedFluidCompound.hasKey("block#" + tagIndex)) {
                NBTTagCompound lbTag = removedFluidCompound.getCompoundTag("block#" + tagIndex);
                LocatedBlock locatedBlock = new LocatedBlock(lbTag, worldObj);

                removedFluidBlocks.add(locatedBlock);
                tagIndex++;
            }
            tag.setTag("removedFluidCompounds", new NBTTagCompound());
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        tag = super.writeToNBT(tag);

        tag.setString("name", getInfo().getName());
        if (getInfo().getOwner() != null) {
            tag.setString("owner", getInfo().getOwner().toString());
        }

        tag.setInteger("meta", blockMetadata);
        tag.setString("name", getInfo().getName());
        if (getParentMovingWorld() != null && !getParentMovingWorld().isDead) {
            tag.setInteger("movingWorld", getParentMovingWorld().getEntityId());
        }
        if (assembleResult != null) {
            NBTTagCompound comp = new NBTTagCompound();
            assembleResult.writeNBTFully(comp);
            assembleResult.assemblyInteractor.writeNBTFully(comp);
            tag.setTag("res", comp);
            // Where the hell did this go in the transition to MovingWorld? Lost to the ether I suppose.
        }
        if (removedFluidBlocks != null && !removedFluidBlocks.isEmpty()) {
            NBTTagCompound removedFluidCompound = new NBTTagCompound();
            for (int i = 0; i < removedFluidBlocks.size(); i++) {
                LocatedBlock locatedBlock = removedFluidBlocks.get(i);
                NBTTagCompound lbTag = new NBTTagCompound();
                locatedBlock.writeToNBT(lbTag);

                removedFluidCompound.setTag("block#" + i, lbTag);
            }
            tag.setTag("removedFluidCompounds", removedFluidCompound);
        }

        return tag;
    }

    public void writeNBTForSending(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setInteger("meta", blockMetadata);
        tag.setString("name", getInfo().getName());

        if (getParentMovingWorld() != null && !getParentMovingWorld().isDead) {
            tag.setInteger("movingWorld", getParentMovingWorld().getEntityId());
        }

        if (assembleResult != null) {
            NBTTagCompound comp = new NBTTagCompound();
            assembleResult.writeNBTMetadata(comp);
            assembleResult.assemblyInteractor.writeNBTMetadata(comp);
            tag.setTag("res", comp);
        }
    }

    public enum MountStage {
        PREMSG, PRERIDE, POSTRIDE
    }

}
