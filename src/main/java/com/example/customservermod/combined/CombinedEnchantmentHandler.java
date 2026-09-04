package com.example.customservermod.combined;

import com.example.customservermod.CustomServerMod;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.ArrayDeque;
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
		PlayerBlockBreakEvents.BEFORE.register((level, player, pos, state, blockEntity) -> {
			if (level.isClientSide() || player == null || player.isShiftKeyDown()) {
				return false;
			}
			if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) {
				return false;
			}
			ItemStack tool = player.getMainHandItem();
			if (tool.isEmpty()) {
				return false;
			}

			boolean hasLumberjack = getEnchantmentLevel(tool, LUMBERJACK_KEY) > 0;
			boolean hasExcavation = getEnchantmentLevel(tool, EXCAVATION_KEY) > 0;
			boolean hasTelekinesis = getEnchantmentLevel(tool, TELEKINESIS_KEY) > 0;

			if (!hasLumberjack && !hasExcavation && !hasTelekinesis) {
				return false;
			}

			if (hasLumberjack && state.is(BlockTags.LOGS)) {
				handleLumberjack(serverLevel, (ServerPlayer) player, pos, tool);
				return true;
			}

			if (hasExcavation) {
				handleExcavation(serverLevel, (ServerPlayer) player, pos, tool);
				return true;
			}

			if (hasTelekinesis) {
				handleTelekinesis(serverLevel, (ServerPlayer) player, pos, tool);
				return true;
			}

			return false;
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

	private static void handleLumberjack(ServerLevel level, ServerPlayer player, BlockPos origin, ItemStack tool) {
		Set<BlockPos> logs = collectConnectedLogs(level, origin);
		for (BlockPos logPos : logs) {
			breakBlockAndCollect(level, player, logPos, tool, true);
		}
	}

	private static void handleExcavation(ServerLevel level, ServerPlayer player, BlockPos origin, ItemStack tool) {
		for (int x = -1; x <= 1; x++) {
			for (int y = -1; y <= 1; y++) {
				for (int z = -1; z <= 1; z++) {
					BlockPos pos = origin.offset(x, y, z);
					breakBlockAndCollect(level, player, pos, tool, true);
				}
			}
		}
	}

	private static void handleTelekinesis(ServerLevel level, ServerPlayer player, BlockPos pos, ItemStack tool) {
		breakBlockAndCollect(level, player, pos, tool, true);
	}

	private static void breakBlockAndCollect(ServerLevel level, ServerPlayer player, BlockPos pos, ItemStack tool, boolean hasTelekinesis) {
		BlockState state = level.getBlockState(pos);
		if (state.isAir()) return;

		BlockEntity blockEntity = level.getBlockEntity(pos);
		List<ItemStack> drops = Block.getDrops(state, level, pos, blockEntity, player, tool);

		if (!drops.isEmpty()) {
			for (ItemStack drop : drops) {
				if (drop.isEmpty()) continue;
				if (!player.getInventory().add(drop)) {
					drop.setCount(0);
				}
			}
		}

		level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);

		if (!player.getAbilities().instabuild && !tool.isEmpty()) {
			tool.hurtAndBreak(1, level, player, item -> { });
		}

		level.gameEvent(player, GameEvent.BLOCK_DESTROY, pos);
		level.levelEvent(player, 2001, pos, Block.getId(state));
	}

	private static Set<BlockPos> collectConnectedLogs(ServerLevel level, BlockPos origin) {
		Set<BlockPos> result = new HashSet<>();
		Deque<BlockPos> queue = new ArrayDeque<>();
		queue.add(origin);
		result.add(origin);

		int cap = 256;
		while (!queue.isEmpty() && result.size() < cap) {
			BlockPos current = queue.poll();
			for (Direction dir : Direction.values()) {
				BlockPos neighbor = current.relative(dir);
				if (result.size() >= cap || result.contains(neighbor)) continue;
				BlockState state = level.getBlockState(neighbor);
				if (state.is(BlockTags.LOGS)) {
					result.add(neighbor);
					queue.add(neighbor);
				}
			}
		}
		return result;
	}
}