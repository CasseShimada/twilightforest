package twilightforest.client.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.util.Util;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import twilightforest.TwilightForestMod;
import twilightforest.block.AbstractTrophyBlock;
import twilightforest.block.TrophyBlock;
import twilightforest.block.TrophyWallBlock;
import twilightforest.block.entity.TrophyBlockEntity;
import twilightforest.client.model.TFModelLayers;
import twilightforest.client.model.entity.*;
import twilightforest.enums.BossVariant;
import twilightforest.init.TFEntities;

import java.util.function.BiFunction;
import java.util.function.Function;

public class TrophyRenderer implements BlockEntityRenderer<TrophyBlockEntity, TrophyRenderer.RenderState> {

	protected final Function<BossVariant, TrophyBlockModel> modelByType;

	public TrophyRenderer(BlockEntityRendererProvider.Context context) {
		this.modelByType = Util.memoize(variant -> createTrophyModel(context.entityModelSet(), variant));
	}

	@Override
	public RenderState createRenderState() {
		return new RenderState();
	}

	@Override
	public void extractRenderState(TrophyBlockEntity blockEntity, RenderState renderState, float partialTick, Vec3 cameraPosition, @Nullable net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay breakProgress) {
		BlockEntityRenderer.super.extractRenderState(blockEntity, renderState, partialTick, cameraPosition, breakProgress);
		renderState.animationProgress = blockEntity.getAnimationProgress(partialTick);
		renderState.blockState = blockEntity.getBlockState();
		renderState.variant = ((AbstractTrophyBlock) renderState.blockState.getBlock()).getVariant();
	}

	@Override
	public void submit(RenderState renderState, PoseStack stack, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState) {
		if (renderState.blockState == null) return;

		boolean wall = renderState.blockState.getBlock() instanceof TrophyWallBlock;
		Direction direction = wall ? renderState.blockState.getValue(TrophyWallBlock.FACING) : null;
		float yRot = 22.5F * (wall ? (2 + direction.get2DDataValue()) * 4 : renderState.blockState.getValue(TrophyBlock.ROTATION));

		TrophyBlockModel model = this.modelByType.apply(renderState.variant);
		if (model == null) return;

		render(direction, yRot, model, renderState.variant != BossVariant.UR_GHAST, renderState.animationProgress, stack, nodeCollector, renderState.lightCoords, OverlayTexture.NO_OVERLAY, ItemDisplayContext.NONE, renderState.breakProgress);
	}

	public static void render(@Nullable Direction direction, float yRot, TrophyBlockModel model, boolean snapToWalls, float animationProgress, PoseStack stack, SubmitNodeCollector nodeCollector, int light, int overlay, ItemDisplayContext context, @Nullable net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay breakProgress) {
		stack.pushPose();
		if (direction == null || !snapToWalls) {
			stack.translate(0.5D, 0.0D, 0.5D);
		} else {
			stack.translate(0.5F - direction.getStepX() * 0.249F, 0.25D, 0.5F - direction.getStepZ() * 0.249F);
		}
		stack.scale(-1.0F, -1.0F, 1.0F);
		model.setupRotationsForTrophy(animationProgress * 4.5F, yRot, 0.0F, context == ItemDisplayContext.GUI ? 0.35F : direction != null ? 0.5F : 0.0F);
		model.submitTrophy(stack, nodeCollector, light, overlay, -1, context, breakProgress);
		stack.popPose();
	}

	@Nullable
	public static TrophyBlockModel createTrophyModel(EntityModelSet set, BossVariant variant) {
		return createTrophyModel((type, layer) -> {
			try {
				if (Minecraft.getInstance().level == null) {
					return createFallback(set, variant);
				}

				Entity entity = type.create(Minecraft.getInstance().level, EntitySpawnReason.SPAWN_ITEM_USE);
				if (entity == null) {
					return createFallback(set, variant);
				}

				var renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(entity);
				if (renderer instanceof LivingEntityRenderer<?, ?, ?> livingRenderer) {
					return (TrophyBlockModel) livingRenderer.getModel()
						.getClass()
						.getDeclaredConstructor(ModelPart.class)
						.newInstance(set.bakeLayer(layer));
				}

				return createFallback(set, variant);
			} catch (Exception e) {
				TwilightForestMod.LOGGER.warn("Failed to create trophy renderer for entity {}, using fallback", type.getDescription().getString());
				return createFallback(set, variant);
			}
		}, variant);
	}

	@Nullable
	public static TrophyBlockModel createTrophyModel(BiFunction<EntityType<?>, ModelLayerLocation, TrophyBlockModel> modelFunction, BossVariant variant) {
		return switch (variant) {
			case NAGA -> modelFunction.apply(TFEntities.NAGA.get(), TFModelLayers.NAGA_TROPHY);
			case LICH -> modelFunction.apply(TFEntities.LICH.get(), TFModelLayers.LICH_TROPHY);
			case MINOSHROOM -> modelFunction.apply(TFEntities.MINOSHROOM.get(), TFModelLayers.MINOSHROOM_TROPHY);
			case HYDRA -> new HydraHeadModel(Minecraft.getInstance().getEntityModels().bakeLayer(TFModelLayers.HYDRA_TROPHY));
			case KNIGHT_PHANTOM -> modelFunction.apply(TFEntities.KNIGHT_PHANTOM.get(), TFModelLayers.KNIGHT_PHANTOM_TROPHY);
			case UR_GHAST -> modelFunction.apply(TFEntities.UR_GHAST.get(), TFModelLayers.UR_GHAST_TROPHY);
			case ALPHA_YETI -> modelFunction.apply(TFEntities.ALPHA_YETI.get(), TFModelLayers.ALPHA_YETI_TROPHY);
			case SNOW_QUEEN -> modelFunction.apply(TFEntities.SNOW_QUEEN.get(), TFModelLayers.SNOW_QUEEN_TROPHY);
			case QUEST_RAM -> modelFunction.apply(TFEntities.QUEST_RAM.get(), TFModelLayers.QUEST_RAM_TROPHY);
			case FINAL_BOSS -> null;
		};
	}

	@Nullable
	public static TrophyBlockModel createFallback(EntityModelSet set, BossVariant variant) {
		return switch (variant) {
			case NAGA -> new NagaModel<>(set.bakeLayer(TFModelLayers.NAGA_TROPHY));
			case LICH -> new LichModel(set.bakeLayer(TFModelLayers.LICH_TROPHY));
			case MINOSHROOM -> new MinoshroomModel(set.bakeLayer(TFModelLayers.MINOSHROOM_TROPHY));
			case HYDRA -> new HydraHeadModel(set.bakeLayer(TFModelLayers.HYDRA_TROPHY));
			case KNIGHT_PHANTOM -> new KnightPhantomModel(set.bakeLayer(TFModelLayers.KNIGHT_PHANTOM_TROPHY));
			case UR_GHAST -> new UrGhastModel(set.bakeLayer(TFModelLayers.UR_GHAST_TROPHY));
			case ALPHA_YETI -> new AlphaYetiModel(set.bakeLayer(TFModelLayers.ALPHA_YETI_TROPHY));
			case SNOW_QUEEN -> new SnowQueenModel(set.bakeLayer(TFModelLayers.SNOW_QUEEN_TROPHY));
			case QUEST_RAM -> new QuestRamModel(set.bakeLayer(TFModelLayers.QUEST_RAM_TROPHY));
			case FINAL_BOSS -> null;
		};
	}

	public static class RenderState extends BlockEntityRenderState {
		public float animationProgress;
		public BlockState blockState;
		public BossVariant variant = BossVariant.NAGA;
	}
}
