package com.example.customservermod.combined;

import com.example.customservermod.CustomServerMod;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

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
		PlayerBlockBreakEvents.AFTER.register((level, player, pos, state, blockEntity) -> {
			if (level.isClientSide() || player == null || player.isShiftKeyDown()) return;
			if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) return;
			ItemStack tool = player.getMainHandItem();
			if (tool.isEmpty() || tool.getEnchantments().isEmpty()) return;
			boolean hasLumberjack = getEnchantmentLevel(tool, LUMBERJACK_KEY) > 0;
			boolean hasExcavation = getEnchantmentLevel(tool, EXCAVATION_KEY) > 0;
			boolean hasTelekinesis = getEnchantmentLevel(tool, TELEKINESIS_KEY) > 0;
			if (!hasLumberjack && !hasExcavation && !hasTelekinesis) return;

			if (hasLumberjack && state.is(BlockTags.LOGS)) {
				handleLumberjack(serverLevel, serverPlayer, pos, state, blockEntity, tool, hasTelekinesis);
				return;
			}
			if (hasExcavation) {
				handleExcavationC3(serverLevel, serverPlayer, pos, state, blockEntity, tool, hasTelekinesis);
				return;
			}
			if (hasTelekinesis) {
				pickupItemEntitiesAt(serverLevel, serverPlayer, pos, 2.5);
			}
		});
	}

	private static int getEnchantmentLevel(ItemStack stack, ResourceKey<Enchantment> key) {
		ItemEnchantments enchantments = stack.getEnchantments();
		for (Holder<Enchantment> holder : enchantments.keySet()) {
			if (holder.is(key)) return EnchantmentHelper.getItemEnchantmentLevel(holder, stack);
		}
		return 0;
	}

	private static Direction getHitDirection(Player player) {
		Vec3 look = player.getLookAngle();
		double ax = Math.abs(look.x);
		double ay = Math.abs(look.y);
		double az = Math.abs(look.z);
		if (ay > ax && ay > az) return look.y > 0 ? Direction.UP : Direction.DOWN;
		if (ax > az) return look.x > 0 ? Direction.EAST : Direction.WEST;
		return look.z > 0 ? Direction.SOUTH : Direction.NORTH;
	}

	private static void handleLumberjack(ServerLevel level, ServerPlayer player, BlockPos origin, BlockState originState, BlockEntity originBe, ItemStack tool, boolean hasTelekinesis) {
		if (hasTelekinesis) pickupItemEntitiesAt(level, player, origin, 2.5);
		Set<BlockPos> logs = collectConnectedLogs(level, origin);
		boolean creative = player.getAbilities().instabuild;
		for (BlockPos p : logs) {
			if (p.equals(origin)) continue;
			BlockState s = level.getBlockState(p);
			if (s.isAir()) continue;
			breakAdditionalBlock(level, player, p, s, level.getBlockEntity(p), tool, hasTelekinesis, creative);
		}
		if (hasTelekinesis) pickupItemEntitiesAt(level, player, origin, 3.0);
	}

	private static void handleExcavationC3(ServerLevel level, ServerPlayer player, BlockPos origin, BlockState originState, BlockEntity originBe, ItemStack tool, boolean hasTelekinesis) {
		if (hasTelekinesis) pickupItemEntitiesAt(level, player, origin, 2.5);
		Direction dir = getHitDirection(player);
		boolean creative = player.getAbilities().instabuild;
		// 3 layers deep in hit direction: depth 0,1,2
		for (int depth = 0; depth < 3; depth++) {
			BlockPos base = origin.relative(dir, depth);
			for (int a = -1; a <= 1; a++) {
				for (int b = -1; b <= 1; b++) {
					BlockPos p;
					if (dir == Direction.UP || dir == Direction.DOWN) {
						// XZ plane
						if (depth == 0 && a == 0 && b == 0) continue; // origin handled by vanilla
						p = base.offset(a, 0, b);
					} else if (dir == Direction.NORTH || dir == Direction.SOUTH) {
						// XY plane at base Z
						if (depth == 0 && a == 0 && b == 0) continue;
						p = base.offset(a, b, 0);
					} else {
						// EAST/WEST: YZ plane at base X
						if (depth == 0 && a == 0 && b == 0) continue;
						p = base.offset(0, a, b);
					}
					BlockState s = level.getBlockState(p);
					if (s.isAir()) continue;
					breakAdditionalBlock(level, player, p, s, level.getBlockEntity(p), tool, hasTelekinesis, creative);
				}
			}
		}
		if (hasTelekinesis) pickupItemEntitiesAt(level, player, origin, 4.0);
	}

	private static void breakAdditionalBlock(ServerLevel level, ServerPlayer player, BlockPos pos, BlockState state, BlockEntity be, ItemStack tool, boolean hasTelekinesis, boolean creative) {
		List<ItemStack> drops = Block.getDrops(state, level, pos, be, player, tool);
		if (hasTelekinesis) {
			for (ItemStack drop : drops) if (!drop.isEmpty()) if (!player.getInventory().add(drop)) Block.popResource(level, pos, drop);
		} else {
			for (ItemStack drop : drops) if (!drop.isEmpty()) Block.popResource(level, pos, drop);
		}
		level.removeBlock(pos, false);
		if (!creative && !tool.isEmpty()) tool.hurtAndBreak(1, level, player, item->{});
	}

	private static void pickupItemEntitiesAt(ServerLevel level, ServerPlayer player, BlockPos pos, double radius) {
		List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, new net.minecraft.world.phys.AABB(pos).inflate(radius), e->!e.getItem().isEmpty());
		for (ItemEntity item : items) {
			if (player.getInventory().add(item.getItem())) item.discard();
		}
	}

	private static Set<BlockPos> collectConnectedLogs(ServerLevel level, BlockPos origin) {
		Set<BlockPos> result = new HashSet<>();
		Deque<BlockPos> queue = new ArrayDeque<>();
		queue.add(origin);
		result.add(origin);
		int cap=256;
		while(!queue.isEmpty() && result.size()<cap){
			BlockPos cur=queue.poll();
			for(Direction d:Direction.values()){
				BlockPos n=cur.relative(d);
				if(result.contains(n)||result.size()>=cap) continue;
				if(level.getBlockState(n).is(BlockTags.LOGS)){result.add(n);queue.add(n);}
			}
		}
		return result;
	}
}