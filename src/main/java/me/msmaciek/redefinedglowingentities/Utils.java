package me.msmaciek.redefinedglowingentities;

import org.bukkit.entity.Player;

import java.util.UUID;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

public class Utils {
	// Maximum length for a scoreboard team name in Minecraft is 16 characters.
	private static final int MAX_TEAM_NAME_LENGTH = 16;
	private static final String TEAM_NAME_PREFIX = "rge"; // short plugin prefix (Redefined Glowing Entities)

	public static String getTeamName(Player receiver, int targetEntityId) {
		return getTeamName(receiver.getUniqueId(), targetEntityId);
	}

	public static String getTeamName(UUID uuid, int targetEntityId) {
		// Build a raw string to hash; includes UUID and entity id for uniqueness.
		String raw = uuid.toString() + ":" + targetEntityId;
		String hashHex = sha1Hex(raw);
		// We only need enough hex chars to fit into the limit with the prefix.
		int remaining = MAX_TEAM_NAME_LENGTH - TEAM_NAME_PREFIX.length();
        if (hashHex.length() > remaining) {
			hashHex = hashHex.substring(0, remaining);
		}
		return TEAM_NAME_PREFIX + hashHex;
	}

	private static String sha1Hex(String input) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
			// Convert to hex
			StringBuilder sb = new StringBuilder(digest.length * 2);
			for (byte b : digest) {
				int v = b & 0xFF;
				if (v < 0x10) sb.append('0');
				sb.append(Integer.toHexString(v));
			}
			return sb.toString();
		} catch (NoSuchAlgorithmException e) {
			// Should never happen for SHA-1; fallback to simple hashCode hex.
			String hex = Integer.toHexString(input.hashCode());
			// Ensure lowercase and only hex digits (hashCode may be negative; handle sign).
			if (hex.startsWith("-")) {
				hex = hex.substring(1);
			}
			return hex.toLowerCase();
		}
	}
}
