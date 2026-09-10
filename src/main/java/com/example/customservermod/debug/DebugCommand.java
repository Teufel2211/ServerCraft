package com.example.customservermod.debug;

import com.example.customservermod.CustomServerMod;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
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
							ctx.getSource().sendSuccess(() -> Component.literal("\u00A76[ServerCraft Debug] \u00A77Version: \u00A7a" + modVersion), false);
							ctx.getSource().sendSuccess(() -> Component.literal("\u00A77Registry custom-server-mod:"), false);
							int count = 0;
							for (var entry : BuiltInRegistries.ITEM) {
								var key = BuiltInRegistries.ITEM.getKey(entry);
								if (key != null && key.getNamespace().equals(CustomServerMod.MOD_ID)) {
									ctx.getSource().sendSuccess(() -> Component.literal(" \u00A77- item: \u00A7f" + key), false);
									count++;
								}
							}
							for (var entry : BuiltInRegistries.BLOCK) {
								var key = BuiltInRegistries.BLOCK.getKey(entry);
								if (key != null && key.getNamespace().equals(CustomServerMod.MOD_ID)) {
									ctx.getSource().sendSuccess(() -> Component.literal(" \u00A77- block: \u00A7f" + key), false);
									count++;
								}
							}
							var enchantReg = ctx.getSource().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
							for (var holder : enchantReg.listElements().toList()) {
								var key = holder.key().identifier();
								if (key.getNamespace().equals(CustomServerMod.MOD_ID)) {
									ctx.getSource().sendSuccess(() -> Component.literal(" \u00A77- enchantment: \u00A7f" + key), false);
									count++;
								}
							}
							int finalCount = count;
							ctx.getSource().sendSuccess(() -> Component.literal("\u00A77Total custom-server-mod entries: \u00A7a" + finalCount + " \u00A77(expected 10: 5 items + 1 block + 4 enchants)"), false);
							ctx.getSource().sendSuccess(() -> Component.literal("\u00A77Server: \u00A7f" + server.getServerVersion() + " \u00A77Mods: \u00A7f" + FabricLoader.getInstance().getAllMods().size()), false);
							ctx.getSource().sendSuccess(() -> Component.literal("\u00A77Fabric Loader: \u00A7f" + FabricLoader.getInstance().getModContainer("fabricloader").map(c -> c.getMetadata().getVersion().getFriendlyString()).orElse("?")), false);
							return 1;
						}))
				.then(Commands.literal("version")
						.executes(ctx -> {
							String v = FabricLoader.getInstance().getModContainer(CustomServerMod.MOD_ID)
									.map(c -> c.getMetadata().getVersion().getFriendlyString()).orElse("unknown");
							ctx.getSource().sendSuccess(() -> Component.literal("\u00A76[ServerCraft] Version: \u00A7a" + v), false);
							return 1;
						})));
	}
}