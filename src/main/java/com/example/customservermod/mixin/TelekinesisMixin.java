package com.example.customservermod.mixin;

import com.example.customservermod.CustomServerMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Block.class)
public class TelekinesisMixin {

	@Inject(method = "dropResources(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)V", at = @At("HEAD"), cancellable = true)
	private static void onDropResources(BlockState state, Level level, BlockPos pos, BlockEntity blockEntity, Entity entity, ItemStack tool, CallbackInfo ci) {
		if (!(entity instanceof ServerPlayer player)) return;
		if (level.isClientSide) return;
		if (!(level instanceof ServerLevel serverLevel)) return;

		ItemStack mainHand = player.getMainHandItem();
		ItemStack checkStack = tool.isEmpty() ? mainHand : tool;
		if (checkStack.isEmpty()) return;

		ResourceKey<Enchantment> key = ResourceKey.create(Registries.ENCHANTMENT, CustomServerMod.TELEKINESIS_ID);
		boolean hasTele = false;
		for (Holder<Enchantment> h : checkStack.getEnchantments().keySet()) {
			if (h.is(key)) { hasTele = true; break; }
		}
		if (!hasTele) return;

		List<ItemStack> drops = Block.getDrops(state, serverLevel, pos, blockEntity, player, checkStack);
		for (ItemStack drop : drops) {
			if (drop.isEmpty()) continue;
			if (!player.getInventory().add(drop)) {
				Block.popResource(level, pos, drop);
			}
		}
		ci.cancel();
	}
}