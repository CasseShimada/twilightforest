package twilightforest.init;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import twilightforest.TwilightForestMod;
import twilightforest.enchantment.ApplyFrostedEffect;
import twilightforest.enchantment.RechargeScepterEffect;
import twilightforest.enchantment.SmashBlocksEffect;

import java.util.LinkedHashMap;
import java.util.Map;

public class TFEnchantmentEffects {

	private static final Map<Identifier, MapCodec<? extends EnchantmentEntityEffect>> ENTITY_EFFECTS = new LinkedHashMap<>();
	private static boolean registered;

	public static final MapCodec<ApplyFrostedEffect> APPLY_FROSTED = register("apply_frosted", ApplyFrostedEffect.CODEC);
	public static final MapCodec<RechargeScepterEffect> RECHARGE_SCEPTER = register("recharge_scepter", RechargeScepterEffect.CODEC);
	public static final MapCodec<SmashBlocksEffect> SMASH_BLOCKS = register("smash_blocks", SmashBlocksEffect.CODEC);

	private static <T extends EnchantmentEntityEffect> MapCodec<T> register(String name, MapCodec<T> codec) {
		ENTITY_EFFECTS.put(TwilightForestMod.prefix(name), codec);
		return codec;
	}

	public static void register() {
		if (registered) {
			return;
		}

		registered = true;
		ENTITY_EFFECTS.forEach((id, codec) -> Registry.register(BuiltInRegistries.ENCHANTMENT_ENTITY_EFFECT_TYPE, id, codec));
	}
}
