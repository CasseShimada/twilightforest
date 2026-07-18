package twilightforest.events;

import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.Nullable;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalEntityTypeTags;
import twilightforest.api.WeaponApi;
import twilightforest.tags.TFBlockTags;
import twilightforest.init.TFDataAttachments;
import twilightforest.init.TFItems;
import twilightforest.item.EnderBowItem;

public class ToolEvents {

	private static final int KNIGHTMETAL_BONUS_DAMAGE = 2;
	private static final int MINOTAUR_AXE_BONUS_DAMAGE = 7;

	public static void handleEnderBowHit(Projectile arrow, EntityHitResult result) {
		if (!(arrow.getOwner() instanceof Player player)) {
			return;
		}
		if (!(result.getEntity() instanceof LivingEntity living)) {
			return;
		}
		if (arrow.getOwner() == result.getEntity()) {
			return;
		}
		if (result.getEntity().getType().builtInRegistryHolder().is(ConventionalEntityTypeTags.BOSSES)) {
			return;
		}
		if (!TFDataAttachments.has(arrow, TFDataAttachments.ENDER_BOW_ARROW)) {
			return;
		}

		double sourceX = player.getX(), sourceY = player.getY(), sourceZ = player.getZ();
		float sourceYaw = player.getYRot(), sourcePitch = player.getXRot();
		@Nullable Entity playerVehicle = player.getVehicle();

		player.setYRot(living.getYRot());
		player.teleportTo(living.getX(), living.getY(), living.getZ());
		player.invulnerableTime = 40;
		player.level().broadcastEntityEvent(player, (byte) 46);
		if (living.isPassenger() && living.getVehicle() != null) {
			player.startRiding(living.getVehicle(), true, true);
			living.stopRiding();
		}
		player.playSound(SoundEvents.CHORUS_FRUIT_TELEPORT, 1.0F, 1.0F);

		living.setYRot(sourceYaw);
		living.setXRot(sourcePitch);
		living.teleportTo(sourceX, sourceY, sourceZ);
		living.level().broadcastEntityEvent(player, (byte) 46);
		if (playerVehicle != null) {
			living.startRiding(playerVehicle, true, true);
			player.stopRiding();
		}
		living.playSound(SoundEvents.CHORUS_FRUIT_TELEPORT, 1.0F, 1.0F);
	}

	public static void applyFieryToolFire(LivingEntity target, Entity sourceEntity) {
		if (!(sourceEntity instanceof LivingEntity living)) {
			return;
		}
		if (WeaponApi.hasTrait(living.getMainHandItem(), WeaponApi.IGNITES_TARGETS) && !target.fireImmune()) {
			target.igniteForSeconds(1);
		}
	}

	public static float applyKnightmetalBonus(LivingEntity target, Entity sourceEntity, float amount) {
		if (target.level().isClientSide()) {
			return amount;
		}
		if (!(sourceEntity instanceof LivingEntity living)) {
			return amount;
		}
		ItemStack weapon = living.getMainHandItem();
		if (weapon.isEmpty()) {
			return amount;
		}

		if (target.getArmorValue() > 0 && WeaponApi.hasTrait(weapon, WeaponApi.BONUS_AGAINST_ARMORED)) {
			int moreBonus = target.getArmorCoverPercentage() > 0 ? (int) (KNIGHTMETAL_BONUS_DAMAGE * target.getArmorCoverPercentage()) : KNIGHTMETAL_BONUS_DAMAGE;
			((ServerLevel) target.level()).getChunkSource().sendToTrackingPlayersAndSelf(target, new ClientboundAnimatePacket(target, 5));
			return amount + moreBonus;
		}

		if (target.getArmorValue() == 0 && WeaponApi.hasTrait(weapon, WeaponApi.BONUS_AGAINST_UNARMORED)) {
			((ServerLevel) target.level()).getChunkSource().sendToTrackingPlayersAndSelf(target, new ClientboundAnimatePacket(target, 5));
			return amount + KNIGHTMETAL_BONUS_DAMAGE;
		}

		return amount;
	}

	public static float applyMinotaurChargeBonus(LivingEntity target, Entity sourceEntity, String damageMsgId, float amount) {
		if (target.level().isClientSide()) {
			return amount;
		}
		if (!(sourceEntity instanceof LivingEntity living)) {
			return amount;
		}
		if (!living.isSprinting()) {
			return amount;
		}
		if (!"player".equals(damageMsgId) && !"mob".equals(damageMsgId)) {
			return amount;
		}

		ItemStack weapon = living.getMainHandItem();
		if (!weapon.isEmpty() && WeaponApi.hasTrait(weapon, WeaponApi.SPRINT_CHARGE_BONUS)) {
			((ServerLevel) target.level()).getChunkSource().sendToTrackingPlayersAndSelf(target, new ClientboundAnimatePacket(target, 5));
			return amount + MINOTAUR_AXE_BONUS_DAMAGE;
		}
		return amount;
	}

	public static void damageToolsExtra(Player player, ItemStack stack, BlockState state) {
		if (state.is(TFBlockTags.MAZESTONE) || state.is(TFBlockTags.CASTLE_BLOCKS)) {
			if (stack.isDamageableItem() && !WeaponApi.hasTrait(stack, WeaponApi.MAZESTONE_WEAR_EXEMPT)) {
				stack.hurtAndBreak(16, player, EquipmentSlot.MAINHAND);
			}
		}
	}

	public static boolean shouldBlockDigSlowdown(LivingEntity entity, MobEffectInstance effect) {
		return effect.is(MobEffects.MINING_FATIGUE) && entity.isHolding(TFItems.POCKET_WATCH);
	}
}
