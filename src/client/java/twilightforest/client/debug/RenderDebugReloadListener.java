package twilightforest.client.debug;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import twilightforest.TwilightForestMod;

import java.util.List;

public final class RenderDebugReloadListener implements ResourceManagerReloadListener, IdentifiableResourceReloadListener {
	public static final RenderDebugReloadListener INSTANCE = new RenderDebugReloadListener();
	private static final Identifier ID = TwilightForestMod.prefix("render_debug");

	private static final List<Identifier> RESOURCES = List.of(
		TwilightForestMod.prefix("models/block/util/three_layer_block.json"),
		TwilightForestMod.prefix("models/block/util/three_layer_device.json"),
		TwilightForestMod.prefix("models/block/vanishing_block.json"),
		TwilightForestMod.prefix("models/block/reappearing_block.json"),
		TwilightForestMod.prefix("models/block/ghast_trap.json"),
		TwilightForestMod.prefix("models/block/torchberry_plant.json"),
		TwilightForestMod.prefix("models/block/mushgloom.json"),
		TwilightForestMod.prefix("models/block/fiddlehead.json"),
		TwilightForestMod.prefix("models/block/rope_x.json"),
		TwilightForestMod.prefix("textures/block/vanishing_block.png"),
		TwilightForestMod.prefix("textures/block/reappearing_block.png"),
		TwilightForestMod.prefix("textures/block/ghast_trap.png"),
		TwilightForestMod.prefix("textures/block/torchberry_plant.png"),
		TwilightForestMod.prefix("textures/block/mushgloom.png"),
		TwilightForestMod.prefix("textures/block/fiddlehead.png"),
		TwilightForestMod.prefix("textures/block/rope.png")
	);

	private RenderDebugReloadListener() {
	}

	@Override
	public Identifier getFabricId() {
		return ID;
	}

	@Override
	public void onResourceManagerReload(ResourceManager manager) {
		for (Identifier id : RESOURCES) {
			if (manager.getResource(id).isEmpty()) {
				TwilightForestMod.LOGGER.warn("[TF Debug] Missing resource {}", id);
			} else {
				TwilightForestMod.LOGGER.info("[TF Debug] Found resource {}", id);
			}
		}
	}
}
