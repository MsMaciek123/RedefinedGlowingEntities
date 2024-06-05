package me.msmaciek.redefinedglowingentities.listeners;

import me.msmaciek.redefinedglowingentities.api.RedefinedGlowingEntitiesAPI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class EventListener implements Listener {
	private final RedefinedGlowingEntitiesAPI geAPI;

	public EventListener(RedefinedGlowingEntitiesAPI geAPI) {
		this.geAPI = geAPI;
	}

	// Make sure there are no leftovers
	@EventHandler
	public void onEntityDeath(EntityDeathEvent e) {
		geAPI.removeDataForEntity(e.getEntity());
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		geAPI.removeDataForLeftPlayer(e.getPlayer());
	}
}
