package com.dpeter99.lob.datagen;

import com.dpeter99.lob.registry.ModRegistries;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.item.Items;

import java.util.concurrent.CompletableFuture;

final class LobRecipeProvider extends RecipeProvider {
    private LobRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }

    @Override
    protected void buildRecipes() {
        shaped(RecipeCategory.TOOLS, ModRegistries.LIBRARIAN_KEY.get())
                .pattern(" E ")
                .pattern("GBG")
                .pattern(" G ")
                .define('E', Items.ENDER_EYE)
                .define('G', Items.GOLD_INGOT)
                .define('B', Items.BOOK)
                .unlockedBy("has_book", has(Items.BOOK))
                .save(output);
    }

    static final class Runner extends RecipeProvider.Runner {
        Runner(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
            super(output, registries);
        }

        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
            return new LobRecipeProvider(registries, output);
        }

        @Override
        public String getName() {
            return "Library of Babel recipes";
        }
    }
}
