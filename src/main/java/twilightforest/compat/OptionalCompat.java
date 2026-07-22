package twilightforest.compat;

import twilightforest.compat.carryon.CarryOnCompat;
import twilightforest.compat.diggus.DiggusMaximusCompat;
import twilightforest.platform.Mods;

/** Loads integrations only after Fabric confirms their owner mod is present. */
public final class OptionalCompat {
	private OptionalCompat() {
	}

	public static void init() {
		if (Mods.isLoaded("diggusmaximus")) {
			DiggusMaximusCompat.init();
		}
		if (Mods.isLoaded("carryon")) {
			CarryOnCompat.init();
		}
	}
}
