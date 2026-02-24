package twilightforest.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;

public final class ClientPortalHelper {
	private ClientPortalHelper() {
	}

	public static boolean handlePortalScreenClose(Player player, boolean inPortal) {
		if (!inPortal) {
			return false;
		}
		if (!(player instanceof LocalPlayer local)) {
			return false;
		}

		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.screen != null && !minecraft.screen.isPauseScreen() && !(minecraft.screen instanceof DeathScreen)) {
			if (minecraft.screen instanceof AbstractContainerScreen) {
				local.closeContainer();
			}
			minecraft.setScreen(null);
		}
		return true;
	}
}
