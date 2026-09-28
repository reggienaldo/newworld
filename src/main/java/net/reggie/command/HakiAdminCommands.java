package net.reggie.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.reggie.NewWorld;
import net.reggie.game.system.haki_energy.HakiEnergyComponent;

public class HakiAdminCommands {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("newworld")
                    .requires(source -> source.hasPermissionLevel(2)) // Benötigt OP-Rechte (Level 2)

                    .then(CommandManager.literal("haki")
                            // --- UNTERBEFEHL: /newworld haki xp <player> <buso/kenbun> <anzahl> ---
                            .then(CommandManager.literal("xp")
                                    .then(CommandManager.argument("player", EntityArgumentType.player())
                                            .then(CommandManager.argument("hakiType", StringArgumentType.string())
                                                    .suggests((ctx, builder) -> {
                                                        builder.suggest("buso");
                                                        builder.suggest("kenbun");
                                                        return builder.buildFuture();
                                                    })
                                                    .then(CommandManager.argument("amount", IntegerArgumentType.integer(1))
                                                            .executes(ctx -> addHakiXp(
                                                                    ctx,
                                                                    EntityArgumentType.getPlayer(ctx, "player"),
                                                                    StringArgumentType.getString(ctx, "hakiType"),
                                                                    IntegerArgumentType.getInteger(ctx, "amount")
                                                            ))
                                                    )
                                            )
                                    )
                            )

                            // --- UNTERBEFEHL: /newworld haki haoshoku <player> <unlock/lock> ---
                            .then(CommandManager.literal("haoshoku")
                                    .then(CommandManager.argument("player", EntityArgumentType.player())
                                            .then(CommandManager.argument("action", StringArgumentType.string())
                                                    .suggests((ctx, builder) -> {
                                                        builder.suggest("unlock");
                                                        builder.suggest("lock");
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

    // Logik für das Hinzufügen von Haki-XP
    private static int addHakiXp(CommandContext<ServerCommandSource> context, ServerPlayerEntity target, String type, int amount) {
        HakiEnergyComponent haki = NewWorld.HAKI_ENERGY.get(target);

        if (type.equalsIgnoreCase("buso")) {
            haki.addBusoXp(amount);
            context.getSource().sendFeedback(() -> Text.literal("§a" + amount + " Busoshoku (Rüstung) XP an " + target.getName().getString() + " gegeben!"), true);
        } else if (type.equalsIgnoreCase("kenbun")) {
            haki.addKenbunXp(amount);
            context.getSource().sendFeedback(() -> Text.literal("§a" + amount + " Kenbunshoku (Observierung) XP an " + target.getName().getString() + " gegeben!"), true);
        } else {
            context.getSource().sendError(Text.literal("§cUngültiger Haki-Typ! Nutze 'buso' oder 'kenbun'."));
            return 0;
        }
        return 1;
    }

    // Logik für das Freischalten von Königshaki
    private static int toggleHaoshoku(CommandContext<ServerCommandSource> context, ServerPlayerEntity target, String action) {
        HakiEnergyComponent haki = NewWorld.HAKI_ENERGY.get(target);

        if (action.equalsIgnoreCase("unlock")) {
            // Wir schalten sowohl das geheime Potenzial als auch das Erwachen sofort frei!
            haki.setPotentialHaoshoku(true);
            haki.setHaoshokuAwakened(true);

            context.getSource().sendFeedback(() -> Text.literal("§dKönigshaki (Haoshoku) für " + target.getName().getString() + " erfolgreich ERWACHT!"), true);
            target.sendMessage(Text.literal("§d👑 Ein Operator hat dein Königshaki (Haoshoku) permanent freigeschaltet!"));
        } else if (action.equalsIgnoreCase("lock")) {
            haki.setPotentialHaoshoku(false);
            haki.setHaoshokuAwakened(false);

            context.getSource().sendFeedback(() -> Text.literal("§cKönigshaki für " + target.getName().getString() + " wieder gesperrt."), true);
        } else {
            context.getSource().sendError(Text.literal("§cUngültige Aktion! Nutze 'unlock' oder 'lock'."));
            return 0;
        }
        return 1;
    }
}