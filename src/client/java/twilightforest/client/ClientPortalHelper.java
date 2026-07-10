package twilightforest.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;

public final class ClientPortalHelper {
	private ClientPortalHelper() {
	}

	public static boolean handlePortalScreenClose(Player player) {
		if (!(player instanceof LocalPlayer local)) {
			return false;
		}

		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.gui.screen() != null && !minecraft.gui.screen().isPauseScreen() && !(minecraft.gui.screen() instanceof DeathScreen)) {
			if (minecraft.gui.screen() instanceof AbstractContainerScreen) {
				local.closeContainer();
			}
			minecraft.gui.setScreen(null);
		}
		return true;
	}
}
