package com.example.customservermod;

import com.example.customservermod.combined.CombinedEnchantmentHandler;
import com.example.customservermod.msgspy.MsgSpyCommand;
import net.fabricmc.api.DedicatedServerModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomServerModServer implements DedicatedServerModInitializer {

	private static final Logger LOGGER = LoggerFactory.getLogger(CustomServerMod.MOD_ID);

	@Override
	public void onInitializeServer() {
		CombinedEnchantmentHandler.register();
		MsgSpyCommand.register();
		LOGGER.info("[Custom Server Mod] Server initialized: Lumberjack + Telekinesis + Excavation + Auto Smelting enchantments + custom Mace recipe + MsgSpy");
	}
}