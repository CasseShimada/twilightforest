package twilightforest.mixin;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twilightforest.ASMHooks;

@Mixin(ChunkGenerator.class)
public class ChunkGeneratorMixin {
	@Inject(method = "findNearestMapStructure", at = @At("RETURN"), cancellable = true)
	private void twilightforest$resolveNonRandomSpreadMapStructure(ServerLevel level, HolderSet<Structure> targetStructures, BlockPos pos, int searchRadius, boolean skipKnown, CallbackInfoReturnable<Pair<BlockPos, Holder<Structure>>> cir) {
		Pair<BlockPos, Holder<Structure>> resolved = ASMHooks.resolveNearestNonRandomSpreadMapStructure(cir.getReturnValue(), level, targetStructures, pos, searchRadius, skipKnown);
		if (resolved != cir.getReturnValue()) {
			cir.setReturnValue(resolved);
		}
	}
}
