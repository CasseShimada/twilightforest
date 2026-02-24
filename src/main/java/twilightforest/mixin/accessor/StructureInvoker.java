package twilightforest.mixin.accessor;

import net.minecraft.world.level.levelgen.structure.Structure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Optional;

@Mixin(Structure.class)
public interface StructureInvoker {
	@Invoker("findGenerationPoint")
	Optional<Structure.GenerationStub> twilightforest$invokeFindGenerationPoint(Structure.GenerationContext context);
}
