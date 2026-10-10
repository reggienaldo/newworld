package net.reggie.event;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.reggie.NewWorld;
import net.reggie.game.system.haki_energy.HakiEnergyComponent;
import net.reggie.network.C2S.CombatModeTogglePayload;
import net.reggie.network.C2S.HakiTogglePayload;
import org.lwjgl.glfw.GLFW;

public class KeyInputHandler {
    public static final String KEY_CATEGORY = "key.category.newworld.newworld";
    public static final String KEY_COMBAT_MODE = "key.newworld.combat_mode";
    public static final String KEY_ABILITY_INV = "key.newworld.ability_inv";
    public static final String KEY_SKILL_1 = "key.newworld.skillkey_1";
    public static final String KEY_SKILL_2 = "key.newworld.skillkey_2";
    public static final String KEY_SKILL_3 = "key.newworld.skillkey_3";
    public static final String KEY_SKILL_4 = "key.newworld.skillkey_4";

    public static final String KEY_BUSO = "key.newworld.buso_haki";
    public static final String KEY_KEN = "key.newworld.kenbun_haki";

    private static KeyBinding combatModeKey;
    private static KeyBinding busoKey;
    private static KeyBinding kenKey;

    public static void registerKeyInputs() {

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;
            HakiEnergyComponent haki = NewWorld.HAKI_ENERGY.get(client.player);

            while (combatModeKey.wasPressed()) {
                // Schicke das Paket an den Server
                ClientPlayNetworking.send(new CombatModeTogglePayload());
            }
        // =========================================================================
        // ACTION KEYS (Nur aktiv, wenn der Kampfmodus aktiv ist)
        // =========================================================================
        if (haki.isInCombatMode()) {
            // Rüstungshaki umschalten (Taste G) - Erlaubt im Kampfmodus
            while (busoKey.wasPressed()) {
                ClientPlayNetworking.send(new HakiTogglePayload("buso"));
            }

            // Observierungshaki umschalten (Taste V) - Erlaubt im Kampfmodus
            while (kenKey.wasPressed()) {
                ClientPlayNetworking.send(new HakiTogglePayload("ken"));
            }
        }
        // =========================================================================
        // DEPENDENCY CHECK: Wenn der Kampfmodus AUS ist und Haki gedrückt wird
        // =========================================================================
        else {
            // Spieler drückt G im Friedensmodus
            while (busoKey.wasPressed()) {
                client.player.sendMessage(Text.literal("§cDu musst zuerst den Kampfmodus (§eR§c) aktivieren, um Haki zu nutzen!"), true);
            }

            // Spieler drückt V im Friedensmodus
            while (kenKey.wasPressed()) {
                client.player.sendMessage(Text.literal("§cDu musst zuerst den Kampfmodus (§eR§c) aktivieren, um Haki zu nutzen!"), true);
            }
        }
        });
    }

    public static void register() {
        combatModeKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                KEY_COMBAT_MODE,
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R, // Beispielbelegung für den Combat Mode
                KEY_CATEGORY
        ));

        busoKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                KEY_BUSO, InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_G, KEY_CATEGORY
        ));

        kenKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                KEY_KEN, InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_V, KEY_CATEGORY
        ));
        registerKeyInputs();
    }
}
