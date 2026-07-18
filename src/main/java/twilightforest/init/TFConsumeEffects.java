package twilightforest.init;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import twilightforest.TwilightForestMod;
import twilightforest.item.effects.StackableEffectConsumeEffect;

public final class TFConsumeEffects {
	public static final ConsumeEffect.Type<StackableEffectConsumeEffect> STACKABLE_EFFECTS = Registry.register(
		BuiltInRegistries.CONSUME_EFFECT_TYPE,
		TwilightForestMod.prefix("stackable_effects"),
		new ConsumeEffect.Type<>(StackableEffectConsumeEffect.CODEC, StackableEffectConsumeEffect.STREAM_CODEC)
	);

	private TFConsumeEffects() {
	}

	public static void init() {
	}
}
