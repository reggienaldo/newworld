package net.reggie.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.reggie.NewWorld;
import net.reggie.game.system.race.PlayerRace;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerJumpMixin {

    @Inject(method = "jump", at = @At("HEAD"))
    private void injectLunarianSuperJump(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;

        // 1. Rasse über deine Cardinal Component abfragen
        PlayerRace race = NewWorld.STATS.get(player).getRace();

        // 2. Nur ausführen, wenn der Spieler ein LUNARIER ist und gleichzeitig SNEAKT
        if (player.isSneaking() && (race == PlayerRace.LUNARIAN || race == PlayerRace.SKYPIEAN)) {

            // Gibt den kräftigen Jump Boost Stufe 3 für den Absprung
            player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.JUMP_BOOST,
                    25,    // 1 Sekunden Dauer
                    5,     // Stufe 3
                    true,  // Ambient
                    false, // Keine Partikel
                    false  // Kein Icon
            ));
        }
    }
}
