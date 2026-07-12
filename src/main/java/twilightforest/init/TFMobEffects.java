package twilightforest.init;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import twilightforest.TwilightForestMod;
import twilightforest.potions.FrostedEffect;

public class TFMobEffects {
	public static final Holder<MobEffect> FROSTY = register("frosted", new FrostedEffect());

	private static Holder<MobEffect> register(String name, MobEffect effect) {
		MobEffect value = Registry.register(BuiltInRegistries.MOB_EFFECT, TwilightForestMod.prefix(name), effect);
		return BuiltInRegistries.MOB_EFFECT.wrapAsHolder(value);
	}

	public static void init() {
	}
}
