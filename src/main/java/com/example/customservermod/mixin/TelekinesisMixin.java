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
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
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
import java.util.Optional;

@Mixin(Block.class)
public class TelekinesisMixin {

	@Inject(method = "dropResources(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)V", at = @At("HEAD"), cancellable = true)
	private static void onDropResources(BlockState state, Level level, BlockPos pos, BlockEntity blockEntity, Entity entity, ItemStack tool, CallbackInfo ci) {
		if (!(entity instanceof ServerPlayer player)) return;
		if (level.isClientSide()) return;
		if (!(level instanceof ServerLevel serverLevel)) return;

		ItemStack mainHand = player.getMainHandItem();
		ItemStack checkStack = tool.isEmpty() ? mainHand : tool;
		if (checkStack.isEmpty()) return;

		ResourceKey<Enchantment> teleKey = ResourceKey.create(Registries.ENCHANTMENT, CustomServerMod.TELEKINESIS_ID);
		ResourceKey<Enchantment> smeltKey = ResourceKey.create(Registries.ENCHANTMENT, CustomServerMod.AUTO_SMELTING_ID);
		boolean hasTele = false;
		boolean hasSmelt = false;
		for (Holder<Enchantment> h : checkStack.getEnchantments().keySet()) {
			if (h.is(teleKey)) hasTele = true;
			if (h.is(smeltKey)) hasSmelt = true;
		}
		if (!hasTele && !hasSmelt) return;

		List<ItemStack> drops = Block.getDrops(state, serverLevel, pos, blockEntity, player, checkStack);
		for (ItemStack drop : drops) {
			if (drop.isEmpty()) continue;
			ItemStack out = hasSmelt ? trySmelt(serverLevel, drop) : drop;
			if (hasTele) {
				if (!player.getInventory().add(out)) {
					Block.popResource(level, pos, out);
				}
			} else {
				Block.popResource(level, pos, out);
			}
		}
		ci.cancel();
	}

	private static ItemStack trySmelt(ServerLevel level, ItemStack stack) {
		try {
			var input = new SingleRecipeInput(stack);
			Optional<net.minecraft.world.item.crafting.RecipeHolder<net.minecraft.world.item.crafting.SmeltingRecipe>> opt = level.recipeAccess().getRecipeFor(RecipeType.SMELTING, input, level);
			if (opt.isPresent()) {
				ItemStack result = opt.get().value().assemble(input);
				result.setCount(stack.getCount());
				return result;
			}
		} catch (Exception ignored) {}
		return stack;
	}
}