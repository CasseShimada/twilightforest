package twilightforest.mixin.client;

import net.fabricmc.fabric.api.client.rendering.v1.FabricRenderState;
import net.minecraft.client.renderer.MapRenderer;
import net.minecraft.client.renderer.state.MapRenderState;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twilightforest.client.renderer.map.TFMagicMapRenderKeys;
import twilightforest.item.mapdata.TFMagicMapData;

import java.util.List;

@Mixin(MapRenderer.class)
public class MapRendererMixin {
	@Inject(method = "extractRenderState", at = @At("TAIL"))
	private void twilightforest$extractRenderState(MapId mapId, MapItemSavedData data, MapRenderState state, CallbackInfo ci) {
		if (!(state instanceof FabricRenderState fabricState)) return;
		if (data instanceof TFMagicMapData magic) {
			fabricState.setData(TFMagicMapRenderKeys.MAGIC_MAP, true);
			fabricState.setData(TFMagicMapRenderKeys.CONQUERED_STRUCTURES, magic.conqueredStructures);
		} else {
			fabricState.setData(TFMagicMapRenderKeys.MAGIC_MAP, false);
			fabricState.setData(TFMagicMapRenderKeys.CONQUERED_STRUCTURES, List.of());
		}
	}
}
