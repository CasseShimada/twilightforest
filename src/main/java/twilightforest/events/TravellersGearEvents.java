package twilightforest.events;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import twilightforest.components.entity.SlimySolesAttachment;
import twilightforest.components.item.ItemDisplayContents;
import twilightforest.init.TFAttributeModifiers;
import twilightforest.init.TFDataAttachments;
import twilightforest.init.TFDataComponents;
import twilightforest.init.TFParticleType;
import twilightforest.init.TFSounds;
import twilightforest.init.custom.TravellersModifiersManager;
import twilightforest.inventory.InventoryUtil;
import twilightforest.item.travellers_gear.TravellersGearLogic;
import twilightforest.item.travellers_gear.modifiers.InsertableTravellersModifier;
import twilightforest.item.travellers_gear.modifiers.TravellersModifier;
import twilightforest.network.GradualGlidePacket;
import twilightforest.network.ParticlePacket;
import twilightforest.network.TFNetworking;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public final class TravellersGearEvents {
	private TravellersGearEvents() {
	}

	public static void onProjectileHitBlock(Projectile projectile, BlockHitResult hit) {
		Entity owner = projectile.getOwner();
		if (!(owner instanceof LivingEntity living) || projectile.tickCount >= 200
			|| !TravellersModifiersManager.isModifierActive(living, TravellersModifiersManager.ARROW_MAGNETISM_MODIFIER)
			|| !(projectile instanceof AbstractArrow arrow) || projectile.level().isClientSide()) {
			return;
		}

		if (!(living instanceof Player player)) {
			projectile.discard();
			return;
		}
		AbstractArrow.Pickup pickup = arrow.pickup;
		if (!player.hasInfiniteMaterials() && pickup == AbstractArrow.Pickup.ALLOWED) {
			InventoryUtil.giveItemToPlayer(player, arrow.getPickupItemStackOrigin());
			player.getInventory().setChanged();
		}
		if (pickup == AbstractArrow.Pickup.ALLOWED || pickup == AbstractArrow.Pickup.CREATIVE_ONLY && player.isCreative()) {
			projectile.discard();
		}
	}

	public static boolean onProjectileHitEntity(Projectile projectile, EntityHitResult hit) {
		if (!(hit.getEntity() instanceof LivingEntity living)) {
			return false;
		}
		ItemStack chest = living.getItemBySlot(EquipmentSlot.CHEST);
		Float probability = chest.get(TFDataComponents.PERFECT_DODGE_PROBABILITY);
		Level level = living.level();
		if (probability == null
			|| !TravellersModifiersManager.isModifierActive(living, chest, TravellersModifiersManager.PERFECT_DODGE_MODIFIER)) {
			return false;
		}
		if (level.isClientSide()) {
			return true;
		}
		if (probability <= level.getRandom().nextFloat()) {
			return false;
		}

		Vec3 hitPosition = projectile.position().add(projectile.getDeltaMovement());
		level.playSound(null, hitPosition.x(), hitPosition.y(), hitPosition.z(), TFSounds.PERFECT_DODGE, living.getSoundSource(), 1.5F, living.getVoicePitch());
		ParticlePacket particlePacket = new ParticlePacket();
		for (int particleNumber = 0; particleNumber < 20; particleNumber++) {
			Vec3 particleVelocity = new Vec3(level.getRandom().nextDouble() - 0.5, level.getRandom().nextDouble() - 0.5, level.getRandom().nextDouble() - 0.5);
			ParticleOptions type = TFParticleType.PERFECT_DODGE;
			particlePacket.queueParticle(type, hitPosition, particleVelocity);
		}
		TFNetworking.sendToTrackingAndSelf(living, particlePacket);
		return true;
	}

	public static boolean onFall(LivingEntity living, double fallDistance, float damageMultiplier, DamageSource source) {
		ItemStack boots = living.getItemBySlot(EquipmentSlot.FEET);
		Float coefficient = boots.get(TFDataComponents.SLIMY_SOLES_COEFFICIENT);
		SlimySolesAttachment attachment = TFDataAttachments.get(living, TFDataAttachments.SLIMY_SOLES_BOUNCE_INFO);
		double safeFallDistance = living.getAttributeValue(Attributes.SAFE_FALL_DISTANCE);
		double unsafeFallDistance = fallDistance - safeFallDistance;
		int calculatedDamage = Mth.ceil(unsafeFallDistance * damageMultiplier * living.getAttributeValue(Attributes.FALL_DAMAGE_MULTIPLIER));
		if (living.isShiftKeyDown() || coefficient == null || calculatedDamage <= 0 && !attachment.forceBounce
			|| !TravellersModifiersManager.isModifierActive(living, boots, TravellersModifiersManager.SLIMY_SOLES_MODIFIER)) {
			return false;
		}

		attachment.bounceVelocity = -living.getDeltaMovement().y() * Math.sqrt(coefficient);
		attachment.doubleJumpBoostVelocity = attachment.bounceVelocity;
		attachment.hasBounced = false;
		TFDataAttachments.set(living, TFDataAttachments.SLIMY_SOLES_BOUNCE_INFO, attachment);
		return true;
	}

	public static void onLivingJump(LivingEntity living) {
		SlimySolesAttachment attachment = TFDataAttachments.get(living, TFDataAttachments.SLIMY_SOLES_BOUNCE_INFO);
		attachment.bounceVelocity = 0;
		attachment.forceBounce = false;
		TFDataAttachments.set(living, TFDataAttachments.SLIMY_SOLES_BOUNCE_INFO, attachment);
	}

	public static void onPlayerTickStart(Player player) {
		Boolean hasDoubleJump = null;
		if (!TravellersModifiersManager.isModifierActive(player, TravellersModifiersManager.DOUBLE_JUMP_MODIFIER)) {
			hasDoubleJump = false;
		} else if (player.onGround() || player.isInLiquid() || player.onClimbable()) {
			hasDoubleJump = true;
		}
		if (hasDoubleJump != null && hasDoubleJump != TFDataAttachments.get(player, TFDataAttachments.HAS_DOUBLE_JUMP)) {
			TFDataAttachments.set(player, TFDataAttachments.HAS_DOUBLE_JUMP, hasDoubleJump);
			TFDataAttachments.set(player, TFDataAttachments.DOUBLE_JUMP_VALIDATOR, 0);
			AttributeInstance safeFall = player.getAttribute(Attributes.SAFE_FALL_DISTANCE);
			if (safeFall != null) {
				safeFall.removeModifier(TFAttributeModifiers.TRAVELLERS_DOUBLE_JUMP_SAFE_FALL_DISTANCE);
			}
		}

		if (!player.level().isClientSide()
			&& !TravellersModifiersManager.isModifierActive(player, TravellersModifiersManager.GRADUAL_GLIDE_MODIFIER)
			&& TFDataAttachments.get(player, TFDataAttachments.IS_GRADUALLY_GLIDING)) {
			TFDataAttachments.set(player, TFDataAttachments.IS_GRADUALLY_GLIDING, false);
			TFNetworking.sendToTrackingAndSelf(player, new GradualGlidePacket(false, player.getUUID()));
		}
		if (player.level().isClientSide()
			&& TFDataAttachments.get(player, TFDataAttachments.TRAVELLERS_WINGS_ANIM).doubleJump
			&& player.onGround()) {
			TFDataAttachments.get(player, TFDataAttachments.TRAVELLERS_WINGS_ANIM).doubleJump = false;
		}

		updateHighStep(player);
		TravellersGearLogic.travellersWingsSidestepCooldownSound(player);
	}

	private static void updateHighStep(Player player) {
		if (!TravellersModifiersManager.isModifierActive(player, TravellersModifiersManager.STEP_UP_ABILITY)) {
			return;
		}
		AttributeInstance stepHeight = player.getAttributes().getInstance(Attributes.STEP_HEIGHT);
		if (stepHeight == null) {
			return;
		}
		boolean shouldHaveModifier = !player.isCrouching();
		boolean hasModifier = stepHeight.hasModifier(TFAttributeModifiers.TRAVELLERS_HIGH_STEP.id());
		if (!shouldHaveModifier && hasModifier) {
			stepHeight.removeModifier(TFAttributeModifiers.TRAVELLERS_HIGH_STEP);
		} else if (shouldHaveModifier && !hasModifier) {
			stepHeight.addPermanentModifier(TFAttributeModifiers.TRAVELLERS_HIGH_STEP);
		}
	}

	public static void onPlayerTickEnd(Player player) {
		if (!player.level().isClientSide()) {
			TravellersGearLogic.travellersStealth(player, target -> target.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 2, 0, false, false, false)));
		}
	}

	public static void onEntityTick(Entity entity) {
		if (!(entity instanceof LivingEntity living)) {
			return;
		}
		TravellersGearLogic.travellersWingsGradualGlide(living);
		TravellersGearLogic.travellersBootsUnrestrained(living);
		TravellersGearLogic.travellersBootsSlimySolesBounce(living);
		if (living.level().isClientSide()) {
			return;
		}
		TravellersGearLogic.travellersVestHaste(living);
		TravellersGearLogic.travellersWingsHighJump(living);
		TravellersGearLogic.travellersGearAutoRepair(living);
		TravellersGearLogic.travellersBootsStraightAhead(living);
		TravellersGearLogic.determineWingState(living);
	}

	public static void damageArmor(ItemStack stack, int amount, LivingEntity entity, EquipmentSlot slot) {
		int appliedDamage = amount;
		if (stack.has(TFDataComponents.IS_TRAVELLERS_GEAR) && stack.isDamageableItem()) {
			int remaining = stack.getMaxDamage() - stack.getDamageValue() - 1;
			appliedDamage = Math.min(amount, Math.max(remaining, 0));
			if (appliedDamage > 0 && appliedDamage >= remaining && entity instanceof ServerPlayer player) {
				player.level().playLocalSound(player.blockPosition(), SoundEvents.ITEM_BREAK.value(), SoundSource.PLAYERS, 1.0F, player.getVoicePitch(), false);
			}
		}

		if (appliedDamage <= 0) {
			return;
		}
		TFDataAttachments.set(entity, TFDataAttachments.LAST_DAMAGE_ARMOR_TIME, entity.level().getGameTime());
		stack.hurtAndBreak(appliedDamage, entity, slot);
	}

	public static ItemAttributeModifiers activeAttributeModifiers(ItemStack stack, ItemAttributeModifiers current) {
		if (!stack.has(TFDataComponents.IS_TRAVELLERS_GEAR) || !stack.isDamageableItem()) {
			return current;
		}

		ItemAttributeModifiers stored = stack.get(TFDataComponents.STORED_BROKEN_ATTRIBUTES);
		if (stack.getDamageValue() >= stack.getMaxDamage() - 1) {
			ItemAttributeModifiers combined = mergeAttributeModifiers(current, stored);
			if (stored == null || !stored.equals(combined)) {
				stack.set(TFDataComponents.STORED_BROKEN_ATTRIBUTES, combined);
			}
			return ItemAttributeModifiers.EMPTY;
		}

		if (stored != null) {
			ItemAttributeModifiers restored = mergeAttributeModifiers(current, stored);
			stack.set(DataComponents.ATTRIBUTE_MODIFIERS, restored);
			stack.remove(TFDataComponents.STORED_BROKEN_ATTRIBUTES);
			return restored;
		}
		return current;
	}

	private static ItemAttributeModifiers mergeAttributeModifiers(ItemAttributeModifiers current, ItemAttributeModifiers stored) {
		if (stored == null || stored.modifiers().isEmpty()) {
			return current;
		}
		Set<ItemAttributeModifiers.Entry> entries = new LinkedHashSet<>(current.modifiers());
		entries.addAll(stored.modifiers());
		return new ItemAttributeModifiers(entries.stream().toList());
	}

	public static boolean blocksAnvilCombination(ItemStack left, ItemStack right) {
		return left.has(TFDataComponents.IS_TRAVELLERS_GEAR) && right.has(TFDataComponents.IS_TRAVELLERS_GEAR);
	}

	public static Optional<ItemStack> computeGrindstoneResult(HolderLookup.Provider registries, ItemStack top, ItemStack bottom) {
		List<ItemStack> travellersGear = Stream.of(top, bottom)
			.filter(stack -> stack.has(TFDataComponents.IS_TRAVELLERS_GEAR))
			.toList();
		if (travellersGear.isEmpty()) {
			return Optional.empty();
		}
		if (travellersGear.size() > 1) {
			return Optional.of(ItemStack.EMPTY);
		}

		ItemStack input = travellersGear.getFirst();
		List<Holder.Reference<TravellersModifier>> modifiers = TravellersModifiersManager.findAllInsertableModifiers(registries, input);
		if (modifiers.isEmpty()) {
			return Optional.of(ItemStack.EMPTY);
		}

		ItemStack result = input.copy();
		modifiers.forEach(modifier -> ((InsertableTravellersModifier) modifier.value()).removeModifier(result));
		return Optional.of(result);
	}

	public static void returnGrindstoneContents(Player player, ItemStack top, ItemStack bottom) {
		findUniqueTravellersGear(player.registryAccess(), top, bottom, TravellersModifiersManager.SWAP_HOTBAR_MODIFIER)
			.map(stack -> stack.get(DataComponents.CONTAINER))
			.ifPresent(contents -> contents.nonEmptyItemCopyStream().forEach(stack -> InventoryUtil.giveItemToPlayer(player, stack)));

		findUniqueTravellersGear(player.registryAccess(), top, bottom, TravellersModifiersManager.ITEM_DISPLAY_MODIFIER)
			.map(stack -> stack.get(TFDataComponents.ITEM_DISPLAY))
			.map(ItemDisplayContents::items)
			.ifPresent(items -> items.stream().forEach(stack -> InventoryUtil.giveItemToPlayer(player, stack)));
	}

	private static Optional<ItemStack> findUniqueTravellersGear(HolderLookup.Provider registries, ItemStack top, ItemStack bottom, net.minecraft.resources.ResourceKey<TravellersModifier> modifier) {
		List<ItemStack> matching = Stream.of(top, bottom)
			.filter(stack -> stack.has(TFDataComponents.IS_TRAVELLERS_GEAR))
			.filter(stack -> TravellersModifiersManager.hasTravellersModifier(registries, stack, modifier))
			.toList();
		return matching.size() == 1 ? Optional.of(matching.getFirst()) : Optional.empty();
	}

	public static void onPlayerRespawn(ServerPlayer oldPlayer, ServerPlayer newPlayer, boolean alive) {
		if (!alive && TFDataAttachments.has(oldPlayer, TFDataAttachments.TRAVELLERS_GOGGLES_RED_THREAD_VISION)) {
			TFDataAttachments.set(newPlayer, TFDataAttachments.TRAVELLERS_GOGGLES_RED_THREAD_VISION,
				TFDataAttachments.get(oldPlayer, TFDataAttachments.TRAVELLERS_GOGGLES_RED_THREAD_VISION));
		}
	}
}
