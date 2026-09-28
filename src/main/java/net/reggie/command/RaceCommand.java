package net.reggie.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.reggie.NewWorld;
import net.reggie.game.system.race.PlayerRace;
import net.reggie.game.system.race.RacePassiveHandler;
import net.reggie.game.system.stats.StatsComponent;

public class RaceCommand {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("newworld")
                    .requires(source -> source.hasPermissionLevel(2)) // Benötigt OP-Rechte
                    .then(CommandManager.literal("race")
                            .then(CommandManager.argument("player", EntityArgumentType.player())
                                    .then(CommandManager.literal("set")
                                            // Erzeugt Autovervollständigung für deine Rassen aus dem Enum
                                            .then(CommandManager.argument("raceName", StringArgumentType.string())
                                                    .suggests((ctx, builder) -> {
                                                        for (PlayerRace race : PlayerRace.values()) {
                                                            builder.suggest(race.name());
                                                        }
                                                        return builder.buildFuture();
                                                    })
                                                    .executes(ctx -> setRace(ctx, EntityArgumentType.getPlayer(ctx, "player"), StringArgumentType.getString(ctx, "raceName")))
                                            )
                                    )
                            )
                    )
            );
        });
    }

    private static int setRace(CommandContext<ServerCommandSource> context, ServerPlayerEntity target, String raceName) {
        try {
            PlayerRace selectedRace = PlayerRace.valueOf(raceName.toUpperCase());
            StatsComponent stats = NewWorld.STATS.get(target);

            stats.setRace(selectedRace);
            RacePassiveHandler.applyRaceAttributes(target);

            context.getSource().sendFeedback(() -> Text.literal("§aRasse von " + target.getName().getString() + " erfolgreich auf §6" + selectedRace.name() + " §ageändert!"), true);
            target.sendMessage(Text.literal("§aDeine Rasse wurde auf §6" + selectedRace.name() + " §ageändert!"));

        } catch (IllegalArgumentException e) {
            context.getSource().sendError(Text.literal("§cUngültiger Rassen-Name! Bitte nutze die Tab-Vervollständigung."));
            return 0;
        }
        return 1;
    }
}
