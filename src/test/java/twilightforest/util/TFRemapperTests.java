package twilightforest.util;

import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;
import twilightforest.TwilightForestMod;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TFRemapperTests {
	@Test
	void preservesLegacyCopperNuggetWithCrossNamespaceAlias() {
		assertEquals(
			Identifier.withDefaultNamespace("copper_nugget"),
			TFRemapper.CROSS_NAMESPACE_ITEM_ALIASES.get(TwilightForestMod.prefix("copper_nugget"))
		);
	}
}
