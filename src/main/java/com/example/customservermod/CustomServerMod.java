package com.example.customservermod;

import com.example.customservermod.treefeller.TreeFeller;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomServerMod implements ModInitializer {
	public static final String MOD_ID = "custom-server-mod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final Identifier LUMBERJACK_ID = Identifier.fromNamespaceAndPath("minecraft", "lumberjack");

	@Override
	public void onInitialize() {
		TreeFeller.register();
		LOGGER.info("[Custom Server Mod] Initialized: Lumberjack enchantment + custom Mace recipe");
	}
}
