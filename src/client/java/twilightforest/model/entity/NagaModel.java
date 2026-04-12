package twilightforest.client.model.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import org.jetbrains.annotations.Nullable;
import twilightforest.client.renderer.entity.NagaRenderer;

public class NagaModel<T extends EntityRenderState> extends EntityModel<T> implements TrophyBlockModel {

	private final ModelPart head;

	public NagaModel(ModelPart root) {
		super(root);
		this.head = root.getChild("head");
	}

	public static LayerDefinition create() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		partdefinition.addOrReplaceChild("head", CubeListBuilder.create()
				.texOffs(0, 0)
				.addBox(-8.0F, -12.0F, -8.0F, 16.0F, 16.0F, 16.0F),
			PartPose.ZERO);

		return LayerDefinition.create(meshdefinition, 64, 32);
	}

	@Override
	public void submitTrophy(PoseStack stack, SubmitNodeCollector nodeCollector, int light, int overlay, int color, ItemDisplayContext context, @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
		stack.scale(0.5F, 0.5F, 0.5F);
		stack.translate(0.0F, -0.25F, 0.0F);

		nodeCollector.submitModelPart(this.head, stack, RenderTypes.entityCutout(NagaRenderer.TEXTURE), light, overlay, null, false, false, color, breakProgress, 0);
	}

	@Override
	public void setupRotationsForTrophy(float x, float y, float z, float mouthAngle) {
		this.head.yRot = y * Mth.DEG_TO_RAD;
		this.head.xRot = z * Mth.DEG_TO_RAD;
	}
}
