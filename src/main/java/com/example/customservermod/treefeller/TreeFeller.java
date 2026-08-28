package com.example.customservermod.treefeller;

import com.example.customservermod.CustomServerMod;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

public class TreeFeller {

	public static void register() {
		PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
			if (world.isClient() || player == null || player.isSneaking()) {
				return;
			}
			if (!isLog(state)) {
				return;
			}
			int level = getLumberjackLevel(player.getMainHandStack());
			if (level <= 0) {
				return;
			}
			fellerTree((ServerWorld) world, player, pos, level);
		});
	}

	private static boolean isLog(BlockState state) {
		return state.isIn(BlockTags.LOGS);
	}

	private static int getLumberjackLevel(ItemStack stack) {
		ItemEnchantmentsComponent ench = stack.getOrDefault(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT);
		RegistryKey<net.minecraft.enchantment.Enchantment> key = RegistryKey.of(RegistryKeys.ENCHANTMENT, CustomServerMod.LUMBERJACK_ID);
		for (EnchantmentLevelEntry entry : ench.getEnchantments()) {
			RegistryEntry<net.minecraft.enchantment.Enchantment> holder = entry.enchantment();
			if (holder.matchesKey(key)) {
				return entry.level();
			}
		}
		return 0;
	}

	private static void fellerTree(ServerWorld world, PlayerEntity player, BlockPos origin, int level) {
		int maxBlocks = 64 + (level - 1) * 32;
		Set<BlockPos> toBreak = new HashSet<>();
		Deque<BlockPos> queue = new ArrayDeque<>();
		queue.add(origin);
		toBreak.add(origin);

		while (!queue.isEmpty() && toBreak.size() < maxBlocks) {
			BlockPos current = queue.poll();
			for (BlockPos neighbor : around(current)) {
				if (toBreak.contains(neighbor) || toBreak.size() >= maxBlocks) {
					continue;
				}
				BlockState state = world.getBlockState(neighbor);
				if (isLog(state)) {
					toBreak.add(neighbor);
					queue.add(neighbor);
				}
			}
		}

		// Remove the origin from extra processing (it was already broken by vanilla)
		toBreak.remove(origin);

		ItemStack tool = player.getMainHandStack();
		for (BlockPos pos : toBreak) {
			if (tool.isEmpty()) {
				break;
			}
			if (player.getInventory().contains(ItemStack.EMPTY) && !player.isCreative()) {
				// fallthrough guard
			}
			BlockState state = world.getBlockState(pos);
			if (state.isAir()) {
				continue;
			}
			Block block = state.getBlock();
			BlockState dropState = block.getPickStack(world, pos, state).isEmpty()
				? state
				: state;
			// Drop with the player's tool so Fortune / Silk Touch apply
			block.afterBreak(world, player, pos, state, world.getBlockEntity(pos), tool);
			world.removeBlock(pos, false);
			// Damage the tool for this extra block (respects Unbreaking)
			if (!player.isCreative()) {
				tool.damage(1, player, (e) -> e.sendEquipmentBreakStatus(tool));
			}
		}
	}

	private static BlockPos[] around(BlockPos pos) {
		return new BlockPos[]{
			pos.up(), pos.down(),
			pos.north(), pos.south(), pos.east(), pos.west()
		};
	}
}
