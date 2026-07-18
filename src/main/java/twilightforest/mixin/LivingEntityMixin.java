package twilightforest.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twilightforest.events.EntityEvents;
import twilightforest.events.TravellersGearEvents;
import twilightforest.events.ToolEvents;
import twilightforest.item.travellers_gear.TravellersGearLogic;
import twilightforest.init.custom.TravellersModifiersManager;
import twilightforest.util.ArmorUtil;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
	private static final ArmorUtil ARMOR_UTIL = new ArmorUtil();

	@ModifyVariable(method = "hurtServer", at = @At("HEAD"), argsOnly = true)
	private float twilightforest$modifyDamage(float amount, ServerLevel level, DamageSource source) {
		LivingEntity self = (LivingEntity) (Object) this;
		float modified = EntityEvents.applyFrostyDamageModifiers(self, source, amount);
		modified = ToolEvents.applyKnightmetalBonus(self, source.getDirectEntity(), modified);
		modified = ToolEvents.applyMinotaurChargeBonus(self, source.getDirectEntity(), source.getMsgId(), modified);
		return modified;
	}

	@Inject(method = "jumpFromGround", at = @At("TAIL"))
	private void twilightforest$onJump(CallbackInfo ci) {
		LivingEntity living = (LivingEntity) (Object) this;
		EntityEvents.handleLivingJump(living);
		TravellersGearEvents.onLivingJump(living);
	}

	@Inject(method = "canStandOnFluid", at = @At("RETURN"), cancellable = true)
	private void twilightforest$waterWalking(FluidState fluidState, CallbackInfoReturnable<Boolean> cir) {
		LivingEntity living = (LivingEntity) (Object) this;
		cir.setReturnValue(TravellersGearLogic.canStandOnWater(living, fluidState, cir.getReturnValueZ()));
	}

	@ModifyExpressionValue(
		method = "travelInAir",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;getFriction()F")
	)
	private float twilightforest$unrestrainedFriction(float original) {
		LivingEntity living = (LivingEntity) (Object) this;
		return TravellersModifiersManager.isModifierActive(living, TravellersModifiersManager.UNRESTRAINED_MODIFIER) ? 0.6F : original;
	}

	@Inject(method = "causeFallDamage", at = @At("HEAD"), cancellable = true)
	private void twilightforest$slimySolesFallDamage(double fallDistance, float damageMultiplier, DamageSource source, CallbackInfoReturnable<Boolean> cir) {
		if (TravellersGearEvents.onFall((LivingEntity) (Object) this, fallDistance, damageMultiplier, source)) {
			cir.setReturnValue(false);
		}
	}

	@Redirect(
		method = "doHurtEquipment",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/item/ItemStack;hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;)V"
		)
	)
	private void twilightforest$preserveTravellersGear(ItemStack stack, int amount, LivingEntity entity, EquipmentSlot slot) {
		TravellersGearEvents.damageArmor(stack, amount, entity, slot);
	}

	@Redirect(
		method = "getVisibilityPercent",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/LivingEntity;getArmorCoverPercentage()F"
		)
	)
	private float twilightforest$hideShroudedArmorFromVisibility(LivingEntity entity) {
		return entity.getArmorCoverPercentage() - ARMOR_UTIL.getShroudedArmorPercentage(entity);
	}
}
