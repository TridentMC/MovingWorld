package darkevilmac.movingworld.common.world;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.gen.structure.template.TemplateManager;
import net.minecraft.world.storage.IPlayerFileData;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;

import java.io.File;

/**
 * NO IMPLEMENTATION, WORK IN PROGRESS DO NOT EXPECT IN THE NEAR FUTURE FOR THIS TO BE COMPLETE, THE SAME APPLIES TO THE NEW MOBILECHUNK CLASS
 */
public class MobileWorld extends World {

    private World parent;

    public MobileWorld(World parent) {
        super(//region SaveHandler
                new ISaveHandler() {
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
                    public File getMapFileFromName(String mapName) {
                        return null;
                    }

                    @Override
                    public TemplateManager getStructureTemplateManager() {
                        return null;
                    }


                },
                //endregion
                parent.getWorldInfo(), parent.provider, parent.theProfiler, parent.isRemote);
    }

    @Override
    protected IChunkProvider createChunkProvider() {
        return null;
    }

    @Override
    protected boolean isChunkLoaded(int x, int z, boolean allowEmpty) {
        return false;
    }

}
