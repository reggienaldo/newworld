package net.reggie.item.custom.weapons;

import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class EclipseModel extends GeoModel<EclipseSwordItem> {
    @Override
    public Identifier getModelResource(EclipseSwordItem animatable) {
        // Pfad zu: assets/newworld/geo/item/eclipse.geo.json
        return Identifier.of("newworld", "geo/item/eclipse.geo.json");
    }

    @Override
    public Identifier getTextureResource(EclipseSwordItem animatable) {
        // Pfad zu: assets/newworld/textures/item/eclipse.png
        return Identifier.of("newworld", "textures/item/eclipse.png");
    }

    @Override
    public Identifier getAnimationResource(EclipseSwordItem animatable) {
        // Pfad zu deinen Animationen (falls du noch keine hast, lass es so stehen oder erstelle eine leere Datei)
        return Identifier.of("newworld", "animations/item/eclipse.animation.json");
    }
}
