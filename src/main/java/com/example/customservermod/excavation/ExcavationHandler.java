package com.example.customservermod.excavation;

import com.example.customservermod.CustomServerMod;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class ExcavationHandler {

	private static final ResourceKey<Enchantment> EXCAVATION_KEY =
			ResourceKey.create(Registries.ENCHANTMENT, CustomServerMod.EXCAVATION_ID);

	public static void register() {
		PlayerBlockBreakEvents.AFTER.register((level, player, pos, state, blockEntity) -> {
			if (level.isClientSide() || player == null) {
				return;
			}
			if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) {
				return;
			}
			ItemStack tool = player.getMainHandItem();
			if (tool.isEmpty()) {
				return;
			}
			int excavationLevel = getExcavationLevel(tool);
			if (excavationLevel <= 0) {
				return;
			}
			excavateArea(serverLevel, serverPlayer, pos, tool, excavationLevel);
		});
	}

	private static int getExcavationLevel(ItemStack stack) {
		ItemEnchantments enchantments = stack.getEnchantments();
		for (Holder<Enchantment> holder : enchantments.keySet()) {
			if (holder.is(EXCAVATION_KEY)) {
				return EnchantmentHelper.getItemEnchantmentLevel(holder, stack);
			}
		}
		return 0;
	}

	private static void excavateArea(ServerLevel level, ServerPlayer player, BlockPos origin, ItemStack tool, int levelExcavation) {
		List<BlockPos> toBreak = new ArrayList<>();
		for (int x = -1; x <= 1; x++) {
			for (int z = -1; z <= 1; z++) {
				if (x == 0 && z == 0) continue; // origin already broken
				BlockPos p = origin.offset(x, 0, z);
				BlockState state = level.getBlockState(p);
				if (!state.isAir() && canHarvest(state, tool)) {
					toBreak.add(p);
				}
			}
		}

		boolean creative = player.getAbilities().instabuild;

		for (BlockPos p : toBreak) {
			BlockState state = level.getBlockState(p);
			if (state.isAir()) continue;
			BlockEntity blockEntity = level.getBlockEntity(p);
			List<ItemStack> drops = Block.getDrops(state, level, p, blockEntity, player, tool);
			for (ItemStack drop : drops) {
				if (drop.isEmpty()) continue;
				if (!player.getInventory().add(drop)) {
					drop.setCount(0);
				}
			}
			level.removeBlock(p, false);
			if (!creative && !tool.isEmpty()) {
				tool.hurtAndBreak(1, level, player, item -> { });
			}
		}
	}

	private static boolean canHarvest(BlockState state, ItemStack tool) {
		if (tool.isEmpty()) return false;
		return state.getBlock().isCorrectToolForDrops(state);
	}
}