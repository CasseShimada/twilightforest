package twilightforest.init;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import twilightforest.TwilightForestMod;
import twilightforest.potions.FrostedEffect;

public class TFMobEffects {

	public static final MobEffect FROSTY = new FrostedEffect();
	private static boolean registered;

	public static void register() {
		if (registered) {
			return;
		}

		registered = true;
		Registry.register(BuiltInRegistries.MOB_EFFECT, TwilightForestMod.prefix("frosted"), FROSTY);
	}
}
