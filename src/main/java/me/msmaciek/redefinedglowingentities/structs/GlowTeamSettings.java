package me.msmaciek.redefinedglowingentities.structs;

import lombok.Builder;
import lombok.ToString;
import net.kyori.adventure.text.format.NamedTextColor;

@Builder @ToString
public class GlowTeamSettings {
	@Builder.Default public boolean glowingEnabled = false;
	@Builder.Default public NamedTextColor color = NamedTextColor.WHITE;
	@Builder.Default public GlowTeamNametagVisibility nametagVisibility = GlowTeamNametagVisibility.ALWAYS;
	@Builder.Default public GlowTeamCollisionRule collisionRule = GlowTeamCollisionRule.ALWAYS;

	public boolean isDefault() {
		return !glowingEnabled
			&& color.equals(NamedTextColor.WHITE)
			&& nametagVisibility.equals(GlowTeamNametagVisibility.ALWAYS)
			&& collisionRule.equals(GlowTeamCollisionRule.ALWAYS);
	}
}
