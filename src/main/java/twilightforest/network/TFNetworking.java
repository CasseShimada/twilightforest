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
		PayloadTypeRegistry.playS2C().register(AreaProtectionPacket.TYPE, AreaProtectionPacket.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(CreateMovingCicadaSoundPacket.TYPE, CreateMovingCicadaSoundPacket.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(EnforceProgressionStatusPacket.TYPE, EnforceProgressionStatusPacket.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(LifedrainParticlePacket.TYPE, LifedrainParticlePacket.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(MagicMapPacket.TYPE, MagicMapPacket.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(MazeMapPacket.TYPE, MazeMapPacket.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(MissingAdvancementToastPacket.TYPE, MissingAdvancementToastPacket.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(MovePlayerPacket.TYPE, MovePlayerPacket.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(ParticlePacket.TYPE, ParticlePacket.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(SetMasonJarItemPacket.TYPE, SetMasonJarItemPacket.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(SpawnCharmPacket.TYPE, SpawnCharmPacket.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(SpawnFallenLeafFromPacket.TYPE, SpawnFallenLeafFromPacket.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(StructureProtectionPacket.TYPE, StructureProtectionPacket.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(SyncQuestsPacket.TYPE, SyncQuestsPacket.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(SyncUncraftingTableConfigPacket.TYPE, SyncUncraftingTableConfigPacket.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(TFBossBarPacket.AddTFBossBarPacket.TYPE, TFBossBarPacket.AddTFBossBarPacket.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(TFBossBarPacket.UpdateTFBossBarStylePacket.TYPE, TFBossBarPacket.UpdateTFBossBarStylePacket.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(UpdateDeathTimePacket.TYPE, UpdateDeathTimePacket.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(UpdateFeatherFanFallPacket.TYPE, UpdateFeatherFanFallPacket.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(UpdateShieldPacket.TYPE, UpdateShieldPacket.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(UpdateTFMultipartPacket.TYPE, UpdateTFMultipartPacket.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(UpdateThrownPacket.TYPE, UpdateThrownPacket.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(UpdateUncraftingCostPacket.TYPE, UpdateUncraftingCostPacket.STREAM_CODEC);

		PayloadTypeRegistry.playC2S().register(UncraftingGuiPacket.TYPE, UncraftingGuiPacket.STREAM_CODEC);
		PayloadTypeRegistry.playC2S().register(WipeOreMeterPacket.TYPE, WipeOreMeterPacket.STREAM_CODEC);
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
