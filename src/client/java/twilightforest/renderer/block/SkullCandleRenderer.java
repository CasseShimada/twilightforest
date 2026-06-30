package twilightforest.client.renderer.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.skull.PiglinHeadModel;
import net.minecraft.client.model.object.skull.SkullModel;
import net.minecraft.client.model.object.skull.SkullModelBase;
import net.minecraft.client.renderer.PlayerSkinRenderCache;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import twilightforest.block.AbstractSkullCandleBlock;
import twilightforest.block.LightableBlock;
import twilightforest.block.SkullCandleBlock;
import twilightforest.block.WallSkullCandleBlock;
import twilightforest.block.entity.SkullCandleBlockEntity;
import twilightforest.client.renderer.RenderStateUtil;

import java.util.Map;

// [VanillaCopy] SkullBlockRenderer, adapted to the 1.21.11 render state + submit pipeline, with extra candle rendering.
public class SkullCandleRenderer implements BlockEntityRenderer<SkullCandleBlockEntity, SkullCandleRenderer.RenderState> {

	private final Map<SkullBlock.Type, SkullModelBase> modelByType;
	private final PlayerSkinRenderCache playerSkinRenderCache;

	public static final Map<SkullBlock.Type, Identifier> SKIN_BY_TYPE = Util.make(Maps.newHashMap(), map -> {
		map.put(SkullBlock.Types.SKELETON, Identifier.withDefaultNamespace("textures/entity/skeleton/skeleton.png"));
		map.put(SkullBlock.Types.WITHER_SKELETON, Identifier.withDefaultNamespace("textures/entity/skeleton/wither_skeleton.png"));
		map.put(SkullBlock.Types.ZOMBIE, Identifier.withDefaultNamespace("textures/entity/zombie/zombie.png"));
		map.put(SkullBlock.Types.CREEPER, Identifier.withDefaultNamespace("textures/entity/creeper/creeper.png"));
		map.put(SkullBlock.Types.PIGLIN, Identifier.withDefaultNamespace("textures/entity/piglin/piglin.png"));
		map.put(SkullBlock.Types.PLAYER, DefaultPlayerSkin.getDefaultTexture());
	});

	public static Map<SkullBlock.Type, SkullModelBase> createSkullRenderers(EntityModelSet set) {
		ImmutableMap.Builder<SkullBlock.Type, SkullModelBase> map = ImmutableMap.builder();
		map.put(SkullBlock.Types.SKELETON, new SkullModel(set.bakeLayer(ModelLayers.SKELETON_SKULL)));
		map.put(SkullBlock.Types.WITHER_SKELETON, new SkullModel(set.bakeLayer(ModelLayers.WITHER_SKELETON_SKULL)));
		map.put(SkullBlock.Types.PLAYER, new SkullModel(set.bakeLayer(ModelLayers.PLAYER_HEAD)));
		map.put(SkullBlock.Types.ZOMBIE, new SkullModel(set.bakeLayer(ModelLayers.ZOMBIE_HEAD)));
		map.put(SkullBlock.Types.CREEPER, new SkullModel(set.bakeLayer(ModelLayers.CREEPER_HEAD)));
		map.put(SkullBlock.Types.PIGLIN, new PiglinHeadModel(set.bakeLayer(ModelLayers.PIGLIN_HEAD)));
		return map.build();
	}

	public SkullCandleRenderer(BlockEntityRendererProvider.Context context) {
		this.modelByType = createSkullRenderers(context.entityModelSet());
		this.playerSkinRenderCache = context.playerSkinRenderCache();
	}

	@Override
	public RenderState createRenderState() {
		return new RenderState();
	}

	@Override
	public void extractRenderState(SkullCandleBlockEntity blockEntity, RenderState renderState, float partialTick, Vec3 cameraPosition, net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay breakProgress) {
		BlockEntityRenderer.super.extractRenderState(blockEntity, renderState, partialTick, cameraPosition, breakProgress);

		BlockState state = blockEntity.getBlockState();
		boolean wallSkull = state.getBlock() instanceof WallSkullCandleBlock;
		Direction direction = wallSkull ? state.getValue(WallSkullCandleBlock.FACING) : null;
		int rotation = wallSkull ? RotationSegment.convertToSegment(direction.getOpposite()) : state.getValue(SkullCandleBlock.ROTATION);

		renderState.skullDirection = direction;
		renderState.skullYRot = RotationSegment.convertToDegrees(rotation);
		renderState.animationPos = blockEntity.getAnimation(partialTick);
		renderState.skullType = ((AbstractSkullCandleBlock) state.getBlock()).getType();
		renderState.ownerProfile = blockEntity.getOwnerProfile();

		renderState.candleColor = blockEntity.getCandleColor();
		renderState.candleCount = Math.max(1, state.getValue(BlockStateProperties.CANDLES));
		renderState.lighting = state.getValue(AbstractSkullCandleBlock.LIGHTING);
	}

	@Override
	public void submit(RenderState renderState, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState) {
		SkullModelBase base = this.modelByType.get(renderState.skullType);
		if (base != null) {
			SkullModelBase.State skullState = renderState.skullState;
			skullState.animationPos = renderState.animationPos;
			skullState.yRot = renderState.skullYRot;
			skullState.xRot = 0.0F;

			RenderType renderType = resolveRenderType(renderState.skullType, renderState.ownerProfile);

			poseStack.pushPose();
			if (renderState.skullDirection == null) {
				poseStack.translate(0.5F, 0.0F, 0.5F);
			} else {
				poseStack.translate(
					0.5F - (float) renderState.skullDirection.getStepX() * 0.25F,
					0.25F,
					0.5F - (float) renderState.skullDirection.getStepZ() * 0.25F
				);
			}
			poseStack.scale(-1.0F, -1.0F, 1.0F);
			nodeCollector.submitModel(base, skullState, poseStack, renderType, renderState.lightCoords, OverlayTexture.NO_OVERLAY, 0, renderState.breakProgress);
			poseStack.popPose();
		}

		// Render the candle(s) sitting on the skull.
		if (renderState.skullDirection != null) {
			poseStack.translate(-renderState.skullDirection.getStepX() * 0.25F, 0.75F, -renderState.skullDirection.getStepZ() * 0.25F);
		} else {
			poseStack.translate(0.0F, 0.45F, 0.0F);
		}

		BlockState candle = AbstractSkullCandleBlock.candleColorToCandle(AbstractSkullCandleBlock.CandleColors.colorFromInt(renderState.candleColor))
			.defaultBlockState()
			.setValue(CandleBlock.CANDLES, renderState.candleCount)
			.setValue(CandleBlock.LIT, renderState.lighting != LightableBlock.Lighting.NONE);

		MovingBlockRenderState candleState = new MovingBlockRenderState();
		RenderStateUtil.populateMovingBlockRenderState(candleState, candle, net.minecraft.client.Minecraft.getInstance().level, renderState.blockPos, renderState.blockPos);
		nodeCollector.submitMovingBlock(poseStack, candleState, 0);
	}

	private RenderType resolveRenderType(SkullBlock.Type type, @Nullable ResolvableProfile profile) {
		Identifier texture = SKIN_BY_TYPE.get(type);
		if (type == SkullBlock.Types.PLAYER && profile != null) {
			return this.playerSkinRenderCache.getOrDefault(profile).renderType();
		}
		return RenderTypes.entityCutoutZOffset(texture);
	}

	public static class RenderState extends BlockEntityRenderState {
		public @Nullable Direction skullDirection;
		public float skullYRot;
		public float animationPos;
		public SkullBlock.Type skullType = SkullBlock.Types.SKELETON;
		public @Nullable ResolvableProfile ownerProfile;
		public final SkullModelBase.State skullState = new SkullModelBase.State();

		public int candleColor;
		public int candleCount = 1;
		public LightableBlock.Lighting lighting = LightableBlock.Lighting.NONE;
	}
}
