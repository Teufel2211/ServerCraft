package com.example.customservermod.treefeller;

import com.example.customservermod.CustomServerMod;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

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
			if (!state.isIn(BlockTags.LOGS)) {
				return;
			}
			int level = getLumberjackLevel(player.getMainHandStack());
			if (level <= 0) {
				return;
			}
			fellerTree((ServerWorld) world, player, pos);
		});
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

	private static void fellerTree(ServerWorld world, PlayerEntity player, BlockPos origin) {
		// The origin log was already broken by vanilla.
		Set<BlockPos> toBreak = collectLogs(world, origin);

		ItemStack tool = player.getMainHandStack();
		for (BlockPos pos : toBreak) {
			if (tool.isEmpty()) {
				break;
			}
			BlockState state = world.getBlockState(pos);
			if (state.isAir()) {
				continue;
			}
			Block block = state.getBlock();
			// Drop with the player's tool so Fortune / Silk Touch apply.
			block.afterBreak(world, player, pos, state, world.getBlockEntity(pos), tool);
			world.removeBlock(pos, false);
			// Damage the tool for this extra block (respects Unbreaking).
			if (!player.isCreative()) {
				tool.damage(1, player, (e) -> e.sendEquipmentBreakStatus(tool));
			}
		}
	}

	private static Set<BlockPos> collectLogs(ServerWorld world, BlockPos origin) {
		Set<BlockPos> result = new HashSet<>();
		Deque<BlockPos> queue = new ArrayDeque<>();
		queue.add(origin);
		result.add(origin);

		int cap = 256;
		while (!queue.isEmpty() && result.size() < cap) {
			BlockPos current = queue.poll();
			for (BlockPos neighbor : around(current)) {
				if (result.size() >= cap || result.contains(neighbor)) {
					continue;
				}
				BlockState state = world.getBlockState(neighbor);
				if (state.isIn(BlockTags.LOGS)) {
					result.add(neighbor);
					queue.add(neighbor);
				}
			}
		}
		return result;
	}

	private static BlockPos[] around(BlockPos pos) {
		return new BlockPos[]{
			pos.up(), pos.down(),
			pos.north(), pos.south(), pos.east(), pos.west()
		};
	}
}