package com.dpeter99.lob.datagen;

import com.dpeter99.lob.LobMod;
import com.dpeter99.lob.registry.LobRegistries;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

/** Entry point for {@code ./gradlew runData}; output lands in {@code src/generated/resources}. */
@EventBusSubscriber(modid = LobMod.MOD_ID)
public final class LobDataGenerators {
    private LobDataGenerators() {
    }

    @SubscribeEvent
    static void gather(GatherDataEvent.Client event) {
        event.createDatapackRegistryObjects(new RegistrySetBuilder()
                .add(LobRegistries.ROOM, RoomBootstrap::rooms)
                .add(LobRegistries.LIBRARY, RoomBootstrap::libraries)
                .add(Registries.DIMENSION_TYPE, DimensionBootstrap::dimensionTypes)
                .add(Registries.BIOME, DimensionBootstrap::biomes)
                .add(Registries.LEVEL_STEM, DimensionBootstrap::levelStems));
        event.createProvider(LobModelProvider::new);
        event.createProvider(LobLanguageProvider::new);
        event.createProvider(LobRecipeProvider.Runner::new);
    }
}
