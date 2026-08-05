package com.wdcftgg.witherstormmod.common.world;

import com.wdcftgg.witherstormmod.common.init.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.IChunkGenerator;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class BowelsChunkGenerator implements IChunkGenerator {
    private final World world;

    public BowelsChunkGenerator(World world) {
        this.world = world;
    }

    @Override
    public Chunk generateChunk(int x, int z) {
        ChunkPrimer primer = new ChunkPrimer();
        Block flesh = ModBlocks.get("hardened_flesh_block");
        IBlockState fillState = flesh == null ? Blocks.OBSIDIAN.getDefaultState() : flesh.getDefaultState();
        for (int localX = 0; localX < 16; localX++) {
            for (int localZ = 0; localZ < 16; localZ++) {
                for (int y = 0; y < 256; y++) {
                    primer.setBlockState(localX, y, localZ, fillState);
                }
            }
        }
        Chunk chunk = new Chunk(world, primer, x, z);
        byte biome = (byte) Biome.getIdForBiome(world.provider.getBiomeForCoords(new BlockPos(x << 4, 64, z << 4)));
        byte[] biomes = chunk.getBiomeArray();
        for (int i = 0; i < biomes.length; i++) biomes[i] = biome;
        chunk.generateSkylightMap();
        return chunk;
    }

    @Override public void populate(int x, int z) { }
    @Override public boolean generateStructures(Chunk chunkIn, int x, int z) { return false; }
    @Override public List<Biome.SpawnListEntry> getPossibleCreatures(EnumCreatureType type, BlockPos pos) { return Collections.emptyList(); }
    @Nullable @Override public BlockPos getNearestStructurePos(World worldIn, String name, BlockPos pos, boolean unexplored) { return null; }
    @Override public void recreateStructures(Chunk chunkIn, int x, int z) { }
    @Override public boolean isInsideStructure(World worldIn, String name, BlockPos pos) { return false; }
}
