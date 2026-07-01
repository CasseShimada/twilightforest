package twilightforest.init;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import twilightforest.TwilightForestMod;

import java.util.LinkedHashMap;
import java.util.Map;

public class TFAttributes {
	private static final Map<Identifier, Attribute> ATTRIBUTES = new LinkedHashMap<>();
	private static boolean registered;

	public static final Attribute CLONE_COUNT = makeAttribute("clone_count", new RangedAttribute("attribute.name.lich.clone_count", 2, 0, 1024).setSyncable(true));
	public static final Attribute SHIELD_STRENGTH = makeAttribute("max_shield_strength", new RangedAttribute("attribute.name.lich.shield_strength", 6, 0, 1024).setSyncable(true));
	public static final Attribute MINION_COUNT = makeAttribute("minion_count", new RangedAttribute("attribute.name.lich.minion_count", 9, 0, 1024).setSyncable(true));

	private static Attribute makeAttribute(String name, Attribute attribute) {
		ATTRIBUTES.put(TwilightForestMod.prefix(name), attribute);
		return attribute;
	}

	public static void register() {
		if (registered) {
			return;
		}

		registered = true;
		ATTRIBUTES.forEach((id, attribute) -> Registry.register(BuiltInRegistries.ATTRIBUTE, id, attribute));
	}
}
