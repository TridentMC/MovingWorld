package darkevilmac.movingworld.common.core.world;

import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.chunk.IChunkGenerator;

public class MovingWorldProvider extends WorldProvider {

    public static DimensionType TYPE = DimensionType.register("MovingWorld", "_moving", MovingWorldProvider.PROVIDERID, MovingWorldProvider.class, false);
    public static int PROVIDERID;

    @Override
    public IChunkGenerator createChunkGenerator() {
        return new ChunkGeneratorMovingWorld(worldObj);
    }

    @Override
    public DimensionType getDimensionType() {
        return TYPE;
    }
}
