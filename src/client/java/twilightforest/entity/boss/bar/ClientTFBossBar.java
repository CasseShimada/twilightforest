package twilightforest.entity.boss.bar;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import twilightforest.mixin.client.accessor.BossHealthOverlayAccessor;

import java.util.UUID;

public class ClientTFBossBar extends LerpingBossEvent {
	private int color;

	public ClientTFBossBar(UUID id, Component name, float progress, int color, BossBarOverlay overlay, boolean darkenScreen, boolean bossMusic, boolean worldFog) {
		super(id, name, progress, BossBarColor.WHITE, overlay, darkenScreen, bossMusic, worldFog);
		this.color = color;
	}

	public void setBarColor(int color) {
		this.color = color;
	}

	public long getSetTime() {
		return this.setTime;
	}

	public void setSetTime(long setTime) {
		this.setTime = setTime;
	}

	private static final Identifier BAR_BACKGROUND = Identifier.withDefaultNamespace("boss_bar/white_background");
	private static final Identifier BAR_PROGRESS = Identifier.withDefaultNamespace("boss_bar/white_progress");

	public void renderBossBar(GuiGraphics guiGraphics, int x, int y) {
		int tint = 0xFF000000 | (this.color & 0xFFFFFF);
		guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, BAR_BACKGROUND, x, y, 182, 5, tint);
		if (this.overlay != BossEvent.BossBarOverlay.PROGRESS) {
			Identifier overlaySprite = BossHealthOverlayAccessor.twilightforest$getOverlayBackgroundSprites()[this.overlay.ordinal() - 1];
			guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, overlaySprite, x, y, 182, 5, tint);
		}
		int progress = Mth.lerpDiscrete(this.getProgress(), 0, 182);
		if (progress > 0) {
			guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, BAR_PROGRESS, x, y, progress, 5, tint);
			if (this.overlay != BossEvent.BossBarOverlay.PROGRESS) {
				Identifier overlaySprite = BossHealthOverlayAccessor.twilightforest$getOverlayProgressSprites()[this.overlay.ordinal() - 1];
				guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, overlaySprite, x, y, progress, 5, tint);
			}
		}

		Component title = this.getName();
		int width = Minecraft.getInstance().font.width(title);
		int fontX = guiGraphics.guiWidth() / 2 - width / 2;
		int fontY = y - 9;
		guiGraphics.drawString(Minecraft.getInstance().font, title, fontX, fontY, 0xFFFFFF);
	}
}
