package twilightforest.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import twilightforest.TwilightForestMod;

import java.util.UUID;

public record GradualGlidePacket(boolean isGraduallyGliding, UUID playerUUID) implements CustomPacketPayload {
	public static final Type<GradualGlidePacket> TYPE = new Type<>(TwilightForestMod.prefix("gradual_glide_packet"));
	public static final StreamCodec<RegistryFriendlyByteBuf, GradualGlidePacket> STREAM_CODEC = CustomPacketPayload.codec(GradualGlidePacket::write, GradualGlidePacket::new);

	private GradualGlidePacket(RegistryFriendlyByteBuf buf) {
		this(buf.readBoolean(), buf.readUUID());
	}

	private void write(RegistryFriendlyByteBuf buf) {
		buf.writeBoolean(this.isGraduallyGliding);
		buf.writeUUID(this.playerUUID);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
