package twilightforest.events;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Unit;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.LevelData;
import twilightforest.components.entity.FortificationShieldAttachment;
import twilightforest.config.TFConfig;
import twilightforest.init.TFDataAttachments;
import twilightforest.init.TFDimension;
import twilightforest.network.UpdateFeatherFanFallPacket;
import twilightforest.network.UpdateShieldPacket;
import twilightforest.world.NoReturnTeleporter;
import twilightforest.world.TFTeleporter;

public final class AttachmentEvents {
	private AttachmentEvents() {
	}

	public static void onPlayerTick(ServerPlayer player) {
		if (TFDataAttachments.get(player, TFDataAttachments.FEATHER_FAN)) {
			player.setIgnoreFallDamageFromCurrentImpulse(true, player.position());
			player.currentImpulseImpactPos = player.position();

			if (player.onGround() || player.isSwimming() || player.isInWater()) {
				TFDataAttachments.set(player, TFDataAttachments.FEATHER_FAN, false);
				UpdateFeatherFanFallPacket packet = new UpdateFeatherFanFallPacket(player.getId(), false);
				PlayerLookup.tracking(player).forEach(tracker -> ServerPlayNetworking.send(tracker, packet));
				ServerPlayNetworking.send(player, packet);
			}
		}
		TFDataAttachments.get(player, TFDataAttachments.YETI_THROWING).tick(player);
		TFDataAttachments.get(player, TFDataAttachments.TF_PORTAL_COOLDOWN).tick(player);
	}

	public static boolean handleShieldDamage(LivingEntity living, DamageSource source) {
		// shields
		if (!living.level().isClientSide() && !source.is(DamageTypeTags.BYPASSES_ARMOR)) {
			FortificationShieldAttachment attachment = TFDataAttachments.get(living, TFDataAttachments.FORTIFICATION_SHIELDS);
			if (attachment.shieldsLeft() > 0) {
				if (living.invulnerableTime <= 0) {
					attachment.breakShield(living, false);
					FortificationShieldAttachment.addShieldBreakParticles(source, living);
					living.invulnerableTime = 20;
				}
				return false;
			}
		}
		return true;
	}

	public static void onPlayerRespawn(ServerPlayer serverPlayer) {
		if (serverPlayer == null) {
			return;
		}
		ServerPlayer.RespawnConfig respawnConfig = serverPlayer.getRespawnConfig();
		if (respawnConfig == null) {
			newSpawnInTwilightForest(serverPlayer);
			return;
		}
		LevelData.RespawnData respawnData = respawnConfig.respawnData();
		if (respawnData == null || LevelData.RespawnData.DEFAULT.equals(respawnData)) {
			newSpawnInTwilightForest(serverPlayer);
		}
	}

	/**
	 * When player logs in, report conflict status, set progression status
	 */
	public static void onPlayerJoin(ServerPlayer player) {
		if (player == null || player.level().isClientSide()) {
			return;
		}
		syncAttachments(player, player);
		TFConfig.syncUncraftingConfig(player);
		if (!TFDataAttachments.has(player, TFDataAttachments.BANISHED_TO_TWILIGHT_FOREST)) {
			newSpawnInTwilightForest(player);
		}
	}

	public static void onPlayerChangeWorld(ServerPlayer player) {
		if (player != null && !player.level().isClientSide()) {
			syncAttachments(player, player);
		}
	}

	public static void onStartTracking(ServerPlayer tracker, Entity target) {
		syncAttachments(tracker, target);
	}

	private static void syncAttachments(ServerPlayer clientTarget, Entity shielded) {
		var attachment = TFDataAttachments.get(shielded, TFDataAttachments.FORTIFICATION_SHIELDS);
		if (attachment.shieldsLeft() > 0) {
			ServerPlayNetworking.send(clientTarget, new UpdateShieldPacket(shielded.getId(), attachment.temporaryShieldsLeft(), attachment.permanentShieldsLeft()));
		}
	}

	private static void newSpawnInTwilightForest(ServerPlayer player) {
		if (!TFConfig.newPlayersSpawnInTF)
			return;
		ServerLevel level = player.level().getServer().getLevel(TFDimension.DIMENSION_KEY);
		if (level == null)
			return;

		BlockPos newDefaultSpawn = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, player.blockPosition());

		player.teleport(TFConfig.portalForNewPlayerSpawn ?
			TFTeleporter.createTransition(player, level, newDefaultSpawn, true) :
			NoReturnTeleporter.createNoPortalTransition(level, player, newDefaultSpawn));
		player.setRespawnPosition(new ServerPlayer.RespawnConfig(LevelData.RespawnData.of(TFDimension.DIMENSION_KEY, newDefaultSpawn, player.getYRot(), player.getXRot()), true), false);

		TFDataAttachments.set(player, TFDataAttachments.BANISHED_TO_TWILIGHT_FOREST, Unit.INSTANCE);
	}
}
