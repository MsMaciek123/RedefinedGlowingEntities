package me.msmaciek.redefinedglowingentities;

import org.bukkit.entity.Player;

import java.util.UUID;

public class Utils {
	public static String getTeamName(Player receiver, int targetEntityId) {
		return getTeamName(receiver.getUniqueId(), targetEntityId);
	}

	public static String getTeamName(UUID uuid, int targetEntityId) {
		return "glowapi-" + uuid + "-" + targetEntityId;
	}
}
