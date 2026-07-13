package twilightforest.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import twilightforest.config.TFConfig;
import twilightforest.init.TFDataComponents;
import twilightforest.init.TFItems;
import twilightforest.inventory.UncraftingMenu;

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

		PayloadTypeRegistry.serverboundPlay().register(UncraftingGuiPacket.TYPE, UncraftingGuiPacket.STREAM_CODEC);
		PayloadTypeRegistry.serverboundPlay().register(WipeOreMeterPacket.TYPE, WipeOreMeterPacket.STREAM_CODEC);
	}

	private static void registerServerReceivers() {
		ServerPlayNetworking.registerGlobalReceiver(UncraftingGuiPacket.TYPE, TFNetworking::handleUncraftingGui);
		ServerPlayNetworking.registerGlobalReceiver(WipeOreMeterPacket.TYPE, TFNetworking::handleWipeOreMeter);
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
}
