package com.example.customservermod.telekinesis;

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

import java.util.List;

public class TelekinesisHandler {

	private static final ResourceKey<Enchantment> TELEKINESIS_KEY =
			ResourceKey.create(Registries.ENCHANTMENT, CustomServerMod.TELEKINESIS_ID);

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
			if (!hasTelekinesis(tool)) {
				return;
			}
			List<ItemStack> drops = getDrops(state, serverLevel, pos, blockEntity, serverPlayer, tool);
			for (ItemStack drop : drops) {
				if (drop.isEmpty()) {
					continue;
				}
				if (!serverPlayer.getInventory().add(drop)) {
					drop.setCount(0);
				}
			}
		});
	}

	private static boolean hasTelekinesis(ItemStack stack) {
		ItemEnchantments enchantments = stack.getEnchantments();
		for (Holder<Enchantment> holder : enchantments.keySet()) {
			if (holder.is(TELEKINESIS_KEY)) {
				return true;
			}
		}
		return false;
	}

	private static List<ItemStack> getDrops(BlockState state, ServerLevel level, BlockPos pos,
			BlockEntity blockEntity,
			ServerPlayer player, ItemStack tool) {
		return Block.getDrops(state, level, pos, blockEntity, player, tool);
	}
}