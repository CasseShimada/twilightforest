package twilightforest.events;

import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.entity.event.v1.effect.ServerMobEffectEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.TriState;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Mob;
import twilightforest.init.TFDimension;
import twilightforest.item.OreMagnetItem;

public final class TFEventHandlers {
	private TFEventHandlers() {
	}

	public static void register() {
		RegistrationEvents.register();
		LootEvents.register();

		PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
			if (world instanceof ServerLevel level && !level.isClientSide()) {
				if (ProgressionEvents.shouldCancelBlockBreak(level, player, pos)) {
					return false;
				}
				if (EntityEvents.shouldCancelCasketBreak(player, state, blockEntity)) {
					return false;
				}
				if (player instanceof ServerPlayer serverPlayer && LootEvents.tryHandleGiantPickBreak(level, serverPlayer, pos, state)) {
					return false;
				}
			}
			return true;
		});

		PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
			if (!world.isClientSide()) {
				ToolEvents.damageToolsExtra(player, player.getMainHandItem(), state);
			}
		});

		UseBlockCallback.EVENT.register((player, world, hand, hit) -> {
			if (!(world instanceof ServerLevel level) || world.isClientSide()) {
				return InteractionResult.PASS;
			}
			TriState allow = ProgressionEvents.shouldAllowBlockInteraction(level, player, hit.getBlockPos());
			if (allow == TriState.FALSE) {
				return InteractionResult.FAIL;
			}

			InteractionResult result = EntityEvents.handleWroughtFenceLead(player, world, hand, hit);
			if (result != InteractionResult.PASS) {
				return result;
			}
			result = EntityEvents.handleSkullCandleUse(player, world, hand, hit);
			if (result != InteractionResult.PASS) {
				return result;
			}
			result = MiscEvents.handleDeathTomeUse(player, world, hand, hit);
			if (result != InteractionResult.PASS) {
				return result;
			}
			result = MiscEvents.handleWashOffCloth(player, world, hand, hit);
			if (result != InteractionResult.PASS) {
				return result;
			}
			return InteractionResult.PASS;
		});

		AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
			if (world.isClientSide()) {
				return InteractionResult.PASS;
			}
			return EntityEvents.handleAttackEntity(entity);
		});

		ServerTickEvents.END_WORLD_TICK.register(world -> {
			if (world.dimension().equals(TFDimension.DIMENSION_KEY)) {
				long targetTime = 13000L;
				if (world.getDayTime() != targetTime) {
					world.setDayTime(targetTime);
				}
			}

			for (ServerPlayer player : world.players()) {
				TFTickHandler.onPlayerTick(player, world);
				CapabilityEvents.onPlayerTick(player);
			}
		});

		ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
			if (entity instanceof Mob mob) {
				MiscEvents.addPrey(mob);
			}
			EntityEvents.adjustEntityHealthInMultiplayerFights(entity, world);
			EntityEvents.handleEntityLoad(entity);
		});

		ServerEntityEvents.EQUIPMENT_CHANGE.register((entity, slot, from, to) ->
			MiscEvents.armorChanged(entity, slot, to)
		);

		ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
			ToolEvents.applyFieryToolFire(entity, source.getEntity());

			if (ProgressionEvents.shouldCancelAttackInProtectedArea(entity, source.getEntity())) {
				return false;
			}
			if (!CapabilityEvents.handleShieldDamage(entity, source)) {
				return false;
			}
			if (!HostileMountEvents.handleIncomingDamage(entity, source, amount)) {
				return false;
			}
			return EntityEvents.handleZombifiedPlayerAttack(entity, source, amount);
		});

		ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, amount, finalDamage, blocked) -> {
			EntityEvents.handleAfterDamage(entity, source, finalDamage);
			EntityEvents.handleMultiplayerDamage(entity, source);
		});

		ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
			EntityEvents.handleOminousFireDeath(entity, source);
			EntityEvents.handleMultiplayerDeath(entity);
		});

		ServerMobEffectEvents.ALLOW_ADD.register((effectInstance, entity, context) ->
			!ToolEvents.shouldBlockDigSlowdown(entity, effectInstance)
		);

		ServerPlayerEvents.ALLOW_DEATH.register((player, source, amount) -> {
			if (CharmEvents.tryPreventDeath(player, source)) {
				return false;
			}
			CharmEvents.handleDeath(player);
			return true;
		});

		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
			CharmEvents.onPlayerRespawn(newPlayer, alive);
			CapabilityEvents.onPlayerRespawn(newPlayer);
		});

		ServerPlayerEvents.JOIN.register(CapabilityEvents::onPlayerJoin);

		ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) ->
			CapabilityEvents.onPlayerChangeWorld(player)
		);

		EntityTrackingEvents.START_TRACKING.register((entity, player) ->
			CapabilityEvents.onStartTracking(player, entity)
		);

		ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((player, isReloading) ->
			EntityEvents.handleQuestSync(player)
		);

		ServerLifecycleEvents.SERVER_STARTED.register(server ->
			OreMagnetItem.rebuildOreMappings(server.registryAccess())
		);

		ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> {
			if (success) {
				OreMagnetItem.rebuildOreMappings(server.registryAccess());
			}
		});
	}
}
