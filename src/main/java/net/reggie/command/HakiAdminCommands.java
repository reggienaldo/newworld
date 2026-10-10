package net.reggie.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.reggie.NewWorld;
import net.reggie.game.system.haki_energy.HakiBalance;
import net.reggie.game.system.haki_energy.HakiEnergyComponent;
import net.reggie.game.system.haki_energy.HakiType;

public class HakiAdminCommands {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("newworld")
                    .requires(source -> source.hasPermissionLevel(2)) // Benötigt OP-Rechte (Level 2)

                    .then(CommandManager.literal("haki")
                            // --- /newworld haki xp <player> <buso/kenbun> <add/set> <anzahl> ---
                            .then(CommandManager.literal("xp")
                                    .then(CommandManager.argument("player", EntityArgumentType.player())
                                            .then(xpBranch(HakiType.BUSO))
                                            .then(xpBranch(HakiType.KENBUN))
                                    )
                            )

                            // --- /newworld haki level <player> <buso/kenbun> <level> ---
                            .then(CommandManager.literal("level")
                                    .then(CommandManager.argument("player", EntityArgumentType.player())
                                            .then(levelBranch(HakiType.BUSO))
                                            .then(levelBranch(HakiType.KENBUN))
                                    )
                            )

                            // --- /newworld haki reset <player> --- (setzt nur die XP zurück, nicht die Freischaltungen)
                            .then(CommandManager.literal("reset")
                                    .then(CommandManager.argument("player", EntityArgumentType.player())
                                            .executes(ctx -> resetXp(ctx, EntityArgumentType.getPlayer(ctx, "player")))
                                    )
                            )

                            // --- /newworld haki haoshoku <player> <unlock/lock/gene> ---
                            .then(CommandManager.literal("haoshoku")
                                    .then(CommandManager.argument("player", EntityArgumentType.player())
                                            .then(CommandManager.argument("action", StringArgumentType.string())
                                                    .suggests((ctx, builder) -> {
                                                        builder.suggest("unlock");
                                                        builder.suggest("lock");
                                                        builder.suggest("gene");
                                                        return builder.buildFuture();
                                                    })
                                                    .executes(ctx -> toggleHaoshoku(
                                                            ctx,
                                                            EntityArgumentType.getPlayer(ctx, "player"),
                                                            StringArgumentType.getString(ctx, "action")
                                                    ))
                                            )
                                    )
                            )
                    )
            );
        });
    }

    private static LiteralArgumentBuilder<ServerCommandSource> xpBranch(HakiType type) {
        return CommandManager.literal(type.id())
                .then(CommandManager.literal("add")
                        .then(CommandManager.argument("amount", LongArgumentType.longArg(1))
                                .executes(ctx -> modifyXp(ctx, EntityArgumentType.getPlayer(ctx, "player"), type, LongArgumentType.getLong(ctx, "amount"), true))
                        )
                )
                .then(CommandManager.literal("set")
                        .then(CommandManager.argument("amount", LongArgumentType.longArg(0))
                                .executes(ctx -> modifyXp(ctx, EntityArgumentType.getPlayer(ctx, "player"), type, LongArgumentType.getLong(ctx, "amount"), false))
                        )
                );
    }

    private static LiteralArgumentBuilder<ServerCommandSource> levelBranch(HakiType type) {
        return CommandManager.literal(type.id())
                .then(CommandManager.argument("level", IntegerArgumentType.integer(1))
                        .executes(ctx -> {
                            ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
                            int level = IntegerArgumentType.getInteger(ctx, "level");
                            return modifyXp(ctx, target, type, HakiBalance.totalXpForLevel(level), false);
                        })
                );
    }

    // Admin-XP: ohne Freischalt-Prüfung und ohne Rassen-Multiplikator
    private static int modifyXp(CommandContext<ServerCommandSource> context, ServerPlayerEntity target, HakiType type, long amount, boolean add) {
        HakiEnergyComponent haki = NewWorld.HAKI_ENERGY.get(target);

        long current = haki.getTotalXp(type);
        long newTotal = add ? (current > Long.MAX_VALUE - amount ? Long.MAX_VALUE : current + amount) : amount;
        haki.setTotalXp(type, newTotal);

        context.getSource().sendFeedback(() -> Text.translatable("haki.newworld.admin.xp_set",
                type.displayName(), target.getName(), haki.getTotalXp(type), haki.getLevel(type)), true);
        return 1;
    }

    private static int resetXp(CommandContext<ServerCommandSource> context, ServerPlayerEntity target) {
        HakiEnergyComponent haki = NewWorld.HAKI_ENERGY.get(target);
        for (HakiType type : HakiType.values()) {
            haki.setTotalXp(type, 0);
        }
        context.getSource().sendFeedback(() -> Text.translatable("haki.newworld.admin.reset", target.getName()), true);
        return 1;
    }

    // Logik für das Freischalten von Königshaki
    private static int toggleHaoshoku(CommandContext<ServerCommandSource> context, ServerPlayerEntity target, String action) {
        HakiEnergyComponent haki = NewWorld.HAKI_ENERGY.get(target);

        if (action.equalsIgnoreCase("unlock")) {
            // Wir schalten sowohl das geheime Potenzial als auch das Erwachen sofort frei!
            haki.setPotentialHaoshoku(true);
            haki.setHaoshokuAwakened(true);

            context.getSource().sendFeedback(() -> Text.translatable("haki.newworld.admin.haoshoku_unlocked", target.getName().getString()), true);
            target.sendMessage(Text.translatable("haki.newworld.admin.haoshoku_unlocked_target"));
        } else if (action.equalsIgnoreCase("lock")) {
            haki.setPotentialHaoshoku(false);
            haki.setHaoshokuAwakened(false);

            context.getSource().sendFeedback(() -> Text.translatable("haki.newworld.admin.haoshoku_locked", target.getName().getString()), true);
        } else if (action.equalsIgnoreCase("gene")) {
            // Gibt nur das Gen (Potenzial), ohne zu erwecken - Erwachen dann über Doriki oder Haki-XP
            haki.setPotentialHaoshoku(true);

            context.getSource().sendFeedback(() -> Text.translatable("haki.newworld.admin.haoshoku_gene", target.getName().getString()), true);
        } else {
            context.getSource().sendError(Text.translatable("haki.newworld.admin.invalid_action"));
            return 0;
        }
        return 1;
    }
}
