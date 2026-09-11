package com.dpeter99.lob.registry;

import com.dpeter99.lob.LobMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;

/** Resource keys for the library dimensions and their shared type and biome. */
public final class LobDimensions {
    private LobDimensions() {
    }

    /** Lowest block Y of every library dimension. */
    public static final int MIN_Y = 0;
    /** Block height of every library dimension; must be a multiple of 16. */
    public static final int HEIGHT = 256;

    public static final ResourceKey<DimensionType> LIBRARY_TYPE = ResourceKey.create(Registries.DIMENSION_TYPE, LobMod.id("library"));
    public static final ResourceKey<Biome> LIBRARY_BIOME = ResourceKey.create(Registries.BIOME, LobMod.id("library"));

    public static final ResourceKey<Level> LIBRARY_ALPHA = ResourceKey.create(Registries.DIMENSION, LobMod.id("library_alpha"));
    public static final ResourceKey<LevelStem> LIBRARY_ALPHA_STEM = ResourceKey.create(Registries.LEVEL_STEM, LobMod.id("library_alpha"));

    public static final ResourceKey<Level> LIBRARY_BETA = ResourceKey.create(Registries.DIMENSION, LobMod.id("library_beta"));
    public static final ResourceKey<LevelStem> LIBRARY_BETA_STEM = ResourceKey.create(Registries.LEVEL_STEM, LobMod.id("library_beta"));
}
