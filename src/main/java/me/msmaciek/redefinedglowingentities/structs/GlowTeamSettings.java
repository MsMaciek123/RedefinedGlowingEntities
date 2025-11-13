package me.msmaciek.redefinedglowingentities.structs;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

@Builder(toBuilder = true) @ToString @Getter
public class GlowTeamSettings {
	@Builder.Default private final boolean glowingEnabled = false;
	@Builder.Default private final NamedTextColor color = NamedTextColor.WHITE;
	@Builder.Default private final GlowTeamNametagVisibility nametagVisibility = GlowTeamNametagVisibility.ALWAYS;
	@Builder.Default private final GlowTeamCollisionRule collisionRule = GlowTeamCollisionRule.ALWAYS;
	@Builder.Default private final Component displayName = Component.empty();
	@Builder.Default private final Component prefix = Component.empty();
	@Builder.Default private final Component suffix = Component.empty();

	public boolean isDefault() {
		return !glowingEnabled
			&& color.equals(NamedTextColor.WHITE)
			&& nametagVisibility.equals(GlowTeamNametagVisibility.ALWAYS)
			&& collisionRule.equals(GlowTeamCollisionRule.ALWAYS)
			&& displayName.equals(Component.empty())
			&& prefix.equals(Component.empty())
			&& suffix.equals(Component.empty());
	}
}
