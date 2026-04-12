package twilightforest.client.renderer.block;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.chest.ChestModel;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BrightnessCombiner;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.client.renderer.blockentity.state.ChestRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
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
	public static final Map<Block, EnumMap<ChestType, SpriteId>> SPRITES;

	static {
		ImmutableMap.Builder<Block, EnumMap<ChestType, SpriteId>> builder = ImmutableMap.builder();

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

		SPRITES = builder.build();
	}

	private final SpriteGetter sprites;
	private final net.minecraft.client.renderer.MultiblockChestResources<ChestModel> models;
	private final boolean xmasTextures;

	public TFChestRenderer(BlockEntityRendererProvider.Context context) {
		this.sprites = context.sprites();
		this.xmasTextures = net.minecraft.client.renderer.blockentity.ChestRenderer.xmasTextures();
		this.models = ChestRenderer.LAYERS.map(layer -> new ChestModel(context.bakeLayer(layer)));
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
		renderState.facing = state.getValue(ChestBlock.FACING);
		renderState.material = getChestMaterial(blockEntity, this.xmasTextures);
		renderState.customSprite = getCustomSprite(blockEntity, renderState.type);

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
		poseStack.mulPose(ChestRenderer.modelTransformation(renderState.facing));

		float openness = renderState.open;
		openness = 1.0F - openness;
		openness = 1.0F - openness * openness * openness;

		SpriteId sprite = renderState.customSprite != null ? renderState.customSprite : Sheets.chooseSprite(renderState.material, renderState.type);
		ChestModel model = this.models.select(renderState.type);
		nodeCollector.submitModel(model, openness, poseStack, renderState.lightCoords, OverlayTexture.NO_OVERLAY, -1, sprite, this.sprites, 0, renderState.breakProgress);
		poseStack.popPose();
	}

	private static SpriteId getCustomSprite(BlockEntity blockEntity, ChestType type) {
		EnumMap<ChestType, SpriteId> map = SPRITES.get(blockEntity.getBlockState().getBlock());
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

	private static EnumMap<ChestType, SpriteId> chestMaterial(String wood, boolean trapped) {
		EnumMap<ChestType, SpriteId> map = new EnumMap<>(ChestType.class);
		String type = (trapped ? "trapped" : "normal");

		map.put(ChestType.SINGLE, Sheets.CHEST_MAPPER.apply(TwilightForestMod.prefix("entity/chest/" + wood + "/" + type)));
		map.put(ChestType.LEFT, Sheets.CHEST_MAPPER.apply(TwilightForestMod.prefix("entity/chest/" + wood + "/" + type + "_left")));
		map.put(ChestType.RIGHT, Sheets.CHEST_MAPPER.apply(TwilightForestMod.prefix("entity/chest/" + wood + "/" + type + "_right")));

		return map;
	}

	public static class RenderState extends ChestRenderState {
		public SpriteId customSprite;
	}
}
