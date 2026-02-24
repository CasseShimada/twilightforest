package twilightforest.client.model.entity;

import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.effects.SpearAnimations;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;

//fixes probably my biggest issue with HumanoidModel: the hardcoded positions of limbs.
//this is most noticeable in the body (which is offset by 24 voxels), but it also fixes mobs that have smaller or bigger arms.
//this also cleans up some movement logic that doesnt apply to mobs (swimming and elytra flying)
public class FixedHumanoidModel<T extends HumanoidRenderState> extends HumanoidModel<T> {

	private final float armWidth;

	public FixedHumanoidModel(ModelPart part, float armWidth) {
		super(part);
		this.armWidth = armWidth;
	}

	@Override
	public void setupAnim(T state) {
		super.setupAnim(state);
		HumanoidModel.ArmPose leftPose = state.leftArmPose;
		HumanoidModel.ArmPose rightPose = state.rightArmPose;
		this.head.xRot = state.xRot * Mth.DEG_TO_RAD;
		this.head.yRot = state.yRot * Mth.DEG_TO_RAD;

		float f1 = state.walkAnimationPos;
		float f2 = state.walkAnimationSpeed;
		this.rightArm.xRot = Mth.cos(f1 * 0.6662F + Mth.PI) * 2.0F * f2 * 0.5F / state.speedValue;
		this.leftArm.xRot = Mth.cos(f1 * 0.6662F) * 2.0F * f2 * 0.5F / state.speedValue;
		this.rightLeg.xRot = Mth.cos(f1 * 0.6662F) * 1.4F * f2 / state.speedValue;
		this.leftLeg.xRot = Mth.cos(f1 * 0.6662F + Mth.PI) * 1.4F * f2 / state.speedValue;
		this.rightLeg.yRot = 0.005F;
		this.leftLeg.yRot = -0.005F;
		this.rightLeg.zRot = 0.005F;
		this.leftLeg.zRot = -0.005F;
		if (state.isPassenger) {
			this.rightArm.xRot += -Mth.PI / 5;
			this.leftArm.xRot += -Mth.PI / 5;
			this.rightLeg.xRot = -1.4137167F;
			this.rightLeg.yRot = Mth.PI / 10;
			this.rightLeg.zRot = 0.07853982F;
			this.leftLeg.xRot = -1.4137167F;
			this.leftLeg.yRot = -Mth.PI / 10;
			this.leftLeg.zRot = -0.07853982F;
		}

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

		this.setupAttackAnimation(state);

		if (rightPose != HumanoidModel.ArmPose.SPYGLASS) {
			AnimationUtils.bobModelPart(this.rightArm, state.ageInTicks, 1.0F);
		}

		if (leftPose != HumanoidModel.ArmPose.SPYGLASS) {
			AnimationUtils.bobModelPart(this.leftArm, state.ageInTicks, -1.0F);
		}
	}

	@Override
	protected void setupAttackAnimation(T state) {
		float f = state.attackTime;
		if (!(f <= 0.0F)) {
			HumanoidArm humanoidarm = state.attackArm;
			ModelPart modelpart = this.getArm(humanoidarm);
			this.body.yRot = Mth.sin(Mth.sqrt(f) * Mth.TWO_PI) * 0.2F;
			if (humanoidarm == HumanoidArm.LEFT) {
				this.body.yRot *= -1.0F;
			}

			float f2 = state.ageScale;
			this.rightArm.z = Mth.sin(this.body.yRot) * (this.armWidth + 1.0F) * f2;
			this.rightArm.x = -Mth.cos(this.body.yRot) * (this.armWidth + 1.0F) * f2;
			this.leftArm.z = -Mth.sin(this.body.yRot) * (this.armWidth + 1.0F) * f2;
			this.leftArm.x = Mth.cos(this.body.yRot) * (this.armWidth + 1.0F) * f2;
			this.rightArm.yRot = this.rightArm.yRot + this.body.yRot;
			this.leftArm.yRot = this.leftArm.yRot + this.body.yRot;
			this.leftArm.xRot = this.leftArm.xRot + this.body.yRot;
			float $$5 = 1.0F - f;
			$$5 *= $$5;
			$$5 *= $$5;
			$$5 = 1.0F - $$5;
			float f3 = Mth.sin($$5 * Mth.PI);
			float f4 = Mth.sin(f * Mth.PI) * -(this.head.xRot - 0.7F) * 0.75F;
			modelpart.xRot -= f3 * 1.2F + f4;
			modelpart.yRot = modelpart.yRot + this.body.yRot * 2.0F;
			modelpart.zRot = modelpart.zRot + Mth.sin(f * Mth.PI) * -0.4F;
		}
	}

	private float quadraticArmUpdate(float limbSwing) {
		return -65.0F * limbSwing + limbSwing * limbSwing;
	}

	private void poseRightArm(T state, HumanoidModel.ArmPose pose) {
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

	private void poseLeftArm(T state, HumanoidModel.ArmPose pose) {
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
}
