package twilightforest.world.components.structures.lichtowerrevamp;

import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import org.junit.jupiter.api.Test;
import twilightforest.TwilightForestMod;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class StructureTemplateDefinitionsTest {

	public StructureTemplateDefinitions structureTemplateDefinitions = new StructureTemplateDefinitions();

	@Test
	public void afterDeserialize() {
		Identifier alcovePool = TwilightForestMod.prefix("pools/alcovePool");
		Identifier balconyPool = TwilightForestMod.prefix("pools/balcony");
		Identifier chamber = TwilightForestMod.prefix("pools/chamber");

		Identifier architectureTemplate = TwilightForestMod.prefix("template/architecture");
		StructureTemplateDefinition templateDefinition = new StructureTemplateDefinition(Map.of(
			alcovePool, 1,
			balconyPool, 10,
			chamber, 0
		));
		this.structureTemplateDefinitions.apply(Map.of(architectureTemplate, templateDefinition), mock(ResourceManager.class), mock(ProfilerFiller.class));

		RandomSource random = RandomSource.create(42L);

		assertEquals(architectureTemplate, this.structureTemplateDefinitions.rollTemplatePool(random, alcovePool));
		assertEquals(architectureTemplate, this.structureTemplateDefinitions.rollTemplatePool(random, balconyPool));
		assertNull(this.structureTemplateDefinitions.rollTemplatePool(random, chamber)); // Has zero weight
	}
}
