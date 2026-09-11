package com.dpeter99.lob.datagen;

import com.dpeter99.lob.LobMod;
import com.dpeter99.lob.registry.ModRegistries;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.data.PackOutput;

/** Block states and item models. The portal block model is hand-written to set a translucent render type. */
final class LobModelProvider extends ModelProvider {
    LobModelProvider(PackOutput output) {
        super(output, LobMod.MOD_ID);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        blockModels.createNonTemplateModelBlock(ModRegistries.LIBRARY_PORTAL.get());
        itemModels.generateFlatItem(ModRegistries.LIBRARIAN_KEY.get(), ModelTemplates.FLAT_ITEM);
    }
}
