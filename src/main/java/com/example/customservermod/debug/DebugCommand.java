package com.example.customservermod.debug;

import com.example.customservermod.CustomServerMod;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.Permissions;

public class DebugCommand {
	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			register(dispatcher);
		});
	}

	private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("servercraft")
				.then(Commands.literal("debug")
						.requires(src -> src.permissions().hasPermission(Permissions.COMMANDS_OWNER))
						.executes(ctx -> {
							var server = ctx.getSource().getServer();
							String modVersion = FabricLoader.getInstance().getModContainer(CustomServerMod.MOD_ID)
									.map(c -> c.getMetadata().getVersion().getFriendlyString()).orElse("unknown");
							ctx.getSource().sendSuccess(() -> Component.literal("Â§6[ServerCraft Debug] Â§7Version: Â§a" + modVersion), false);
							ctx.getSource().sendSuccess(() -> Component.literal("Â§7Registry custom-server-mod:"), false);
							int count = 0;
							for (var entry : BuiltInRegistries.ITEM) {
								var key = BuiltInRegistries.ITEM.getKey(entry);
								if (key != null && key.getNamespace().equals(CustomServerMod.MOD_ID)) {
									ctx.getSource().sendSuccess(() -> Component.literal(" Â§7- item: Â§f" + key), false);
									count++;
								}
							}
							for (var entry : BuiltInRegistries.BLOCK) {
								var key = BuiltInRegistries.BLOCK.getKey(entry);
								if (key != null && key.getNamespace().equals(CustomServerMod.MOD_ID)) {
									ctx.getSource().sendSuccess(() -> Component.literal(" Â§7- block: Â§f" + key), false);
									count++;
								}
							}
							for (var entry : BuiltInRegistries.ITEM) {
								var key = BuiltInRegistries.ITEM.getKey(entry);
								if (key != null && key.getNamespace().equals(CustomServerMod.MOD_ID)) {
									// Already counted
								}
							}
							ctx.getSource().sendSuccess(() -> Component.literal("Â§7Total custom-server-mod entries: Â§a" + count), false);
							ctx.getSource().sendSuccess(() -> Component.literal("Â§7Server: Â§f" + server.getServerVersion() + " Â§7Mods: Â§f" + FabricLoader.getInstance().getAllMods().size()), false);
							return 1;
						}))
				.then(Commands.literal("version")
						.executes(ctx -> {
							String v = FabricLoader.getInstance().getModContainer(CustomServerMod.MOD_ID)
									.map(c -> c.getMetadata().getVersion().getFriendlyString()).orElse("unknown");
							ctx.getSource().sendSuccess(() -> Component.literal("Â§6[ServerCraft] Version: Â§a" + v), false);
							return 1;
						}))
		);
	}
}
