package twilightforest.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twilightforest.world.components.structures.util.LegacyConqueredStructureStart;
import twilightforest.world.components.structures.util.LegacyStructureStartData;

@Mixin(StructureStart.class)
public abstract class StructureStartMixin implements LegacyConqueredStructureStart {
	@Unique
	@Nullable
	private Boolean twilightforest$legacyConquered;

	@Inject(method = "loadStaticStart", at = @At("RETURN"))
	private static void twilightforest$readLegacyConquered(
		StructurePieceSerializationContext context,
		CompoundTag tag,
		long seed,
		CallbackInfoReturnable<StructureStart> callback) {
		StructureStart start = callback.getReturnValue();
		if (start != null && start != StructureStart.INVALID_START) {
			LegacyStructureStartData.read(tag).ifPresent(value ->
				((LegacyConqueredStructureStart) (Object) start).twilightforest$setLegacyConquered(value));
		}
	}

	@Inject(method = "createTag", at = @At("RETURN"))
	private void twilightforest$preserveLegacyConqueredUntilImported(
		StructurePieceSerializationContext context,
		ChunkPos chunkPos,
		CallbackInfoReturnable<CompoundTag> callback) {
		LegacyStructureStartData.writePending(callback.getReturnValue(), this.twilightforest$legacyConquered);
	}

	@Override
	public @Nullable Boolean twilightforest$getLegacyConquered() {
		return this.twilightforest$legacyConquered;
	}

	@Override
	public void twilightforest$setLegacyConquered(@Nullable Boolean conquered) {
		this.twilightforest$legacyConquered = conquered;
	}
}
