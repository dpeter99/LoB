package com.dpeter99.lob.world;

import com.dpeter99.lob.registry.LobDimensions;
import com.dpeter99.lob.room.LibraryDefinition;
import com.dpeter99.lob.room.RoomResolver;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Generates an endless library: every 16x16x16 cell of the world is filled with a room template.
 * Cells are stamped in {@link #buildSurface} because the {@link WorldGenRegion} there exposes the
 * world seed and the structure template manager, and is a server level accessor templates can be
 * placed into. Writes are clipped to the chunk being generated so every chunk is independent.
 */
public class LibraryChunkGenerator extends ChunkGenerator {
    public static final MapCodec<LibraryChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Biome.CODEC.fieldOf("biome").forGetter(g -> g.biome),
            LibraryDefinition.HOLDER_CODEC.fieldOf("library").forGetter(g -> g.library)
    ).apply(i, LibraryChunkGenerator::new));

    private static final long SALT_PLACE = 0x4C4F425F504C4143L; // "LOB_PLAC"

    private final Holder<Biome> biome;
    private final Holder<LibraryDefinition> library;
    private final TemplateStamper stamper = new TemplateStamper();
    private volatile RoomResolver resolver;

    public LibraryChunkGenerator(Holder<Biome> biome, Holder<LibraryDefinition> library) {
        super(new FixedBiomeSource(biome));
        this.biome = biome;
        this.library = library;
    }

    public Holder<LibraryDefinition> library() {
        return library;
    }

    /** The resolver is built lazily because the library holder is only bound after registry load. */
    public RoomResolver resolver() {
        RoomResolver r = resolver;
        if (r == null) {
            synchronized (this) {
                r = resolver;
                if (r == null) {
                    resolver = r = library.value().createResolver();
                }
            }
        }
        return r;
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Blender blender, RandomState randomState, StructureManager structureManager, ChunkAccess chunk) {
        return CompletableFuture.completedFuture(chunk);
    }

    @Override
    public void buildSurface(WorldGenRegion region, StructureManager structureManager, RandomState randomState, ChunkAccess chunk) {
        ChunkPos chunkPos = chunk.getPos();
        LevelHeightAccessor height = chunk.getHeightAccessorForGeneration();
        BoundingBox clip = new BoundingBox(chunkPos.getMinBlockX(), height.getMinY(), chunkPos.getMinBlockZ(),
                chunkPos.getMaxBlockX(), height.getMaxY(), chunkPos.getMaxBlockZ());
        int firstCell = Math.floorDiv(height.getMinY(), CellPos.SIZE);
        int lastCell = Math.floorDiv(height.getMinY() + height.getHeight() - 1, CellPos.SIZE);
        long seed = region.getSeed();
        RoomResolver resolver = resolver();
        for (int cellY = firstCell; cellY <= lastCell; cellY++) {
            CellPos cell = CellPos.of(chunkPos, cellY);
            RoomResolver.Placement placement = resolver.resolve(seed, cell);
            // Large rooms are stamped once per covering cell, each time clipped to this chunk, so the
            // template origin is the room origin rather than the cell origin.
            stamper.stamp(region, placement.room().template(), placement.origin().origin(),
                    placement.room().templateSize(), clip, CellHash.random(seed, placement.origin(), SALT_PLACE));
        }
    }

    @Override
    public void applyCarvers(WorldGenRegion region, long seed, RandomState randomState, BiomeManager biomeManager, StructureManager structureManager, ChunkAccess chunk) {
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion region) {
    }

    @Override
    public int getGenDepth() {
        return LobDimensions.HEIGHT;
    }

    @Override
    public int getSeaLevel() {
        return LobDimensions.MIN_Y;
    }

    @Override
    public int getMinY() {
        return LobDimensions.MIN_Y;
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor heightAccessor, RandomState randomState) {
        return heightAccessor.getMinY() + 1;
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor heightAccessor, RandomState randomState) {
        return new NoiseColumn(heightAccessor.getMinY(), new BlockState[]{Blocks.STONE.defaultBlockState()});
    }

    @Override
    public void addDebugScreenInfo(List<String> result, RandomState randomState, BlockPos feetPos) {
        CellPos cell = CellPos.fromBlock(feetPos);
        result.add("Library cell: " + cell.x() + " " + cell.y() + " " + cell.z());
        RoomResolver r = resolver;
        if (r != null) {
            result.add("Library room: " + r.resolve(0L, cell).room().template() + " (seed-independent preview)");
        }
    }
}
