package twilightforest.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import twilightforest.TwilightForestMod;

public record UncraftingGuiPacket(int operationType) implements CustomPacketPayload {

	public static final Type<UncraftingGuiPacket> TYPE = new Type<>(TwilightForestMod.prefix("switch_uncrafting_operation"));
	public static final StreamCodec<RegistryFriendlyByteBuf, UncraftingGuiPacket> STREAM_CODEC = CustomPacketPayload.codec(UncraftingGuiPacket::write, UncraftingGuiPacket::new);

	public UncraftingGuiPacket(FriendlyByteBuf buf) {
		this(buf.readInt());
	}

	public void write(FriendlyByteBuf buf) {
		buf.writeInt(this.operationType());
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

}
