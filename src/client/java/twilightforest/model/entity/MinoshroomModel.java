package twilightforest.client.model.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.effects.SpearAnimations;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import org.jetbrains.annotations.Nullable;
import twilightforest.client.renderer.entity.MinoshroomRenderer;
import twilightforest.client.state.MinoshroomRenderState;

public class MinoshroomModel extends HumanoidModel<MinoshroomRenderState> implements TrophyBlockModel {

	public final ModelPart cowTorso;
	protected final ModelPart rightFrontLeg;
	protected final ModelPart leftFrontLeg;
	protected final ModelPart rightBackLeg;
	protected final ModelPart leftBackLeg;

	public MinoshroomModel(ModelPart root) {
		super(root);
		this.cowTorso = root.getChild("cow_torso");
		this.rightFrontLeg = root.getChild("right_front_leg");
		this.leftFrontLeg = root.getChild("left_front_leg");
		this.rightBackLeg = root.getChild("right_back_leg");
		this.leftBackLeg = root.getChild("left_back_leg");
	}

	public static LayerDefinition create() {
		MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0);
		PartDefinition partdefinition = meshdefinition.getRoot();

		var head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create()
				.texOffs(96, 16)
				.addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F),
			PartPose.offset(0.0F, -6.0F, -9.0F));

		head.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);

		head.addOrReplaceChild("snout", CubeListBuilder.create()
				.texOffs(105, 28)
				.addBox(-2.0F, -1.0F, -1.0F, 4.0F, 3.0F, 1.0F),
			PartPose.offset(0.0F, -2.0F, -4.0F));

		var rightHorn = head.addOrReplaceChild("right_horn_1", CubeListBuilder.create().mirror()
				.texOffs(0, 0)
				.addBox(-5.5F, -1.5F, -1.5F, 5.0F, 3.0F, 3.0F),
			PartPose.offsetAndRotation(-2.5F, -6.5F, 0.0F, 0.0F, -25.0F * Mth.DEG_TO_RAD, 10.0F * Mth.DEG_TO_RAD));

		rightHorn.addOrReplaceChild("right_horn_2", CubeListBuilder.create().mirror()
				.texOffs(16, 0)
				.addBox(-3.5F, -1.0F, -1.0F, 3.0F, 2.0F, 2.0F),
			PartPose.offsetAndRotation(-4.5F, 0.0F, 0.0F, 0.0F, -15.0F * Mth.DEG_TO_RAD, 45.0F * Mth.DEG_TO_RAD));

		var leftHorn = head.addOrReplaceChild("left_horn_1", CubeListBuilder.create()
				.texOffs(0, 0)
				.addBox(0.5F, -1.5F, -1.5F, 5.0F, 3.0F, 3.0F),
			PartPose.offsetAndRotation(2.5F, -6.5F, 0.0F, 0.0F, 25.0F * Mth.DEG_TO_RAD, -10.0F * Mth.DEG_TO_RAD));

		leftHorn.addOrReplaceChild("left_horn_2", CubeListBuilder.create()
				.texOffs(16, 0)
				.addBox(0.5F, -1.0F, -1.0F, 3.0F, 2.0F, 2.0F),
			PartPose.offsetAndRotation(4.5F, 0.0F, 0.0F, 0.0F, 15.0F * Mth.DEG_TO_RAD, -45.0F * Mth.DEG_TO_RAD));

		partdefinition.addOrReplaceChild("body", CubeListBuilder.create()
				.texOffs(64, 0)
				.addBox(-4.0F, 0.0F, -2.5F, 8.0F, 12.0F, 5.0F),
			PartPose.offset(0.0F, -6.0F, -9.0F));

		partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create()
				.texOffs(90, 0)
				.addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F),
			PartPose.offset(-5.0F, -4.0F, -9.0F));

		partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().mirror()
				.texOffs(90, 0)
				.addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F),
			PartPose.offset(5.0F, -4.0F, -9.0F));

		partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.ZERO);
		partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.ZERO);

		var body = partdefinition.addOrReplaceChild("cow_torso", CubeListBuilder.create()
				.texOffs(18, 4)
				.addBox(-6.0F, -10.0F, -7.0F, 12.0F, 18.0F, 10.0F),
			PartPose.offsetAndRotation(0.0F, 5.0F, 2.0F, 1.570796F, 0.0F, 0.0F));

		body.addOrReplaceChild("udders", CubeListBuilder.create()
				.texOffs(53, 1)
				.addBox(-2.0F, -3.0F, 0.0F, 4.0F, 6.0F, 1.0F),
			PartPose.offset(0.0F, 5.0F, -8.0F));

		partdefinition.addOrReplaceChild("left_back_leg", CubeListBuilder.create()
				.texOffs(0, 16)
				.addBox(-3.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F),
			PartPose.offset(-3.0F, 12.0F, 7.0F));

		partdefinition.addOrReplaceChild("right_back_leg", CubeListBuilder.create()
				.texOffs(0, 16)
				.addBox(-1.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F),
			PartPose.offset(3.0F, 12.0F, 7.0F));

		partdefinition.addOrReplaceChild("left_front_leg", CubeListBuilder.create()
				.texOffs(0, 16)
				.addBox(-3.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F),
			PartPose.offset(-3.0F, 12.0F, -5.0F));

		partdefinition.addOrReplaceChild("right_front_leg", CubeListBuilder.create()
				.texOffs(0, 16)
				.addBox(-1.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F),
			PartPose.offset(3.0F, 12.0F, -5.0F));

		return LayerDefinition.create(meshdefinition, 128, 32);
	}

	@Override
	public void setupAnim(MinoshroomRenderState state) {
		this.resetPose();
		// copied from HumanoidModel
		HumanoidModel.ArmPose leftPose = state.leftArmPose;
		HumanoidModel.ArmPose rightPose = state.rightArmPose;
		this.head.yRot = state.yRot * Mth.DEG_TO_RAD;
		this.head.xRot = state.xRot * Mth.DEG_TO_RAD;
		this.hat.yRot = this.head.yRot;
		this.hat.xRot = this.head.xRot;

		this.rightArm.xRot = Mth.cos(state.walkAnimationPos * 0.6662F + Mth.PI) * 2.0F * state.walkAnimationSpeed * 0.5F;
		this.leftArm.xRot = Mth.cos(state.walkAnimationPos * 0.6662F) * 2.0F * state.walkAnimationSpeed * 0.5F;
		this.rightArm.zRot = 0.0F;
		this.leftArm.zRot = 0.0F;

		this.rightLeg.xRot = Mth.cos(state.walkAnimationPos * 0.6662F) * 1.4F * state.walkAnimationSpeed;
		this.leftLeg.xRot = Mth.cos(state.walkAnimationPos * 0.6662F + Mth.PI) * 1.4F * state.walkAnimationSpeed;
		this.rightLeg.yRot = 0.0F;
		this.leftLeg.yRot = 0.0F;

		this.rightArm.yRot = 0.0F;
		this.leftArm.yRot = 0.0F;
		boolean flag1 = state.mainArm == HumanoidArm.RIGHT;
		if (state.isUsingItem) {
			boolean flag2 = state.useItemHand == InteractionHand.MAIN_HAND;
			if (flag2 == flag1) {
				this.poseRightArm(state, rightPose);
			} else {
				this.poseLeftArm(state, leftPose);
			}
		} else {
			boolean flag3 = flag1 ? leftPose.isTwoHanded() : rightPose.isTwoHanded();
			if (flag1 != flag3) {
				this.poseLeftArm(state, leftPose);
				this.poseRightArm(state, rightPose);
			} else {
				this.poseRightArm(state, rightPose);
				this.poseLeftArm(state, leftPose);
			}
		}

		if (rightPose != HumanoidModel.ArmPose.SPYGLASS) {
			AnimationUtils.bobModelPart(this.rightArm, state.ageInTicks, 1.0F);
		}

		if (leftPose != HumanoidModel.ArmPose.SPYGLASS) {
			AnimationUtils.bobModelPart(this.leftArm, state.ageInTicks, -1.0F);
		}

		// copied from QuadrupedModel
		this.cowTorso.xRot = Mth.HALF_PI;
		this.leftFrontLeg.xRot = Mth.cos(state.walkAnimationPos * 0.6662F) * 1.4F * state.walkAnimationSpeed;
		this.rightFrontLeg.xRot = Mth.cos(state.walkAnimationPos * 0.6662F + Mth.PI) * 1.4F * state.walkAnimationSpeed;
		this.leftBackLeg.xRot = Mth.cos(state.walkAnimationPos * 0.6662F + Mth.PI) * 1.4F * state.walkAnimationSpeed;
		this.rightBackLeg.xRot = Mth.cos(state.walkAnimationPos * 0.6662F) * 1.4F * state.walkAnimationSpeed;

		// Ground slam animation
		float f1 = state.chargeAnim;
		f1 = f1 * f1;

		this.leftFrontLeg.y += -5.0F * f1;
		this.leftFrontLeg.z += f1;
		this.rightFrontLeg.y = this.leftFrontLeg.y;
		this.rightFrontLeg.z = this.leftFrontLeg.z;
		this.head.y += -3.0F * f1;
		this.head.z += 6.0F * f1;
		this.body.y += -3.0F * f1;
		this.body.z += 6.0F * f1;
		this.cowTorso.y += f1;
		this.cowTorso.z += 4.0F * f1;
		this.rightArm.y -= 3.0F * f1;
		this.rightArm.z += (6.0F * f1);
		this.leftArm.y = this.rightArm.y;
		this.leftArm.z = this.rightArm.z;

		if (f1 > 0) {
			if (state.mainArm == HumanoidArm.RIGHT) {
				this.rightArm.xRot = f1 * -1.8F;
				this.leftArm.xRot = 0.0F;
				this.rightArm.zRot = -0.2F;
			} else {
				this.rightArm.xRot = 0.0F;
				this.leftArm.xRot = f1 * -1.8F;
				this.leftArm.zRot = 0.2F;
			}
			this.cowTorso.xRot = Mth.HALF_PI - f1 * Mth.PI * 0.2F;
			this.leftFrontLeg.xRot -= f1 * Mth.PI * 0.3F;
			this.rightFrontLeg.xRot -= f1 * Mth.PI * 0.3F;
		}
	}

	@Override
	public void setupRotationsForTrophy(float x, float y, float z, float mouthAngle) {
		this.head.yRot = y * Mth.DEG_TO_RAD;
		this.head.xRot = z * Mth.DEG_TO_RAD;
	}

	private void poseRightArm(MinoshroomRenderState state, HumanoidModel.ArmPose pose) {
		switch (pose) {
			case EMPTY -> this.rightArm.yRot = 0.0F;
			case ITEM -> {
				this.rightArm.xRot = this.rightArm.xRot * 0.5F - 0.31415927F;
				this.rightArm.yRot = 0.0F;
			}
			case BLOCK -> this.poseBlockingArm(this.rightArm, true);
			case BOW_AND_ARROW -> {
				this.rightArm.yRot = -0.1F + this.head.yRot;
				this.leftArm.yRot = 0.1F + this.head.yRot + 0.4F;
				this.rightArm.xRot = -Mth.HALF_PI + this.head.xRot;
				this.leftArm.xRot = -Mth.HALF_PI + this.head.xRot;
			}
			case THROW_TRIDENT -> {
				this.rightArm.xRot = this.rightArm.xRot * 0.5F - Mth.PI;
				this.rightArm.yRot = 0.0F;
			}
			case CROSSBOW_CHARGE -> AnimationUtils.animateCrossbowCharge(this.rightArm, this.leftArm, state.maxCrossbowChargeDuration, state.ticksUsingItem, true);
			case CROSSBOW_HOLD -> AnimationUtils.animateCrossbowHold(this.rightArm, this.leftArm, this.head, true);
			case SPYGLASS -> {
				this.rightArm.xRot = Mth.clamp(this.head.xRot - 1.9198622F - (state.isCrouching ? 0.2617994F : 0.0F), -2.4F, 3.3F);
				this.rightArm.yRot = this.head.yRot - 0.2617994F;
			}
			case TOOT_HORN -> {
				this.rightArm.xRot = Mth.clamp(this.head.xRot, -1.2F, 1.2F) - 1.4835298F;
				this.rightArm.yRot = this.head.yRot - 0.5235988F;
			}
			case BRUSH -> {
				this.rightArm.xRot = this.rightArm.xRot * 0.5F - 0.62831855F;
				this.rightArm.yRot = 0.0F;
			}
			case SPEAR -> SpearAnimations.thirdPersonHandUse(this.rightArm, this.head, true, state.getUseItemStackForArm(HumanoidArm.RIGHT), state);
		}
	}

	private void poseLeftArm(MinoshroomRenderState state, HumanoidModel.ArmPose pose) {
		switch (pose) {
			case EMPTY -> this.leftArm.yRot = 0.0F;
			case ITEM -> {
				this.leftArm.xRot = this.leftArm.xRot * 0.5F - 0.31415927F;
				this.leftArm.yRot = 0.0F;
			}
			case BLOCK -> this.poseBlockingArm(this.leftArm, false);
			case BOW_AND_ARROW -> {
				this.rightArm.yRot = -0.1F + this.head.yRot - 0.4F;
				this.leftArm.yRot = 0.1F + this.head.yRot;
				this.rightArm.xRot = -Mth.HALF_PI + this.head.xRot;
				this.leftArm.xRot = -Mth.HALF_PI + this.head.xRot;
			}
			case THROW_TRIDENT -> {
				this.leftArm.xRot = this.leftArm.xRot * 0.5F - Mth.PI;
				this.leftArm.yRot = 0.0F;
			}
			case CROSSBOW_CHARGE -> AnimationUtils.animateCrossbowCharge(this.rightArm, this.leftArm, state.maxCrossbowChargeDuration, state.ticksUsingItem, false);
			case CROSSBOW_HOLD -> AnimationUtils.animateCrossbowHold(this.rightArm, this.leftArm, this.head, false);
			case SPYGLASS -> {
				this.leftArm.xRot = Mth.clamp(this.head.xRot - 1.9198622F - (state.isCrouching ? 0.2617994F : 0.0F), -2.4F, 3.3F);
				this.leftArm.yRot = this.head.yRot + 0.2617994F;
			}
			case TOOT_HORN -> {
				this.leftArm.xRot = Mth.clamp(this.head.xRot, -1.2F, 1.2F) - 1.4835298F;
				this.leftArm.yRot = this.head.yRot + 0.5235988F;
			}
			case BRUSH -> {
				this.leftArm.xRot = this.leftArm.xRot * 0.5F - 0.62831855F;
				this.leftArm.yRot = 0.0F;
			}
			case SPEAR -> SpearAnimations.thirdPersonHandUse(this.leftArm, this.head, false, state.getUseItemStackForArm(HumanoidArm.LEFT), state);
		}
	}

	private void poseBlockingArm(ModelPart arm, boolean right) {
		arm.xRot = arm.xRot * 0.5F - 0.9424779F + Mth.clamp(this.head.xRot, -1.3962634F, 0.43633232F);
		arm.yRot = (right ? -30.0F : 30.0F) * Mth.DEG_TO_RAD + Mth.clamp(this.head.yRot, -0.5235988F, 0.5235988F);
	}

	@Override
	public void submitTrophy(PoseStack stack, SubmitNodeCollector nodeCollector, int light, int overlay, int color, ItemDisplayContext context, @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
		stack.translate(0.0F, 0.375F, 0.56F);
		nodeCollector.submitModelPart(this.head, stack, RenderTypes.entityCutoutNoCull(MinoshroomRenderer.TEXTURE), light, overlay, null, false, false, color, breakProgress, 0);
	}
}
