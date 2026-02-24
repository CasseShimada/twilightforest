package twilightforest.client;

import net.fabricmc.api.ClientModInitializer;
import twilightforest.client.model.ThreeLayerDeviceEmissiveModelPlugin;
import twilightforest.client.event.ClientEvents;
import twilightforest.client.event.LockedBiomeToastHandler;
import twilightforest.client.event.RegistrationEvents;
import twilightforest.network.TFClientNetworking;

public class TwilightForestClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		RegistrationEvents.register();
		ThreeLayerDeviceEmissiveModelPlugin.register();
		ClientEvents.register();
		LockedBiomeToastHandler.register();
		TFClientNetworking.init();
	}
}
