package twilightforest.mixin;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.levelgen.Beardifier;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import twilightforest.ASMHooks;
import twilightforest.util.density.CustomDensityBeardifier;

@Mixin(NoiseBasedChunkGenerator.class)
public class NoiseBasedChunkGeneratorMixin {
	@Redirect(
		method = "createNoiseChunk",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/levelgen/Beardifier;forStructuresInChunk(Lnet/minecraft/world/level/StructureManager;Lnet/minecraft/world/level/ChunkPos;)Lnet/minecraft/world/level/levelgen/Beardifier;"
		)
	)
	private Beardifier twilightforest$attachCustomDensities(StructureManager structureManager, ChunkPos chunkPos) {
		Beardifier beardifier = Beardifier.forStructuresInChunk(structureManager, chunkPos);
		if (beardifier instanceof CustomDensityBeardifier custom) {
			custom.twilightforest$setCustomDensities(ASMHooks.gatherCustomTerrain(structureManager, chunkPos));
		}
		return beardifier;
	}
}
