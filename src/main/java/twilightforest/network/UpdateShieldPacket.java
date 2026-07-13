package twilightforest.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import twilightforest.TwilightForestMod;

public record UpdateShieldPacket(int entityID, int temporaryShields, int permanentShields) implements CustomPacketPayload {

	public static final Type<UpdateShieldPacket> TYPE = new Type<>(TwilightForestMod.prefix("update_shield_attachment"));
	public static final StreamCodec<RegistryFriendlyByteBuf, UpdateShieldPacket> STREAM_CODEC = CustomPacketPayload.codec(UpdateShieldPacket::write, UpdateShieldPacket::new);

	public UpdateShieldPacket(FriendlyByteBuf buf) {
		this(buf.readInt(), buf.readInt(), buf.readInt());
	}

	public void write(FriendlyByteBuf buf) {
		buf.writeInt(this.entityID());
		buf.writeInt(this.temporaryShields());
		buf.writeInt(this.permanentShields());
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

}
