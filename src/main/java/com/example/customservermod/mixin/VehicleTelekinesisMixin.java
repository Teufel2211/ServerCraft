package com.example.customservermod.mixin;

import com.example.customservermod.CustomServerMod;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VehicleEntity.class)
public abstract class VehicleTelekinesisMixin {

	@Shadow
	protected abstract Item getDropItem();

	@Inject(method = "destroy(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;)V", at = @At("HEAD"), cancellable = true)
	private void onDestroyWithDamageSource(ServerLevel level, DamageSource damageSource, CallbackInfo ci) {
		Entity attacker = damageSource.getEntity();
		if (!(attacker instanceof ServerPlayer player)) return;
		ItemStack tool = player.getMainHandItem();
		if (tool.isEmpty()) return;
		ResourceKey<Enchantment> teleKey = ResourceKey.create(Registries.ENCHANTMENT, CustomServerMod.TELEKINESIS_ID);
		boolean hasTele = false;
		for (Holder<Enchantment> h : tool.getEnchantments().keySet()) {
			if (h.is(teleKey)) { hasTele = true; break; }
		}
		if (!hasTele) return;

		VehicleEntity self = (VehicleEntity)(Object)this;
		ItemStack drop = new ItemStack(this.getDropItem());
		if (!player.getInventory().add(drop)) {
			return;
		}
		ci.cancel();
		self.discard();
	}
}