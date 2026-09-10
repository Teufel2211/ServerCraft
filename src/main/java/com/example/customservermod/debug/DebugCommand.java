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
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;

public class DebugCommand {
	public static void register() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> registerClient(dispatcher));
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
							int finalCount = count;
							ctx.getSource().sendSuccess(() -> Component.literal("\u00A77Total custom-server-mod entries: \u00A7a" + finalCount), false);
							ctx.getSource().sendSuccess(() -> Component.literal("\u00A77Server: \u00A7f" + server.getServerVersion() + " \u00A77Mods: \u00A7f" + FabricLoader.getInstance().getAllMods().size()), false);
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

	private static void registerClient\(com.mojang.brigadier.CommandDispatcher<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource> dispatcher\) \{
		dispatcher.register\(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal\("servercraft"\)
				\.then\(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal\("debug"\)
						\.executes\(ctx -> \{
							String modVersion = net.fabricmc.loader.api.FabricLoader.getInstance\(\).getModContainer\(CustomServerMod.MOD_ID\).map\(c -> c.getMetadata\(\).getVersion\(\).getFriendlyString\(\)\).orElse\("unknown"\)\;
							ctx.getSource\(\).sendFeedback\(net.minecraft.network.chat.Component.literal\("\u00A76\[ServerCraft Debug Client\] \u00A77Version: \u00A7a" \+ modVersion\)\)\;
							return 1\;
						\}\)\)
				\.then\(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal\("version"\)
						\.executes\(ctx -> \{
							String v = net.fabricmc.loader.api.FabricLoader.getInstance\(\).getModContainer\(CustomServerMod.MOD_ID\).map\(c -> c.getMetadata\(\).getVersion\(\).getFriendlyString\(\)\).orElse\("unknown"\)\;
							ctx.getSource\(\).sendFeedback\(net.minecraft.network.chat.Component.literal\("\u00A76\[ServerCraft\] Version: \u00A7a" \+ v\)\)\;
							return 1\;
						\}\)\)\)\;
		\}
}