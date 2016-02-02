package darkevilmac.movingworld.common.core;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.storage.IPlayerFileData;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;

import java.io.File;

public class MovingWorldSaveHandler implements ISaveHandler {

    public ISaveHandler parentSaveHandler;
    public IMovingWorld movingWorld;

    public MovingWorldSaveHandler(ISaveHandler parentSaveHandler) {
        super();
        this.parentSaveHandler = parentSaveHandler;
    }

    @Override
    public WorldInfo loadWorldInfo() {
        return null;
    }

    @Override
    public void checkSessionLock() throws MinecraftException {

    }

    @Override
    public IChunkLoader getChunkLoader(WorldProvider provider) {
        return null;
    }

    @Override
    public void saveWorldInfoWithPlayer(WorldInfo worldInformation, NBTTagCompound tagCompound) {

    }

    @Override
    public void saveWorldInfo(WorldInfo worldInformation) {

    }

    @Override
    public IPlayerFileData getPlayerNBTManager() {
        return null;
    }

    @Override
    public void flush() {

    }

    @Override
    public File getWorldDirectory() {
        return null;
    }

    @Override
    public File getMapFileFromName(String datFile) {
        File dataDir = parentSaveHandler.getMapFileFromName("substring");
        if (dataDir != null)
            dataDir = dataDir.getParentFile();

        if (dataDir == null) {
            dataDir = new File(new File(new File(movingWorld.parent().getSaveHandler().getWorldDirectory(), "MovingWorld"), movingWorld.identifier().toString()), "data");
            dataDir.mkdirs();
        }
        File file = new File(dataDir, datFile + ".dat");
        return file;
    }

    @Override
    public String getWorldDirectoryName() {
        return null;
    }
}
