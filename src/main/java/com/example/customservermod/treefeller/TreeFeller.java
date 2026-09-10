package com.example.customservermod.treefeller;

import com.example.customservermod.CustomServerMod;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

public class TreeFeller {

	private static final ResourceKey<Enchantment> LUMBERJACK_KEY =
			ResourceKey.create(Registries.ENCHANTMENT, CustomServerMod.LUMBERJACK_ID);

	private static boolean registered = false;

	public static void register() {
		if (registered) return;
		registered = true;
		PlayerBlockBreakEvents.AFTER.register((level, player, pos, state, blockEntity) -> {
			if (level.isClientSide() || player == null || player.isShiftKeyDown()) {
				return;
			}
			if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) {
				return;
			}
			if (!state.is(BlockTags.LOGS)) {
				return;
			}
			int enchantLevel = getLumberjackLevel(player.getMainHandItem());
			if (enchantLevel <= 0) {
				return;
			}
			fellerTree(serverLevel, serverPlayer, pos);
		});
	}

	private static int getLumberjackLevel(ItemStack stack) {
		ItemEnchantments enchantments = stack.getEnchantments();
		for (Holder<Enchantment> holder : enchantments.keySet()) {
			if (holder.is(LUMBERJACK_KEY)) {
				return EnchantmentHelper.getItemEnchantmentLevel(holder, stack);
			}
		}
		return 0;
	}

	private static void fellerTree(ServerLevel level, ServerPlayer player, BlockPos origin) {
		// The origin log was already broken by vanilla.
		Set<BlockPos> toBreak = collectLogs(level, origin);

		ItemStack tool = player.getMainHandItem();
		boolean creative = player.getAbilities().instabuild;

		for (BlockPos pos : toBreak) {
			BlockState state = level.getBlockState(pos);
			if (state.isAir()) {
				continue;
			}
			Block block = state.getBlock();
			// Drop with the player's tool so Fortune / Silk Touch apply.
			Block.dropResources(state, level, pos, level.getBlockEntity(pos), player, tool);
			level.removeBlock(pos, false);
			// Damage the tool for this extra block (respects Unbreaking via hurtAndBreak).
			if (!creative && !tool.isEmpty()) {
				tool.hurtAndBreak(1, level, player, item -> { });
			}
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
