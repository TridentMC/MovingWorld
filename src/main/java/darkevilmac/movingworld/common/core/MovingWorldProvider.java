package darkevilmac.movingworld.common.core;

import net.minecraft.world.WorldProvider;
import net.minecraft.world.chunk.IChunkProvider;

public class MovingWorldProvider extends WorldProvider {

    public static int PROVIDERID;

    @Override
    public String getDimensionName() {
        return "MovingWorld";
    }

    @Override
    public String getInternalNameSuffix() {
        return "_moving";
    }

    @Override
    public String getSaveFolder() {
        return parentProvider().getSaveFolder();
    }

    public IChunkProvider createChunkGenerator() {
        return new ChunkProviderMovingWorld(this.worldObj);
    }

    public WorldProvider parentProvider() {
        return movingWorld().parent().provider;
    }

    public IMovingWorld movingWorld() {
        return (IMovingWorld) worldObj;
    }
}
