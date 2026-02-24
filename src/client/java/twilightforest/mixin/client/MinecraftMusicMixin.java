package twilightforest.mixin.client;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.Musics;
import net.minecraft.world.attribute.BackgroundMusic;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.material.FogType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twilightforest.init.TFDimension;

import java.util.Optional;

@Mixin(Minecraft.class)
public class MinecraftMusicMixin {
	@Inject(method = "getSituationalMusic", at = @At("RETURN"), cancellable = true)
	private void twilightforest$overrideSituationalMusic(CallbackInfoReturnable<Music> cir) {
		Minecraft mc = (Minecraft) (Object) this;
		if (mc.level == null || mc.player == null || !TFDimension.isTwilightWorldOnClient(mc.level)) {
			return;
		}

		Music original = cir.getReturnValue();
		if (original != Musics.CREATIVE && original != Musics.UNDER_WATER) {
			return;
		}

		Camera camera = mc.gameRenderer.getMainCamera();
		BackgroundMusic backgroundMusic = camera.attributeProbe().getValue(EnvironmentAttributes.BACKGROUND_MUSIC, mc.getDeltaTracker().getGameTimeDeltaPartialTick(false));
		if (backgroundMusic == null) {
			return;
		}

		boolean isCreative = mc.player.isCreative();
		boolean isUnderwater = camera.getFluidInCamera() == FogType.WATER;
		Optional<Music> selected = backgroundMusic.select(isCreative, isUnderwater);
		cir.setReturnValue(selected.orElse(Musics.GAME));
	}
}
