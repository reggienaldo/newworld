package net.reggie.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import net.reggie.NewWorld;
import net.reggie.game.system.haki_energy.HakiBalance;
import net.reggie.game.system.haki_energy.HakiEnergyComponent;
import net.reggie.game.system.haki_energy.HakiProgression;
import net.reggie.game.system.haki_energy.HakiSkill;
import net.reggie.game.system.haki_energy.HakiType;

import java.util.List;

// /haki - zeigt dem Spieler seinen Haki-Fortschritt (für alle Spieler, keine OP-Rechte nötig)
public class HakiCommand {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {

            // 1. STANDARD SPEILER-BEFEHL: /haki (Keine OP-Rechte nötig)
            dispatcher.register(CommandManager.literal("haki")
                    .executes(ctx -> showHaki(ctx, ctx.getSource().getPlayerOrThrow()))
            );

            // 2. ADMIN-BEFEHL HIERARCHIE: /newworld haki unlock <player> <buso/kenbun>
            dispatcher.register(CommandManager.literal("newworld")
                    .requires(source -> source.hasPermissionLevel(2)) // Benötigt OP-Rechte (Level 2)
                    .then(CommandManager.literal("haki")
                            .then(CommandManager.literal("unlock")
                                    .then(CommandManager.argument("targetPlayer", EntityArgumentType.player())
                                            .then(CommandManager.argument("hakiType", StringArgumentType.string())
                                                    .suggests((ctx, builder) -> {
                                                        builder.suggest("buso");
                                                        builder.suggest("kenbun");
                                                        return builder.buildFuture();
                                                    })
                                                    .executes(ctx -> unlockHaki(
                                                            ctx,
                                                            EntityArgumentType.getPlayer(ctx, "targetPlayer"),
                                                            StringArgumentType.getString(ctx, "hakiType")
                                                    ))
                                            )
                                    )
                            )
                    )
            );
        });
    }

    // --- REINE UNLOCK LOGIK (Ausschließlich für den Haki-Zustand, ohne Abilities) ---
    private static int unlockHaki(CommandContext<ServerCommandSource> context, ServerPlayerEntity target, String type) {
        HakiEnergyComponent haki = NewWorld.HAKI_ENERGY.get(target);
        if (haki == null) return 0;

        if (type.equalsIgnoreCase("buso")) {
            haki.setBusoUnlocked(true); // Schaltet NUR das Rüstungshaki frei
            context.getSource().sendFeedback(() -> Text.literal("§5⚔ Rüstungshaki (Busoshoku) permanent für " + target.getName().getString() + " freigeschaltet!"), true);
            target.sendMessage(Text.literal("§5⚔ Dein Rüstungshaki wurde von einem Administrator erwacht!"));
        } else if (type.equalsIgnoreCase("kenbun")) {
            haki.setKenbunUnlocked(true); // Schaltet NUR das Observierungshaki frei
            context.getSource().sendFeedback(() -> Text.literal("§e👁 Observierungshaki (Kenbunshoku) permanent für " + target.getName().getString() + " freigeschaltet!"), true);
            target.sendMessage(Text.literal("§e👁 Dein Observierungshaki wurde von einem Administrator erwacht!"));
        } else {
            context.getSource().sendError(Text.literal("§cUngültiger Haki-Typ! Nutze 'buso' oder 'kenbun'."));
            return 0;
        }
        return 1;
    }

    private static int showHaki(CommandContext<ServerCommandSource> context, ServerPlayerEntity player) {
        HakiEnergyComponent haki = NewWorld.HAKI_ENERGY.get(player);

        player.sendMessage(Text.translatable("haki.newworld.info.header").formatted(Formatting.GOLD, Formatting.BOLD));

        for (HakiType type : HakiType.values()) {
            // Gesperrte Haki-Arten sammeln keine XP - stattdessen zeigen wir, wie man sie freischaltet
            if (!haki.isUnlocked(type)) {
                Text hint = switch (type) {
                    case BUSO -> Text.translatable("haki.newworld.info.unlock_hint.buso", NewWorld.STATS.get(player).getDoriki());
                    case KENBUN -> Text.translatable("haki.newworld.info.unlock_hint.kenbun");
                };
                player.sendMessage(Text.translatable("haki.newworld.info.locked", type.displayName(), hint).formatted(Formatting.GRAY));
                continue;
            }
            int level = haki.getLevel(type);
            long total = haki.getTotalXp(type);
            long levelStart = HakiBalance.totalXpForLevel(level);
            long levelEnd = HakiBalance.totalXpForLevel(level + 1);
            player.sendMessage(Text.translatable("haki.newworld.info.level",
                    type.displayName(), level, total - levelStart, levelEnd - levelStart, total).formatted(Formatting.WHITE));
        }

        player.sendMessage(Text.translatable("haki.newworld.info.total", haki.getTotalHakiXp()).formatted(Formatting.WHITE));

        // Das Gen wird nicht verraten - nur ob Königshaki bereits erwacht ist
        String haoshokuKey = haki.hasAwakenedHaoshoku() ? "haki.newworld.info.haoshoku_awakened" : "haki.newworld.info.haoshoku_dormant";
        player.sendMessage(Text.translatable(haoshokuKey).formatted(Formatting.LIGHT_PURPLE));

        player.sendMessage(Text.translatable("haki.newworld.info.energy",
                haki.getEnergy(), haki.getMaxEnergy(), String.format("%.1f", haki.getRegenPerSecond())).formatted(Formatting.AQUA));

        List<Text> skills = HakiProgression.unlockedSkills(haki).stream().map(HakiSkill::displayName).toList();
        MutableText skillLine = skills.isEmpty()
                ? Text.translatable("haki.newworld.info.skills_none")
                : Text.translatable("haki.newworld.info.skills", Texts.join(skills, Text.literal(", ")));
        player.sendMessage(skillLine.formatted(Formatting.YELLOW));
        return 1;
    }
}
