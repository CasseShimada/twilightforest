package twilightforest.item.travellers_gear;

import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import twilightforest.TwilightForestMod;
import twilightforest.components.entity.SlimySolesAttachment;
import twilightforest.components.entity.TravellersWingsAttachment;
import twilightforest.init.TFAttributeModifiers;
import twilightforest.init.TFDataAttachments;
import twilightforest.init.TFDataComponents;
import twilightforest.init.TFDimensionData;
import twilightforest.init.TFItems;
import twilightforest.init.TFParticleType;
import twilightforest.init.TFSounds;
import twilightforest.init.custom.TravellersModifiersManager;
import twilightforest.network.ParticlePacket;
import twilightforest.network.TFNetworking;
import twilightforest.network.TravellersWingsStatePacket;
import twilightforest.util.TFMathUtil;

import java.util.Collections;
import java.util.function.Consumer;

public final class TravellersGearLogic {
	public static final double WATER_WALKING_MAX_SUBMERGED_HEIGHT = 0.4;
	private static final double AUTO_REPAIR_SUNLIGHT_BOOST = 3;
	private static final double AUTO_REPAIR_TWILIGHT_BOOST = AUTO_REPAIR_SUNLIGHT_BOOST / 2;

	private TravellersGearLogic() {
	}

	public static void travellersStealth(Player player, Consumer<Player> invisibilityHandler) {
		if (!TravellersModifiersManager.isModifierActive(player, TravellersModifiersManager.STEALTH_MODIFIER)) {
			return;
		}

		if (player.isCrouching()) {
			invisibilityHandler.accept(player);
		} else {
			MobEffectInstance invisibilityEffect = player.getEffect(MobEffects.INVISIBILITY);
			if (invisibilityEffect != null && invisibilityEffect.getDuration() < 2) {
				player.setInvisible(false);
			}
		}
	}

	public static void waterWalkingSplashEffect(LivingEntity livingEntity) {
		long lastTickWaterWalking = TFDataAttachments.get(livingEntity, TFDataAttachments.LAST_TICK_WATER_WALKING);
		Level level = livingEntity.level();
		Vec3 velocity = livingEntity.getKnownMovement();
		if (lastTickWaterWalking + 1 == level.getGameTime() || velocity.horizontalDistance() < 0.01) {
			return;
		}

		TFDataAttachments.set(livingEntity, TFDataAttachments.LAST_TICK_WATER_WALKING, level.getGameTime());
		ParticlePacket packet = new ParticlePacket();
		for (int particleNumber = 0; particleNumber < livingEntity.getBbWidth(); particleNumber++) {
			double dx = (level.getRandom().nextDouble() * 2.0 - 1.0) * livingEntity.getBbWidth() / 2.0;
			double dz = (level.getRandom().nextDouble() * 2.0 - 1.0) * livingEntity.getBbWidth() / 2.0;
			Vec3 position = new Vec3(livingEntity.getX() + dx, livingEntity.getY() + WATER_WALKING_MAX_SUBMERGED_HEIGHT, livingEntity.getZ() + dz);
			Vec3 particleVelocity = new Vec3(-velocity.x, 0.5, -velocity.z);
			if (level.isClientSide()) {
				level.addParticle(ParticleTypes.SPLASH, position.x(), position.y(), position.z(), particleVelocity.x(), particleVelocity.y(), particleVelocity.z());
			} else {
				packet.queueParticle(ParticleTypes.SPLASH, position, particleVelocity);
			}
		}

		if (!level.isClientSide()) {
			TFNetworking.sendToTracking(livingEntity, packet);
		}
	}

	public static boolean isBelowMaxWaterWalkingSubmergedHeight(LivingEntity livingEntity) {
		return livingEntity.getFluidHeight(FluidTags.WATER) < WATER_WALKING_MAX_SUBMERGED_HEIGHT;
	}

	public static boolean canStandOnWater(LivingEntity livingEntity, FluidState fluidState, boolean original) {
		if (original || !fluidState.is(FluidTags.WATER)
			|| !TravellersModifiersManager.isModifierActive(livingEntity, TravellersModifiersManager.WATER_WALK_MODIFIER)) {
			return original;
		}

		boolean waterWalking = isBelowMaxWaterWalkingSubmergedHeight(livingEntity) && !livingEntity.isShiftKeyDown();
		if (waterWalking && livingEntity.getFluidHeight(FluidTags.WATER) > 0.0D && livingEntity.level().getGameTime() % 3 == 1) {
			waterWalkingSplashEffect(livingEntity);
		}
		return waterWalking;
	}

	public static float modifyMovementExhaustion(Player player, float exhaustion) {
		ItemStack vest = player.getItemBySlot(EquipmentSlot.CHEST);
		Float divisor = vest.get(TFDataComponents.EFFICIENT_EATER);
		if (divisor == null || !TravellersModifiersManager.isModifierActive(player, vest, TravellersModifiersManager.EFFICIENT_EATER_MODIFIER)) {
			return exhaustion;
		}
		return exhaustion / divisor;
	}

	public static void travellersBootsStraightAhead(LivingEntity livingEntity) {
		ItemStack boots = livingEntity.getItemBySlot(EquipmentSlot.FEET);
		Double multiplier = boots.get(TFDataComponents.STRAIGHT_AHEAD_MULTIPLIER);
		AttributeInstance attribute = livingEntity.getAttributes().getInstance(Attributes.MOVEMENT_SPEED);
		if (attribute == null) {
			return;
		}
		if (multiplier == null) {
			multiplier = 1.0D;
		}
		boolean active = TravellersModifiersManager.isModifierActive(livingEntity, boots, TravellersModifiersManager.STRAIGHT_AHEAD_MODIFIER) && multiplier != 1.0D;
		if (active == attribute.hasModifier(TFAttributeModifiers.STRAIGHT_AHEAD_ATTRIBUTE_MODIFIER_LOCATION)) {
			return;
		}
		if (active) {
			attribute.addOrUpdateTransientModifier(new AttributeModifier(TFAttributeModifiers.STRAIGHT_AHEAD_ATTRIBUTE_MODIFIER_LOCATION, multiplier - 1.0D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
		} else {
			attribute.removeModifier(TFAttributeModifiers.STRAIGHT_AHEAD_ATTRIBUTE_MODIFIER_LOCATION);
		}
	}

	public static void travellersWingsSidestepCooldownSound(Player player) {
		ItemStack wings = player.getItemBySlot(EquipmentSlot.LEGS);
		Long cooldown = wings.get(TFDataComponents.SIDESTEP_COOLDOWN);
		if (cooldown == null) {
			return;
		}
		TravellersWingsAttachment attachment = TFDataAttachments.get(player, TFDataAttachments.TRAVELLERS_WINGS);
		long elapsed = player.level().getGameTime() - attachment.lastSidestepTime;
		if (TravellersModifiersManager.isModifierActive(player, wings, TravellersModifiersManager.SIDESTEP_MODIFIER)
			&& elapsed > cooldown && attachment.shouldPlaySideStepCooldownSound) {
			player.level().playLocalSound(player.blockPosition(), TFSounds.SIDE_STEP_CHARGED, player.getSoundSource(), 1.0F, player.getVoicePitch(), false);
			attachment.shouldPlaySideStepCooldownSound = false;
		}
	}

	public static void travellersWingsGradualGlide(LivingEntity livingEntity) {
		ItemStack wings = livingEntity.getItemBySlot(EquipmentSlot.LEGS);
		Float multiplier = wings.get(TFDataComponents.GRADUALLY_GLIDING_MULTIPLIER);
		Vec3 movement = livingEntity.getDeltaMovement();
		if (!TravellersModifiersManager.isModifierActive(livingEntity, wings, TravellersModifiersManager.GRADUAL_GLIDE_MODIFIER)
			|| multiplier == null || movement.y() >= 0 || livingEntity.isFallFlying()) {
			return;
		}

		boolean gliding = !(livingEntity instanceof Player player)
			|| TFDataAttachments.get(player, TFDataAttachments.IS_GRADUALLY_GLIDING);
		if (!gliding) {
			return;
		}

		double newY = movement.y() * multiplier;
		livingEntity.setDeltaMovement(movement.x(), newY, movement.z());
		livingEntity.fallDistance = (float) (Math.pow(newY, 2) / 2 / livingEntity.getGravity());
	}

	public static void travellersGearAutoRepair(LivingEntity livingEntity) {
		long lastHitTime = TFDataAttachments.get(livingEntity, TFDataAttachments.LAST_DAMAGE_ARMOR_TIME);
		if (livingEntity.level().getGameTime() - lastHitTime <= 10 * 20) {
			return;
		}

		for (EquipmentSlot slot : EquipmentSlotGroup.ARMOR.slots()) {
			ItemStack stack = livingEntity.getItemBySlot(slot);
			Float probability = stack.get(TFDataComponents.AUTO_REPAIR_PROBABILITY);
			if (probability == null || !TravellersModifiersManager.isModifierActive(livingEntity, stack, TravellersModifiersManager.AUTO_REPAIR_MODIFIER)) {
				return;
			}
			Level level = livingEntity.level();
			double boostedProbability = getAutoRepairChance(probability, level, livingEntity.blockPosition());
			if (boostedProbability > level.getRandom().nextFloat()) {
				stack.setDamageValue(Math.max(stack.getDamageValue() - 1, 0));
			}
		}
	}

	private static double getAutoRepairChance(double baseProbability, Level level, BlockPos pos) {
		if (!level.canSeeSky(pos)) {
			return baseProbability;
		}

		double boostFactor;
		if (level.dimensionTypeRegistration().is(TFDimensionData.TWILIGHT_DIM_TYPE)) {
			boostFactor = AUTO_REPAIR_TWILIGHT_BOOST;
		} else if (level.isBrightOutside()) {
			boostFactor = AUTO_REPAIR_SUNLIGHT_BOOST;
		} else {
			return baseProbability;
		}
		return TFMathUtil.probabilityOfAtLeastOneSuccess(baseProbability, boostFactor);
	}

	public static void travellersWingsHighJump(LivingEntity livingEntity) {
		ItemStack wings = livingEntity.getItemBySlot(EquipmentSlot.LEGS);
		Integer amplifier = wings.get(TFDataComponents.HIGH_JUMP_AMPLIFIER);
		if (TravellersModifiersManager.isModifierActive(livingEntity, wings, TravellersModifiersManager.HIGH_JUMP_ABILITY) && amplifier != null) {
			livingEntity.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, 2, amplifier, false, false, false));
		}
	}

	public static void travellersVestHaste(LivingEntity livingEntity) {
		ItemStack vest = livingEntity.getItemBySlot(EquipmentSlot.CHEST);
		Integer amplifier = vest.get(TFDataComponents.HASTE_AMPLIFIER);
		if (TravellersModifiersManager.isModifierActive(livingEntity, vest, TravellersModifiersManager.HASTE_MODIFIER) && amplifier != null) {
			livingEntity.addEffect(new MobEffectInstance(MobEffects.HASTE, 2, amplifier, false, false, false));
		}
	}

	public static void travellersBootsUnrestrained(LivingEntity livingEntity) {
		if (TravellersModifiersManager.isModifierActive(livingEntity, TravellersModifiersManager.UNRESTRAINED_MODIFIER)) {
			livingEntity.stuckSpeedMultiplier = Vec3.ZERO;
		}
	}

	public static boolean tryPerformSidestep(Player player, boolean isLeftSidestep) {
		TravellersWingsAttachment attachment = TFDataAttachments.get(player, TFDataAttachments.TRAVELLERS_WINGS);
		ItemStack wings = player.getItemBySlot(EquipmentSlot.LEGS);
		Long cooldown = wings.get(TFDataComponents.SIDESTEP_COOLDOWN);
		long currentTime = player.level().getGameTime();
		if (TravellersModifiersManager.isModifierActive(player, wings, TravellersModifiersManager.SIDESTEP_MODIFIER)
			&& cooldown != null && currentTime - attachment.lastSidestepTime > cooldown
			&& !player.isFallFlying() && player.onGround() && !player.isCrouching()) {
			performSidestep(player, isLeftSidestep);
			attachment.lastSidestepTime = currentTime;
			attachment.shouldPlaySideStepCooldownSound = true;
			return true;
		}
		return false;
	}

	public static void performSidestep(Player player, boolean isLeftSidestep) {
		float angle = player.getYRot();
		double rotation = isLeftSidestep ? -Math.PI / 2 : Math.PI / 2;
		Vec3 direction = new Vec3(-Math.sin(Math.toRadians(angle) + rotation), 0, Math.cos(Math.toRadians(angle) + rotation));
		player.push(direction.scale(1.6));
		player.playSound(TFSounds.SIDE_STEP, 1.0F, player.getVoicePitch());

		TravellersWingsAttachment attachment = TFDataAttachments.get(player, TFDataAttachments.TRAVELLERS_WINGS);
		attachment.state = TravellersWingsAttachment.WingState.SIDESTEP;
		attachment.sidestepLeft = isLeftSidestep;
		attachment.sidestepTimer = 0;
		if (player.level() instanceof ServerLevel) {
			TFNetworking.sendToTrackingAndSelf(player, new TravellersWingsStatePacket(player.getId(), attachment.state, isLeftSidestep, attachment.doubleJumpTimer, attachment.sidestepTimer));
		}
	}

	public static boolean performDoubleJump(Player player) {
		boolean hasDoubleJump = TFDataAttachments.get(player, TFDataAttachments.HAS_DOUBLE_JUMP);
		if (!hasDoubleJump || player.isFallFlying() || player.onClimbable() || player.onGround() || player.isSwimming()
			|| player.getAbilities().flying || player.isInLiquid() || player.isPassenger()) {
			return false;
		}
		player.jumpFromGround();
		Vec3 velocity = player.getDeltaMovement();
		SlimySolesAttachment slimySoles = TFDataAttachments.get(player, TFDataAttachments.SLIMY_SOLES_BOUNCE_INFO);
		double boostVelocity = slimySoles.doubleJumpBoostVelocity;
		if (boostVelocity != 0) {
			player.setDeltaMovement(velocity.x(), Math.sqrt(Math.pow(velocity.y(), 2) + Math.pow(boostVelocity, 2)), velocity.z());
			slimySoles.doubleJumpBoostVelocity = 0;
		}
		player.resetFallDistance();
		float pitchShift = 0.1F;
		player.playSound(TFSounds.DOUBLE_JUMP, 1.5F, (player.getVoicePitch() - 1) * (1 + pitchShift) + (1 - pitchShift * 0.2F));
		TFDataAttachments.set(player, TFDataAttachments.HAS_DOUBLE_JUMP, false);
		TFDataAttachments.set(player, TFDataAttachments.DOUBLE_JUMP_VALIDATOR, 0);
		AttributeInstance safeFall = player.getAttribute(Attributes.SAFE_FALL_DISTANCE);
		if (safeFall != null) {
			safeFall.addOrUpdateTransientModifier(TFAttributeModifiers.TRAVELLERS_DOUBLE_JUMP_SAFE_FALL_DISTANCE);
		}

		boolean wearingWings = player.getItemBySlot(EquipmentSlot.LEGS).is(TFItems.TRAVELLERS_WINGS);
		TravellersWingsAttachment wingsAttachment = TFDataAttachments.get(player, TFDataAttachments.TRAVELLERS_WINGS);
		if (wearingWings) {
			wingsAttachment.state = TravellersWingsAttachment.WingState.DOUBLE_JUMP;
			wingsAttachment.doubleJumpTimer = 0;
		}

		if (wearingWings && player.level() instanceof ServerLevel serverLevel) {
			ParticlePacket particlePacket = new ParticlePacket();
			Vec3 deltaMovement = player.getDeltaMovement();
			for (int particleNumber = 0; particleNumber < 10; particleNumber++) {
				Vec3 particleVelocity = new Vec3(serverLevel.getRandom().nextDouble() - 0.5, serverLevel.getRandom().nextDouble() + 1, serverLevel.getRandom().nextDouble() - 0.5);
				ParticleOptions type = TFParticleType.DOUBLE_JUMP;
				Vec3 wingsPosition = player.position().add(Math.sin(Math.toRadians(player.yBodyRot)) / 3, 1.2, -Math.cos(Math.toRadians(player.yBodyRot)) / 3);
				particlePacket.queueParticle(type, wingsPosition, particleVelocity.multiply(0.25, -0.5, 0.25).add(deltaMovement));
			}
			TFNetworking.sendToTrackingAndSelf(player, particlePacket);
			TFNetworking.sendToTrackingAndSelf(player, new TravellersWingsStatePacket(player.getId(), wingsAttachment.state, wingsAttachment.sidestepLeft, wingsAttachment.doubleJumpTimer, wingsAttachment.sidestepTimer));
		}
		return true;
	}

	public static void travellersBootsSlimySolesBounce(LivingEntity livingEntity) {
		SlimySolesAttachment attachment = TFDataAttachments.get(livingEntity, TFDataAttachments.SLIMY_SOLES_BOUNCE_INFO);
		if (attachment.bounceVelocity == 0 || attachment.hasBounced) {
			return;
		}
		Vec3 velocity = livingEntity.getDeltaMovement();
		livingEntity.playSound(SoundEvents.SLIME_JUMP, 0.5F, 1.0F);
		travellersBootsSlimySolesParticles(livingEntity, attachment);
		attachment.hasBounced = true;
		livingEntity.setDeltaMovement(velocity.x(), Math.sqrt(Math.pow(velocity.y(), 2) + Math.pow(attachment.bounceVelocity, 2)), velocity.z());
		attachment.forceBounce = Math.abs(livingEntity.getDeltaMovement().y()) > 0.25;
	}

	public static void travellersBootsSlimySolesParticles(LivingEntity entity, SlimySolesAttachment attachment) {
		if (!(entity.level() instanceof ServerLevel level) || attachment.bounceVelocity <= 0) {
			return;
		}
		double intensity = Math.min(0.2 + attachment.bounceVelocity, 2.5);
		int count = (int) (40 * intensity);
		if (count > 0) {
			level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SLIME_BLOCK.defaultBlockState()), entity.getX(), entity.getY(), entity.getZ(), count, 0.0, 0.0, 0.0, 0.15F);
		}
	}

	private static void validateMovement(ServerPlayer player, AttachmentType<Integer> validator, AttachmentType<Integer> lastCheck, String movementType) {
		MinecraftServer server = player.level().getServer();
		if (server == null || !server.isDedicatedServer()) {
			return;
		}
		int count = TFDataAttachments.get(player, validator);
		int lastTick = TFDataAttachments.get(player, lastCheck);
		int currentTick = player.tickCount;
		int elapsed = currentTick - lastTick;
		TwilightForestMod.LOGGER.debug("{} {} check: count={}, lastTick={}, currentTick={}, diff={}", player.getName().getString(), movementType, count, lastTick, currentTick, elapsed);
		if (elapsed >= 45 && !player.isFallFlying()) {
			count = -1;
		}
		TFDataAttachments.set(player, lastCheck, currentTick);
		if (count >= 5) {
			player.connection.disconnect(new DisconnectionDetails(Component.translatable("multiplayer.disconnect.flying")));
			return;
		}
		TFDataAttachments.set(player, validator, count + 1);
		if (count > 1) {
			TwilightForestMod.LOGGER.warn("{} illegal {}", player.getName().getString(), movementType);
			player.absSnapTo(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());
			player.connection.send(ClientboundPlayerPositionPacket.of(player.getId(), new PositionMoveRotation(player.position(), Vec3.ZERO, player.getYRot(), player.getXRot()), Collections.emptySet()));
		}
	}

	public static void handleSidestepAbuse(Player player) {
		if (player instanceof ServerPlayer serverPlayer) {
			validateMovement(serverPlayer, TFDataAttachments.SIDESTEP_VALIDATOR, TFDataAttachments.SIDESTEP_VALIDATOR_LAST_CHECK, "sidestep");
		}
	}

	public static void handleDoubleJumpAbuse(Player player) {
		if (player instanceof ServerPlayer serverPlayer) {
			validateMovement(serverPlayer, TFDataAttachments.DOUBLE_JUMP_VALIDATOR, TFDataAttachments.DOUBLE_JUMP_VALIDATOR_LAST_CHECK, "double jump");
		}
	}

	public static void determineWingState(LivingEntity livingEntity) {
		TravellersWingsAttachment attachment = TFDataAttachments.get(livingEntity, TFDataAttachments.TRAVELLERS_WINGS);
		TravellersWingsAttachment.WingState newState = TravellersWingsAttachment.WingState.IDLE;
		boolean locked = false;
		if (attachment.state == TravellersWingsAttachment.WingState.DOUBLE_JUMP) {
			attachment.doubleJumpTimer++;
			if (attachment.doubleJumpTimer < TravellersWingsAttachment.DOUBLE_JUMP_DURATION) {
				locked = true;
				newState = TravellersWingsAttachment.WingState.DOUBLE_JUMP;
			}
		} else if (attachment.state == TravellersWingsAttachment.WingState.SIDESTEP) {
			attachment.sidestepTimer++;
			if (attachment.sidestepTimer < TravellersWingsAttachment.SIDESTEP_DURATION) {
				locked = true;
				newState = attachment.state;
			}
		} else {
			attachment.doubleJumpTimer = 0;
			attachment.sidestepTimer = 0;
		}

		if (!locked) {
			if (livingEntity.isPassenger()) {
				newState = TravellersWingsAttachment.WingState.RIDE;
			} else if (livingEntity.isSwimming()) {
				newState = TravellersWingsAttachment.WingState.SWIM;
			} else if (!livingEntity.onGround() && !livingEntity.isInLiquid() && livingEntity.fallDistance < 2.3F
				&& (!(livingEntity instanceof Player player) || !player.getAbilities().flying)) {
				newState = TravellersWingsAttachment.WingState.FALL_SLOW;
			} else if (livingEntity.getDeltaMovement().y < 0 && livingEntity.fallDistance > 2.3F) {
				newState = TravellersWingsAttachment.WingState.FALL_FAST;
			} else if (livingEntity.isSprinting()) {
				newState = TravellersWingsAttachment.WingState.SPRINT;
			} else if (livingEntity.walkAnimation.speed() > 0.1) {
				newState = TravellersWingsAttachment.WingState.WALK;
			}
		}

		if (newState != attachment.state) {
			attachment.state = newState;
			if (livingEntity.level() instanceof ServerLevel) {
				TFNetworking.sendToTrackingAndSelf(livingEntity, new TravellersWingsStatePacket(livingEntity.getId(), newState, attachment.sidestepLeft, attachment.doubleJumpTimer, attachment.sidestepTimer));
			}
		}
	}
}
