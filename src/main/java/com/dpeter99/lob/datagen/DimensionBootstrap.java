package com.dpeter99.lob.datagen;

import com.dpeter99.lob.registry.LobDimensions;
import com.dpeter99.lob.registry.LobRegistries;
import com.dpeter99.lob.room.LibraryDefinition;
import com.dpeter99.lob.world.LibraryChunkGenerator;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.attribute.BedRule;
import net.minecraft.world.attribute.EnvironmentAttributeMap;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;

import java.util.Optional;

/** Datagen bootstraps for the library dimension type, biome and level stems. */
final class DimensionBootstrap {
    private DimensionBootstrap() {
    }

    private static final int FOG_COLOR = 0x0A0806;

    static void dimensionTypes(BootstrapContext<DimensionType> ctx) {
        HolderGetter<Block> blocks = ctx.lookup(Registries.BLOCK);
        ctx.register(LobDimensions.LIBRARY_TYPE, new DimensionType(
                true,   // fixed time: the library has no day
                false,  // no skylight
                true,   // has ceiling (bedrock-style behaviour for maps and weather)
                false,  // no dragon fight
                1.0,    // coordinate scale
                LobDimensions.MIN_Y,
                LobDimensions.HEIGHT,
                LobDimensions.HEIGHT,
                blocks.getOrThrow(BlockTags.INFINIBURN_OVERWORLD),
                0.35F,  // ambient light so unlit corridors are still readable
                new DimensionType.MonsterSettings(ConstantInt.of(0), 0),
                DimensionType.Skybox.NONE,
                CardinalLighting.Type.DEFAULT,
                EnvironmentAttributeMap.builder()
                        .set(EnvironmentAttributes.FOG_COLOR, FOG_COLOR)
                        .set(EnvironmentAttributes.SKY_COLOR, FOG_COLOR)
                        .set(EnvironmentAttributes.FOG_START_DISTANCE, 16.0F)
                        .set(EnvironmentAttributes.FOG_END_DISTANCE, 96.0F)
                        .set(EnvironmentAttributes.SKY_LIGHT_LEVEL, 0.0F)
                        .set(EnvironmentAttributes.SKY_LIGHT_FACTOR, 0.0F)
                        .set(EnvironmentAttributes.BED_RULE, BedRule.EXPLODES)
                        .set(EnvironmentAttributes.RESPAWN_ANCHOR_WORKS, false)
                        .set(EnvironmentAttributes.CAN_START_RAID, false)
                        .set(EnvironmentAttributes.CAN_PILLAGER_PATROL_SPAWN, false)
                        .set(EnvironmentAttributes.PIGLINS_ZOMBIFY, false)
                        .build(),
                HolderSet.empty(),
                Optional.empty()
        ));
    }

    static void biomes(BootstrapContext<Biome> ctx) {
        ctx.register(LobDimensions.LIBRARY_BIOME, new Biome.BiomeBuilder()
                .hasPrecipitation(false)
                .temperature(0.5F)
                .downfall(0.0F)
                .setAttribute(EnvironmentAttributes.SKY_COLOR, FOG_COLOR)
                .setAttribute(EnvironmentAttributes.FOG_COLOR, FOG_COLOR)
                .specialEffects(new BiomeSpecialEffects.Builder().waterColor(0x3F76E4).build())
                .mobSpawnSettings(MobSpawnSettings.EMPTY)
                .generationSettings(BiomeGenerationSettings.EMPTY)
                .build());
    }

    static void levelStems(BootstrapContext<LevelStem> ctx) {
        HolderGetter<DimensionType> types = ctx.lookup(Registries.DIMENSION_TYPE);
        HolderGetter<Biome> biomes = ctx.lookup(Registries.BIOME);
        HolderGetter<LibraryDefinition> libraries = ctx.lookup(LobRegistries.LIBRARY);
        ctx.register(LobDimensions.LIBRARY_ALPHA_STEM, new LevelStem(
                types.getOrThrow(LobDimensions.LIBRARY_TYPE),
                new LibraryChunkGenerator(biomes.getOrThrow(LobDimensions.LIBRARY_BIOME), libraries.getOrThrow(RoomBootstrap.LIBRARY_ALPHA))));
        ctx.register(LobDimensions.LIBRARY_BETA_STEM, new LevelStem(
                types.getOrThrow(LobDimensions.LIBRARY_TYPE),
                new LibraryChunkGenerator(biomes.getOrThrow(LobDimensions.LIBRARY_BIOME), libraries.getOrThrow(RoomBootstrap.LIBRARY_BETA))));
    }
}
