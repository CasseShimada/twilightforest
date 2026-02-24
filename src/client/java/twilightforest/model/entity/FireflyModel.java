package twilightforest.client.model.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ARGB;

public class FireflyModel extends Model<Void> {

	private static final String PART_LEGS = "legs";
	private static final String PART_FAT_BODY = "fat_body";
	private static final String PART_SKINNY_BODY = "skinny_body";
	private static final String PART_GLOW = "glow";

	private final ModelPart glow;

	public FireflyModel(ModelPart root) {
		super(root, RenderTypes::entityCutoutNoCull);
		this.glow = root.getChild(PART_GLOW);
	}

	public static LayerDefinition create() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();
		partdefinition.addOrReplaceChild(PART_LEGS,
			CubeListBuilder.create()
				.texOffs(0, 21)
				.addBox(-4.0F, 7.9F, -5.0F, 8.0F, 0.0F, 10.0F),
			PartPose.ZERO);
		partdefinition.addOrReplaceChild(PART_FAT_BODY,
			CubeListBuilder.create()
				.texOffs(0, 11)
				.addBox(-2.0F, 6.0F, -4.0F, 4.0F, 2.0F, 6.0F),
			PartPose.ZERO);
		partdefinition.addOrReplaceChild(PART_SKINNY_BODY,
			CubeListBuilder.create()
				.texOffs(0, 0)
				.addBox(-1.0F, 6.9F, -5.0F, 2.0F, 1.0F, 8.0F),
			PartPose.ZERO);
		partdefinition.addOrReplaceChild(PART_GLOW,
			CubeListBuilder.create()
				.texOffs(20, 0)
				.addBox(-5.0F, 5.9F, -9.0F, 10.0F, 0.0F, 10.0F),
			PartPose.ZERO);

		return LayerDefinition.create(meshdefinition, 64, 32);
	}

	public void setupBasePass() {
		setAllPartsSkipDraw(false);
		this.glow.skipDraw = true;
	}

	public void setupGlowPass() {
		setAllPartsSkipDraw(true);
		this.glow.skipDraw = false;
	}

	public void renderGlow(PoseStack stack, VertexConsumer consumer, int overlay, float alpha) {
		this.glow.skipDraw = false;
		this.glow.render(stack, consumer, 0xF000F0, overlay, ARGB.colorFromFloat(alpha, 1.0F, 1.0F, 1.0F));
	}

	private void setAllPartsSkipDraw(boolean skipDraw) {
		this.root().getAllParts().forEach(part -> part.skipDraw = skipDraw);
	}
}
