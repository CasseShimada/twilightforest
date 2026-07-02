package twilightforest.mixin.client;

import net.fabricmc.fabric.api.client.rendering.v1.FabricRenderState;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twilightforest.client.event.ClientEvents;
import twilightforest.client.renderer.entity.layers.IceLayer;
import twilightforest.client.renderer.entity.layers.ShieldLayer;
import twilightforest.item.TrophyItem;
import twilightforest.potions.FrostedEffect;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState> {
	@Inject(method = "extractRenderState", at = @At("TAIL"))
	private void twilightforest$extractRenderState(T entity, S state, float partialTick, CallbackInfo ci) {
		if (!(state instanceof FabricRenderState fabricState)) {
			return;
		}
		boolean wearingTrophy = entity.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof TrophyItem;
		fabricState.setData(ClientEvents.HEAD_KEY, wearingTrophy);
		fabricState.setData(ShieldLayer.SHIELD_COUNT_KEY, ShieldLayer.getShieldCount(entity));

		AttributeInstance speed = entity.getAttribute(Attributes.MOVEMENT_SPEED);
		AttributeModifier frost = speed != null ? speed.getModifier(FrostedEffect.MOVEMENT_SPEED_MODIFIER) : null;
		if (frost != null) {
			fabricState.setData(IceLayer.FROST_COUNT_KEY, frost.amount());
			fabricState.setData(IceLayer.FROST_ID_KEY, entity.getId());
		} else {
			fabricState.setData(IceLayer.FROST_COUNT_KEY, 0.0D);
			fabricState.setData(IceLayer.FROST_ID_KEY, 0);
		}
	}
}
