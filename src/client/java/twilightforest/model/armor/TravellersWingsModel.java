package twilightforest.client.model.armor;

import net.fabricmc.fabric.api.client.rendering.v1.FabricRenderState;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Vector3f;
import twilightforest.TwilightForestMod;
import twilightforest.components.entity.TravellersWingsAnimAttachment;
import twilightforest.components.entity.TravellersWingsAttachment;
import twilightforest.init.TFDataAttachments;
import twilightforest.util.TFMathUtil;

import java.util.List;

public final class TravellersWingsModel extends HumanoidModel<HumanoidRenderState> {
	private static final double INTERPOLATION_TIME = 4.0D;
	private static final float ANGLE_10_DEG = Mth.PI / 18.0F;
	private static final Vector3f SMALL_SWING = new Vector3f(8.0F, 8.0F, 8.0F);
	private static final Vector3f BIG_SWING = new Vector3f(15.0F, 15.0F, 15.0F);
	private static final float BASE_OFFSET = -0.5F;
	private static final float PART_OFFSET = 0.002F;

	public static final RenderStateDataKey<AnimationState> ANIMATION_KEY = RenderStateDataKey.create(
		() -> TwilightForestMod.prefix("travellers_wings_animation").toString());

	private final ModelPart wingBaseRight;
	private final ModelPart wingBaseLeft;
	private final List<ModelPart> wingPartsRight;
	private final List<ModelPart> wingPartsLeft;
	private final List<ModelPart> beltParts;

	public TravellersWingsModel(ModelPart root) {
		super(root);
		ModelPart body = root.getChild("body");
		this.wingBaseLeft = body.getChild("wingBaseLeft");
		this.wingPartsLeft = List.of(
			this.wingBaseLeft.getChild("wingEdgeLeft"),
			this.wingBaseLeft.getChild("wingInsetLeft"),
			this.wingBaseLeft.getChild("wingCenterLeft"),
			this.wingBaseLeft.getChild("wingFlangeLeft"),
			this.wingBaseLeft.getChild("wingAuxLeft"));
		this.wingBaseRight = body.getChild("wingBaseRight");
		this.wingPartsRight = List.of(
			this.wingBaseRight.getChild("wingEdgeRight"),
			this.wingBaseRight.getChild("wingInsetRight"),
			this.wingBaseRight.getChild("wingCenterRight"),
			this.wingBaseRight.getChild("wingFlangeRight"),
			this.wingBaseRight.getChild("wingAuxRight"));
		this.beltParts = List.of(
			body.getChild("buckle"), body.getChild("frontRight"), body.getChild("frontLeft"),
			body.getChild("sideRight"), body.getChild("sideLeft"), body.getChild("back"));
		this.body.skipDraw = true;
	}

	public static LayerDefinition createLayer(float deformation) {
		MeshDefinition mesh = HumanoidModel.createMesh(new CubeDeformation(deformation), 0.0F);
		PartDefinition body = mesh.getRoot().getChild("body");
		createWings(body);
		createBelt(body, 0.0F);
		return LayerDefinition.create(mesh, 128, 32);
	}

	private static void createWings(PartDefinition root) {
		PartDefinition left = root.addOrReplaceChild("wingBaseLeft",
			CubeListBuilder.create().texOffs(64, 9).mirror().addBox(BASE_OFFSET, -1.0F, 0.0F, 1.0F, 2.0F, 10.0F),
			PartPose.offsetAndRotation(1.0F, 1.0F, 0.0F, ANGLE_10_DEG * 3.0F, ANGLE_10_DEG * 3.0F, 0.0F));
		left.addOrReplaceChild("wingEdgeLeft",
			CubeListBuilder.create().texOffs(64, 21).mirror().addBox(0.0F, 0.0F, -2.0F, 1.0F, 9.0F, 2.0F),
			PartPose.offsetAndRotation(BASE_OFFSET + PART_OFFSET, -1.0F, 10.0F, ANGLE_10_DEG * 3.0F, 0.0F, 0.0F));
		left.addOrReplaceChild("wingInsetLeft",
			CubeListBuilder.create().texOffs(70, 21).mirror().addBox(0.0F, 0.0F, -1.0F, 1.0F, 9.0F, 2.0F),
			PartPose.offsetAndRotation(BASE_OFFSET + PART_OFFSET * 2.0F, 0.0F, 7.8F, ANGLE_10_DEG * 2.0F, 0.0F, 0.0F));
		left.addOrReplaceChild("wingCenterLeft",
			CubeListBuilder.create().texOffs(76, 21).mirror().addBox(0.0F, 0.0F, -1.0F, 1.0F, 9.0F, 2.0F),
			PartPose.offsetAndRotation(BASE_OFFSET + PART_OFFSET * 3.0F, 0.3F, 6.3F, ANGLE_10_DEG, 0.0F, 0.0F));
		left.addOrReplaceChild("wingFlangeLeft",
			CubeListBuilder.create().texOffs(82, 21).mirror().addBox(0.0F, 0.0F, -1.0F, 1.0F, 8.0F, 2.0F),
			PartPose.offset(BASE_OFFSET + PART_OFFSET * 4.0F, 0.3F, 5.1F));
		left.addOrReplaceChild("wingAuxLeft",
			CubeListBuilder.create().texOffs(88, 21).mirror().addBox(0.0F, 0.0F, -1.0F, 1.0F, 7.0F, 2.0F),
			PartPose.offsetAndRotation(BASE_OFFSET + PART_OFFSET * 5.0F, 0.1F, 4.0F, -ANGLE_10_DEG, 0.0F, 0.0F));

		PartDefinition right = root.addOrReplaceChild("wingBaseRight",
			CubeListBuilder.create().texOffs(98, 9).addBox(BASE_OFFSET, -1.0F, 0.0F, 1.0F, 2.0F, 10.0F),
			PartPose.offsetAndRotation(-1.0F, 1.0F, 0.0F, ANGLE_10_DEG * 3.0F, -ANGLE_10_DEG * 3.0F, 0.0F));
		right.addOrReplaceChild("wingEdgeRight",
			CubeListBuilder.create().texOffs(98, 21).addBox(0.0F, 0.0F, -2.0F, 1.0F, 9.0F, 2.0F),
			PartPose.offsetAndRotation(BASE_OFFSET - PART_OFFSET, -1.0F, 10.0F, ANGLE_10_DEG * 3.0F, 0.0F, 0.0F));
		right.addOrReplaceChild("wingInsetRight",
			CubeListBuilder.create().texOffs(104, 21).addBox(0.0F, 0.0F, -1.0F, 1.0F, 9.0F, 2.0F),
			PartPose.offsetAndRotation(BASE_OFFSET - PART_OFFSET * 2.0F, 0.0F, 7.8F, ANGLE_10_DEG * 2.0F, 0.0F, 0.0F));
		right.addOrReplaceChild("wingCenterRight",
			CubeListBuilder.create().texOffs(110, 21).addBox(0.0F, 0.0F, -1.0F, 1.0F, 9.0F, 2.0F),
			PartPose.offsetAndRotation(BASE_OFFSET - PART_OFFSET * 3.0F, 0.3F, 6.3F, ANGLE_10_DEG, 0.0F, 0.0F));
		right.addOrReplaceChild("wingFlangeRight",
			CubeListBuilder.create().texOffs(116, 21).addBox(0.0F, 0.0F, -1.0F, 1.0F, 8.0F, 2.0F),
			PartPose.offset(BASE_OFFSET - PART_OFFSET * 4.0F, 0.3F, 5.1F));
		right.addOrReplaceChild("wingAuxRight",
			CubeListBuilder.create().texOffs(122, 21).addBox(0.0F, 0.0F, -1.0F, 1.0F, 7.0F, 2.0F),
			PartPose.offsetAndRotation(BASE_OFFSET - PART_OFFSET * 5.0F, 0.1F, 4.0F, -ANGLE_10_DEG, 0.0F, 0.0F));
	}

	private static void createBelt(PartDefinition root, float deformation) {
		CubeDeformation cube = new CubeDeformation(deformation);
		root.addOrReplaceChild("buckle", CubeListBuilder.create().texOffs(8, 9).addBox(-2.0F, -2.0F, 0.0F, 4.0F, 4.0F, 1.0F, cube), PartPose.offset(0.0F, 10.0F, -2.75F));
		root.addOrReplaceChild("frontRight", CubeListBuilder.create().texOffs(0, 9).addBox(-3.0F, -1.0F, 0.0F, 3.0F, 2.0F, 1.0F, cube), PartPose.offset(-1.75F, 10.0F, -2.5F));
		root.addOrReplaceChild("frontLeft", CubeListBuilder.create().texOffs(18, 9).addBox(0.0F, -1.0F, 0.0F, 3.0F, 2.0F, 1.0F, cube), PartPose.offset(1.75F, 10.0F, -2.5F));
		root.addOrReplaceChild("sideRight", CubeListBuilder.create().texOffs(0, 3).addBox(-1.0F, -1.0F, 0.0F, 1.0F, 2.0F, 4.0F, cube.extend(-0.01F)), PartPose.offset(-3.75F, 10.0F, -2.0F));
		root.addOrReplaceChild("sideLeft", CubeListBuilder.create().texOffs(16, 3).addBox(0.0F, -1.0F, 0.0F, 1.0F, 2.0F, 4.0F, cube.extend(-0.01F)), PartPose.offset(3.75F, 10.0F, -2.0F));
		root.addOrReplaceChild("back", CubeListBuilder.create().texOffs(2, 0).addBox(-4.5F, -1.0F, 0.0F, 9.0F, 2.0F, 1.0F, cube), PartPose.offset(0.0F, 10.0F, 1.5F));
	}

	public static void extractAnimationState(LivingEntity entity, HumanoidRenderState state, FabricRenderState fabricState) {
		TravellersWingsAnimAttachment animation = TFDataAttachments.get(entity, TFDataAttachments.TRAVELLERS_WINGS_ANIM);
		TravellersWingsAttachment wings = TFDataAttachments.get(entity, TFDataAttachments.TRAVELLERS_WINGS);
		double deltaTicks = state.ageInTicks - animation.oldAgeInTicks;
		double interpolationTime = INTERPOLATION_TIME;

		Vector3f rightRotations;
		switch (wings.state) {
			case DOUBLE_JUMP -> {
				rightRotations = new Vector3f(-0.4F, -0.8F, -0.1F);
				interpolationTime = INTERPOLATION_TIME - 1.0D;
			}
			case RIDE -> rightRotations = calculateRotations(animation, deltaTicks, 10.0F, ANGLE_10_DEG * 3.0F, -0.6F, -0.3F, BIG_SWING);
			case SWIM -> rightRotations = calculateRotations(animation, deltaTicks, 17.0F, ANGLE_10_DEG * 4.0F, -1.0F, -0.5F, BIG_SWING);
			case FALL_SLOW -> rightRotations = calculateRotations(animation, deltaTicks, 17.0F, ANGLE_10_DEG * 5.0F, -1.1F, -0.1F, BIG_SWING);
			case FALL_FAST -> rightRotations = calculateRotations(animation, deltaTicks, 2.0F, ANGLE_10_DEG * 4.0F, -1.1F, -0.3F, SMALL_SWING);
			case SPRINT -> rightRotations = calculateRotations(animation, deltaTicks, 2.0F, ANGLE_10_DEG * 3.0F, -0.3F, 0.0F, BIG_SWING);
			case SIDESTEP -> rightRotations = calculateRotations(animation, deltaTicks, 2.0F, ANGLE_10_DEG * 3.0F, wings.sidestepLeft ? -0.6F : 0.4F, 0.0F, BIG_SWING);
			default -> rightRotations = calculateRotations(animation, deltaTicks, state.walkAnimationSpeed > 0.1F ? 4.0F : 20.0F, ANGLE_10_DEG * 3.0F, -0.6F, -0.3F, BIG_SWING);
		}

		float rightX = (float) TFMathUtil.interpolateToTarget(animation.xRotOld, rightRotations.x, deltaTicks, interpolationTime);
		float rightY = (float) TFMathUtil.interpolateToTarget(animation.yRotOldRight, rightRotations.y, deltaTicks, interpolationTime);
		float rightZ = (float) TFMathUtil.interpolateToTarget(animation.zRotOld, rightRotations.z, deltaTicks, interpolationTime);
		float targetLeftY = wings.state == TravellersWingsAttachment.WingState.SIDESTEP
			? calculateRotations(animation, deltaTicks, 2.0F, ANGLE_10_DEG * 3.0F, wings.sidestepLeft ? -0.4F : 0.6F, 0.0F, BIG_SWING).y
			: -rightRotations.y;
		float leftY = (float) TFMathUtil.interpolateToTarget(animation.yRotOldLeft, targetLeftY, deltaTicks, interpolationTime);

		animation.accumulatedPhase %= Mth.TWO_PI;
		animation.oldAgeInTicks = state.ageInTicks;
		animation.xRotOld = rightX;
		animation.yRotOldRight = rightY;
		animation.yRotOldLeft = leftY;
		animation.zRotOld = rightZ;

		fabricState.setData(ANIMATION_KEY, new AnimationState(
			rightX, rightY, rightZ, rightX, leftY, -rightZ,
			state.isCrouching, (float) Math.sqrt(state.distanceToCameraSq) * PART_OFFSET));
	}

	private static Vector3f calculateRotations(TravellersWingsAnimAttachment animation, double deltaTicks, float phaseDivisor,
		float xOffset, float yOffset, float zOffset, Vector3f sinDivisors) {
		animation.accumulatedPhase += deltaTicks / phaseDivisor;
		float sin = (float) Math.sin(animation.accumulatedPhase);
		return new Vector3f(sin / sinDivisors.x + xOffset, sin / sinDivisors.y + yOffset, sin / sinDivisors.z + zOffset);
	}

	@Override
	public void setupAnim(HumanoidRenderState state) {
		super.setupAnim(state);
		AnimationState animation = ((FabricRenderState) state).getData(ANIMATION_KEY);
		if (animation == null) {
			return;
		}
		if (animation.crouching) {
			this.wingBaseRight.y += 2.0F;
			this.wingBaseLeft.y += 2.0F;
		}
		this.wingBaseRight.xRot = animation.rightX;
		this.wingBaseRight.yRot = animation.rightY;
		this.wingBaseRight.zRot = animation.rightZ;
		this.wingBaseLeft.xRot = animation.leftX;
		this.wingBaseLeft.yRot = animation.leftY;
		this.wingBaseLeft.zRot = animation.leftZ;
		for (int index = 0; index < this.wingPartsLeft.size(); index++) {
			float offset = (index + 1) * animation.partDistanceOffset;
			this.wingPartsLeft.get(index).x = BASE_OFFSET + offset;
			this.wingPartsRight.get(index).x = BASE_OFFSET - offset;
		}
	}

	public void setWingsVisible(boolean visible) {
		this.wingBaseLeft.skipDraw = !visible;
		this.wingBaseRight.skipDraw = !visible;
		this.wingPartsLeft.forEach(part -> part.skipDraw = !visible);
		this.wingPartsRight.forEach(part -> part.skipDraw = !visible);
	}

	public void setBeltVisible(boolean visible) {
		this.beltParts.forEach(part -> part.skipDraw = !visible);
	}

	public record AnimationState(
		float rightX, float rightY, float rightZ,
		float leftX, float leftY, float leftZ,
		boolean crouching, float partDistanceOffset
	) {
	}
}
