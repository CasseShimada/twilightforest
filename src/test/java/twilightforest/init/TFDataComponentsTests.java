package twilightforest.init;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.component.DataComponentType;
import org.junit.jupiter.api.Test;
import twilightforest.TwilightForestMod;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TFDataComponentsTests {
	@Test
	void registersDataComponents() {
		assertEquals(TwilightForestMod.prefix("emperors_cloth"), BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(TFDataComponents.EMPERORS_CLOTH));
	}

	@Test
	void registersEveryTravellersGearDataComponentUnderItsPersistentId() {
		Map<String, DataComponentType<?>> expected = new LinkedHashMap<>();
		expected.put("travellers_armor", TFDataComponents.IS_TRAVELLERS_GEAR);
		expected.put("stored_broken_attributes", TFDataComponents.STORED_BROKEN_ATTRIBUTES);
		expected.put("has_travellers_chestplate", TFDataComponents.TRAVELLERS_HAS_CHESTPLATE);
		expected.put("has_travellers_gloves", TFDataComponents.TRAVELLERS_HAS_GLOVES);
		expected.put("has_travellers_belt", TFDataComponents.TRAVELLERS_HAS_BELT);
		expected.put("has_travellers_wings", TFDataComponents.TRAVELLERS_HAS_WINGS);
		expected.put("has_travellers_boots", TFDataComponents.TRAVELLERS_HAS_BOOTS);
		expected.put("auto_repair_probability", TFDataComponents.AUTO_REPAIR_PROBABILITY);
		expected.put("zoom_ability_modifier", TFDataComponents.ZOOM_ABILITY_MODIFIER);
		expected.put("red_thread_vision", TFDataComponents.RED_THREAD_VISION);
		expected.put("stealth_crouching", TFDataComponents.STEALTH_CROUCHING);
		expected.put("arrow_magnetism", TFDataComponents.ARROW_MAGNETISM);
		expected.put("efficient_eater", TFDataComponents.EFFICIENT_EATER);
		expected.put("perfect_dodge_probability", TFDataComponents.PERFECT_DODGE_PROBABILITY);
		expected.put("haste_amplifier", TFDataComponents.HASTE_AMPLIFIER);
		expected.put("swap_hotbar_ability", TFDataComponents.SWAP_HOTBAR_ABILITY);
		expected.put("swap_hotbar_modifier", TFDataComponents.SWAP_HOTBAR_MODIFIER);
		expected.put("high_jump_amplifier", TFDataComponents.HIGH_JUMP_AMPLIFIER);
		expected.put("gradually_gliding_multiplier", TFDataComponents.GRADUALLY_GLIDING_MULTIPLIER);
		expected.put("agile_ranger_modifier", TFDataComponents.AGILE_RANGER_MODIFIER);
		expected.put("double_jump", TFDataComponents.DOUBLE_JUMP);
		expected.put("sidestep_cooldown", TFDataComponents.SIDESTEP_COOLDOWN);
		expected.put("straight_ahead_multiplier", TFDataComponents.STRAIGHT_AHEAD_MULTIPLIER);
		expected.put("slimy_soles_coefficient", TFDataComponents.SLIMY_SOLES_COEFFICIENT);
		expected.put("water_walk", TFDataComponents.WATER_WALK);
		expected.put("all_night_goggles", TFDataComponents.ALL_NIGHT_GOGGLES);
		expected.put("item_display", TFDataComponents.ITEM_DISPLAY);
		expected.put("unrestrained", TFDataComponents.UNRESTRAINED);
		expected.put("swift_swim", TFDataComponents.SWIFT_SWIM);
		expected.put("high_step", TFDataComponents.HIGH_STEP);
		expected.put("aquatic_agility", TFDataComponents.AQUATIC_AGILITY);

		assertEquals(31, expected.size());
		expected.forEach((path, component) ->
			assertEquals(TwilightForestMod.prefix(path), BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(component), path));
	}
}
