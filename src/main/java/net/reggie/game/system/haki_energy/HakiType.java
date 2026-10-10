package net.reggie.game.system.haki_energy;

import net.minecraft.text.Text;

// Haki-Arten, die über Haki-XP leveln. Königshaki (Haoshoku) levelt nicht, es wird nur erweckt.
public enum HakiType {
    BUSO("buso", "BusoXpTotal"),
    KENBUN("kenbun", "KenbunXpTotal");

    private final String id;
    private final String nbtKey;

    HakiType(String id, String nbtKey) {
        this.id = id;
        this.nbtKey = nbtKey;
    }

    public String id() { return this.id; }

    public String nbtKey() { return this.nbtKey; }

    public Text displayName() {
        return Text.translatable("haki.newworld.type." + this.id);
    }
}
