package darkevilmac.movingworld.common.core.world;

import darkevilmac.movingworld.common.core.IMovingWorld;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.DataFixesManager;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.world.chunk.storage.AnvilSaveHandler;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.SaveHandler;
import net.minecraft.world.storage.WorldInfo;

import java.io.File;
import java.io.FileInputStream;

public class MovingWorldSaveHandler extends AnvilSaveHandler {

    public ISaveHandler parentSaveHandler;
    public IMovingWorld movingWorld;

    public MovingWorldSaveHandler(ISaveHandler parentSaveHandler, Integer id) {
        super(new File(parentSaveHandler.getWorldDirectory(), "MovingWorld"), id.toString(), false, DataFixesManager.createFixer());
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

    @Override
    public WorldInfo loadWorldInfo() {
        File file1 = new File(this.getWorldDirectory(), "level.dat");

        if (file1.exists()) {
            WorldInfo worldinfo = loadAndFix(file1, this.dataFixer, this);

            if (worldinfo != null) {
                return worldinfo;
            }
        }

        net.minecraftforge.fml.common.FMLCommonHandler.instance().confirmBackupLevelDatUse(this);
        file1 = new File(this.getWorldDirectory(), "level.dat_old");
        return file1.exists() ? loadAndFix(file1, this.dataFixer, this) : null;
    }

    public WorldInfo loadAndFix(File file, DataFixer fixer, SaveHandler save) {
        try {
            NBTTagCompound nbt = CompressedStreamTools.readCompressed(new FileInputStream(file));
            MovingWorldInfo info = new MovingWorldInfo(fixer.process(FixTypes.LEVEL, nbt.getCompoundTag("Data")), parentSaveHandler.loadWorldInfo(), movingWorld);
            net.minecraftforge.fml.common.FMLCommonHandler.instance().handleWorldDataLoad(save, info, nbt);
            return info;
        } catch (Exception exception) {
            return null;
        }
    }
}
