package com.elytradev.movingworld.common.experiments.world;

import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.IChunkGenerator;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * where am i?
 */
public class ChunkProviderEmpty implements IChunkGenerator {
    private World world;

    public ChunkProviderEmpty(World world) {
        this.world = world;
    }

    /**
     * Generates the chunk at the specified position, from scratch
     *
     * @param x
     * @param z
     */
    @Override
    public Chunk generateChunk(int x, int z) {
        Chunk chunk = new Chunk(world, x, z);
        chunk.generateSkylightMap();
        return chunk;
    }

    @Override
    public void populate(int x, int z) {
        // dont?
    }

    @Override
    public boolean generateStructures(Chunk chunkIn, int x, int z) {
        //pfft no.
        return false;
    }

    @Override
    public List<Biome.SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos) {
        // nothin
        return Collections.emptyList();
    }

    @Nullable
    @Override
    public BlockPos getNearestStructurePos(World worldIn, String structureName, BlockPos position, boolean findUnexplored) {
        // nah
        return null;
    }

    @Override
    public void recreateStructures(Chunk chunkIn, int x, int z) {
        // they were never here
    }

    @Override
    public boolean isInsideStructure(World worldIn, String structureName, BlockPos pos) {
        return false;
    }

}
