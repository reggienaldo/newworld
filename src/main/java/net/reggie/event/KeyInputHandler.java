package net.reggie.event;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.reggie.network.C2S.CombatModeTogglePayload;
import org.lwjgl.glfw.GLFW;

public class KeyInputHandler {
    public static final String KEY_CATEGORY = "key.category.newworld.newworld";
    public static final String KEY_COMBAT_MODE = "key.newworld.combat_mode";
    public static final String KEY_ABILITY_INV = "key.newworld.ability_inv";
    public static final String KEY_SKILL_1 = "key.newworld.skillkey_1";
    public static final String KEY_SKILL_2 = "key.newworld.skillkey_2";
    public static final String KEY_SKILL_3 = "key.newworld.skillkey_3";
    public static final String KEY_SKILL_4 = "key.newworld.skillkey_4";

    private static KeyBinding combatModeKey;

    public static void registerKeyInputs() {

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (combatModeKey.wasPressed()) {
                // Schicke das Paket an den Server
                ClientPlayNetworking.send(new CombatModeTogglePayload());
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
        registerKeyInputs();
    }
}
