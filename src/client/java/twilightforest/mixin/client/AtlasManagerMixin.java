package twilightforest.mixin.client;

import net.minecraft.client.resources.model.AtlasManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twilightforest.client.MagicPaintingTextureManager;

import java.util.ArrayList;
import java.util.List;

@Mixin(AtlasManager.class)
public class AtlasManagerMixin {
	@Shadow
	@Final
	@Mutable
	private static List<AtlasManager.AtlasConfig> KNOWN_ATLASES;

	@Inject(method = "<clinit>", at = @At("TAIL"))
	private static void twilightforest$registerMagicPaintingsAtlas(CallbackInfo ci) {
		List<AtlasManager.AtlasConfig> atlases = new ArrayList<>(KNOWN_ATLASES);
		atlases.add(new AtlasManager.AtlasConfig(
			MagicPaintingTextureManager.ATLAS_LOCATION,
			MagicPaintingTextureManager.ATLAS_INFO_LOCATION,
			false
		));
		KNOWN_ATLASES = List.copyOf(atlases);
	}
}
