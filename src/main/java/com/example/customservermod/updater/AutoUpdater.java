package com.example.customservermod.updater;

import com.example.customservermod.CustomServerMod;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.CompletableFuture;

public class AutoUpdater {
	private static final Logger LOGGER = LoggerFactory.getLogger("ServerCraft-Updater");
	private static final String REPO = "Teufel2211/ServerCraft";
	private static final String API_LATEST = "https://api.github.com/repos/" + REPO + "/releases/latest";

	public static void checkOnStartup(MinecraftServer server) {
		String current = FabricLoader.getInstance().getModContainer(CustomServerMod.MOD_ID)
				.map(c -> c.getMetadata().getVersion().getFriendlyString()).orElse("0.0.0");
		LOGGER.info("[ServerCraft] AutoUpdater: current version {}, checking {}...", current, API_LATEST);
		CompletableFuture.runAsync(() -> {
			try {
				HttpClient client = HttpClient.newHttpClient();
				HttpRequest req = HttpRequest.newBuilder(URI.create(API_LATEST))
						.header("Accept", "application/vnd.github+json")
						.header("User-Agent", "ServerCraft-Updater")
						.GET().build();
				HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
				if (resp.statusCode() != 200) {
					LOGGER.warn("[ServerCraft] Update check failed: HTTP {}", resp.statusCode());
					return;
				}
				JsonObject json = JsonParser.parseString(resp.body()).getAsJsonObject();
				String latestTag = json.get("tag_name").getAsString().replaceFirst("^v", "");
				String htmlUrl = json.get("html_url").getAsString();
				if (isNewer(latestTag, current)) {
					LOGGER.info("[ServerCraft] Update available: {} -> {} ({})", current, latestTag, htmlUrl);
					broadcastToOps(server, Component.literal("§6[ServerCraft] §aUpdate verfügbar: §e" + current + " §7-> §a" + latestTag + " §7" + htmlUrl));
					// Auto-download jar if not already present
					JsonArray assets = json.getAsJsonArray("assets");
					if (assets != null && assets.size() > 0) {
						String downloadUrl = assets.get(0).getAsJsonObject().get("browser_download_url").getAsString();
						String fileName = assets.get(0).getAsJsonObject().get("name").getAsString();
						downloadUpdate(server, downloadUrl, fileName, latestTag);
					}
				} else {
					LOGGER.info("[ServerCraft] Up to date ({}).", current);
				}
			} catch (Exception e) {
				LOGGER.warn("[ServerCraft] Update check error: {}", e.getMessage());
			}
		});
	}

	private static void downloadUpdate(MinecraftServer server, String url, String fileName, String version) {
		try {
			Path modsDir = FabricLoader.getInstance().getGameDir().resolve("mods");
			Path updateDir = modsDir.resolve("update");
			Files.createDirectories(updateDir);
			Path dest = updateDir.resolve(fileName);
			if (Files.exists(dest)) {
				LOGGER.info("[ServerCraft] Update already downloaded: {}", dest);
				return;
			}
			LOGGER.info("[ServerCraft] Downloading update {} -> {}", url, dest);
			HttpClient client = HttpClient.newHttpClient();
			HttpRequest req = HttpRequest.newBuilder(URI.create(url))
					.header("User-Agent", "ServerCraft-Updater")
					.GET().build();
			HttpResponse<InputStream> resp = client.send(req, HttpResponse.BodyHandlers.ofInputStream());
			if (resp.statusCode() == 200) {
				Files.copy(resp.body(), dest, StandardCopyOption.REPLACE_EXISTING);
				LOGGER.info("[ServerCraft] Update downloaded to {} — wird beim nächsten Neustart aktiv.", dest);
				broadcastToOps(server, Component.literal("§6[ServerCraft] §aUpdate heruntergeladen: §e" + fileName + " §7— Neustart zum Aktivieren."));
			} else {
				LOGGER.warn("[ServerCraft] Download failed: HTTP {}", resp.statusCode());
			}
		} catch (Exception e) {
			LOGGER.warn("[ServerCraft] Download error: {}", e.getMessage());
		}
	}

	private static void broadcastToOps(MinecraftServer server, Component msg) {
		for (ServerPlayer p : server.getPlayerList().getPlayers()) {
			if (p.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_OWNER)) {
				p.sendSystemMessage(msg, false);
			}
		}
		server.sendSystemMessage(msg);
	}

	private static boolean isNewer(String latest, String current) {
		try {
			String[] l = latest.split("\\.");
			String[] c = current.split("\\.");
			for (int i = 0; i < Math.max(l.length, c.length); i++) {
				int lv = i < l.length ? Integer.parseInt(l[i]) : 0;
				int cv = i < c.length ? Integer.parseInt(c[i]) : 0;
				if (lv > cv) return true;
				if (lv < cv) return false;
			}
		} catch (Exception ignored) {}
		return false;
	}
}