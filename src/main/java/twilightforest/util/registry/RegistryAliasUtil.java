package twilightforest.util.registry;

import net.fabricmc.fabric.api.event.registry.FabricRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import twilightforest.TwilightForestMod;

import java.util.Map;

public final class RegistryAliasUtil {
	private RegistryAliasUtil() {
	}

	public static void applyAliases(Registry<?> registry, Map<Identifier, Identifier> aliases) {
		if (!(registry instanceof FabricRegistry fabricRegistry)) {
			throw new IllegalStateException("Registry does not support Fabric aliases: " + registry.key());
		}

		aliases.forEach((from, to) -> {
			if (registry.containsKey(from)) {
				TwilightForestMod.LOGGER.warn("Skipping registry alias {} -> {} in {} because the source id is already registered.", from, to, registry.key());
				return;
			}
			if (!registry.containsKey(to)) {
				TwilightForestMod.LOGGER.warn("Skipping registry alias {} -> {} in {} because the target id is missing.", from, to, registry.key());
				return;
			}

			fabricRegistry.addAlias(from, to);
		});
	}
}
