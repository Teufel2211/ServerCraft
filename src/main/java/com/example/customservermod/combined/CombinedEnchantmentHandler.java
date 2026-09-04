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
				handleExcavation(serverLevel, serverPlayer, pos, state, blockEntity, tool, hasTelekinesis);
				return;
			}
			if (hasTelekinesis) {
				// Single block telekinesis: vanilla already spawned drops, pick them up
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

	private static void handleLumberjack(ServerLevel level, ServerPlayer player, BlockPos origin, BlockState originState, BlockEntity originBe, ItemStack tool, boolean hasTelekinesis) {
		if (hasTelekinesis) {
			// For origin, vanilla drops already spawned - collect them
			pickupItemEntitiesAt(level, player, origin, 2.5);
		}
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

	private static void handleExcavation(ServerLevel level, ServerPlayer player, BlockPos origin, BlockState originState, BlockEntity originBe, ItemStack tool, boolean hasTelekinesis) {
		if (hasTelekinesis) {
			pickupItemEntitiesAt(level, player, origin, 2.5);
		}
		boolean creative = player.getAbilities().instabuild;
		for (int x=-1;x<=1;x++) for (int y=-1;y<=1;y++) for (int z=-1;z<=1;z++) {
			if (x==0&&y==0&&z==0) continue;
			BlockPos p = origin.offset(x,y,z);
			BlockState s = level.getBlockState(p);
			if (s.isAir()) continue;
			breakAdditionalBlock(level, player, p, s, level.getBlockEntity(p), tool, hasTelekinesis, creative);
		}
		if (hasTelekinesis) pickupItemEntitiesAt(level, player, origin, 3.0);
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