package com.example.customservermod.combined;

import com.example.customservermod.CustomServerMod;
import com.example.customservermod.excavation.ExcavationHandler;
import com.example.customservermod.treefeller.TreeFeller;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CombinedEnchantmentHandler {

	private static final ResourceKey<Enchantment> LUMBERJACK_KEY =
			ResourceKey.create(Registries.ENCHANTMENT, CustomServerMod.LUMBERJACK_ID);
	private static final ResourceKey<Enchantment> TELEKINESIS_KEY =
			ResourceKey.create(Registries.ENCHANTMENT, CustomServerMod.TELEKINESIS_ID);
	private static final ResourceKey<Enchantment> EXCAVATION_KEY =
			ResourceKey.create(Registries.ENCHANTMENT, CustomServerMod.EXCAVATION_ID);

	public static void register() {
		PlayerBlockBreakEvents.AFTER.register((level, player, pos, state, blockEntity) -> {
			if (level.isClientSide() || player == null || player.isShiftKeyDown()) {
				return;
			}
			if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) {
				return;
			}
			ItemStack tool = player.getMainHandItem();
			if (tool.isEmpty()) {
				return;
			}

			boolean hasLumberjack = getEnchantmentLevel(tool, LUMBERJACK_KEY) > 0;
			boolean hasExcavation = getEnchantmentLevel(tool, EXCAVATION_KEY) > 0;
			boolean hasTelekinesis = getEnchantmentLevel(tool, TELEKINESIS_KEY) > 0;

			if (!hasLumberjack && !hasExcavation && !hasTelekinesis) {
				return;
			}

			// Lumberjack: fell entire tree
			if (hasLumberjack && state.is(BlockTags.LOGS)) {
				fellTreeAndCollect(serverLevel, serverPlayer, pos, tool, hasTelekinesis);
				return; // tree felling handles all blocks internally
			}

			// Excavation: 3x3 area
			if (hasExcavation) {
				excavateAreaAndCollect(serverLevel, serverPlayer, pos, tool, hasTelekinesis);
				return;
			}

			// Only Telekinesis: auto-pickup drops
			if (hasTelekinesis) {
				pickupDrops(serverLevel, serverPlayer, pos, state, blockEntity, tool);
			}
		});
	}

	private static int getEnchantmentLevel(ItemStack stack, ResourceKey<Enchantment> key) {
		ItemEnchantments enchantments = stack.getEnchantments();
		for (Holder<Enchantment> holder : enchantments.keySet()) {
			if (holder.is(key)) {
				return EnchantmentHelper.getItemEnchantmentLevel(holder, stack);
			}
		}
		return 0;
	}

	// Lumberjack: fell tree + collect drops
	private static void fellTreeAndCollect(ServerLevel level, ServerPlayer player, BlockPos origin, ItemStack tool, boolean hasTelekinesis) {
		Set<BlockPos> toBreak = collectLogs(level, origin);
		boolean creative = player.getAbilities().instabuild;

		for (BlockPos pos : toBreak) {
			BlockState state = level.getBlockState(pos);
			if (state.isAir()) continue;

			BlockEntity blockEntity = level.getBlockEntity(pos);
			breakBlockAndCollectDrops(level, player, pos, state, blockEntity, tool, hasTelekinesis, creative);
		}
	}

	// Excavation: 3x3 area
	private static void excavateAreaAndCollect(ServerLevel level, ServerPlayer player, BlockPos origin, ItemStack tool, boolean hasTelekinesis) {
		boolean creative = player.getAbilities().instabuild;

		for (int x = -1; x <= 1; x++) {
			for (int z = -1; z <= 1; z++) {
				if (x == 0 && z == 0) continue; // origin already broken by vanilla
				BlockPos pos = origin.offset(x, 0, z);
				BlockState state = level.getBlockState(pos);
				if (state.isAir()) continue;

				BlockEntity blockEntity = level.getBlockEntity(pos);
				breakBlockAndCollectDrops(level, player, pos, state, blockEntity, tool, hasTelekinesis, creative);
			}
		}
	}

	// Common: break block + collect drops (with Telekinesis if enabled)
	private static void breakBlockAndCollectDrops(ServerLevel level, ServerPlayer player, BlockPos pos, BlockState state, BlockEntity blockEntity, ItemStack tool, boolean hasTelekinesis, boolean creative) {
		List<ItemStack> drops = Block.getDrops(state, level, pos, blockEntity, player, tool);
		for (ItemStack drop : drops) {
			if (drop.isEmpty()) continue;
			if (hasTelekinesis) {
				if (!player.getInventory().add(drop)) {
					drop.setCount(0);
				}
			} else {
				Block.popResource(level, pos, drop);
			}
		}

		level.removeBlock(pos, false);
		if (!creative && !tool.isEmpty()) {
			tool.hurtAndBreak(1, level, player, item -> { });
		}
	}

	// Only Telekinesis (no other custom enchantment): auto-pickup drops
	private static void pickupDrops(ServerLevel level, ServerPlayer player, BlockPos pos, BlockState state, BlockEntity blockEntity, ItemStack tool) {
		List<ItemStack> drops = Block.getDrops(state, level, pos, blockEntity, player, tool);
		for (ItemStack drop : drops) {
			if (drop.isEmpty()) continue;
			if (!player.getInventory().add(drop)) {
				drop.setCount(0);
			}
		}

		// Also pick up any vanilla item entities already spawned at this position
		List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class,
			new net.minecraft.world.phys.AABB(pos).inflate(1.0),
			e -> !e.getItem().isEmpty() && e.getOwner() == null);
		for (ItemEntity item : items) {
			ItemStack stack = item.getItem();
			if (!player.getInventory().add(stack)) {
				break;
			}
			item.discard();
		}
	}

	private static Set<BlockPos> collectLogs(Level level, BlockPos origin) {
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
				BlockState state = level.getBlockState(neighbor);
				if (state.is(BlockTags.LOGS)) {
					result.add(neighbor);
					queue.add(neighbor);
				}
			}
		}
		return result;
	}

	private static BlockPos[] around(BlockPos pos) {
		return new BlockPos[]{
			pos.above(), pos.below(),
			pos.north(), pos.south(), pos.east(), pos.west()
		};
	}
}