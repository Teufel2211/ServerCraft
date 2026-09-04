package com.example.customservermod.combined;

import com.example.customservermod.CustomServerMod;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.item.ItemEntity;
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
import java.util.Deque
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
				return;
			}

			// Excavation: 3x3x3 cube
			if (hasExcavation) {
				excavateAreaAndCollect(serverLevel, serverPlayer, pos, tool, hasTelekinesis);
				return;
			}

			// Only Telekinesis: break origin block + collect drops
			if (hasTelekinesis) {
				breakOriginAndCollect(serverLevel, serverPlayer, pos, state, blockEntity, tool);
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
		boolean creative = player.getAbilities().instabuild;

		// Origin block
		BlockState originState = level.getBlockState(origin);
		if (!originState.isAir()) {
			breakBlockAndCollectDrops(level, player, origin, originState, level.getBlockEntity(origin), tool, hasTelekinesis, creative);
		}

		// Rest of tree
		Set<BlockPos> toBreak = collectLogs(level, origin);
		for (BlockPos logPos : toBreak) {
			if (logPos.equals(origin)) continue;
			BlockState state = level.getBlockState(logPos);
			if (state.isAir()) continue;
			breakBlockAndCollectDrops(level, player, logPos, state, level.getBlockEntity(logPos), tool, hasTelekinesis, creative);
		}
	}

	// Excavation: 3x3x3 cube
	private static void excavateAreaAndCollect(ServerLevel level, ServerPlayer player, BlockPos origin, ItemStack tool, boolean hasTelekinesis) {
		boolean creative = player.getAbilities().instabuild;

		// Origin block
		BlockState originState = level.getBlockState(origin);
		if (!originState.isAir()) {
			breakBlockAndCollectDrops(level, player, origin, originState, level.getBlockEntity(origin), tool, hasTelekinesis, creative);
		}

		// 3x3x3 cube around origin
		for (int x = -1; x <= 1; x++) {
			for (int y = -1; y <= 1; y++) {
				for (int z = -1; z <= 1; z++) {
					if (x == 0 && y == 0 && z == 0) continue;
					BlockPos pos = origin.offset(x, y, z);
					BlockState state = level.getBlockState(pos);
					if (state.isAir()) continue;

					breakBlockAndCollectDrops(level, player, pos, state, level.getBlockEntity(pos), tool, hasTelekinesis, creative);
				}
			}
		}
	}

	// Only Telekinesis on a single block
	private static void breakOriginAndCollect(ServerLevel level, ServerPlayer player, BlockPos pos, BlockState state, BlockEntity blockEntity, ItemStack tool) {
		breakBlockAndCollectDrops(level, player, pos, state, blockEntity, tool, true, player.getAbilities().instabuild);
	}

	// Core: break block, collect drops, remove block
	private static void breakBlockAndCollectDrops(ServerLevel level, ServerPlayer player, BlockPos pos, BlockState state, BlockEntity blockEntity, ItemStack tool, boolean hasTelekinesis, boolean creative) {
		if (state.isAir()) return;

		// Get drops that vanilla would produce
		List<ItemStack> drops = Block.getDrops(state, level, pos, blockEntity, player, tool);
		
		// Collect drops into inventory if telekinesis
		if (hasTelekinesis) {
			for (ItemStack drop : drops) {
				if (drop.isEmpty()) continue;
				if (!player.getInventory().add(drop)) {
					drop.setCount(0);
				}
			}
		} else {
			// Normal drop behavior
			for (ItemStack drop : drops) {
				if (!drop.isEmpty()) {
					Block.popResource(level, pos, drop);
				}
			}
		}

		// Remove the block (vanilla may have already removed origin, but safe to call)
		level.removeBlock(pos, false);

		// Damage tool
		if (!creative && !tool.isEmpty()) {
			tool.hurtAndBreak(1, level, player, item -> { });
		}

		// Clean up any item entities that vanilla may have spawned
		pickupItemEntitiesAt(level, player, pos);
	}

	// Pick up item entities at a position
	private static void pickupItemEntitiesAt(ServerLevel level, ServerPlayer player, BlockPos pos) {
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