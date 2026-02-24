package twilightforest.mixin.accessor;

import net.minecraft.world.level.levelgen.Beardifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Beardifier.class)
public interface BeardifierInvoker {
	@Invoker("getBuryContribution")
	static double twilightforest$getBuryContribution(double xDist, double yDist, double zDist) {
		throw new AssertionError();
	}

	@Invoker("getBeardContribution")
	static double twilightforest$getBeardContribution(int xDist, int yDist, int zDist, int distAboveBottom) {
		throw new AssertionError();
	}
}
