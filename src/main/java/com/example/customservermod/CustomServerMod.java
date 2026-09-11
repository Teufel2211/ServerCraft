package com.example.customservermod;

import com.example.customservermod.block.PizzaOvenBlock;
import com.example.customservermod.combined.CombinedEnchantmentHandler;
import com.example.customservermod.item.PizzaItem;
import com.example.customservermod.treefeller.TreeFeller;
import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
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
	public static final Identifier PIZZA_OVEN_ID = Identifier.fromNamespaceAndPath(MOD_ID, "pizza_oven");

	@Override
	public void onInitialize() {
		// Registry muss auf Client + Server identisch sein (environment: *)
		Registry.register(BuiltInRegistries.ITEM, PIZZA_ID, new PizzaItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, PIZZA_ID)).food(new FoodProperties.Builder().nutrition(8).saturationModifier(0.8F).build()).stacksTo(16)));
		Registry.register(BuiltInRegistries.ITEM, TOMATO_ID, new Item(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, TOMATO_ID)).food(new FoodProperties.Builder().nutrition(2).saturationModifier(0.3F).build())));
		Registry.register(BuiltInRegistries.ITEM, CHEESE_ID, new Item(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, CHEESE_ID)).food(new FoodProperties.Builder().nutrition(3).saturationModifier(0.4F).build())));
		Registry.register(BuiltInRegistries.ITEM, PIZZA_DOUGH_ID, new Item(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, PIZZA_DOUGH_ID))));
		Block pizzaOvenBlock = Registry.register(BuiltInRegistries.BLOCK, PIZZA_OVEN_ID, new PizzaOvenBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, PIZZA_OVEN_ID)).strength(2.0F, 6.0F).requiresCorrectToolForDrops()));
		Registry.register(BuiltInRegistries.ITEM, PIZZA_OVEN_ID, new BlockItem(pizzaOvenBlock, new Item.Properties().setId(ResourceKey.create(Registries.ITEM, PIZZA_OVEN_ID))));

		// Beide Handler in main registrieren (laufen auf Client+Server), Server ruft sie zusätzlich nochmal - dort guard gegen Doppel-Registrierung
		TreeFeller.register();
		CombinedEnchantmentHandler.register();
		LOGGER.info("[ServerCraft] Initialized: Lumberjack + Telekinesis + Excavation + Auto Smelting enchantments + custom Mace recipe + Pizza + Oven");
	}
}
