package com.example.customservermod.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import java.util.function.Consumer;

public class PizzaItem extends Item {
	public PizzaItem(Properties properties) {
		super(properties);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
		tooltip.accept(Component.literal("§6Pizza §7- Lecker!"));
		tooltip.accept(Component.literal("§7Stellt 8 Hunger wieder her"));
		super.appendHoverText(stack, context, display, tooltip, flag);
	}
}