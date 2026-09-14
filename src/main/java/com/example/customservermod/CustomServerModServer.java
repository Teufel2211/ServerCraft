package com.example.customservermod;

import com.example.customservermod.combined.CombinedEnchantmentHandler;
import com.example.customservermod.debug.DebugCommand;
import com.example.customservermod.geyser.GeyserCompatibility;
import com.example.customservermod.msgspy.MsgSpyCommand;
import com.example.customservermod.treefeller.TreeFeller;
import com.example.customservermod.updater.AutoUpdater;
import com.example.customservermod.version.VersionChecker;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomServerModServer implements DedicatedServerModInitializer {

	private static final Logger LOGGER = LoggerFactory.getLogger(CustomServerMod.MOD_ID);

	@Override
	public void onInitializeServer() {
		TreeFeller.register();
		CombinedEnchantmentHandler.register();
		MsgSpyCommand.register();
		DebugCommand.register();
		VersionChecker.init();
		GeyserCompatibility.init();
		ServerLifecycleEvents.SERVER_STARTED.register(AutoUpdater::checkOnStartup);
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> GeyserCompatibility.onBedrockJoin(handler.getPlayer()));
		LOGGER.info("[ServerCraft] Server initialized: Lumberjack + Telekinesis + Excavation + Auto Smelting + Infinite Totem (vanilla NBT) + Pizza + Tomato + Cheese + Dough + Pizza Oven + MsgSpy + AutoUpdater + VersionCheck + Geyser");
	}
}