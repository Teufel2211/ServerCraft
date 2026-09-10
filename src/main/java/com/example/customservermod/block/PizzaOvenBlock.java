package com.example.customservermod.block;

import com.example.customservermod.CustomServerMod;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class PizzaOvenBlock extends Block {
	public PizzaOvenBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
		if (level.isClientSide) return InteractionResult.SUCCESS;

		if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.CONSUME;

		// Check for required ingredients in inventory
		ItemStack dough = findItem(player, CustomServerMod.PIZZA_DOUGH_ID);
		ItemStack tomato = findItem(player, CustomServerMod.TOMATO_ID);
		ItemStack cheese = findItem(player, CustomServerMod.CHEESE_ID);

		if (dough.isEmpty() || tomato.isEmpty() || cheese.isEmpty()) {
			player.displayClientMessage(Component.literal("§cBenötigt: Pizza Teig + Tomate + Käse im Inventar!"), true);
			return InteractionResult.CONSUME;
		}

		// Consume one of each
		dough.shrink(1);
		tomato.shrink(1);
		cheese.shrink(1);

		// Give pizza
		ItemStack pizza = new ItemStack(level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ITEM).getValueOrThrow(CustomServerMod.PIZZA_ID));
		if (!player.getInventory().add(pizza)) {
			Block.popResource(level, pos, pizza);
		}

		level.playSound(null, pos, SoundEvents.SMOKER_SMOKE, SoundSource.BLOCKS, 1.0F, 1.0F);
		player.displayClientMessage(Component.literal("§aPizza gebacken!"), true);

		return InteractionResult.CONSUME;
	}

	private ItemStack findItem(Player player, net.minecraft.resources.Identifier id) {
		var item = player.level().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ITEM).getValue(id);
		if (item == null) return ItemStack.EMPTY;
		for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
			ItemStack stack = player.getInventory().getItem(i);
			if (!stack.isEmpty() && stack.is(item)) return stack;
		}
		return ItemStack.EMPTY;
	}
}