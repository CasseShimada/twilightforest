package twilightforest.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.Minecraft;
import twilightforest.client.overlay.display.ItemDisplayRenderers;
import twilightforest.config.TFConfig;
import twilightforest.client.model.ThreeLayerDeviceEmissiveModelPlugin;
import twilightforest.client.event.ClientEvents;
import twilightforest.client.event.LockedBiomeToastHandler;
import twilightforest.client.event.RegistrationEvents;
import twilightforest.client.event.TravellersClientEvents;
import twilightforest.init.TFKeyBinds;
import twilightforest.item.travellers_gear.modifiers.TooltipStringInterpolator;
import twilightforest.item.travellers_gear.modifiers.TravellersTooltipContext;
import twilightforest.network.TFClientNetworking;

public class TwilightForestClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		TFConfig.loadClient();
		TFKeyBinds.init();
		ItemDisplayRenderers.init();
		TravellersTooltipContext.registerClient(TwilightForestClient::hasShiftDown, TooltipStringInterpolator::render);
		RegistrationEvents.register();
		ThreeLayerDeviceEmissiveModelPlugin.register();
		ClientEvents.register();
		TravellersClientEvents.register();
		LockedBiomeToastHandler.register();
		TFClientNetworking.init();
	}

	private static boolean hasShiftDown() {
		var window = Minecraft.getInstance().getWindow();
		return InputConstants.isKeyDown(window, InputConstants.KEY_LSHIFT)
			|| InputConstants.isKeyDown(window, InputConstants.KEY_RSHIFT);
	}
}
