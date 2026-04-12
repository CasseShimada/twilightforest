package twilightforest.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.world.entity.player.Player;

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
		ServerPlayNetworking.registerGlobalReceiver(UncraftingGuiPacket.TYPE, (packet, context) ->
			UncraftingGuiPacket.handle(packet, new ServerPayloadContext(context))
		);
		ServerPlayNetworking.registerGlobalReceiver(WipeOreMeterPacket.TYPE, (packet, context) ->
			WipeOreMeterPacket.handle(packet, new ServerPayloadContext(context))
		);
	}

	private record ServerPayloadContext(ServerPlayNetworking.Context context) implements PayloadContext {
		@Override
		public Player player() {
			return context.player();
		}

		@Override
		public void enqueueWork(Runnable task) {
			context.server().execute(task);
		}
	}
}
