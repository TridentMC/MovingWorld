package darkevilmac.movingworld.block;

import darkevilmac.movingworld.chunk.AssembleResult;
import darkevilmac.movingworld.chunk.ChunkAssembler;
import darkevilmac.movingworld.chunk.MovingWorldAssemblyInteractor;
import darkevilmac.movingworld.entity.EntityMovingWorld;
import darkevilmac.movingworld.entity.IMovingWorldTileEntity;
import darkevilmac.movingworld.entity.MovingWorldInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.World;

import java.util.UUID;

public abstract class TileMovingWorldMarkingBlock extends TileEntity implements IMovingWorldTileEntity {

    private AssembleResult assembleResult, prevResult;

    public TileMovingWorldMarkingBlock() {
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
     *
     * @return
     */
    public abstract EntityMovingWorld getMovingWorld(World worldObj);

    public boolean assembleMovingWorld(EntityPlayer player) {
        boolean returnVal = false;

        if (!worldObj.isRemote) {
            prevResult = assembleResult;
            ChunkAssembler assembler = new ChunkAssembler(worldObj, xCoord, yCoord, zCoord, getMaxBlocks());
            MovingWorldAssemblyInteractor interactor = getNewAssemblyInteractor();
            assembleResult = assembler.doAssemble(interactor);

            assembledMovingWorld(player, returnVal);

            setInteractor(interactor);
            ChatComponentText c;
            switch (assembleResult.getCode()) {
                case AssembleResult.RESULT_OK:
                    c = new ChatComponentText("Assembled " + getInfo().getName() + "!");
                    player.addChatMessage(c);
                    break;
                case AssembleResult.RESULT_OK_WITH_WARNINGS:
                    returnVal = true;
                case AssembleResult.RESULT_BLOCK_OVERFLOW:
                    c = new ChatComponentText("Cannot create moving world with more than " + getMaxBlocks() + " blocks");
                    player.addChatMessage(c);
                    break;
                case AssembleResult.RESULT_MISSING_MARKER:
                    c = new ChatComponentText("Cannot create moving world with no moving world marker");
                    player.addChatMessage(c);
                    break;
                case AssembleResult.RESULT_ERROR_OCCURED:
                    c = new ChatComponentText("An error occured while assembling moving world. See console log for details.");
                    player.addChatMessage(c);
                    break;
                case AssembleResult.RESULT_NONE:
                    c = new ChatComponentText("Nothing was assembled");
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
                mountedMovingWorld(player, movingWorld, 1);
                if (assembleResult.getCode() == AssembleResult.RESULT_INCONSISTENT) {
                    return false;
                }
                if (assembleResult.getCode() == AssembleResult.RESULT_OK_WITH_WARNINGS) {
                    IChatComponent c = new ChatComponentText("Ship contains changes");
                    player.addChatMessage(c);
                }

                mountedMovingWorld(player, movingWorld, 2);

                EntityMovingWorld entity = assembleResult.getEntity(worldObj, getMovingWorld(worldObj));
                if (entity != null) {
                    System.out.println("Not Null");
                    entity.setInfo(getInfo());
                    if (worldObj.spawnEntityInWorld(entity)) {
                        System.out.println("Not Null spawned.");
                        //player.mountEntity(entity);
                        //entity.getCapabilities().mountEntity(entityplayer);
                        assembleResult = null;
                        return true;
                    }
                }
                mountedMovingWorld(player, movingWorld, 3);
            }
        }
        return false;
    }


    public void undoCompilation(EntityPlayer player) {
        assembleResult = prevResult;
        prevResult = null;
    }

    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound compound = new NBTTagCompound();
        writeNBTForSending(compound);
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, compound);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet) {
        readFromNBT(packet.func_148857_g());
    }

    public abstract MovingWorldAssemblyInteractor getNewAssemblyInteractor();

    /**
     * Called during/after mountMovingWorld();
     *
     * @param player
     * @param movingWorld
     * @param stage       can be 1, 2, or 3 this represents the stage of the method we're at.
     *                    more information can be viewed at the github repo to see when your code will be executed.
     *                    http://github.com/darkevilmac/MovingWorld
     */
    public void mountedMovingWorld(EntityPlayer player, EntityMovingWorld movingWorld, int stage) {
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);

        if (compound.hasKey("name"))
            System.out.println("Reading from NBT: " + compound.getString("name"));
        else
            System.out.println("No key found no need to log");


        getInfo().setName(compound.getString("name"));
        if (compound.hasKey("owner")) {
            getInfo().setOwner(UUID.fromString(compound.getString("owner")));
        }
        blockMetadata = compound.getInteger("meta");
        if (compound.hasKey("ship") && worldObj != null) {
            int id = compound.getInteger("ship");
            Entity entity = worldObj.getEntityByID(id);
            if (entity instanceof EntityMovingWorld) {
                setParentMovingWorld((EntityMovingWorld) entity);
            }
        }
        if (compound.hasKey("res")) {
            assembleResult = new AssembleResult(compound.getCompoundTag("res"), worldObj);
            assembleResult.assemblyInteractor = getNewAssemblyInteractor().fromNBT(compound.getCompoundTag("res"), worldObj);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);

        compound.setString("name", getInfo().getName());
        if (getInfo().getOwner() != null) {
            compound.setString("owner", getInfo().getOwner().toString());
        }

        compound.setInteger("meta", blockMetadata);
        compound.setString("name", getInfo().getName());
        if (getParentMovingWorld() != null && !getParentMovingWorld().isDead) {
            compound.setInteger("movingWorld", getParentMovingWorld().getEntityId());
        }
        if (assembleResult != null) {
            NBTTagCompound comp = new NBTTagCompound();
            assembleResult.writeNBTFully(comp);
            assembleResult.assemblyInteractor.writeNBTFully(comp);
            compound.setTag("res", comp);
        }
    }

    public void writeNBTForSending(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("meta", blockMetadata);
        compound.setString("name", getInfo().getName());

        if (getParentMovingWorld() != null && !getParentMovingWorld().isDead) {
            compound.setInteger("movingWorld", getParentMovingWorld().getEntityId());
        }

        if (assembleResult != null) {
            NBTTagCompound comp = new NBTTagCompound();
            assembleResult.writeNBTMetadata(comp);
            assembleResult.assemblyInteractor.writeNBTMetadata(comp);
            compound.setTag("res", comp);
        }
    }

}
