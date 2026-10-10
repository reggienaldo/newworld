package net.reggie.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.reggie.NewWorld;

public class ModBlocks {

    public static final Block STEEL_MINERAL = registerBlock("steel_mineral",
            new Block(AbstractBlock.Settings.create().strength(3f)
                    .requiresTool()));

    public static final Block SEA_STONE_MINERAL = registerBlock("sea_stone_mineral",
            new Block(AbstractBlock.Settings.create().strength(3f)
                    .requiresTool()));


    private static Block registerBlock(String name, Block block) {
        registerBlockItem(name, block);
        return Registry.register(Registries.BLOCK, Identifier.of(NewWorld.MOD_ID, name), block);
    }

    private static void registerBlockItem(String name, Block block) {
        Registry.register(Registries.ITEM, Identifier.of(NewWorld.MOD_ID, name),
                new BlockItem(block, new Item.Settings()));
    }

    public static void registerModBlocks() {
        NewWorld.LOGGER.info("Registering Mod Blocks for " + NewWorld.MOD_ID);

    }
}
