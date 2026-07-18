package twilightforest.world.components.structures.lichtowerrevamp;

import com.mojang.serialization.Codec;
import net.minecraft.resources.Identifier;
import twilightforest.world.components.structures.util.TemplatePoolInstance;

import java.util.Map;

/**
 * @param poolWeights weighted metadata assigning this template to its pools
 */
public record StructureTemplateDefinition(Map<Identifier, TemplatePoolInstance> poolWeights) {
	public static final Codec<StructureTemplateDefinition> CODEC = Codec.unboundedMap(Identifier.CODEC, TemplatePoolInstance.CODEC)
		.xmap(StructureTemplateDefinition::new, StructureTemplateDefinition::poolWeights);
}
