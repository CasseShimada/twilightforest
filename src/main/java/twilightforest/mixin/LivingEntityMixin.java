package twilightforest.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twilightforest.events.EntityEvents;
import twilightforest.events.ToolEvents;
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
		EntityEvents.handleLivingJump((LivingEntity) (Object) this);
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
