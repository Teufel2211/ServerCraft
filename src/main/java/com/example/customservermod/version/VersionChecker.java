package com.example.customservermod.version;

import com.example.customservermod.CustomServerMod;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VersionChecker {
	private static final Logger LOGGER = LoggerFactory.getLogger("ServerCraft-VersionCheck");
	public static final Identifier VERSION_CHECK_ID = Identifier.fromNamespaceAndPath(CustomServerMod.MOD_ID, "version_check");
	private static String serverVersion = "unknown";

	public static void init() {
		serverVersion = FabricLoader.getInstance().getModContainer(CustomServerMod.MOD_ID)
				.map(c -> c.getMetadata().getVersion().getFriendlyString()).orElse("unknown");
		ServerLoginConnectionEvents.QUERY_START.register((handler, server, sender, synchronizer) -> {
			FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
			buf.writeUtf(serverVersion);
			sender.sendPacket(VERSION_CHECK_ID, buf);
		});
		ServerLoginNetworking.registerGlobalReceiver(VERSION_CHECK_ID, (server, handler, understood, buf, synchronizer, responseSender) -> {
			if (!understood) {
				handler.disconnect(Component.literal("Â§c[ServerCraft] Client braucht ServerCraft Mod!\nÂ§7Lade gleiche Version wie Server (" + serverVersion + ") von\nÂ§bhttps://github.com/Teufel2211/ServerCraft/releases"));
				return;
			}
			String clientVersion = buf.readUtf();
			if (!clientVersion.equals(serverVersion)) {
				LOGGER.warn("[ServerCraft] Version mismatch: Server {} vs Client {} ({})", serverVersion, clientVersion, handler.getUserName());
				handler.disconnect(Component.literal("Â§c[ServerCraft] Version Mismatch!\nÂ§7Server: Â§a" + serverVersion + " Â§7Client: Â§c" + clientVersion + "\nÂ§7Bitte gleiche Version von\nÂ§bhttps://github.com/Teufel2211/ServerCraft/releases\nÂ§7auf beiden Seiten nutzen!"));
			}
		});
		LOGGER.info("[ServerCraft] VersionCheck registered (server version {})", serverVersion);
	}

	public static String getServerVersion() {
		return serverVersion;
	}
}

