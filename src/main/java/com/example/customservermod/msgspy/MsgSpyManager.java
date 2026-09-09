package com.example.customservermod.msgspy;

import net.minecraft.server.level.ServerPlayer;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MsgSpyManager {
	private static final Set<UUID> SPY_ENABLED = Collections.newSetFromMap(new ConcurrentHashMap<>());

	public static boolean isSpyEnabled(ServerPlayer player) {
		return SPY_ENABLED.contains(player.getUUID());
	}

	public static boolean toggleSpy(ServerPlayer player) {
		UUID id = player.getUUID();
		if (SPY_ENABLED.contains(id)) {
			SPY_ENABLED.remove(id);
			return false;
		} else {
			SPY_ENABLED.add(id);
			return true;
		}
	}

	public static Set<UUID> getSpyPlayers() {
		return Collections.unmodifiableSet(SPY_ENABLED);
	}
}