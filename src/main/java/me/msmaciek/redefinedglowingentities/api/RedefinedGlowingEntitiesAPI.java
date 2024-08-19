package me.msmaciek.redefinedglowingentities.api;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import lombok.Getter;
import me.msmaciek.redefinedglowingentities.Utils;
import me.msmaciek.redefinedglowingentities.listeners.EntityMetadataPacketListener;
import me.msmaciek.redefinedglowingentities.listeners.EventListener;
import me.msmaciek.redefinedglowingentities.structs.GlowTeamCollisionRule;
import me.msmaciek.redefinedglowingentities.structs.GlowTeamNametagVisibility;
import me.msmaciek.redefinedglowingentities.structs.GlowTeamSettings;
import me.msmaciek.redefinedglowingentities.structs.QReversibleHashMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

import static org.bukkit.Bukkit.getServer;

@Getter
public class RedefinedGlowingEntitiesAPI {
	// Player UUID -> EntityID of entity that the player sees as glowing
	// UUID, ArrayList<Integer>
	private	final QReversibleHashMap<UUID, Integer> glowingEntities = new QReversibleHashMap<>();

	// team name -> team settings
	private final HashMap<String, GlowTeamSettings> entitiesData = new HashMap<>();

	public RedefinedGlowingEntitiesAPI(JavaPlugin plugin) {
		if(!PacketEvents.getAPI().isLoaded()) {
			PacketEvents.setAPI(SpigotPacketEventsBuilder.build(plugin));

			PacketEvents.getAPI().getSettings()
				.reEncodeByDefault(true)
				.checkForUpdates(false)
				.bStats(false);
		}

		PacketEvents.getAPI().getEventManager().registerListener(
			new EntityMetadataPacketListener(this),
			PacketListenerPriority.LOW
		);

		getServer().getPluginManager().registerEvents(new EventListener(this), plugin);
		PacketEvents.getAPI().load();
	}

	private boolean isTeamSettingsAbsent(UUID receiverUUID, int targetId) {
		return !entitiesData.containsKey(Utils.getTeamName(receiverUUID, targetId));
	}

	private void setTeamSettingsIfAbsent(Player receiver, Entity target) {
		if(isTeamSettingsAbsent(receiver.getUniqueId(), target.getEntityId()))
			setTeamSettings(receiver, target, GlowTeamSettings.builder().build());
	}
	private void setTeamSettings(Player receiver, Entity target, GlowTeamSettings teamSettings) {
		UUID receiverUUID = receiver.getUniqueId();

		glowingEntities.getAndAdd(receiverUUID, target.getEntityId());
		entitiesData.put(Utils.getTeamName(receiverUUID, target.getEntityId()), teamSettings);

		resendTeam(receiver, target);
		resendEntityMetadata(receiver, target);
	}

	public void resendTeam(Player receiver, Entity target) {
		GlowTeamSettings teamSettings = getEntityData(receiver.getUniqueId(), target.getEntityId());

		String entityTeamId = target.getUniqueId().toString();
		if(target instanceof Player)
			entityTeamId = target.getName();

		var teamRemovePacket = new WrapperPlayServerTeams(
			Utils.getTeamName(receiver, target.getEntityId()),
			WrapperPlayServerTeams.TeamMode.REMOVE,
			Optional.empty()
		);

		PacketEvents.getAPI().getPlayerManager().sendPacket(receiver, teamRemovePacket);

		var teamCreatePacket = new WrapperPlayServerTeams(
			Utils.getTeamName(receiver, target.getEntityId()),
			WrapperPlayServerTeams.TeamMode.CREATE,
			new WrapperPlayServerTeams.ScoreBoardTeamInfo(
				Component.empty(),
				Component.empty(),
				Component.empty(),
				WrapperPlayServerTeams.NameTagVisibility.fromID(teamSettings.nametagVisibility.name()),
				WrapperPlayServerTeams.CollisionRule.fromID(teamSettings.collisionRule.name()),
				teamSettings.color,
				WrapperPlayServerTeams.OptionData.NONE
			),
			List.of(entityTeamId)
		);

		PacketEvents.getAPI().getPlayerManager().sendPacket(receiver, teamCreatePacket);
	}

	//#region properties
	public void setGlowing(Player receiver, Entity target, NamedTextColor color) {
		setTeamSettingsIfAbsent(receiver, target);
		getEntityData(receiver.getUniqueId(), target.getEntityId()).color = color;
		getEntityData(receiver.getUniqueId(), target.getEntityId()).glowingEnabled = true;
		resendTeam(receiver, target);
		resendEntityMetadata(receiver, target);
	}

	public void unsetGlowing(Player receiver, Entity target) {
		getEntityData(receiver.getUniqueId(), target.getEntityId()).color = NamedTextColor.WHITE;
		getEntityData(receiver.getUniqueId(), target.getEntityId()).glowingEnabled = false;
		resendTeam(receiver, target);

		if(!removeIfDefault(receiver, target))
			resendEntityMetadata(receiver, target);
	}

	public void setNametagVisiblity(Player receiver, Entity target, GlowTeamNametagVisibility visibility) {
		setTeamSettingsIfAbsent(receiver, target);
		getEntityData(receiver.getUniqueId(), target.getEntityId()).nametagVisibility = visibility;
		resendTeam(receiver, target);

		if(!removeIfDefault(receiver, target))
			resendEntityMetadata(receiver, target);
	}

	public void setCollisionRule(Player receiver, Entity target, GlowTeamCollisionRule collisionRule) {
		setTeamSettingsIfAbsent(receiver, target);
		getEntityData(receiver.getUniqueId(), target.getEntityId()).collisionRule = collisionRule;
		resendTeam(receiver, target);

		if(!removeIfDefault(receiver, target))
			resendEntityMetadata(receiver, target);
	}

	public void setGlowingColor(Player receiver, Entity target, NamedTextColor color) {
		setTeamSettingsIfAbsent(receiver, target);
		getEntityData(receiver.getUniqueId(), target.getEntityId()).color = color;
		resendTeam(receiver, target);

		if(!removeIfDefault(receiver, target))
			resendEntityMetadata(receiver, target);
	}
	//#endregion

	// Assumes that the player left
	public void removeDataForLeftPlayer(Player receiver) {
		UUID receiverUUID = receiver.getUniqueId();

		if(glowingEntities.containsKey(receiverUUID)) {
			while(true) {
				ArrayList<Integer> ar = glowingEntities.getReadOnly(receiverUUID);

				if(ar == null || ar.isEmpty())
					break;

				int entityId = ar.get(0);

				glowingEntities.getAndRemove(receiverUUID, entityId);
				entitiesData.remove(Utils.getTeamName(receiverUUID, entityId));
			}
		}

		removeDataForEntity(receiver);
	}

	// Sends packet to all player-observers and removes data
	public void removeDataForEntity(Entity entity) {
		int entityId = entity.getEntityId();

		if(!glowingEntities.getReversedHashMap().containsKey(entity.getEntityId()))
			return;

		while(true) {
			ArrayList<UUID> ar = glowingEntities.getReversedHashMap().get(entityId);

			if (ar == null || ar.isEmpty())
				break;

			UUID receiverUUID = ar.get(0);

			makeDefault(receiverUUID, entity.getEntityId());
			removeIfDefault(Objects.requireNonNull(Bukkit.getPlayer(receiverUUID)), entity);
		}
	}

	public void makeDefault(UUID receiverUUID, int entityId) {
		entitiesData.put(Utils.getTeamName(receiverUUID, entityId), GlowTeamSettings.builder().build());
	}

	private boolean removeIfDefault(UUID receiverUUID, int entityId) {
		if(isTeamSettingsAbsent(receiverUUID, entityId))
			return false;

		if(!getEntityData(receiverUUID, entityId).isDefault())
			return false;

		glowingEntities.getAndRemove(receiverUUID, entityId);
		entitiesData.remove(Utils.getTeamName(receiverUUID, entityId));
		return true;
	}

	// Removes all data if they're default ones
	// Return true if data was removed
	public boolean removeIfDefault(Player receiver, Entity target) {
		UUID receiverUUID = receiver.getUniqueId();
		int entityId = target.getEntityId();

		if(!removeIfDefault(receiverUUID, entityId))
			return false;

		var teamRemovePacket = new WrapperPlayServerTeams(
			Utils.getTeamName(receiver, entityId),
			WrapperPlayServerTeams.TeamMode.REMOVE,
			(WrapperPlayServerTeams.ScoreBoardTeamInfo) null
		);

		PacketEvents.getAPI().getPlayerManager().sendPacket(receiver, teamRemovePacket);
		resendEntityMetadata(receiver, target);
		return true;
	}

	public void resendEntityMetadata(Player receiver, Entity target) {
		var packet = new WrapperPlayServerEntityMetadata(
			target.getEntityId(),
			List.of(
				new EntityData(0, EntityDataTypes.BYTE, getMetadataByte(target))
			)
		);

		PacketEvents.getAPI().getPlayerManager().sendPacket(receiver, packet);
	}
	private byte getMetadataByte(Entity entity) {
		byte output = 0x00;

		if (entity.isVisualFire()) output |= 0x01;
		if (entity instanceof Player player) {
			if (player.isSneaking()) output |= 0x02;
			if (player.isSprinting()) output |= 0x08;
			if (player.isSwimming()) output |= 0x10;
			if (player.isGliding()) output |= (byte) 0x80;
		}
		if (entity instanceof LivingEntity livingEntity) {
			if (livingEntity.isInvisible()) output |= 0x20;
		}
		if (entity.isGlowing()) output |= 0x40;

		return output;
	}

	public GlowTeamSettings getEntityData(UUID receiverUUID, int targetId) {
		return entitiesData.get(Utils.getTeamName(receiverUUID, targetId));
	}

	public boolean isEntityDataSet(UUID receiverUUID, int targetId) {
		return entitiesData.containsKey(Utils.getTeamName(receiverUUID, targetId));
	}

	public boolean isPlayerDataSet(Player receiver, Player target) {
		return isEntityDataSet(receiver.getUniqueId(), target.getEntityId());
	}
}
