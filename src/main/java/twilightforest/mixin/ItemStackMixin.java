package twilightforest.mixin;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.apache.commons.lang3.function.TriConsumer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twilightforest.events.TravellersGearEvents;
import twilightforest.util.LegacyMapItemStackFix;

import java.util.function.BiConsumer;

@Mixin(ItemStack.class)
public class ItemStackMixin {
	@Shadow @Final @Mutable private Holder<net.minecraft.world.item.Item> item;
	@Shadow @Final @Mutable private PatchedDataComponentMap components;

	@Inject(
		method = "<init>(Lnet/minecraft/core/Holder;ILnet/minecraft/core/component/DataComponentPatch;)V",
		at = @At("RETURN")
	)
	private void twilightforest$fixLegacyMapStack(Holder<net.minecraft.world.item.Item> item, int count, DataComponentPatch components, CallbackInfo callback) {
		LegacyMapItemStackFix.FixedStack fixed = LegacyMapItemStackFix.fix(item, components);
		if (fixed.item() != item || fixed.components() != components) {
			this.item = fixed.item();
			this.components = PatchedDataComponentMap.fromPatch(fixed.item().components(), fixed.components());
		}
	}

	@Redirect(
		method = "forEachModifier(Lnet/minecraft/world/entity/EquipmentSlotGroup;Lorg/apache/commons/lang3/function/TriConsumer;)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/item/component/ItemAttributeModifiers;forEach(Lnet/minecraft/world/entity/EquipmentSlotGroup;Lorg/apache/commons/lang3/function/TriConsumer;)V"
		)
	)
	private void twilightforest$applyActiveGroupModifiers(ItemAttributeModifiers modifiers, EquipmentSlotGroup group, TriConsumer<Holder<Attribute>, AttributeModifier, ItemAttributeModifiers.Display> consumer) {
		TravellersGearEvents.activeAttributeModifiers((ItemStack) (Object) this, modifiers).forEach(group, consumer);
	}

	@Redirect(
		method = "forEachModifier(Lnet/minecraft/world/entity/EquipmentSlot;Ljava/util/function/BiConsumer;)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/item/component/ItemAttributeModifiers;forEach(Lnet/minecraft/world/entity/EquipmentSlot;Ljava/util/function/BiConsumer;)V"
		)
	)
	private void twilightforest$applyActiveSlotModifiers(ItemAttributeModifiers modifiers, EquipmentSlot slot, BiConsumer<Holder<Attribute>, AttributeModifier> consumer) {
		TravellersGearEvents.activeAttributeModifiers((ItemStack) (Object) this, modifiers).forEach(slot, consumer);
	}
}
