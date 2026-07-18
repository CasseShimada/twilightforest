package twilightforest.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import twilightforest.TwilightForestMod;

public record PerformSidestepPacket(boolean isLeftStepSide) implements CustomPacketPayload {
	public static final Type<PerformSidestepPacket> TYPE = new Type<>(TwilightForestMod.prefix("perform_sidestep_packet"));
	public static final StreamCodec<RegistryFriendlyByteBuf, PerformSidestepPacket> STREAM_CODEC = CustomPacketPayload.codec(PerformSidestepPacket::write, PerformSidestepPacket::new);

	private PerformSidestepPacket(RegistryFriendlyByteBuf buf) {
		this(buf.readBoolean());
	}

	private void write(RegistryFriendlyByteBuf buf) {
		buf.writeBoolean(this.isLeftStepSide);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
