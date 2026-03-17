package twilightforest.client.renderer.special;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Vector3fc;
import twilightforest.client.model.entity.TrophyBlockModel;
import twilightforest.client.renderer.block.TrophyRenderer;
import twilightforest.config.TFConfig;
import twilightforest.enums.BossVariant;

import java.util.Optional;
import java.util.function.Function;

public record TrophySpecialRenderer(Function<BossVariant, TrophyBlockModel> trophy, BossVariant variant, Optional<Integer> fixedRotation) implements NoDataSpecialModelRenderer {

	@Override
	public void submit(ItemDisplayContext context, PoseStack stack, SubmitNodeCollector nodeCollector, int light, int overlay, boolean foil, int outlineColor) {
		TrophyBlockModel model = this.trophy().apply(this.variant());
		// 1.21.11 item display transforms already provide the forward-facing trophy orientation.
		float itemYaw = 0.0F;
		float rotation = this.fixedRotation.orElse(TFConfig.rotateTrophyHeadsGui && !Minecraft.getInstance().isPaused() ? (int) (Util.getMillis() / 35) : 0);
		float animation = !Minecraft.getInstance().isPaused() ? (int) (Util.getMillis() / 30) + Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaTicks() : 0;
		if (model != null) {
			if (context == ItemDisplayContext.GUI) {
				stack.pushPose();
				stack.translate(0.5F, 0.5F, 0.5F);
				stack.mulPose(Axis.YN.rotationDegrees(rotation));
				stack.translate(-0.5F, -0.5F, -0.5F);
				TrophyRenderer.render(null, itemYaw, model, false, animation, stack, nodeCollector, light, overlay, context, null);
				stack.popPose();
			} else {
				TrophyRenderer.render(null, itemYaw, model, false, animation, stack, nodeCollector, light, overlay, context, null);
			}
		}
	}

	@Override
	public void getExtents(java.util.function.Consumer<Vector3fc> output) {
		TrophyBlockModel model = this.trophy().apply(this.variant());
		if (model instanceof net.minecraft.client.model.Model<?> mcModel) {
			PoseStack poseStack = new PoseStack();
			mcModel.root().getExtentsForGui(poseStack, output);
		}
	}

	public record Unbaked(BossVariant variant, Optional<Integer> fixedRotation) implements SpecialModelRenderer.Unbaked {
		public static final MapCodec<TrophySpecialRenderer.Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				BossVariant.CODEC.fieldOf("kind").forGetter(TrophySpecialRenderer.Unbaked::variant),
				Codec.INT.optionalFieldOf("fixed_rotation").forGetter(TrophySpecialRenderer.Unbaked::fixedRotation))
			.apply(instance, TrophySpecialRenderer.Unbaked::new));

		public Unbaked(BossVariant variant) {
			this(variant, Optional.empty());
		}

		@Override
		public MapCodec<TrophySpecialRenderer.Unbaked> type() {
			return MAP_CODEC;
		}

		@Override
		public SpecialModelRenderer<?> bake(SpecialModelRenderer.BakingContext context) {
			Function<BossVariant, TrophyBlockModel> model = Util.memoize(variant -> TrophyRenderer.createTrophyModel(context.entityModelSet(), variant));
			return new TrophySpecialRenderer(model, variant, this.fixedRotation());
		}
	}
}
