package twilightforest.entity.boss.bar;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import twilightforest.network.PacketDistributor;
import twilightforest.network.TFBossBarPacket;

public class ServerTFBossBar extends ServerBossEvent {
	private int color;

	public ServerTFBossBar(Component name, int color, BossBarOverlay overlay) {
		super(name, BossBarColor.WHITE, overlay);
		this.color = color;
	}

	public int getBarColor() {
		return this.color;
	}

	@Override
	public void addPlayer(ServerPlayer player) {
		boolean wasPresent = this.getPlayers().contains(player);
		super.addPlayer(player);
		if (!wasPresent && this.isVisible()) {
			PacketDistributor.sendToPlayer(player, new TFBossBarPacket.AddTFBossBarPacket(this));
		}
	}

	public void updateStyle(int color, BossBarOverlay overlay, boolean allowLerp) {
		boolean change = false;
		if (this.color != color) {
			this.color = color;
			change = true;
		}
		if (this.overlay != overlay) {
			this.overlay = overlay;
			change = true;
		}
		if (change) this.getPlayers().forEach(serverPlayer -> PacketDistributor.sendToPlayer(serverPlayer, new TFBossBarPacket.UpdateTFBossBarStylePacket(this, allowLerp)));
	}
}
