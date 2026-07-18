package twilightforest.client.model.armor;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public final class TravellersGearModels {
	private TravellersGearModels() {
	}

	public static MeshDefinition addGogglePieces(CubeDeformation deformation) {
		MeshDefinition mesh = HumanoidModel.createMesh(deformation, 0.0F);
		mesh.getRoot().addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
		return mesh;
	}

	public static MeshDefinition addGlovePieces(CubeDeformation deformation, boolean slim) {
		MeshDefinition mesh = HumanoidModel.createMesh(deformation, 0.0F);
		PartDefinition root = mesh.getRoot();
		if (slim) {
			root.addOrReplaceChild("right_arm",
				CubeListBuilder.create().texOffs(40, 0).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, deformation),
				PartPose.offset(-5.0F, 2.5F, 0.0F));
			root.addOrReplaceChild("left_arm",
				CubeListBuilder.create().texOffs(40, 0).mirror().addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, deformation),
				PartPose.offset(5.0F, 2.5F, 0.0F));
		} else {
			root.addOrReplaceChild("right_arm",
				CubeListBuilder.create().texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, deformation),
				PartPose.offset(-5.0F, 2.0F, 0.0F));
			root.addOrReplaceChild("left_arm",
				CubeListBuilder.create().texOffs(40, 16).mirror().addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, deformation),
				PartPose.offset(5.0F, 2.0F, 0.0F));
		}
		return mesh;
	}

	public static MeshDefinition addBootPieces(CubeDeformation deformation) {
		MeshDefinition mesh = HumanoidModel.createMesh(deformation, 0.0F);
		PartDefinition root = mesh.getRoot();
		root.getChild("right_leg").addOrReplaceChild("right_bump",
			CubeListBuilder.create().texOffs(24, 0).addBox(-0.5F, -1.0F, 0.0F, 4.0F, 3.0F, 1.0F, deformation),
			PartPose.offset(-1.5F, 10.0F, -4.0F));
		root.getChild("left_leg").addOrReplaceChild("left_bump",
			CubeListBuilder.create().texOffs(24, 0).mirror().addBox(-0.5F, -1.0F, 0.0F, 4.0F, 3.0F, 1.0F, deformation),
			PartPose.offset(-1.5F, 10.0F, -4.0F));
		return mesh;
	}
}
