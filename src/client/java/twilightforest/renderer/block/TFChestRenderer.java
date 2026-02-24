package twilightforest.client.renderer.block;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.chest.ChestModel;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BrightnessCombiner;
import net.minecraft.client.renderer.blockentity.state.ChestRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.MaterialSet;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.CopperChestBlock;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.level.block.entity.TrappedChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.core.Direction;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import twilightforest.TwilightForestMod;
import twilightforest.init.TFBlocks;

import java.util.EnumMap;
import java.util.Map;

public class TFChestRenderer<T extends ChestBlockEntity> implements BlockEntityRenderer<T, TFChestRenderer.RenderState> {
	public static final Map<Block, EnumMap<ChestType, Material>> MATERIALS;

	static {
		ImmutableMap.Builder<Block, EnumMap<ChestType, Material>> builder = ImmutableMap.builder();

		builder.put(TFBlocks.TWILIGHT_OAK_CHEST.get(), chestMaterial("twilight_oak", false));
		builder.put(TFBlocks.CANOPY_CHEST.get(), chestMaterial("canopy", false));
		builder.put(TFBlocks.MANGROVE_CHEST.get(), chestMaterial("mangrove", false));
		builder.put(TFBlocks.DARK_CHEST.get(), chestMaterial("darkwood", false));
		builder.put(TFBlocks.TIME_CHEST.get(), chestMaterial("time", false));
		builder.put(TFBlocks.TRANSFORMATION_CHEST.get(), chestMaterial("transformation", false));
		builder.put(TFBlocks.MINING_CHEST.get(), chestMaterial("mining", false));
		builder.put(TFBlocks.SORTING_CHEST.get(), chestMaterial("sorting", false));

		builder.put(TFBlocks.TWILIGHT_OAK_TRAPPED_CHEST.get(), chestMaterial("twilight_oak", true));
		builder.put(TFBlocks.CANOPY_TRAPPED_CHEST.get(), chestMaterial("canopy", true));
		builder.put(TFBlocks.MANGROVE_TRAPPED_CHEST.get(), chestMaterial("mangrove", true));
		builder.put(TFBlocks.DARK_TRAPPED_CHEST.get(), chestMaterial("darkwood", true));
		builder.put(TFBlocks.TIME_TRAPPED_CHEST.get(), chestMaterial("time", true));
		builder.put(TFBlocks.TRANSFORMATION_TRAPPED_CHEST.get(), chestMaterial("transformation", true));
		builder.put(TFBlocks.MINING_TRAPPED_CHEST.get(), chestMaterial("mining", true));
		builder.put(TFBlocks.SORTING_TRAPPED_CHEST.get(), chestMaterial("sorting", true));

		MATERIALS = builder.build();
	}

	private final MaterialSet materials;
	private final ChestModel singleModel;
	private final ChestModel doubleLeftModel;
	private final ChestModel doubleRightModel;
	private final boolean xmasTextures;

	public TFChestRenderer(BlockEntityRendererProvider.Context context) {
		this.materials = context.materials();
		this.xmasTextures = net.minecraft.client.renderer.blockentity.ChestRenderer.xmasTextures();
		this.singleModel = new ChestModel(context.bakeLayer(ModelLayers.CHEST));
		this.doubleLeftModel = new ChestModel(context.bakeLayer(ModelLayers.DOUBLE_CHEST_LEFT));
		this.doubleRightModel = new ChestModel(context.bakeLayer(ModelLayers.DOUBLE_CHEST_RIGHT));
	}

	@Override
	public RenderState createRenderState() {
		return new RenderState();
	}

	@Override
	public void extractRenderState(T blockEntity, RenderState renderState, float partialTick, net.minecraft.world.phys.Vec3 cameraPosition, net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay breakProgress) {
		BlockEntityRenderer.super.extractRenderState(blockEntity, renderState, partialTick, cameraPosition, breakProgress);

		boolean hasLevel = blockEntity.getLevel() != null;
		BlockState state = hasLevel
			? blockEntity.getBlockState()
			: Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH);

		renderState.type = state.hasProperty(ChestBlock.TYPE) ? state.getValue(ChestBlock.TYPE) : ChestType.SINGLE;
		renderState.angle = state.getValue(ChestBlock.FACING).toYRot();
		renderState.material = getChestMaterial(blockEntity, this.xmasTextures);
		renderState.customMaterial = getCustomMaterial(blockEntity, renderState.type);

		DoubleBlockCombiner.NeighborCombineResult<? extends ChestBlockEntity> combiner = hasLevel && state.getBlock() instanceof ChestBlock chestBlock
			? chestBlock.combine(state, blockEntity.getLevel(), blockEntity.getBlockPos(), true)
			: new DoubleBlockCombiner.NeighborCombineResult.Single<>(blockEntity);

		renderState.open = combiner.apply(ChestBlock.opennessCombiner(blockEntity)).get(partialTick);
		if (renderState.type != ChestType.SINGLE) {
			Int2IntFunction brightness = combiner.apply(new BrightnessCombiner<ChestBlockEntity>());
			renderState.lightCoords = brightness.applyAsInt(renderState.lightCoords);
		}
	}

	@Override
	public void submit(RenderState renderState, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState) {
		poseStack.pushPose();
		poseStack.translate(0.5F, 0.5F, 0.5F);
		poseStack.mulPose(Axis.YP.rotationDegrees(-renderState.angle));
		poseStack.translate(-0.5F, -0.5F, -0.5F);

		float openness = renderState.open;
		openness = 1.0F - openness;
		openness = 1.0F - openness * openness * openness;

		Material material = renderState.customMaterial;
		if (material == null) {
			material = Sheets.chooseMaterial(renderState.material, renderState.type);
		}

		RenderType renderType = material.renderType(id -> Sheets.chestSheet());
		TextureAtlasSprite sprite = this.materials.get(material);

		ChestModel model = switch (renderState.type) {
			case LEFT -> this.doubleLeftModel;
			case RIGHT -> this.doubleRightModel;
			default -> this.singleModel;
		};

		nodeCollector.submitModel(model, openness, poseStack, renderType, renderState.lightCoords, OverlayTexture.NO_OVERLAY, -1, sprite, 0, renderState.breakProgress);
		poseStack.popPose();
	}

	private static Material getCustomMaterial(BlockEntity blockEntity, ChestType type) {
		EnumMap<ChestType, Material> map = MATERIALS.get(blockEntity.getBlockState().getBlock());
		return map != null ? map.get(type) : null;
	}

	private static ChestRenderState.ChestMaterialType getChestMaterial(BlockEntity blockEntity, boolean xmasTextures) {
		if (blockEntity instanceof EnderChestBlockEntity) {
			return ChestRenderState.ChestMaterialType.ENDER_CHEST;
		}
		if (xmasTextures) {
			return ChestRenderState.ChestMaterialType.CHRISTMAS;
		}
		if (blockEntity instanceof TrappedChestBlockEntity) {
			return ChestRenderState.ChestMaterialType.TRAPPED;
		}

		Block block = blockEntity.getBlockState().getBlock();
		if (block instanceof CopperChestBlock copperChest) {
			return switch (copperChest.getState()) {
				case UNAFFECTED -> ChestRenderState.ChestMaterialType.COPPER_UNAFFECTED;
				case EXPOSED -> ChestRenderState.ChestMaterialType.COPPER_EXPOSED;
				case WEATHERED -> ChestRenderState.ChestMaterialType.COPPER_WEATHERED;
				case OXIDIZED -> ChestRenderState.ChestMaterialType.COPPER_OXIDIZED;
			};
		}

		return ChestRenderState.ChestMaterialType.REGULAR;
	}

	private static EnumMap<ChestType, Material> chestMaterial(String wood, boolean trapped) {
		EnumMap<ChestType, Material> map = new EnumMap<>(ChestType.class);
		String type = (trapped ? "trapped" : "normal");

		map.put(ChestType.SINGLE, new Material(Sheets.CHEST_SHEET, TwilightForestMod.prefix("entity/chest/" + wood + "/" + type)));
		map.put(ChestType.LEFT, new Material(Sheets.CHEST_SHEET, TwilightForestMod.prefix("entity/chest/" + wood + "/" + type + "_left")));
		map.put(ChestType.RIGHT, new Material(Sheets.CHEST_SHEET, TwilightForestMod.prefix("entity/chest/" + wood + "/" + type + "_right")));

		return map;
	}

	public static class RenderState extends ChestRenderState {
		public Material customMaterial;
	}
}
