package com.example.customservermod;

import com.example.customservermod.block.PizzaOvenBlock;
import com.example.customservermod.combined.CombinedEnchantmentHandler;
import com.example.customservermod.item.PizzaItem;
import com.example.customservermod.msgspy.MsgSpyCommand;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomServerModServer implements DedicatedServerModInitializer {

	private static final Logger LOGGER = LoggerFactory.getLogger(CustomServerMod.MOD_ID);

	@Override
	public void onInitializeServer() {
		Registry.register(BuiltInRegistries.ITEM, CustomServerMod.PIZZA_ID, new PizzaItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, CustomServerMod.PIZZA_ID)).food(new FoodProperties.Builder().nutrition(8).saturationModifier(0.8F).build()).stacksTo(16)));
		Registry.register(BuiltInRegistries.ITEM, CustomServerMod.TOMATO_ID, new Item(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, CustomServerMod.TOMATO_ID)).food(new FoodProperties.Builder().nutrition(2).saturationModifier(0.3F).build())));
		Registry.register(BuiltInRegistries.ITEM, CustomServerMod.CHEESE_ID, new Item(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, CustomServerMod.CHEESE_ID)).food(new FoodProperties.Builder().nutrition(3).saturationModifier(0.4F).build())));
		Registry.register(BuiltInRegistries.ITEM, CustomServerMod.PIZZA_DOUGH_ID, new Item(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, CustomServerMod.PIZZA_DOUGH_ID))));
		Block pizzaOvenBlock = Registry.register(BuiltInRegistries.BLOCK, CustomServerMod.PIZZA_OVEN_ID, new PizzaOvenBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, CustomServerMod.PIZZA_OVEN_ID)).strength(2.0F, 6.0F).requiresCorrectToolForDrops()));
		Registry.register(BuiltInRegistries.ITEM, CustomServerMod.PIZZA_OVEN_ID, new BlockItem(pizzaOvenBlock, new Item.Properties().setId(ResourceKey.create(Registries.ITEM, CustomServerMod.PIZZA_OVEN_ID))));
		CombinedEnchantmentHandler.register();
		MsgSpyCommand.register();
		LOGGER.info("[Custom Server Mod] Server initialized: Teufel's Essentials - Lumberjack + Telekinesis + Excavation + Auto Smelting + Infinite Totem (vanilla NBT) + Pizza + Tomato + Cheese + Dough + Pizza Oven + MsgSpy");
	}
}