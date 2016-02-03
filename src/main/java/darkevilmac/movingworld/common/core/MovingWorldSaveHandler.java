package darkevilmac.movingworld.common.core;

import net.minecraft.world.chunk.storage.AnvilSaveHandler;
import net.minecraft.world.storage.ISaveHandler;

import java.io.File;

public class MovingWorldSaveHandler extends AnvilSaveHandler {

    public ISaveHandler parentSaveHandler;
    public IMovingWorld movingWorld;

    public MovingWorldSaveHandler(ISaveHandler parentSaveHandler, Integer id) {
        super(new File(parentSaveHandler.getWorldDirectory(), "MovingWorld"), id.toString(), false);
        this.parentSaveHandler = parentSaveHandler;
    }

    @Override
    public File getMapFileFromName(String datFile) {
        File dataDir = parentSaveHandler.getMapFileFromName("substring");
        if (dataDir != null)
            dataDir = dataDir.getParentFile();

        if (dataDir == null) {
            dataDir = new File(new File(new File(movingWorld.parent().getSaveHandler().getWorldDirectory(), "MovingWorld"), movingWorld.id().toString()), "data");
            dataDir.mkdirs();
        }
        File file = new File(dataDir, datFile + ".dat");
        return file;
    }
}
