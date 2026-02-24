package twilightforest.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.Objects;

public final class ClientPacketDistributor {
	private ClientPacketDistributor() {
	}

	public static void sendToServer(CustomPacketPayload payload) {
		Objects.requireNonNull(payload, "payload");
		ClientPlayNetworking.send(payload);
	}
}
