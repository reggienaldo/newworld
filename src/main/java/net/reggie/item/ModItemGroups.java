package net.reggie.item;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.reggie.NewWorld;

public class ModItemGroups {
    public static final ItemGroup INGRIDINETS_GROUP = Registry.register(Registries.ITEM_GROUP,
            Identifier.of(NewWorld.MOD_ID, "ingridients"),
            FabricItemGroup.builder().displayName(Text.translatable("itemgroup.ingridients"))
                    .icon(() -> new ItemStack(ModItems.STEEL_INGOT)).entries((displayContext, entries) -> {

                        entries.add(ModItems.STEEL_INGOT);
                        entries.add(ModItems.SEA_STONE);

                    }).build());

    public static final ItemGroup COMBAT_GROUP = Registry.register(Registries.ITEM_GROUP,
            Identifier.of(NewWorld.MOD_ID, "combat"),
            FabricItemGroup.builder().displayName(Text.translatable("itemgroup.combat"))
                    .icon(() -> new ItemStack(ModItems.ENMA)).entries((displayContext, entries) -> {

                        entries.add(ModItems.AXE);
                        entries.add(ModItems.ECLIPSE);
                        entries.add(ModItems.ENMA);
                        entries.add(ModItems.GRYPHON);
                        entries.add(ModItems.YORU);
                        entries.add(ModItems.SHUSUI);
                        entries.add(ModItems.WADO_ICHIMONJI);
                        entries.add(ModItems.SHODAI_KITETSU);
                        entries.add(ModItems.SANDAI_KITETSU);


                    }).build());

    public static void registerItemGroups() {
        NewWorld.LOGGER.info("Registering Item Groups for " + NewWorld.MOD_ID);
    }
}
