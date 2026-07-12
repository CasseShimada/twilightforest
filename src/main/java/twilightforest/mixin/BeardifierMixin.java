package twilightforest.mixin;

import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.levelgen.Beardifier;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twilightforest.util.density.CustomDensityBeardifier;
import org.jetbrains.annotations.Nullable;
import java.util.List;

@Mixin(Beardifier.class)
public class BeardifierMixin implements CustomDensityBeardifier {
	@Unique
	private @Nullable ObjectListIterator<DensityFunction> twilightforest$customDensities;

	@Override
	public void twilightforest$setCustomDensities(@Nullable ObjectListIterator<DensityFunction> customDensities) {
		this.twilightforest$customDensities = customDensities;
	}

	@Inject(method = "forStructuresInChunk", at = @At("RETURN"), cancellable = true)
	private static void twilightforest$attachCustomDensities(StructureManager structureManager, ChunkPos chunkPos, CallbackInfoReturnable<Beardifier> cir) {
		ObjectListIterator<DensityFunction> customDensities = CustomDensityBeardifier.gatherCustomTerrain(structureManager, chunkPos);
		if (!customDensities.hasNext()) {
			return;
		}

		Beardifier beardifier = cir.getReturnValue();
		if (beardifier == Beardifier.EMPTY) {
			beardifier = new Beardifier(List.of(), List.of(), BoundingBox.infinite());
			cir.setReturnValue(beardifier);
		}

		if (beardifier instanceof CustomDensityBeardifier custom) {
			custom.twilightforest$setCustomDensities(customDensities);
		}
	}

	@Inject(method = "compute", at = @At("RETURN"), cancellable = true)
	private void twilightforest$applyCustomDensities(DensityFunction.FunctionContext context, CallbackInfoReturnable<Double> cir) {
		ObjectListIterator<DensityFunction> custom = this.twilightforest$customDensities;
		if (custom != null) {
			double base = cir.getReturnValue();
			double updated = CustomDensityBeardifier.applyCustomDensity(base, context, custom);
			if (updated != base) {
				cir.setReturnValue(updated);
			}
		}
	}
}
