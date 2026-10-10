package net.reggie.gen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.item.ItemConvertible;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryWrapper;
import net.reggie.block.ModBlocks;
import net.reggie.item.ModItems;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends FabricRecipeProvider {
    public ModRecipeProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    public void generate(RecipeExporter exporter) {
        List<ItemConvertible> SEA_STONE_SMELTABLES = List.of(ModItems.RAW_SEA_STONE, ModBlocks.SEA_STONE_MINERAL);
        List<ItemConvertible> STEEL_SMELTABLES = List.of(ModItems.RAW_STEEL, ModBlocks.STEEL_MINERAL);

        offerSmelting(exporter, SEA_STONE_SMELTABLES, RecipeCategory.MISC, ModItems.SEA_STONE, 0.25f, 200, "sea_stone");
        offerBlasting(exporter, SEA_STONE_SMELTABLES, RecipeCategory.MISC, ModItems.SEA_STONE, 0.25f, 100, "sea_stone");

        offerSmelting(exporter, STEEL_SMELTABLES, RecipeCategory.MISC, ModItems.STEEL_INGOT, 0.25f, 200, "steel");
        offerBlasting(exporter, STEEL_SMELTABLES, RecipeCategory.MISC, ModItems.STEEL_INGOT, 0.25f, 100, "steel");
    }
}
