package net.reggie.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.reggie.NewWorld;
import net.reggie.game.system.race.PlayerRace;
import net.reggie.network.C2S.SelectRacePayload;

import java.awt.*;

@Environment(EnvType.CLIENT)
public class RaceSelectionScreen extends Screen {
    private static final Identifier ARROW_LEFT = Identifier.of(NewWorld.MOD_ID, "textures/gui/race_selection/back_button.png");
    private static final Identifier ARROW_RIGHT = Identifier.of(NewWorld.MOD_ID, "textures/gui/race_selection/next_button.png");
    private static final Identifier SELECT_BUTTON_TEXTURE = Identifier.of(NewWorld.MOD_ID, "textures/gui/race_selection/select_button.png");
    private static final Identifier WANTED_BG_TEXTURE = Identifier.of(NewWorld.MOD_ID, "textures/gui/race_selection/botton.png");

    private static final int BG_WIDTH = 256;
    private static final int BG_HEIGHT = 256;

    private int currentIndex = 0;
    private final PlayerRace[] races = PlayerRace.values();

    private ButtonWidget selectButton;
    private ButtonWidget prevButton;
    private ButtonWidget nextButton;

    public RaceSelectionScreen() {
        super(Text.literal("Race Selection"));
    }

    @Override
    protected void init() {
        int x = (this.width - BG_WIDTH) / 2;
        int y = (this.height - BG_HEIGHT) / 2;

        // Linker Pfeil-Button (32x32)
        this.prevButton = ButtonWidget.builder(Text.literal(""), button -> {
            currentIndex = (currentIndex - 1 + races.length) % races.length;
        }).dimensions(x + 25, y + 210, 32, 32).build();
        this.addSelectableChild(this.prevButton);

        // Rechter Pfeil-Button (32x32)
        this.nextButton = ButtonWidget.builder(Text.literal(""), button -> {
            currentIndex = (currentIndex + 1) % races.length;
        }).dimensions(x + 75, y + 210, 32, 32).build();
        this.addSelectableChild(this.nextButton);

        // SELECT-Button (32x32)
        this.selectButton = ButtonWidget.builder(Text.literal(""), button -> {
            ClientPlayNetworking.send(new SelectRacePayload(races[currentIndex].name()));
            this.close();
        }).dimensions(x + 125, y + 210, 32, 32).build();
        this.addSelectableChild(this.selectButton);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);

        int x = (this.width - BG_WIDTH) / 2;
        int y = (this.height - BG_HEIGHT) / 2;

        PlayerRace currentRace = races[currentIndex];

        // DYNAMISCHE HINTERGRUND-TEXTUR ERZEUGEN
        // Lädt automatisch: human_interface.png, fishman_interface.png, giant_interface.png, etc.
        Identifier dynamicBgTexture = Identifier.of(NewWorld.MOD_ID, "textures/gui/race_selection/" + currentRace.name().toLowerCase() + "_interface.png");

        // 1. Foto-Hintergrund für den Steckbrief zeichnen
        context.drawTexture(WANTED_BG_TEXTURE, x + 148, y + 83, 0, 0, 80, 63, 80, 63);

        // 2. 3D SPIELERMODELL MIT SCHEREN-EFFEKT (AUSSCHNEIDEN) RENDERN
        if (this.client != null && this.client.player != null) {
            // Grenzen des Wanted-Fotofensters für die Schere definieren
            int clipX = x + 148;
            int clipY = y + 83;
            int clipWidth = 60;
            int clipHeight = 52;

            // Schneidet alle Pixel ab, die außerhalb dieses Rechtecks gezeichnet werden wollen
            context.enableScissor(clipX, clipY, clipX + clipWidth, clipY + clipHeight);

            // Begrenzungsbox für das Entity-Rendering
            int x1 = x + 148;
            int y1 = y + 83;
            int x2 = x + 208;
            int y2 = y + 135;

            int modelSize = 46;

            float lookX = (float)(x1 + x2) / 2.0F - mouseX;
            float lookY = (float)(y1 + y2) / 2.0F - mouseY;

            // Offizielle 9-Parameter Yarn-Methode für Version 1.21.1
            InventoryScreen.drawEntity(
                    context,
                    x1, y1, x2, y2,
                    modelSize,
                    0.0625F,
                    lookX,
                    lookY,
                    this.client.player
            );

            // Scheren-Effekt aufheben, um den Rest des Interfaces normal zu rendern
            context.disableScissor();
        }

        // 3. Haupt-Interface-Brett darüberzeichnen (Nutzt jetzt die dynamische Textur pro Rasse)
        context.drawTexture(dynamicBgTexture, x, y, 0, 0, BG_WIDTH, BG_HEIGHT, BG_WIDTH, BG_HEIGHT);

        // 4. Buttons rendern (32x32)
        context.drawTexture(ARROW_LEFT, x + 25, y + 210, 0, 0, 32, 32, 32, 32);
        context.drawTexture(ARROW_RIGHT, x + 75, y + 210, 0, 0, 32, 32, 32, 32);
        context.drawTexture(SELECT_BUTTON_TEXTURE, x + 125, y + 210, 0, 0, 64, 32, 64, 32);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldCloseOnEsc() { return false; }

    @Override
    public void renderBackground(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        drawContext.fill(0, 0, this.width, this.height, new Color(0, 0, 0, 40).getRGB());
    }

    @Override
    public boolean shouldPause() { return false; }
}
