package twilightforest.mixin.accessor;

import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TrunkPlacer.class)
public interface TrunkPlacerAccessor {
	@Accessor("baseHeight")
	int twilightforest$getBaseHeight();

	@Accessor("heightRandA")
	int twilightforest$getHeightRandA();

	@Accessor("heightRandB")
	int twilightforest$getHeightRandB();
}
