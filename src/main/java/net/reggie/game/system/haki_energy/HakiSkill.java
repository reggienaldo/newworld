package net.reggie.game.system.haki_energy;

import net.minecraft.text.Text;

// Freischalt-Stufen für spätere Haki-Fähigkeiten (Fähigkeiten-Inventar).
// Die Fähigkeiten selbst existieren noch nicht - hier wird nur festgelegt, AB WANN sie erlaubt sind.
// Abfrage: HakiProgression.isUnlocked(player, HakiSkill.BUSO_HARDENING)
public enum HakiSkill {
    BUSO_HARDENING("buso_hardening", HakiType.BUSO, 5, false),
    BUSO_SHOCKWAVE("buso_shockwave", HakiType.BUSO, 15, false),
    KENBUN_DODGE("kenbun_dodge", HakiType.KENBUN, 5, false),
    KENBUN_XRAY("kenbun_xray", HakiType.KENBUN, 15, false),
    HAOSHOKU_KNOCKOUT("haoshoku_knockout", null, 0, true),
    HAOSHOKU_DAMAGE_BUFF("haoshoku_damage_buff", HakiType.BUSO, 20, true);

    private final String id;
    private final HakiType type;       // null = keine Level-Anforderung
    private final int requiredLevel;
    private final boolean requiresHaoshoku;

    HakiSkill(String id, HakiType type, int requiredLevel, boolean requiresHaoshoku) {
        this.id = id;
        this.type = type;
        this.requiredLevel = requiredLevel;
        this.requiresHaoshoku = requiresHaoshoku;
    }

    public boolean isUnlocked(HakiEnergyComponent haki) {
        if (this.requiresHaoshoku && !haki.hasAwakenedHaoshoku()) return false;
        if (this.type == null) return true;
        return haki.isUnlocked(this.type) && haki.getLevel(this.type) >= this.requiredLevel;
    }

    public Text displayName() {
        return Text.translatable("haki.newworld.skill." + this.id);
    }
}
