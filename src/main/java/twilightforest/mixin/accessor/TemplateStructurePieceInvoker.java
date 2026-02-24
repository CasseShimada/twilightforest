package twilightforest.mixin.accessor;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(TemplateStructurePiece.class)
public interface TemplateStructurePieceInvoker {
	@Invoker("makeTemplateLocation")
	Identifier twilightforest$makeTemplateLocation();
}
