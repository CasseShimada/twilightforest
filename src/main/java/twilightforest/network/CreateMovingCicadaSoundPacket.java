package twilightforest.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import twilightforest.TwilightForestMod;

public record CreateMovingCicadaSoundPacket(int entityID) implements CustomPacketPayload {

	public static final Type<CreateMovingCicadaSoundPacket> TYPE = new Type<>(TwilightForestMod.prefix("create_cicada_sound"));
	public static final StreamCodec<RegistryFriendlyByteBuf, CreateMovingCicadaSoundPacket> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.INT, CreateMovingCicadaSoundPacket::entityID,
		CreateMovingCicadaSoundPacket::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
