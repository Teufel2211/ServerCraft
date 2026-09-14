package com.example.customservermod.geyser;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GeyserCompatibility {
	private static final Logger LOGGER = LoggerFactory.getLogger("ServerCraft-Geyser");
	private static boolean geyserLoaded = false;
	private static boolean floodgateLoaded = false;

	public static void init() {
		geyserLoaded = FabricLoader.getInstance().isModLoaded("geyser");
		floodgateLoaded = FabricLoader.getInstance().isModLoaded("floodgate");
		if (geyserLoaded) {
			LOGGER.info("[ServerCraft] Geyser detected — enabling Bedrock compatibility for custom items/blocks");
		}
		if (floodgateLoaded) {
			LOGGER.info("[ServerCraft] Floodgate detected — Bedrock players will be handled");
		}
		if (!geyserLoaded && !floodgateLoaded) {
			LOGGER.info("[ServerCraft] Geyser/Floodgate not detected — running in Java-only mode");
		}
	}

	public static boolean isBedrockPlayer(ServerPlayer player) {
		if (!floodgateLoaded) return false;
		try {
			Class<?> floodgateApi = Class.forName("org.geysermc.floodgate.api.FloodgateApi");
			Object api = floodgateApi.getMethod("getInstance").invoke(null);
			Boolean isFloodgate = (Boolean) floodgateApi.getMethod("isFloodgatePlayer", java.util.UUID.class).invoke(api, player.getUUID());
			return isFloodgate != null && isFloodgate;
		} catch (Exception e) {
			return false;
		}
	}

	public static void onBedrockJoin(ServerPlayer player) {
		if (isBedrockPlayer(player)) {
			LOGGER.info("[ServerCraft] Bedrock player {} joined — custom items (pizza, etc.) will be mapped to vanilla for Bedrock via Geyser", player.getName().getString());
			player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§6[ServerCraft] §7Willkommen! Custom Items wie Pizza funktionieren auch auf Bedrock (via Geyser)."));
		}
	}
}