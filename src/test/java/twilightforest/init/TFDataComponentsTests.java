package twilightforest.init;

import net.minecraft.core.registries.BuiltInRegistries;
import org.junit.jupiter.api.Test;
import twilightforest.TwilightForestMod;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TFDataComponentsTests {
	@Test
	void registersDataComponents() {
		assertEquals(TwilightForestMod.prefix("emperors_cloth"), BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(TFDataComponents.EMPERORS_CLOTH));
	}
}
