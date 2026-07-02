package twilightforest.client.renderer.special;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.PlayerSkinRenderCache;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.SkullBlock;
import org.joml.Vector3fc;
import org.jetbrains.annotations.Nullable;
import twilightforest.block.AbstractSkullCandleBlock;
import twilightforest.components.item.SkullCandles;
import twilightforest.init.TFDataComponents;

import java.util.Optional;
import java.util.function.Consumer;

public record SkullCandleSpecialRenderer(
	PlayerSkinRenderCache playerSkinRenderCache,
	SkullBlock.Type skullType,
	net.minecraft.client.model.object.skull.SkullModelBase model,
	@Nullable Identifier textureOverride,
	float animation
) implements SpecialModelRenderer<Pair<ResolvableProfile, SkullCandles>> {

	@Override
	public @Nullable Pair<ResolvableProfile, SkullCandles> extractArgument(ItemStack stack) {
		return Pair.of(stack.get(DataComponents.PROFILE), stack.get(TFDataComponents.SKULL_CANDLES));
	}

	@Override
	public void submit(@Nullable Pair<ResolvableProfile, SkullCandles> data, PoseStack poseStack, SubmitNodeCollector nodeCollector, int packedLight, int packedOverlay, boolean hasFoil, int outlineColor) {
		ResolvableProfile profile = data != null ? data.getFirst() : null;
		SkullCandles candles = data != null ? data.getSecond() : null;

		RenderType renderType;
		if (this.textureOverride != null) {
			renderType = SkullBlockRenderer.getSkullRenderType(this.skullType, this.textureOverride);
		} else if (this.skullType == SkullBlock.Types.PLAYER) {
			PlayerSkinRenderCache.RenderInfo info = profile != null ? this.playerSkinRenderCache.getOrDefault(profile) : null;
			renderType = info != null ? info.renderType() : PlayerSkinRenderCache.DEFAULT_PLAYER_SKIN_RENDER_TYPE;
		} else {
			renderType = SkullBlockRenderer.getSkullRenderType(this.skullType, null);
		}

		SkullBlockRenderer.submitSkull(this.animation, poseStack, nodeCollector, packedLight, this.model, renderType, outlineColor, null);

		if (candles != null) {
			ItemModelResolver resolver = net.minecraft.client.Minecraft.getInstance().getItemModelResolver();
			ItemStackRenderState candleRenderState = new ItemStackRenderState();
			ItemStack candleStack = new ItemStack(AbstractSkullCandleBlock.candleColorToCandle(AbstractSkullCandleBlock.CandleColors.colorFromInt(candles.color())));
			resolver.updateForTopItem(candleRenderState, candleStack, net.minecraft.world.item.ItemDisplayContext.FIXED, null, null, 0);
			poseStack.pushPose();
			poseStack.translate(0.0F, 0.5F, 0.0F);
			candleRenderState.submit(poseStack, nodeCollector, packedLight, packedOverlay, outlineColor);
			poseStack.popPose();
		}
	}

	@Override
	public void getExtents(Consumer<Vector3fc> output) {
		PoseStack poseStack = new PoseStack();
		poseStack.translate(0.5F, 0.0F, 0.5F);
		poseStack.scale(-1.0F, -1.0F, 1.0F);
		this.model.root().getExtentsForGui(poseStack, output);

		// Include candle bounds roughly (block AABB shifted up).
		output.accept(new org.joml.Vector3f(0.0F, 0.5F, 0.0F));
		output.accept(new org.joml.Vector3f(1.0F, 1.5F, 1.0F));
	}

	public record Unbaked(SkullBlock.Type kind, Optional<Identifier> textureOverride, float animation) implements SpecialModelRenderer.Unbaked<Pair<ResolvableProfile, SkullCandles>> {
		public static final MapCodec<SkullCandleSpecialRenderer.Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				SkullBlock.Type.CODEC.fieldOf("kind").forGetter(SkullCandleSpecialRenderer.Unbaked::kind),
				Identifier.CODEC.optionalFieldOf("texture").forGetter(SkullCandleSpecialRenderer.Unbaked::textureOverride),
				Codec.FLOAT.optionalFieldOf("animation", 0.0F).forGetter(SkullCandleSpecialRenderer.Unbaked::animation))
			.apply(instance, SkullCandleSpecialRenderer.Unbaked::new));

		public Unbaked(SkullBlock.Type kind) {
			this(kind, Optional.empty(), 0.0F);
		}

		@Override
		public MapCodec<SkullCandleSpecialRenderer.Unbaked> type() {
			return MAP_CODEC;
		}

		@Override
		public @Nullable SpecialModelRenderer<Pair<ResolvableProfile, SkullCandles>> bake(SpecialModelRenderer.BakingContext context) {
			var skullModel = SkullBlockRenderer.createModel(context.entityModelSet(), this.kind());
			Identifier texture = this.textureOverride()
				.map(location -> location.withPath(path -> "textures/entity/" + path + ".png"))
				.orElse(null);
			return skullModel != null
				? new SkullCandleSpecialRenderer(context.playerSkinRenderCache(), this.kind(), skullModel, texture, this.animation())
				: null;
		}
	}
}
