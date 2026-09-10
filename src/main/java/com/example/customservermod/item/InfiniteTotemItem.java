package com.example.customservermod.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemLore;

import java.util.List;

public class InfiniteTotemItem extends Item {
	public InfiniteTotemItem(Properties properties) {
		super(properties);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
		tooltipComponents.add(Component.literal("§6Unendlich §7- 30 Sekunden Cooldown"));
		tooltipComponents.add(Component.literal("§7Verbraucht sich nicht, schützt vor Tod"));
		super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
	}

	@Override
	public boolean isFoil(ItemStack stack) {
		return true;
	}
}