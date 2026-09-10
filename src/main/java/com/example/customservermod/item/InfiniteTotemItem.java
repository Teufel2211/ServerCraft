package com.example.customservermod.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import java.util.function.Consumer;

public class InfiniteTotemItem extends Item {
	public InfiniteTotemItem(Properties properties) {
		super(properties);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
		tooltip.accept(Component.literal("§6Unendlich §7- 30 Sekunden Cooldown"));
		tooltip.accept(Component.literal("§7Verbraucht sich nicht, schützt vor Tod"));
		super.appendHoverText(stack, context, display, tooltip, flag);
	}

	@Override
	public boolean isFoil(ItemStack stack) {
		return true;
	}
}