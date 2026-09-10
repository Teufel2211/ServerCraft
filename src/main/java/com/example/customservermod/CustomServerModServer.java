package com.example.customservermod;

import com.example.customservermod.combined.CombinedEnchantmentHandler;
import com.example.customservermod.item.InfiniteTotemItem;
import com.example.customservermod.msgspy.MsgSpyCommand;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomServerModServer implements DedicatedServerModInitializer {

	private static final Logger LOGGER = LoggerFactory.getLogger(CustomServerMod.MOD_ID);

	@Override
	public void onInitializeServer() {
		Registry.register(BuiltInRegistries.ITEM, CustomServerMod.INFINITE_TOTEM_ID, new InfiniteTotemItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, CustomServerMod.INFINITE_TOTEM_ID)).stacksTo(1).fireResistant()));
		CombinedEnchantmentHandler.register();
		MsgSpyCommand.register();
		LOGGER.info("[Custom Server Mod] Server initialized: Lumberjack + Telekinesis + Excavation + Auto Smelting enchantments + Infinite Totem + custom Mace recipe + MsgSpy");
	}
}