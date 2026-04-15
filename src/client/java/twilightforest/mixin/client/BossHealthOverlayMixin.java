package twilightforest.mixin.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.world.BossEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twilightforest.entity.boss.bar.ClientTFBossBar;

@Mixin(BossHealthOverlay.class)
public class BossHealthOverlayMixin {
	@Inject(method = "extractBar", at = @At("HEAD"), cancellable = true)
	private void twilightforest$drawCustomBossBar(GuiGraphicsExtractor graphics, int x, int y, BossEvent event, CallbackInfo ci) {
		if (event instanceof ClientTFBossBar bossEvent) {
			bossEvent.renderBossBar(graphics, x, y);
			ci.cancel();
		}
	}
}
