package me.msmaciek.redefinedglowingentities.listeners;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import me.msmaciek.redefinedglowingentities.Utils;
import me.msmaciek.redefinedglowingentities.api.RedefinedGlowingEntitiesAPI;

import java.util.List;
import java.util.UUID;

public class EntityMetadataPacketListener implements PacketListener {
	public final int GLOWING_METADATA_INDEX = 0;
	public final byte GLOWING_METADATA_BYTE_VALUE = 0x40;

	private final RedefinedGlowingEntitiesAPI geAPI;

	public EntityMetadataPacketListener(RedefinedGlowingEntitiesAPI geAPI) {
		this.geAPI = geAPI;
	}

	@Override
	public void onPacketSend(PacketSendEvent event) {
		if (event.getPacketType() != PacketType.Play.Server.ENTITY_METADATA)
			return;

		var packet = new WrapperPlayServerEntityMetadata(event);

		int entityId = packet.getEntityId();
		UUID receiverUUID = event.getUser().getUUID();

		String teamName = Utils.getTeamName(receiverUUID, entityId);

		if(!geAPI.getEntitiesData().containsKey(teamName))
			return;

		if(!geAPI.getEntitiesData().get(teamName).glowingEnabled)
			return;

		boolean glowingByteSet = false;
		for (var metadata : packet.getEntityMetadata()) {
			if (metadata.getIndex() != GLOWING_METADATA_INDEX)
				continue;

			byte byteWithGlowing = (byte) ((byte) metadata.getValue() | GLOWING_METADATA_BYTE_VALUE);
			metadata.setValue(byteWithGlowing);
			glowingByteSet = true;
		}

		// no metadata for index 0, creating one with only glowing
		if(!glowingByteSet) {
			List<EntityData> metadataList = packet.getEntityMetadata();
			metadataList.add(new EntityData(GLOWING_METADATA_INDEX, EntityDataTypes.BYTE, GLOWING_METADATA_BYTE_VALUE));
		}
	}
}
