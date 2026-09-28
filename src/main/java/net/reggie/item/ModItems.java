package net.reggie.item;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.UnbreakableComponent;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;
import net.minecraft.item.SwordItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.reggie.NewWorld;
import net.reggie.item.custom.weapons.*;

public class ModItems {
    //Items
    public static final Item STEEL_INGOT = registerItem("steel_ingot", new Item(new Item.Settings()));

    public static final Item SEA_STONE = registerItem("sea_stone", new Item(new Item.Settings()));

    //Weapons
    public static final Item GRYPHON = registerItem("emperors_skyrend_saber",
            new GryphonSwordItem(ModToolMaterials.GREAT_GRADE, new Item.Settings()
                    .attributeModifiers(GryphonSwordItem.createAttributeModifiers(ModToolMaterials.GREAT_GRADE, 7, -2.4f))));

    public static final Item ENMA = registerItem("king_of_purgatory_katana",
            new EnmaSwordItem(ModToolMaterials.GREAT_GRADE, new Item.Settings()
                    .attributeModifiers(EnmaSwordItem.createAttributeModifiers(ModToolMaterials.GREAT_GRADE, 6, -2.4f))));

    public static final Item AXE = registerItem("axe",
            new AxeItem(ModToolMaterials.GREAT_GRADE, new Item.Settings()
                    .attributeModifiers(AxeItem.createAttributeModifiers(ModToolMaterials.GREAT_GRADE, 7, -2.4f))));

    public static final Item ECLIPSE = registerItem("eclipse",
            new EclipseSwordItem(ModToolMaterials.GREAT_GRADE, new Item.Settings()
                    .attributeModifiers(EclipseSwordItem.createAttributeModifiers(ModToolMaterials.SUPREME_GRADE, 7, -2.4f))));

    public static final Item YORU = registerItem("dark_sovereign_long_great_sword",
            new YoruSwordItem(ModToolMaterials.GREAT_GRADE, new Item.Settings().component(DataComponentTypes.UNBREAKABLE, new UnbreakableComponent(true))
                    .attributeModifiers(YoruSwordItem.createAttributeModifiers(ModToolMaterials.GREAT_GRADE, 8, -2.5f))
                    .component(DataComponentTypes.UNBREAKABLE, new UnbreakableComponent(true))));

    public static final Item SHUSUI = registerItem("shusui",
            new ShusuiSwordItem(ModToolMaterials.GREAT_GRADE, new Item.Settings()
                    .attributeModifiers(ShusuiSwordItem.createAttributeModifiers(ModToolMaterials.GREAT_GRADE, 6, -2.4f))
                    .component(DataComponentTypes.UNBREAKABLE, new UnbreakableComponent(true))));

    public static final Item WADO_ICHIMONJI = registerItem("wado_ichimonji",
            new WadoIchimonjiSwordItem(ModToolMaterials.GREAT_GRADE, new Item.Settings()
                    .attributeModifiers(WadoIchimonjiSwordItem.createAttributeModifiers(ModToolMaterials.GREAT_GRADE, 6, -2.4f))));

    public static final Item SHODAI_KITETSU = registerItem("shodai_kitetsu",
            new ShodaiSwordItem(ModToolMaterials.GREAT_GRADE, new Item.Settings()
                    .attributeModifiers(ShodaiSwordItem.createAttributeModifiers(ModToolMaterials.SUPREME_GRADE, 6, -2.4f))));

    public static final Item SANDAI_KITETSU = registerItem("sandai_kitetsu",
            new SandaiKitetsuSwordItem(ModToolMaterials.GREAT_GRADE, new Item.Settings()
                    .attributeModifiers(SandaiKitetsuSwordItem.createAttributeModifiers(ModToolMaterials.GRADE, 6, -2.4f))));

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, Identifier.of(NewWorld.MOD_ID, name), item);
    }
    public static void registerModItems() {
        NewWorld.LOGGER.info("Registering Mod Items for " + NewWorld.MOD_ID);
    }
}
