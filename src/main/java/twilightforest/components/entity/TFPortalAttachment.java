package twilightforest.components.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import twilightforest.block.TFPortalBlock;

public class TFPortalAttachment {
	public static int MAX_TICKS = 60;
	protected boolean isInsidePortal = false;
	protected int portalTimer = 0;

	public void setInPortal(boolean inPortal) {
		this.isInsidePortal = inPortal;
	}

	public boolean isInsidePortal() {
		return this.isInsidePortal;
	}

	public int getPortalTimer() {
		return this.portalTimer;
	}

	public void tick(Player player) {
		if (this.isInsidePortal()) {
			this.portalTimer = Math.min(this.portalTimer + 1, MAX_TICKS);

			if (!player.isInWall()) {
				BlockPos pos = player.blockPosition();
				if (!(player.level().getBlockState(pos).getBlock() instanceof TFPortalBlock) && !(player.level().getBlockState(pos.below()).getBlock() instanceof TFPortalBlock)) {
					this.isInsidePortal = false;
				}
			}
		} else if (this.getPortalTimer() > 0) this.portalTimer -= 2;

		if (player.level().isClientSide() && this.isInsidePortal() && handleClientPortalScreenClose(player)) {
			this.isInsidePortal = false;
		}
	}

	private boolean handleClientPortalScreenClose(Player player) {
		if (FabricLoader.getInstance().getEnvironmentType() != EnvType.CLIENT) {
			return false;
		}

		try {
			Class<?> helper = Class.forName("twilightforest.client.ClientPortalHelper");
			return (Boolean) helper.getMethod("handlePortalScreenClose", Player.class, boolean.class).invoke(null, player, true);
		} catch (Throwable ignored) {
			return false;
		}
	}
}
