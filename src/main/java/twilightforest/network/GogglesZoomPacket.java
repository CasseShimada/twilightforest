package twilightforest.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import twilightforest.TwilightForestMod;

import java.util.UUID;

public record GogglesZoomPacket(boolean isUsingZoom, UUID playerUUID) implements CustomPacketPayload {
	public static final Type<GogglesZoomPacket> TYPE = new Type<>(TwilightForestMod.prefix("goggles_zoom_packet"));
	public static final StreamCodec<RegistryFriendlyByteBuf, GogglesZoomPacket> STREAM_CODEC = CustomPacketPayload.codec(GogglesZoomPacket::write, GogglesZoomPacket::new);

	private GogglesZoomPacket(RegistryFriendlyByteBuf buf) {
		this(buf.readBoolean(), buf.readUUID());
	}

	private void write(RegistryFriendlyByteBuf buf) {
		buf.writeBoolean(this.isUsingZoom);
		buf.writeUUID(this.playerUUID);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
