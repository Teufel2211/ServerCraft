package com.example.customservermod.block;

import com.example.customservermod.CustomServerMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
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
		if (level.isClientSide()) return InteractionResult.SUCCESS;

		if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.CONSUME;

		ItemStack dough = findItem(player, CustomServerMod.PIZZA_DOUGH_ID);
		ItemStack tomato = findItem(player, CustomServerMod.TOMATO_ID);
		ItemStack cheese = findItem(player, CustomServerMod.CHEESE_ID);

		if (dough.isEmpty() || tomato.isEmpty() || cheese.isEmpty()) {
			player.sendSystemMessage(Component.literal("§cBenötigt: Pizza Teig + Tomate + Käse im Inventar!"));
			return InteractionResult.CONSUME;
		}

		dough.shrink(1);
		tomato.shrink(1);
		cheese.shrink(1);

		ItemStack pizza = new ItemStack(level.registryAccess().lookupOrThrow(Registries.ITEM).getValueOrThrow(ResourceKey.create(Registries.ITEM, CustomServerMod.PIZZA_ID)));
		if (!player.getInventory().add(pizza)) {
			Block.popResource(level, pos, pizza);
		}

		level.playSound(null, pos, SoundEvents.SMOKER_SMOKE, SoundSource.BLOCKS, 1.0F, 1.0F);
		player.sendSystemMessage(Component.literal("§aPizza gebacken!"));

		return InteractionResult.CONSUME;
	}

	private ItemStack findItem(Player player, net.minecraft.resources.Identifier id) {
		var item = player.level().registryAccess().lookupOrThrow(Registries.ITEM).getValue(ResourceKey.create(Registries.ITEM, id));
		if (item == null) return ItemStack.EMPTY;
		for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
			ItemStack stack = player.getInventory().getItem(i);
			if (!stack.isEmpty() && stack.is(item)) return stack;
		}
		return ItemStack.EMPTY;
	}
}