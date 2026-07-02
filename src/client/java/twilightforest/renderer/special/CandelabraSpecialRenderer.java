package twilightforest.client.renderer.special;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3fc;
import org.jetbrains.annotations.Nullable;
import twilightforest.client.renderer.block.CandelabraRenderer;
import twilightforest.components.item.CandelabraData;
import twilightforest.init.TFBlocks;
import twilightforest.init.TFDataComponents;

import java.util.function.Consumer;

public record CandelabraSpecialRenderer() implements SpecialModelRenderer<CandelabraData> {

	@Override
	public @Nullable CandelabraData extractArgument(ItemStack stack) {
		return stack.get(TFDataComponents.CANDELABRA_DATA);
	}

	@Override
	public void submit(@Nullable CandelabraData data, PoseStack poseStack, SubmitNodeCollector nodeCollector, int packedLight, int packedOverlay, boolean hasFoil, int outlineColor) {
		if (data != null) {
			CandelabraRenderer.renderCandles(TFBlocks.CANDELABRA.defaultBlockState(), data, poseStack, nodeCollector, packedLight, packedOverlay, outlineColor);
		}
	}

	@Override
	public void getExtents(Consumer<Vector3fc> output) {
		// Conservative bounds: candelabra base is in the baked model; candles can extend upward.
		output.accept(new org.joml.Vector3f(0.0F, 0.0F, 0.0F));
		output.accept(new org.joml.Vector3f(1.0F, 1.5F, 1.0F));
	}

	public record Unbaked() implements SpecialModelRenderer.Unbaked<CandelabraData> {
		public static final MapCodec<CandelabraSpecialRenderer.Unbaked> MAP_CODEC = MapCodec.unit(CandelabraSpecialRenderer.Unbaked::new);

		@Override
		public MapCodec<CandelabraSpecialRenderer.Unbaked> type() {
			return MAP_CODEC;
		}

		@Override
		public SpecialModelRenderer<CandelabraData> bake(SpecialModelRenderer.BakingContext context) {
			return new CandelabraSpecialRenderer();
		}
	}
}
