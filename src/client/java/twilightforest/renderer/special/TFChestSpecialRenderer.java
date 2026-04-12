package twilightforest.client.renderer.special;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.model.object.chest.ChestModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.resources.Identifier;
import org.joml.Vector3fc;

import java.util.function.Consumer;

public record TFChestSpecialRenderer(SpriteGetter sprites, ChestModel model, SpriteId sprite, float openness) implements NoDataSpecialModelRenderer {

	@Override
	public void submit(PoseStack poseStack, SubmitNodeCollector nodeCollector, int packedLight, int packedOverlay, boolean hasFoil, int outlineColor) {
		nodeCollector.submitModel(this.model, this.openness, poseStack, packedLight, packedOverlay, -1, this.sprite, this.sprites, outlineColor, null);
	}

	@Override
	public void getExtents(Consumer<Vector3fc> output) {
		PoseStack poseStack = new PoseStack();
		this.model.setupAnim(this.openness);
		this.model.root().getExtentsForGui(poseStack, output);
	}

	public record Unbaked(Identifier texture, float openness) implements SpecialModelRenderer.Unbaked<Void> {
		public static final MapCodec<TFChestSpecialRenderer.Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(
			instance -> instance.group(
					Identifier.CODEC.fieldOf("texture").forGetter(TFChestSpecialRenderer.Unbaked::texture),
					Codec.FLOAT.optionalFieldOf("openness", 0.0F).forGetter(TFChestSpecialRenderer.Unbaked::openness)
				)
				.apply(instance, TFChestSpecialRenderer.Unbaked::new)
		);

		public Unbaked(Identifier location) {
			this(location, 0.0F);
		}

		@Override
		public MapCodec<TFChestSpecialRenderer.Unbaked> type() {
			return MAP_CODEC;
		}

		@Override
		public SpecialModelRenderer<Void> bake(SpecialModelRenderer.BakingContext context) {
			ChestModel chestmodel = new ChestModel(context.entityModelSet().bakeLayer(ModelLayers.CHEST));
			SpriteId sprite = Sheets.CHEST_MAPPER.apply(this.texture);
			return new TFChestSpecialRenderer(context.sprites(), chestmodel, sprite, this.openness());
		}
	}
}
