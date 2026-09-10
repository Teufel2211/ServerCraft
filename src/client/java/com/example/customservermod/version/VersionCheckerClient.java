package com.example.customservermod.version;

import com.example.customservermod.CustomServerMod;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.FriendlyByteBuf;

public class VersionCheckerClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		String clientVersion = FabricLoader.getInstance().getModContainer(CustomServerMod.MOD_ID)
				.map(c -> c.getMetadata().getVersion().getFriendlyString()).orElse("unknown");
		ClientLoginNetworking.registerGlobalReceiver(VersionChecker.VERSION_CHECK_ID, (client, handler, buf, listenerAdder) -> {
			String serverVersion = buf.readUtf();
			FriendlyByteBuf resp = new FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
			resp.writeUtf(clientVersion);
			return java.util.concurrent.CompletableFuture.completedFuture\(resp\);
		});
	}
}
