package com.example.customservermod;

import com.example.customservermod.treefeller.TreeFeller;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomServerMod implements ModInitializer {
	public static final String MOD_ID = "custom-server-mod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final Identifier LUMBERJACK_ID = Identifier.fromNamespaceAndPath(MOD_ID, "lumberjack");
	public static final Identifier TELEKINESIS_ID = Identifier.fromNamespaceAndPath(MOD_ID, "telekinesis");
	public static final Identifier EXCAVATION_ID = Identifier.fromNamespaceAndPath(MOD_ID, "excavation");
	public static final Identifier AUTO_SMELTING_ID = Identifier.fromNamespaceAndPath(MOD_ID, "auto_smelting");
	public static final Identifier INFINITE_TOTEM_ID = Identifier.fromNamespaceAndPath(MOD_ID, "infinite_totem");
	public static final Identifier PIZZA_ID = Identifier.fromNamespaceAndPath(MOD_ID, "pizza");
	public static final Identifier TOMATO_ID = Identifier.fromNamespaceAndPath(MOD_ID, "tomato");
	public static final Identifier CHEESE_ID = Identifier.fromNamespaceAndPath(MOD_ID, "cheese");
	public static final Identifier PIZZA_DOUGH_ID = Identifier.fromNamespaceAndPath(MOD_ID, "pizza_dough");

	@Override
	public void onInitialize() {
		TreeFeller.register();
		LOGGER.info("[Custom Server Mod] Initialized: Lumberjack + Telekinesis + Excavation + Auto Smelting enchantments + custom Mace recipe");
	}
}