package com.example.customservermod.msgspy;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class MsgSpyCommand {
	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			registerCommand(dispatcher);
		});
	}

	private static void registerCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("msgspy")
				.requires(src -> src.hasPermission(4))
				.executes(ctx -> {
					ServerPlayer player = ctx.getSource().getPlayer();
					if (player == null) {
						ctx.getSource().sendFailure(Component.literal("Nur Spieler können diesen Befehl nutzen."));
						return 0;
					}
					boolean enabled = MsgSpyManager.toggleSpy(player);
					if (enabled) {
						ctx.getSource().sendSuccess(() -> Component.literal("§aMsgSpy aktiviert — du siehst jetzt alle /msg Nachrichten."), false);
					} else {
						ctx.getSource().sendSuccess(() -> Component.literal("§cMsgSpy deaktiviert."), false);
					}
					return 1;
				}));
		dispatcher.register(Commands.literal("socialspy")
				.requires(src -> src.hasPermission(4))
				.executes(ctx -> {
					ServerPlayer player = ctx.getSource().getPlayer();
					if (player == null) {
						ctx.getSource().sendFailure(Component.literal("Nur Spieler können diesen Befehl nutzen."));
						return 0;
					}
					boolean enabled = MsgSpyManager.toggleSpy(player);
					if (enabled) {
						ctx.getSource().sendSuccess(() -> Component.literal("§aSocialSpy aktiviert — du siehst jetzt alle /msg Nachrichten."), false);
					} else {
						ctx.getSource().sendSuccess(() -> Component.literal("§cSocialSpy deaktiviert."), false);
					}
					return 1;
				}));
	}
}