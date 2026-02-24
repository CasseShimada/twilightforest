package twilightforest.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.util.random.WeightedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twilightforest.events.EntityEvents;

import java.util.List;

@Mixin(NaturalSpawner.class)
public abstract class NaturalSpawnerMixin {
	@Inject(method = "mobsAt", at = @At("HEAD"), cancellable = true)
	private static void twilightforest$overrideStructureSpawns(ServerLevel level, StructureManager structureManager, ChunkGenerator chunkGenerator, MobCategory category, BlockPos pos, Holder<Biome> biome, CallbackInfoReturnable<WeightedList<MobSpawnSettings.SpawnerData>> cir) {
		WeightedList<MobSpawnSettings.SpawnerData> structureSpawns = EntityEvents.getStructureSpawns(level, category, pos);
		if (structureSpawns == null) {
			return;
		}
		cir.setReturnValue(structureSpawns);
	}
}
