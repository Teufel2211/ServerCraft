package com.example.customservermod.mixin;

import com.example.customservermod.CustomServerMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class InfiniteTotemMixin {

	@Inject(method = "checkTotemDeathProtection", at = @At("HEAD"), cancellable = true)
	private void onCheckTotem(DamageSource damageSource, CallbackInfoReturnable<Boolean> cir) {
		LivingEntity self = (LivingEntity)(Object)this;
		if (!(self instanceof ServerPlayer player)) return;

		ItemStack infiniteTotem = findInfiniteTotem(player);
		if (infiniteTotem.isEmpty()) return;

		// Check cooldown (30 seconds = 600 ticks)
		if (player.getCooldowns().isOnCooldown(infiniteTotem.getItem())) return;

		// Apply totem effects (like vanilla)
		player.setHealth(1.0F);
		player.removeAllEffects();
		player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 900, 1));
		player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 1));
		player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 800, 0));
		player.level().broadcastEntityEvent(player, (byte)35);

		// Set cooldown 30 seconds, don't consume item
		player.getCooldowns().addCooldown(infiniteTotem.getItem(), 600);

		cir.setReturnValue(true);
	}

	private ItemStack findInfiniteTotem(ServerPlayer player) {
		ItemStack mainHand = player.getMainHandItem();
		ItemStack offHand = player.getOffhandItem();
		var infiniteItem = BuiltInRegistries.ITEM.getValue(CustomServerMod.INFINITE_TOTEM_ID);

		if (!mainHand.isEmpty() && mainHand.is(infiniteItem)) return mainHand;
		if (!offHand.isEmpty() && offHand.is(infiniteItem)) return offHand;

		// Also check inventory for convenience
		for (ItemStack stack : player.getInventory().items) {
			if (!stack.isEmpty() && stack.is(infiniteItem)) return stack;
		}
		return ItemStack.EMPTY;
	}
}