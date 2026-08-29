package com.example.customservermod;

import com.example.customservermod.treefeller.TreeFeller;
import net.fabricmc.api.DedicatedServerModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomServerModServer implements DedicatedServerModInitializer {

	private static final Logger LOGGER = LoggerFactory.getLogger(CustomServerMod.MOD_ID);

	@Override
	public void onInitializeServer() {
		TreeFeller.register();
		LOGGER.info("[Custom Server Mod] Server initialized: Lumberjack enchantment + custom Mace recipe");
	}
}
