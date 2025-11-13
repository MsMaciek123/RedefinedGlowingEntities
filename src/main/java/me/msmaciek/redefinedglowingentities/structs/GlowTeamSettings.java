package me.msmaciek.redefinedglowingentities.structs;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import net.kyori.adventure.text.format.NamedTextColor;

@Builder(toBuilder = true) @ToString @Getter
public class GlowTeamSettings {
	@Builder.Default private final boolean glowingEnabled = false;
	@Builder.Default private final NamedTextColor color = NamedTextColor.WHITE;
	@Builder.Default private final GlowTeamNametagVisibility nametagVisibility = GlowTeamNametagVisibility.ALWAYS;
	@Builder.Default private final GlowTeamCollisionRule collisionRule = GlowTeamCollisionRule.ALWAYS;

	public boolean isDefault() {
		return !glowingEnabled
			&& color.equals(NamedTextColor.WHITE)
			&& nametagVisibility.equals(GlowTeamNametagVisibility.ALWAYS)
			&& collisionRule.equals(GlowTeamCollisionRule.ALWAYS);
	}
}
