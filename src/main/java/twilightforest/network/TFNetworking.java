package twilightforest.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import twilightforest.components.item.ItemDisplayContents;
import twilightforest.config.TFConfig;
import twilightforest.init.TFDataAttachments;
import twilightforest.init.TFDataComponents;
import twilightforest.init.TFItems;
import twilightforest.init.TFSounds;
import twilightforest.init.custom.TravellersModifiersManager;
import twilightforest.inventory.UncraftingMenu;
import twilightforest.item.travellers_gear.TravellersArmorBeltItem;
import twilightforest.item.travellers_gear.TravellersGearLogic;

public final class TFNetworking {
	private TFNetworking() {
	}

	public static void init() {
		registerPayloadTypes();
		registerServerReceivers();
	}

	private static void registerPayloadTypes() {
		PayloadTypeRegistry.clientboundPlay().register(AreaProtectionPacket.TYPE, AreaProtectionPacket.STREAM_CODEC);
		PayloadTypeRegistry.clientboundPlay().register(CreateMovingCicadaSoundPacket.TYPE, CreateMovingCicadaSoundPacket.STREAM_CODEC);
		PayloadTypeRegistry.clientboundPlay().register(EnforceProgressionStatusPacket.TYPE, EnforceProgressionStatusPacket.STREAM_CODEC);
		PayloadTypeRegistry.clientboundPlay().register(LifedrainParticlePacket.TYPE, LifedrainParticlePacket.STREAM_CODEC);
		PayloadTypeRegistry.clientboundPlay().register(MagicMapPacket.TYPE, MagicMapPacket.STREAM_CODEC);
		PayloadTypeRegistry.clientboundPlay().register(MazeMapPacket.TYPE, MazeMapPacket.STREAM_CODEC);
		PayloadTypeRegistry.clientboundPlay().register(MissingAdvancementToastPacket.TYPE, MissingAdvancementToastPacket.STREAM_CODEC);
		PayloadTypeRegistry.clientboundPlay().register(MovePlayerPacket.TYPE, MovePlayerPacket.STREAM_CODEC);
		PayloadTypeRegistry.clientboundPlay().register(ParticlePacket.TYPE, ParticlePacket.STREAM_CODEC);
		PayloadTypeRegistry.clientboundPlay().register(SetMasonJarItemPacket.TYPE, SetMasonJarItemPacket.STREAM_CODEC);
		PayloadTypeRegistry.clientboundPlay().register(SpawnCharmPacket.TYPE, SpawnCharmPacket.STREAM_CODEC);
		PayloadTypeRegistry.clientboundPlay().register(SpawnFallenLeafFromPacket.TYPE, SpawnFallenLeafFromPacket.STREAM_CODEC);
		PayloadTypeRegistry.clientboundPlay().register(StructureProtectionPacket.TYPE, StructureProtectionPacket.STREAM_CODEC);
		PayloadTypeRegistry.clientboundPlay().register(SyncQuestsPacket.TYPE, SyncQuestsPacket.STREAM_CODEC);
		PayloadTypeRegistry.clientboundPlay().register(SyncUncraftingTableConfigPacket.TYPE, SyncUncraftingTableConfigPacket.STREAM_CODEC);
		PayloadTypeRegistry.clientboundPlay().register(TFBossBarPacket.AddTFBossBarPacket.TYPE, TFBossBarPacket.AddTFBossBarPacket.STREAM_CODEC);
		PayloadTypeRegistry.clientboundPlay().register(TFBossBarPacket.UpdateTFBossBarStylePacket.TYPE, TFBossBarPacket.UpdateTFBossBarStylePacket.STREAM_CODEC);
		PayloadTypeRegistry.clientboundPlay().register(UpdateDeathTimePacket.TYPE, UpdateDeathTimePacket.STREAM_CODEC);
		PayloadTypeRegistry.clientboundPlay().register(UpdateFeatherFanFallPacket.TYPE, UpdateFeatherFanFallPacket.STREAM_CODEC);
		PayloadTypeRegistry.clientboundPlay().register(UpdateShieldPacket.TYPE, UpdateShieldPacket.STREAM_CODEC);
		PayloadTypeRegistry.clientboundPlay().register(UpdateTFMultipartPacket.TYPE, UpdateTFMultipartPacket.STREAM_CODEC);
		PayloadTypeRegistry.clientboundPlay().register(UpdateThrownPacket.TYPE, UpdateThrownPacket.STREAM_CODEC);
		PayloadTypeRegistry.clientboundPlay().register(UpdateUncraftingCostPacket.TYPE, UpdateUncraftingCostPacket.STREAM_CODEC);
		PayloadTypeRegistry.clientboundPlay().register(GogglesZoomPacket.TYPE, GogglesZoomPacket.STREAM_CODEC);
		PayloadTypeRegistry.clientboundPlay().register(GradualGlidePacket.TYPE, GradualGlidePacket.STREAM_CODEC);
		PayloadTypeRegistry.clientboundPlay().register(TravellersWingsStatePacket.TYPE, TravellersWingsStatePacket.STREAM_CODEC);

		PayloadTypeRegistry.serverboundPlay().register(CycleMapSlotPacket.TYPE, CycleMapSlotPacket.STREAM_CODEC);
		PayloadTypeRegistry.serverboundPlay().register(GogglesZoomPacket.TYPE, GogglesZoomPacket.STREAM_CODEC);
		PayloadTypeRegistry.serverboundPlay().register(GradualGlidePacket.TYPE, GradualGlidePacket.STREAM_CODEC);
		PayloadTypeRegistry.serverboundPlay().register(PerformDoubleJumpPacket.TYPE, PerformDoubleJumpPacket.STREAM_CODEC);
		PayloadTypeRegistry.serverboundPlay().register(PerformSidestepPacket.TYPE, PerformSidestepPacket.STREAM_CODEC);
		PayloadTypeRegistry.serverboundPlay().register(SwapHotbarPacket.TYPE, SwapHotbarPacket.STREAM_CODEC);
		PayloadTypeRegistry.serverboundPlay().register(UncraftingGuiPacket.TYPE, UncraftingGuiPacket.STREAM_CODEC);
		PayloadTypeRegistry.serverboundPlay().register(WipeOreMeterPacket.TYPE, WipeOreMeterPacket.STREAM_CODEC);
	}

	private static void registerServerReceivers() {
		ServerPlayNetworking.registerGlobalReceiver(CycleMapSlotPacket.TYPE, TFNetworking::handleCycleMapSlot);
		ServerPlayNetworking.registerGlobalReceiver(GogglesZoomPacket.TYPE, TFNetworking::handleGogglesZoom);
		ServerPlayNetworking.registerGlobalReceiver(GradualGlidePacket.TYPE, TFNetworking::handleGradualGlide);
		ServerPlayNetworking.registerGlobalReceiver(PerformDoubleJumpPacket.TYPE, TFNetworking::handleDoubleJump);
		ServerPlayNetworking.registerGlobalReceiver(PerformSidestepPacket.TYPE, TFNetworking::handleSidestep);
		ServerPlayNetworking.registerGlobalReceiver(SwapHotbarPacket.TYPE, TFNetworking::handleSwapHotbar);
		ServerPlayNetworking.registerGlobalReceiver(UncraftingGuiPacket.TYPE, TFNetworking::handleUncraftingGui);
		ServerPlayNetworking.registerGlobalReceiver(WipeOreMeterPacket.TYPE, TFNetworking::handleWipeOreMeter);
	}

	private static void handleCycleMapSlot(CycleMapSlotPacket packet, ServerPlayNetworking.Context context) {
		context.server().execute(() -> {
			ServerPlayer player = context.player();
			ItemStack headStack = player.getItemBySlot(EquipmentSlot.HEAD);
			ItemDisplayContents contents = headStack.get(TFDataComponents.ITEM_DISPLAY);
			if (contents == null || contents.isEmpty()
				|| !TravellersModifiersManager.isModifierActive(player, TravellersModifiersManager.ITEM_DISPLAY_MODIFIER)) {
				return;
			}

			ItemDisplayContents.Mutable mutable = new ItemDisplayContents.Mutable(contents);
			int oldIndex = mutable.chosenMapSlot();
			int newIndex = mutable.cycleChosenMapSlot();
			if (oldIndex != newIndex) {
				headStack.set(TFDataComponents.ITEM_DISPLAY, mutable.toImmutable());
				player.getInventory().setChanged();
				player.level().playSound(null, player, newIndex == -1 ? TFSounds.CYCLE_MAPS_EMPTY : TFSounds.CYCLE_MAPS, player.getSoundSource(), 1.0F, 1.0F);
			}
		});
	}

	private static void handleGogglesZoom(GogglesZoomPacket packet, ServerPlayNetworking.Context context) {
		context.server().execute(() -> {
			ServerPlayer player = context.player();
			if (!player.getUUID().equals(packet.playerUUID())) {
				return;
			}
			boolean canZoom = TravellersModifiersManager.isModifierActive(player, TravellersModifiersManager.ZOOM_ABILITY);
			if (packet.isUsingZoom() && !canZoom) {
				return;
			}
			TFDataAttachments.set(player, TFDataAttachments.IS_USING_GOGGLES_ZOOM_MODIFIER, packet.isUsingZoom());
			player.playSound(packet.isUsingZoom() ? TFSounds.GOGGLES_ZOOM_IN : TFSounds.GOGGLES_ZOOM_OUT);
			sendToTracking(player, new GogglesZoomPacket(packet.isUsingZoom(), player.getUUID()));
		});
	}

	private static void handleGradualGlide(GradualGlidePacket packet, ServerPlayNetworking.Context context) {
		context.server().execute(() -> {
			ServerPlayer player = context.player();
			if (!player.getUUID().equals(packet.playerUUID())) {
				return;
			}
			boolean canGlide = TravellersModifiersManager.isModifierActive(player, TravellersModifiersManager.GRADUAL_GLIDE_MODIFIER);
			if (packet.isGraduallyGliding() && !canGlide) {
				return;
			}
			TFDataAttachments.set(player, TFDataAttachments.IS_GRADUALLY_GLIDING, packet.isGraduallyGliding());
			sendToTracking(player, new GradualGlidePacket(packet.isGraduallyGliding(), player.getUUID()));
		});
	}

	private static void handleDoubleJump(PerformDoubleJumpPacket packet, ServerPlayNetworking.Context context) {
		context.server().execute(() -> {
			if (!TravellersGearLogic.performDoubleJump(context.player())) {
				TravellersGearLogic.handleDoubleJumpAbuse(context.player());
			}
		});
	}

	private static void handleSidestep(PerformSidestepPacket packet, ServerPlayNetworking.Context context) {
		context.server().execute(() -> {
			if (!TravellersGearLogic.tryPerformSidestep(context.player(), packet.isLeftStepSide())) {
				TravellersGearLogic.handleSidestepAbuse(context.player());
			}
		});
	}

	private static void handleSwapHotbar(SwapHotbarPacket packet, ServerPlayNetworking.Context context) {
		context.server().execute(() -> {
			TravellersArmorBeltItem.travellersTrySwapHotbar(context.player());
			context.player().getInventory().setChanged();
		});
	}

	private static void handleUncraftingGui(UncraftingGuiPacket packet, ServerPlayNetworking.Context context) {
		context.server().execute(() -> {
			AbstractContainerMenu container = context.player().containerMenu;
			if (!(container instanceof UncraftingMenu uncrafting)) return;

			switch (packet.operationType()) {
				case 0 -> uncrafting.unrecipeInCycle++;
				case 1 -> uncrafting.unrecipeInCycle--;
				case 2 -> {
					if (!TFConfig.disableIngredientSwitching) {
						uncrafting.ingredientsInCycle++;
					}
				}
				case 3 -> {
					if (!TFConfig.disableIngredientSwitching) {
						uncrafting.ingredientsInCycle--;
					}
				}
				case 4 -> uncrafting.recipeInCycle++;
				case 5 -> uncrafting.recipeInCycle--;
			}

			if (packet.operationType() < 4) {
				uncrafting.slotsChanged(uncrafting.tinkerInput);
			}
			if (packet.operationType() >= 4) {
				uncrafting.slotsChanged(uncrafting.getCraftSlots());
			}
		});
	}

	private static void handleWipeOreMeter(WipeOreMeterPacket packet, ServerPlayNetworking.Context context) {
		context.server().execute(() -> {
			ItemStack heldStack = context.player().getItemInHand(packet.hand());
			if (heldStack.is(TFItems.ORE_METER)) {
				heldStack.remove(TFDataComponents.ORE_DATA);
				heldStack.remove(TFDataComponents.ORE_FILTER);
			}
		});
	}

	public static void sendToTracking(Entity entity, CustomPacketPayload payload) {
		PlayerLookup.tracking(entity).forEach(player -> ServerPlayNetworking.send(player, payload));
	}

	public static void sendToTrackingAndSelf(Entity entity, CustomPacketPayload payload) {
		sendToTracking(entity, payload);
		if (entity instanceof ServerPlayer player) {
			ServerPlayNetworking.send(player, payload);
		}
	}
}
