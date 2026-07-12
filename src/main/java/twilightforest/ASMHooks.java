package twilightforest;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;
import twilightforest.init.TFDataAttachments;
import twilightforest.util.ArmorUtil;
import twilightforest.util.multiparts.MultipartEntityUtil;
import twilightforest.entity.boss.UrGhast;
import twilightforest.init.TFDataComponents;
import twilightforest.world.components.structures.CustomDensitySource;

import java.util.Iterator;
import java.util.List;

// TODO: Think about reorganizing each group into their own class or subclass of ASMHooks
@SuppressWarnings({"JavadocReference", "unused", "RedundantSuppression", "deprecation"})
public class ASMHooks {

	private static final ArmorUtil armorUtil = new ArmorUtil();
	private static final MultipartEntityUtil multipartEntityUtil = new MultipartEntityUtil();

	// //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// armor
	// //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * {@link twilightforest.asm.transformers.armor.ArmorVisibilityRenderingTransformer}<p/>
	 *
	 * Injection Point:<br/>
	 * {@link net.minecraft.world.entity.LivingEntity#getVisibilityPercent(Entity)}
	 */
	public static float modifyArmorVisibility(float o, LivingEntity entity) {
		return o - armorUtil.getShroudedArmorPercentage(entity);
	}

	/**
	 * {@link twilightforest.asm.transformers.armor.CancelArmorRenderingTransformer}<p/>
	 *
	 * Injection Point:<br/>
	 * {@link net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer#renderArmorPiece(PoseStack, MultiBufferSource, ItemStack, EquipmentSlot, int, HumanoidModel)}
	 */
	public static boolean cancelArmorRendering(boolean o, ItemStack stack) {
		if (o && stack.get(TFDataComponents.EMPERORS_CLOTH) != null) {
			return false;
		}
		return o;
	}

	public static boolean fixCapeRendering(boolean o, ItemStack stack) {
		return o && stack.get(TFDataComponents.EMPERORS_CLOTH) == null;
	}

	// //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// beardifier
	// //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * {@link twilightforest.asm.transformers.beardifier.InitializeCustomBeardifierFieldsDuringForStructuresInChunkTransformer}<p/>
	 *
	 * Injection point:<br/>
	 * {@link net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator#createNoiseChunk(ChunkAccess, StructureManager, Blender, RandomState)}
	 */
	public static ObjectListIterator<DensityFunction> gatherCustomTerrain(StructureManager structureManager, ChunkPos chunkPos) {
		ObjectArrayList<DensityFunction> customStructureTerraforms = new ObjectArrayList<>(10);

		for (StructureStart structureStart : structureManager.startsForStructure(chunkPos, s -> s instanceof CustomDensitySource))
			if (structureStart.getStructure() instanceof CustomDensitySource customDensitySource)
				customStructureTerraforms.add(customDensitySource.getStructureTerraformer(chunkPos, structureStart));

		return customStructureTerraforms.iterator();
	}

	/**
	 * {@link twilightforest.asm.transformers.beardifier.BeardifierComputeTransformer}<p/>
	 *
	 * Injection point:<br/>
	 * {@link net.minecraft.world.level.levelgen.Beardifier#compute(DensityFunction.FunctionContext)}
	 */
	public static double getCustomDensity(double o, DensityFunction.FunctionContext context, @Nullable ObjectListIterator<DensityFunction> customDensities) {
		if (customDensities == null)
			return o;

		double newDensity = 0;

		while (customDensities.hasNext()) {
			newDensity += customDensities.next().compute(context);
		}
		customDensities.back(Integer.MAX_VALUE);

		return o + newDensity;
	}

	public static boolean isEntityInUrGhastTears(Entity entity) {
		if (!(entity instanceof Player) || !entity.level().hasChunkAt(entity.blockPosition())) {
			return false;
		}

		if (!entity.level().canSeeSkyFromBelowWater(entity.blockPosition())) {
			return false;
		}

		AABB tearArea = entity.getBoundingBox().inflate(32.0D, 32.0D, 32.0D);
		for (UrGhast urGhast : entity.level().getEntitiesOfClass(UrGhast.class, tearArea, UrGhast::isInTantrum)) {
			if (urGhast.getBoundingBox().move(0.0D, -16.0D, 0.0D).inflate(0.0D, 16.0D, 0.0D).intersects(entity.getBoundingBox())) {
				return true;
			}
		}

		return false;
	}

	/**
	 * {@link twilightforest.asm.transformers.multipart.SendDirtytEntityDataTransformer}<p/>
	 *
	 * Injection Point:<br/>
	 * {@link net.minecraft.server.level.ServerEntity#sendDirtyEntityData}
	 */
	public static Entity sendDirtyEntityData(Entity entity) {
		return multipartEntityUtil.sendDirtyMultipartEntityData(entity);
	}

	// //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// mob
	// //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * {@link twilightforest.asm.transformers.mob.PathFinderUnrestrainedByLeash}
	 *
	 * Injection Point:<br/>
	 * {@link net.minecraft.world.entity.PathfinderMob#shouldStayCloseToLeashHolder()}<br/>
	 * Targets: IRETURN
	 */
	public static boolean overrideStayCloseToHolder(boolean prior, PathfinderMob mob) {
		return prior && !TFDataAttachments.has(mob, TFDataAttachments.LEASH_PATHFINDER_OVERRIDE);
	}
}
