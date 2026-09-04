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
import net.minecraft.world.entity.Entity;
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

	// Track positions where telekinesis should cancel vanilla drops
	private static final Set<BlockPos> CANCEL_DROPS_AT = new HashSet<>();

	public static void register() {
		// BEFORE event: cancel vanilla drops at origin when telekinesis active
		PlayerBlockBreakEvents.BEFORE.register((level, player, pos, state, blockEntity) -> {
			if (level.isClientSide() || player == null) {
				return false;
			}
			if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) {
				return false;
			}
			ItemStack tool = player.getMainHandItem();
			if (tool.isEmpty()) {
				return false;
			}
			boolean hasTelekinesis = getEnchantmentLevel(tool, TELEKINESIS_KEY) > 0;
			if (!hasTelekinesis) {
				return false;
			}
			// Check if any custom enchantment is active on this tool
			boolean hasLumberjack = getEnchantmentLevel(tool, LUMBERJACK_KEY) > 0;
			boolean hasExcavation = getEnchantmentLevel(tool, EXCAVATION_KEY) > 0;
			if (!hasLumberjack && !hasExcavation) {
				// Only telekinesis: cancel vanilla drops for this single block
				CANCEL_DROPS_AT.add(pos.immutable());
				return true; // cancel vanilla drops
			}
			// For lumberjack/excavation, we handle drops manually in AFTER
			// Mark origin to cancel vanilla drops
			CANCEL_DROPS_AT.add(pos.immutable());
			return false; // let AFTER handle the rest
		});

		// AFTER event: handle custom enchantment logic
		PlayerBlockBreakEvents.AFTER.register((level, player, pos, state, blockEntity) -> {
			if (level.isClientSide() || player == null || player.isShiftKeyDown()) {
				// Clean up cancel tracking
				CANCEL_DROPS_AT.remove(pos);
				return;
			}
			if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) {
				CANCEL_DROPS_AT.remove(pos);
				return;
			}
			ItemStack tool = player.getMainHandItem();
			if (tool.isEmpty()) {
				CANCEL_DROPS_AT.remove(pos);
				return;
			}

			boolean hasLumberjack = getEnchantmentLevel(tool, LUMBERJACK_KEY) > 0;
			boolean hasExcavation = getEnchantmentLevel(tool, EXCAVATION_KEY) > 0;
			boolean hasTelekinesis = getEnchantmentLevel(tool, TELEKINESIS_KEY) > 0;

			if (!hasLumberjack && !hasExcavation && !hasTelekinesis) {
				CANCEL_DROPS_AT.remove(pos);
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

			// Only Telekinesis: auto-pickup drops (origin already cancelled by BEFORE)
			if (hasTelekinesis) {
				// Origin drops already cancelled by BEFORE event
				// Just pick up any stray entities at adjacent positions
				pickupItemEntitiesAt(serverLevel, serverPlayer, pos);
			}
			CANCEL_DROPS_AT.remove(pos);
		});

		// Also clean up on block break completion
		PlayerBlockBreakEvents.AFTER.register((level, player, pos, state, blockEntity) -> {
			CANCEL_DROPS_AT.remove(pos);
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
		// Origin already handled by BEFORE cancel + AFTER pickup in fell loop
	}

	// Excavation: 3x3x3 cube (27 blocks total, origin already broken by vanilla)
	private static void excavateAreaAndCollect(ServerLevel level, ServerPlayer player, BlockPos origin, ItemStack tool, boolean hasTelekinesis) {
		boolean creative = player.getAbilities().instabuild;
		int brokenCount = 0;

		for (int x = -1; x <= 1; x++) {
			for (int y = -1; y <= 1; y++) {
				for (int z = -1; z <= 1; z++) {
					if (x == 0 && y == 0 && z == 0) continue; // origin already broken by vanilla
					BlockPos pos = origin.offset(x, y, z);
					BlockState state = level.getBlockState(pos);
					if (state.isAir()) continue;

					BlockEntity blockEntity = level.getBlockEntity(pos);
					breakBlockAndCollectDrops(level, player, pos, state, blockEntity, tool, hasTelekinesis, creative);
					brokenCount++;
				}
			}
		}
		// Debug: log how many blocks were broken
		// System.out.println("[Excavation] Broken " + brokenCount + " additional blocks (3x3x3)");
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
		// Origin drops cancelled by BEFORE event
		pickupItemEntitiesAt(level, player, pos);
	}

	// Shared: pick up item entities at a position
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