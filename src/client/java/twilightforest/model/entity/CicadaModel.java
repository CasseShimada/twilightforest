package twilightforest.client.model.entity;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.rendertype.RenderTypes;

public class CicadaModel extends Model<Void> {

	private static final String PART_LEGS = "legs";
	private static final String PART_FAT_BODY = "fat_body";
	private static final String PART_SKINNY_BODY = "skinny_body";
	private static final String PART_EYE_1 = "eye_1";
	private static final String PART_EYE_2 = "eye_2";
	private static final String PART_WINGS = "wings";

	private final ModelPart wings;

	public CicadaModel(ModelPart root) {
		super(root, RenderTypes::entityCutoutNoCull);
		this.wings = root.getChild(PART_WINGS);
	}

	public static LayerDefinition create() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		partdefinition.addOrReplaceChild(PART_LEGS, CubeListBuilder.create()
				.texOffs(0, 21)
				.addBox(-4.0F, 7.9F, -5.0F, 8.0F, 1.0F, 9.0F),
			PartPose.ZERO);

		partdefinition.addOrReplaceChild(PART_FAT_BODY, CubeListBuilder.create()
				.texOffs(0, 11)
				.addBox(-2.0F, 6.0F, -4.0F, 4.0F, 2.0F, 6.0F),
			PartPose.ZERO);

		partdefinition.addOrReplaceChild(PART_SKINNY_BODY, CubeListBuilder.create()
				.texOffs(0, 0)
				.addBox(-1.0F, 7.0F, -5.0F, 2.0F, 1.0F, 8.0F),
			PartPose.ZERO);

		partdefinition.addOrReplaceChild(PART_EYE_1, CubeListBuilder.create()
				.texOffs(20, 15)
				.addBox(1.0F, 5.0F, 2.0F, 2.0F, 2.0F, 2.0F),
			PartPose.ZERO);

		partdefinition.addOrReplaceChild(PART_EYE_2, CubeListBuilder.create()
				.texOffs(20, 15)
				.addBox(-3.0F, 5.0F, 2.0F, 2.0F, 2.0F, 2.0F),
			PartPose.ZERO);

		partdefinition.addOrReplaceChild(PART_WINGS, CubeListBuilder.create()
				.texOffs(20, 0)
				.addBox(-4.0F, 5.0F, -7.0F, 8.0F, 1.0F, 8.0F),
			PartPose.ZERO);

		return LayerDefinition.create(meshdefinition, 64, 32);
	}

	public void setupBasePass() {
		setAllPartsSkipDraw(false);
		this.wings.skipDraw = true;
	}

	public void setupWingPass() {
		setAllPartsSkipDraw(true);
		this.wings.skipDraw = false;
	}

	private void setAllPartsSkipDraw(boolean skipDraw) {
		this.root().getAllParts().forEach(part -> part.skipDraw = skipDraw);
	}
}
