package net.reggie.game.system.cooldown;

import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.reggie.NewWorld;
import net.reggie.game.system.ability.compositions.AbilityComponent;
import net.reggie.game.system.haki_energy.HakiEnergyComponent;

import java.util.List;

public class CooldownComponent implements AbilityComponent {
    private final String abilityName;
    private final int cooldownTicks;

    public CooldownComponent(String abilityName, int cooldownTicks) {
        this.abilityName = abilityName;
        this.cooldownTicks = cooldownTicks;
    }

    @Override
    public boolean execute(ServerWorld world, ServerPlayerEntity user, Vec3d targetPos, List<Entity> targets) {
        HakiEnergyComponent comp = NewWorld.HAKI_ENERGY.get(user);

        // Prüfen, ob die Fähigkeit bereit ist
        if (!comp.isReady(abilityName)) {
            user.sendMessage(Text.literal("Diese Fähigkeit hat noch Cooldown!"), true);
            return false; // Kette abbrechen!
        }

        // Wenn bereit, setze den Cooldown für das nächste Mal
        comp.setCooldown(abilityName, cooldownTicks);
        return true; // Kette darf weiterlaufen
    }
}
