package com.example.customservermod.mixin;

import com.example.customservermod.msgspy.MsgSpyManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.commands.MsgCommand;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;

@Mixin(MsgCommand.class)
public class MsgCommandMixin {

	@Inject(method = "sendMessage", at = @At("TAIL"))
	private static void onSendMessage(CommandSourceStack source, Collection<ServerPlayer> targets, PlayerChatMessage message, CallbackInfo ci) {
		if (source.getServer() == null) return;
		String senderName = source.getTextName();
		String content = message.signedContent();
		for (ServerPlayer spy : source.getServer().getPlayerList().getPlayers()) {
			if (!spy.permissions().hasPermission(Permissions.COMMANDS_OWNER)) continue;
			if (!MsgSpyManager.isSpyEnabled(spy)) continue;
			if (spy.getName().getString().equals(senderName)) continue;
			boolean isTarget = false;
			for (ServerPlayer target : targets) {
				if (target.getUUID().equals(spy.getUUID())) { isTarget = true; break; }
			}
			if (isTarget) continue;
			String targetNames = targets.stream().map(p -> p.getName().getString()).reduce((a,b) -> a + ", " + b).orElse("?");
			Component spyMsg = Component.literal("§8[§cSpy§8] §7" + senderName + " §8-> §7" + targetNames + "§8: §f" + content);
			spy.sendSystemMessage(spyMsg, false);
		}
	}
}