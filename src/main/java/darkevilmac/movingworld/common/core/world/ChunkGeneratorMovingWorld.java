package darkevilmac.movingworld.common.core.world;

import darkevilmac.movingworld.common.core.IMovingWorld;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.IChunkGenerator;

import java.util.List;

public class ChunkGeneratorMovingWorld implements IChunkGenerator {
    private final World worldObj;
    private final IMovingWorld movingWorld;

    public ChunkGeneratorMovingWorld(World worldIn) {
        this.worldObj = worldIn;
        this.movingWorld = (IMovingWorld) worldIn;
        //TODO: Make the game init WorldServerMulti as a MovingWorld when applicable.
    }

    @Override
    public Chunk provideChunk(int x, int z) {
        ChunkPrimer chunkprimer = new ChunkPrimer();
        Chunk chunk = new Chunk(this.worldObj, chunkprimer, x, z);
        chunk.generateSkylightMap();
        return chunk;
    }

    @Override
    public void populate(int x, int z) {
    }

    @Override
    public boolean generateStructures(Chunk chunkIn, int x, int z) {
        return false;
    }

    @Override
    public List<BiomeGenBase.SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos) {
        Vec3d translatedPos = movingWorld.translateToWorldSpace(pos);
        BlockPos translatedBlockPos = new BlockPos(translatedPos.xCoord, translatedPos.yCoord, translatedPos.zCoord);
        return this.movingWorld.parent().getBiomeGenForCoords(translatedBlockPos).getSpawnableList(creatureType);
    }

    @Override
    public BlockPos getStrongholdGen(World worldIn, String structureName, BlockPos position) {
        // N/A
        return null;
    }

    @Override
    public void recreateStructures(Chunk chunkIn, int x, int z) {
        // N/A
    }
}
