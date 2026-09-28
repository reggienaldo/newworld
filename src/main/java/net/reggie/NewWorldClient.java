package net.reggie;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.reggie.event.KeyInputHandler;
import net.reggie.gui.RaceSelectionScreen;
import net.reggie.hud.HakiBarHud;
import net.reggie.network.ModNetworking;
import net.reggie.network.S2C.OpenRaceScreenPayload;

public class NewWorldClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {

        KeyInputHandler.register();

        ModNetworking.registerS2CPackets();

        HudRenderCallback.EVENT.register(new HakiBarHud());

        ClientPlayNetworking.registerGlobalReceiver(OpenRaceScreenPayload.ID, (payload, context) -> {
            // context.client() holt die Minecraft-Clientinstanz sicher auf dem Render-Thread
            context.client().execute(() -> {
                // Öffnet die GUI direkt auf dem Monitor des Spielers
                MinecraftClient.getInstance().setScreen(new RaceSelectionScreen());
            });
        });

    }
}
