package com.example.customservermod;

import com.example.customservermod.combined.CombinedEnchantmentHandler;
import com.example.customservermod.item.PizzaItem;
import com.example.customservermod.msgspy.MsgSpyCommand;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomServerModServer implements DedicatedServerModInitializer {

	private static final Logger LOGGER = LoggerFactory.getLogger(CustomServerMod.MOD_ID);

	@Override
	public void onInitializeServer() {
		Registry.register(BuiltInRegistries.ITEM, CustomServerMod.PIZZA_ID, new PizzaItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, CustomServerMod.PIZZA_ID)).food(new FoodProperties.Builder().nutrition(8).saturationModifier(0.8F).build()).stacksTo(16)));
		CombinedEnchantmentHandler.register();
		MsgSpyCommand.register();
		LOGGER.info("[Custom Server Mod] Server initialized: Teufel's Essentials - Lumberjack + Telekinesis + Excavation + Auto Smelting + Infinite Totem (vanilla NBT) + Pizza + MsgSpy");
	}
}