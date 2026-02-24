package twilightforest.client.renderer.map;

import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import twilightforest.TwilightForestMod;

import java.util.List;

public final class TFMagicMapRenderKeys {
	private TFMagicMapRenderKeys() {
	}

	public static final RenderStateDataKey<Boolean> MAGIC_MAP = RenderStateDataKey.create(() -> TwilightForestMod.prefix("is_magic_map").toString());
	public static final RenderStateDataKey<List<String>> CONQUERED_STRUCTURES = RenderStateDataKey.create(() -> TwilightForestMod.prefix("conquered_structures").toString());
}
