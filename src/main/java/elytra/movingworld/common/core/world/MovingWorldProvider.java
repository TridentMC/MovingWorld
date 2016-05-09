package elytra.movingworld.common.core.world;

import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.chunk.IChunkGenerator;

public class MovingWorldProvider extends WorldProvider {

    public static int PROVIDERID;
    public static DimensionType TYPE = DimensionType.register("MovingWorld", "_moving", MovingWorldProvider.PROVIDERID, MovingWorldProvider.class, false);

    @Override
    public IChunkGenerator createChunkGenerator() {
        return new ChunkGeneratorMovingWorld(worldObj);
    }

    @Override
    public DimensionType getDimensionType() {
        return TYPE;
    }
}
