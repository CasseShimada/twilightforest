package twilightforest.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import twilightforest.TwilightForestMod;
import twilightforest.components.entity.TravellersWingsAttachment;

public record TravellersWingsStatePacket(
	int entityId,
	TravellersWingsAttachment.WingState state,
	boolean sidestepLeft,
	int doubleJumpTimer,
	int sidestepTimer
) implements CustomPacketPayload {
	public static final Type<TravellersWingsStatePacket> TYPE = new Type<>(TwilightForestMod.prefix("travellers_wings_state"));
	public static final StreamCodec<RegistryFriendlyByteBuf, TravellersWingsStatePacket> STREAM_CODEC = CustomPacketPayload.codec(TravellersWingsStatePacket::write, TravellersWingsStatePacket::new);

	public TravellersWingsStatePacket(int entityId, TravellersWingsAttachment.WingState state) {
		this(entityId, state, false, 0, 0);
	}

	private TravellersWingsStatePacket(RegistryFriendlyByteBuf buf) {
		this(buf.readInt(), buf.readEnum(TravellersWingsAttachment.WingState.class), buf.readBoolean(), buf.readInt(), buf.readInt());
	}

	private void write(RegistryFriendlyByteBuf buf) {
		buf.writeInt(this.entityId);
		buf.writeEnum(this.state);
		buf.writeBoolean(this.sidestepLeft);
		buf.writeInt(this.doubleJumpTimer);
		buf.writeInt(this.sidestepTimer);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
