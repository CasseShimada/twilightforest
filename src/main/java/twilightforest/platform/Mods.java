package twilightforest.platform;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import java.util.Optional;

public final class Mods {
	private Mods() {
	}

	public static boolean isLoaded(String modId) {
		return FabricLoader.getInstance().isModLoaded(modId);
	}

	public static Optional<ModContainer> container(String modId) {
		return FabricLoader.getInstance().getModContainer(modId);
	}

	public static Optional<String> version(String modId) {
		return container(modId).map(c -> c.getMetadata().getVersion().getFriendlyString());
	}
}

