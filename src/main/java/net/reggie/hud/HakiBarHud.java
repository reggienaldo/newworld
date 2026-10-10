package net.reggie.hud;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;
import net.reggie.NewWorld;
import net.reggie.game.system.haki_energy.HakiEnergyComponent;

public class HakiBarHud implements HudRenderCallback {
    // Registriere die Pfade zu deinen Texturen
    private static final Identifier BAR_BACKGROUND = Identifier.of(NewWorld.MOD_ID, "textures/gui/haki_energy/bar_empty.png");
    private static final Identifier BAR_FOREGROUND = Identifier.of(NewWorld.MOD_ID, "textures/gui/haki_energy/haki_bar.png");

    // Maße deiner Texturen (Passe diese an die echten Pixel-Maße deiner Bilder an!)
    private static final int EMPTY_BAR_WIDTH = 61;
    private static final int EMPTY_BAR_HEIGHT = 10;

    private static final int BAR_WIDTH = 59;
    private static final int BAR_HEIGHT = 8;

    @Override
    public void onHudRender(DrawContext drawContext, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.hudHidden) return;

        // 1. Hole die Haki-Daten über das Interface vom Spieler
        HakiEnergyComponent comp = NewWorld.HAKI_ENERGY.get(client.player);

        // Wenn der Spieler NICHT im Kampfmodus ist, brechen wir hier sofort ab!
        boolean hasAnyHaki = comp.hasUnlockedBuso() || comp.hasUnlockedKenbun() || comp.hasAwakenedHaoshoku();

        // Das HUD wird nur gezeichnet, wenn der Kampfmodus AKTIV ist UND der Spieler Haki gelernt hat!
        if (!comp.isInCombatMode() || !hasAnyHaki) {
            return; // Bricht das Rendern komplett ab (Bildschirm bleibt absolut Vanilla-sauber)
        }

        int currentEnergy = comp.getEnergy();
        int maxEnergy = comp.getMaxEnergy();

        // Prozentuale Füllung berechnen (Sicherheits-Check gegen Division durch 0)
        float energyPercent = maxEnergy > 0 ? (float) currentEnergy / maxEnergy : 0.0f;
        int filledWidth = (int) (BAR_WIDTH * energyPercent);

        // 2. Position auf dem Bildschirm bestimmen (z.B. unten rechts über der Hotbar)
        int x = drawContext.getScaledWindowWidth() / 2 + 10; // 10 Pixel rechts von der Mitte
        int y = drawContext.getScaledWindowHeight() - 50;    // 50 Pixel vom unteren Rand nach oben

        // 3. Hintergrund zeichnen (Graue leere Bar)
        // drawTexture(Identifier, x, y, u, v, width, height, textureWidth, textureHeight)
        drawContext.drawTexture(BAR_BACKGROUND, x, y, 0, 0, EMPTY_BAR_WIDTH, EMPTY_BAR_HEIGHT, EMPTY_BAR_WIDTH, EMPTY_BAR_HEIGHT);

        // 4. Vordergrund zeichnen (Lila Füllung - wird basierend auf gefüllter Breite abgeschnitten)
        if (filledWidth > 0) {
            drawContext.drawTexture(BAR_FOREGROUND, x + 1, y + 1, 0, 0, filledWidth, BAR_HEIGHT, BAR_WIDTH, BAR_HEIGHT);
        }

        // Optional: Text mit genauer Zahl über oder neben die Bar schreiben
        String energyText = currentEnergy + " / " + maxEnergy;
        drawContext.drawText(client.textRenderer, energyText, x + 2, y - 10, 0xFFFFFF, true);
    }
}
