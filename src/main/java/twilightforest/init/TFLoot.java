package twilightforest.init;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import twilightforest.TwilightForestMod;
import twilightforest.loot.LootingEnchantNumberProvider;
import twilightforest.loot.MultiplayerBasedAdditionLootFunction;
import twilightforest.loot.MultiplayerBasedNumberProvider;
import twilightforest.loot.conditions.GiantPickUsedCondition;
import twilightforest.loot.conditions.IsMinionCondition;
import twilightforest.loot.conditions.ModExistsCondition;
import twilightforest.loot.conditions.UncraftingTableEnabledCondition;

import java.util.LinkedHashMap;
import java.util.Map;

public class TFLoot {

	private static final Map<Identifier, MapCodec<? extends LootItemCondition>> CONDITIONS = new LinkedHashMap<>();
	private static final Map<Identifier, MapCodec<? extends LootItemFunction>> FUNCTIONS = new LinkedHashMap<>();
	private static final Map<Identifier, MapCodec<? extends NumberProvider>> NUMBERS = new LinkedHashMap<>();
	private static boolean registered;

	public static final MapCodec<IsMinionCondition> IS_MINION = condition("is_minion", IsMinionCondition.CODEC);
	public static final MapCodec<ModExistsCondition> MOD_EXISTS = condition("mod_exists", ModExistsCondition.CODEC);
	public static final MapCodec<UncraftingTableEnabledCondition> UNCRAFTING_TABLE_ENABLED = condition("uncrafting_table_enabled", UncraftingTableEnabledCondition.CODEC);
	public static final MapCodec<GiantPickUsedCondition> GIANT_PICK_USED_CONDITION = condition("giant_pick_used", GiantPickUsedCondition.CODEC);
	public static final MapCodec<MultiplayerBasedAdditionLootFunction> MULTIPLAYER_MULTIPLIER = function("multiplayer_addition", MultiplayerBasedAdditionLootFunction.CODEC);
	public static final MapCodec<MultiplayerBasedNumberProvider> MULTIPLAYER_ROLLS = number("multiplayer_rolls", MultiplayerBasedNumberProvider.CODEC);
	public static final MapCodec<LootingEnchantNumberProvider> LOOTING_ROLLS = number("looting_rolls", LootingEnchantNumberProvider.CODEC);

	private static <T extends LootItemCondition> MapCodec<T> condition(String name, MapCodec<T> codec) {
		CONDITIONS.put(TwilightForestMod.prefix(name), codec);
		return codec;
	}

	private static <T extends LootItemFunction> MapCodec<T> function(String name, MapCodec<T> codec) {
		FUNCTIONS.put(TwilightForestMod.prefix(name), codec);
		return codec;
	}

	private static <T extends NumberProvider> MapCodec<T> number(String name, MapCodec<T> codec) {
		NUMBERS.put(TwilightForestMod.prefix(name), codec);
		return codec;
	}

	public static void register() {
		if (registered) {
			return;
		}

		registered = true;
		CONDITIONS.forEach((id, codec) -> Registry.register(BuiltInRegistries.LOOT_CONDITION_TYPE, id, codec));
		FUNCTIONS.forEach((id, codec) -> Registry.register(BuiltInRegistries.LOOT_FUNCTION_TYPE, id, codec));
		NUMBERS.forEach((id, codec) -> Registry.register(BuiltInRegistries.LOOT_NUMBER_PROVIDER_TYPE, id, codec));
	}

}
