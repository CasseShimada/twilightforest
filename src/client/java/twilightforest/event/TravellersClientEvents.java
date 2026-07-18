package twilightforest.client.event;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.phys.Vec2;
import twilightforest.config.TFConfig;
import twilightforest.init.TFAttributeModifiers;
import twilightforest.init.TFDataAttachments;
import twilightforest.init.TFDataComponents;
import twilightforest.init.TFKeyBinds;
import twilightforest.init.TFSounds;
import twilightforest.init.custom.TravellersModifiersManager;
import twilightforest.item.travellers_gear.TravellersArmorBeltItem;
import twilightforest.item.travellers_gear.TravellersGearLogic;
import twilightforest.network.CycleMapSlotPacket;
import twilightforest.network.GogglesZoomPacket;
import twilightforest.network.GradualGlidePacket;
import twilightforest.network.PerformDoubleJumpPacket;
import twilightforest.network.PerformSidestepPacket;
import twilightforest.network.SwapHotbarPacket;
import twilightforest.tags.TFItemTags;

public final class TravellersClientEvents {
	private static boolean jumpWasDown;

	private TravellersClientEvents() {
	}

	public static void register() {
		ClientTickEvents.END_CLIENT_TICK.register(TravellersClientEvents::clientTick);
	}

	public static Vec2 modifyMovementInput(LocalPlayer player, ClientInput input, Vec2 movement) {
		handleDoubleJump(player, input);
		handleSidestep(player, movement.x);

		float leftImpulse = movement.x;
		float forwardImpulse = movement.y;
		ItemStack wings = player.getItemBySlot(EquipmentSlot.LEGS);
		Float agileRangerMultiplier = wings.get(TFDataComponents.AGILE_RANGER_MODIFIER);
		ItemStack useItem = player.getUseItem();
		boolean legalRangedItem = (useItem.getItem() instanceof ProjectileWeaponItem
			|| useItem.is(TFItemTags.TRAVELLERS_AGILE_RANGER_WHITELISTED))
			&& !useItem.is(TFItemTags.TRAVELLERS_AGILE_RANGER_BLACKLISTED);
		if (agileRangerMultiplier != null && player.isUsingItem() && !player.isPassenger() && legalRangedItem
			&& TravellersModifiersManager.isModifierActive(player, wings, TravellersModifiersManager.AGILE_RANGER_MODIFIER)) {
			leftImpulse *= agileRangerMultiplier;
			forwardImpulse *= agileRangerMultiplier;
		}

		ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
		Double straightAheadMultiplier = boots.get(TFDataComponents.STRAIGHT_AHEAD_MULTIPLIER);
		AttributeInstance movementSpeed = player.getAttribute(Attributes.MOVEMENT_SPEED);
		boolean straightAhead = straightAheadMultiplier != null && forwardImpulse > 0.0F
			&& TravellersModifiersManager.isModifierActive(player, boots, TravellersModifiersManager.STRAIGHT_AHEAD_MODIFIER);
		if (!straightAhead) {
			straightAheadMultiplier = 1.0D;
		}
		if (movementSpeed != null) {
			movementSpeed.addOrUpdateTransientModifier(new AttributeModifier(
				TFAttributeModifiers.STRAIGHT_AHEAD_ATTRIBUTE_MODIFIER_LOCATION,
				straightAheadMultiplier - 1.0D,
				AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
		}
		leftImpulse /= straightAheadMultiplier.floatValue();

		if (TFDataAttachments.get(player, TFDataAttachments.IS_GRADUALLY_GLIDING) && player.isShiftKeyDown()) {
			leftImpulse /= 0.2F;
			forwardImpulse /= 0.2F;
		}
		return new Vec2(leftImpulse, forwardImpulse);
	}

	public static float modifyFov(float fov, Player player) {
		ItemStack headStack = player.getItemBySlot(EquipmentSlot.HEAD);
		Float zoomModifier = headStack.get(TFDataComponents.ZOOM_ABILITY_MODIFIER);
		if (zoomModifier != null
			&& TFDataAttachments.get(player, TFDataAttachments.IS_USING_GOGGLES_ZOOM_MODIFIER)
			&& TravellersModifiersManager.isModifierActive(player, headStack, TravellersModifiersManager.ZOOM_ABILITY)) {
			return fov * zoomModifier;
		}
		return fov;
	}

	public static double modifyMouseSensitivity(double sensitivity) {
		Player player = Minecraft.getInstance().player;
		if (player == null || Minecraft.getInstance().options.smoothCamera) {
			return sensitivity;
		}
		ItemStack headStack = player.getItemBySlot(EquipmentSlot.HEAD);
		Float zoomModifier = headStack.get(TFDataComponents.ZOOM_ABILITY_MODIFIER);
		if (zoomModifier == null || !isZoomKeyHeld(player)
			|| !TravellersModifiersManager.isModifierActive(player, headStack, TravellersModifiersManager.ZOOM_ABILITY)) {
			return sensitivity;
		}
		double vanillaCurveInverse = 0.5D - 1.0D / (6.0D * sensitivity);
		return vanillaCurveInverse * sensitivity / (zoomModifier + 0.05F);
	}

	private static void clientTick(Minecraft minecraft) {
		LocalPlayer player = minecraft.player;
		if (player == null || minecraft.level == null) {
			jumpWasDown = false;
			return;
		}

		updateZoomState(player);
		updateGradualGlideState(player);
		handleKeyBindings(player);
		for (Entity entity : minecraft.level.entitiesForRendering()) {
			if (entity instanceof Player visiblePlayer) {
				TravellersGearLogic.travellersStealth(visiblePlayer, stealthPlayer -> stealthPlayer.setInvisible(true));
			}
		}
	}

	private static void handleDoubleJump(LocalPlayer player, ClientInput input) {
		boolean jumpDown = input.keyPresses.jump();
		if (!jumpDown || jumpWasDown || Minecraft.getInstance().gui.screen() != null) {
			jumpWasDown = jumpDown;
			return;
		}

		int lastJumpKeyPressTime = TFDataAttachments.get(player, TFDataAttachments.LAST_JUMP_KEY_PRESS_TIME);
		TFDataAttachments.set(player, TFDataAttachments.LAST_JUMP_KEY_PRESS_TIME, player.tickCount);
		boolean avoidCreativeFlightToggle = player.getAbilities().mayfly && player.tickCount - lastJumpKeyPressTime <= 6;
		if (!avoidCreativeFlightToggle
			&& TravellersModifiersManager.isModifierActive(player, TravellersModifiersManager.DOUBLE_JUMP_MODIFIER)
			&& TravellersGearLogic.performDoubleJump(player)) {
			ClientPlayNetworking.send(PerformDoubleJumpPacket.INSTANCE);
		}
		jumpWasDown = true;
	}

	private static void handleSidestep(LocalPlayer player, float leftImpulse) {
		if (!player.onGround()) {
			return;
		}
		float lastImpulse = TFDataAttachments.get(player, TFDataAttachments.LAST_HORIZONTAL_IMPULSE);
		float lastNonZeroImpulse = TFDataAttachments.get(player, TFDataAttachments.LAST_NON_ZERO_HORIZONTAL_IMPULSE);
		int lastWalkingTime = TFDataAttachments.get(player, TFDataAttachments.LAST_HORIZONTAL_WALKING_TIME);
		boolean doubleTapped = lastImpulse == 0.0F && leftImpulse != 0.0F
			&& Math.signum(lastNonZeroImpulse) == Math.signum(leftImpulse)
			&& player.tickCount - lastWalkingTime < 4;
		if (doubleTapped) {
			boolean leftSidestep = leftImpulse > 0.0F;
			if (TravellersGearLogic.tryPerformSidestep(player, leftSidestep)) {
				ClientPlayNetworking.send(new PerformSidestepPacket(leftSidestep));
			}
		}
		TFDataAttachments.set(player, TFDataAttachments.LAST_HORIZONTAL_IMPULSE, leftImpulse);
		if (leftImpulse != 0.0F) {
			TFDataAttachments.set(player, TFDataAttachments.LAST_HORIZONTAL_WALKING_TIME, player.tickCount);
			TFDataAttachments.set(player, TFDataAttachments.LAST_NON_ZERO_HORIZONTAL_IMPULSE, leftImpulse);
		}
	}

	private static void updateZoomState(LocalPlayer player) {
		boolean wasUsingZoom = TFDataAttachments.get(player, TFDataAttachments.IS_USING_GOGGLES_ZOOM_MODIFIER);
		ItemStack headStack = player.getItemBySlot(EquipmentSlot.HEAD);
		boolean isUsingZoom = headStack.has(TFDataComponents.ZOOM_ABILITY_MODIFIER) && isZoomKeyHeld(player)
			&& TravellersModifiersManager.isModifierActive(player, headStack, TravellersModifiersManager.ZOOM_ABILITY);
		if (isUsingZoom == wasUsingZoom) {
			return;
		}
		TFDataAttachments.set(player, TFDataAttachments.IS_USING_GOGGLES_ZOOM_MODIFIER, isUsingZoom);
		player.playSound(isUsingZoom ? TFSounds.GOGGLES_ZOOM_IN : TFSounds.GOGGLES_ZOOM_OUT);
		ClientPlayNetworking.send(new GogglesZoomPacket(isUsingZoom, player.getUUID()));
	}

	private static void updateGradualGlideState(LocalPlayer player) {
		boolean wasGraduallyGliding = TFDataAttachments.get(player, TFDataAttachments.IS_GRADUALLY_GLIDING);
		boolean configuredInput = TFConfig.manualTravellersWingsGradualGlideDefault == player.isShiftKeyDown();
		boolean isGraduallyGliding = configuredInput && player.getKnownMovement().y() < 0.0D && !player.onGround()
			&& TravellersModifiersManager.isModifierActive(player, TravellersModifiersManager.GRADUAL_GLIDE_MODIFIER);
		if (isGraduallyGliding == wasGraduallyGliding) {
			return;
		}
		TFDataAttachments.set(player, TFDataAttachments.IS_GRADUALLY_GLIDING, isGraduallyGliding);
		ClientPlayNetworking.send(new GradualGlidePacket(isGraduallyGliding, player.getUUID()));
	}

	private static void handleKeyBindings(LocalPlayer player) {
		if (TFKeyBinds.ITEM_DISPLAY_MAP_CYCLE_KEY.consumeClick()) {
			ClientPlayNetworking.send(CycleMapSlotPacket.INSTANCE);
		}
		if (TFKeyBinds.SWAP_HOTBAR_KEY.consumeClick()) {
			ItemStack legArmor = player.getItemBySlot(EquipmentSlot.LEGS);
			ItemContainerContents contents = legArmor.get(DataComponents.CONTAINER);
			if (contents != null && TravellersArmorBeltItem.hasSwapHotbar(player, legArmor)) {
				ClientPlayNetworking.send(SwapHotbarPacket.INSTANCE);
			}
		}
		if (TFKeyBinds.RED_THREAD_VISION_KEY.consumeClick()
			&& TravellersModifiersManager.isModifierActive(player, TravellersModifiersManager.RED_THREAD_VISION_MODIFIER)) {
			boolean enabled = TFDataAttachments.get(player, TFDataAttachments.TRAVELLERS_GOGGLES_RED_THREAD_VISION);
			TFDataAttachments.set(player, TFDataAttachments.TRAVELLERS_GOGGLES_RED_THREAD_VISION, !enabled);
		}
	}

	private static boolean isZoomKeyHeld(Player player) {
		return TFKeyBinds.ZOOM_KEY.isDown() && !player.isScoping();
	}
}
