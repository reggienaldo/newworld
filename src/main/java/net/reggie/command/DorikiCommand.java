package net.reggie.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.reggie.NewWorld;
import net.reggie.game.system.stats.StatsComponent;

public class DorikiCommand {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("newworld")
                    .requires(source -> source.hasPermissionLevel(2)) // Benötigt OP-Rechte
                    .then(CommandManager.literal("doriki")
                            .then(CommandManager.argument("player", EntityArgumentType.player())
                                    // /newworld doriki <player> add <anzahl>
                                    .then(CommandManager.literal("add")
                                            .then(CommandManager.argument("amount", IntegerArgumentType.integer(1))
                                                    .executes(ctx -> modifyDoriki(ctx, EntityArgumentType.getPlayer(ctx, "player"), IntegerArgumentType.getInteger(ctx, "amount"), "add"))
                                            )
                                    )
                                    // /newworld doriki <player> remove <anzahl>
                                    .then(CommandManager.literal("remove")
                                            .then(CommandManager.argument("amount", IntegerArgumentType.integer(1))
                                                    .executes(ctx -> modifyDoriki(ctx, EntityArgumentType.getPlayer(ctx, "player"), IntegerArgumentType.getInteger(ctx, "amount"), "remove"))
                                            )
                                    )
                                    // /newworld doriki <player> set <anzahl>
                                    .then(CommandManager.literal("set")
                                            .then(CommandManager.argument("amount", IntegerArgumentType.integer(0))
                                                    .executes(ctx -> modifyDoriki(ctx, EntityArgumentType.getPlayer(ctx, "player"), IntegerArgumentType.getInteger(ctx, "amount"), "set"))
                                            )
                                    )
                            )
                    )
            );
        });
    }

    private static int modifyDoriki(CommandContext<ServerCommandSource> context, ServerPlayerEntity target, int amount, String operation) {
        StatsComponent stats = NewWorld.STATS.get(target);

        switch (operation) {
            case "add" -> stats.addDoriki(amount);
            case "remove" -> stats.addDoriki(-amount);
            case "set" -> {
                int current = stats.getDoriki();
                stats.addDoriki(amount - current); // Gleicht die Differenz aus
            }
        }

        context.getSource().sendFeedback(() -> Text.literal("§aDoriki von " + target.getName().getString() + " steht nun bei: " + stats.getDoriki()), true);
        return 1;
    }
}
