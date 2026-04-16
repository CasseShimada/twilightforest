package twilightforest.events;

import net.minecraft.ChatFormatting;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.LeadItem;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.WallSkullBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.fabricmc.loader.api.FabricLoader;
import twilightforest.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import twilightforest.TwilightForestMod;
import twilightforest.advancements.DrinkFromFlaskTrigger;
import twilightforest.block.*;
import twilightforest.block.entity.SkullChestBlockEntity;
import twilightforest.block.entity.SkullCandleBlockEntity;
import twilightforest.config.TFConfig;
import twilightforest.tags.TFEntityTypeTags;
import twilightforest.enchantment.ApplyFrostedEffect;
import twilightforest.entity.passive.quest.ram.QuestingRamCurrentContext;
import twilightforest.entity.projectile.ITFProjectile;
import twilightforest.entity.projectile.LichBomb;
import twilightforest.init.*;
import twilightforest.item.FieryArmorItem;
import twilightforest.item.YetiArmorItem;
import twilightforest.mixin.accessor.AgeableMobAccessor;
import twilightforest.network.SyncQuestsPacket;
import twilightforest.mixin.accessor.SkullBlockEntityAccessor;
import twilightforest.util.datamaps.EntityTransformation;
import twilightforest.util.entities.EntityUtil;
import twilightforest.util.PlayerMessaging;
import twilightforest.util.entities.OminousFireDamageSource;
import twilightforest.util.multiparts.MultipartEntityUtil;
import twilightforest.world.components.structures.SpawnIndexProvider;
import twilightforest.world.components.structures.finalcastle.FinalCastleBossGazeboComponent;
import twilightforest.world.components.structures.util.StructureConqueredData;
import twilightforest.world.components.structures.type.HollowHillStructure;
import twilightforest.world.components.structures.util.ControlledSpawns;

import java.net.URI;
import java.util.List;

public class EntityEvents {

	private static final QuestingRamCurrentContext questingRamCurrentContext = QuestingRamCurrentContext.INSTANCE;

	private static final boolean SHIELD_PARRY_MOD_LOADED = FabricLoader.getInstance().isModLoaded("parry");

	public static void handleOminousFireDeath(LivingEntity victim, DamageSource source) {
		if (!source.is(TFDamageTypes.OMINOUS_FIRE)) {
			return;
		}
		EntityTransformation dataMap = TFDataMaps.getOminousFire(victim.getType());

		if (victim instanceof ServerPlayer player) {
			var zombie = EntityType.ZOMBIE.create(player.level(), EntitySpawnReason.CONVERSION);
			TFDataAttachments.set(zombie, TFDataAttachments.ZOMBIFIED_PLAYER, player.getGameProfile());
			zombie.copyPosition(player);
			zombie.setCanPickUpLoot(true);
			zombie.setBaby(false);
			ServerLevel serverLevel = player.level();
			zombie.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(player.blockPosition()), EntitySpawnReason.CONVERSION, null);
			player.level().addFreshEntity(zombie);
		} else if (dataMap != null && victim.level() instanceof ServerLevel) {
			EntityUtil.convertEntity(victim, dataMap.result());
		}
	}

	public static boolean handleZombifiedPlayerAttack(LivingEntity target, DamageSource source, float amount) {
		if (source instanceof OminousFireDamageSource) {
			return true;
		}
		if (!(source.getEntity() instanceof Zombie zombie)) {
			return true;
		}
		if (!TFDataAttachments.has(zombie, TFDataAttachments.ZOMBIFIED_PLAYER)) {
			return true;
		}
		if (target.level() instanceof ServerLevel level) {
			target.hurtServer(level, new OminousFireDamageSource(source), amount);
			return false;
		}
		return true;
	}

	public static void handleAdvancementEarned(ServerPlayer player, AdvancementHolder advancement) {
		if (advancement.id().equals(TwilightForestMod.prefix("progression_end"))) {
			PlayerMessaging.displayClientMessage(player, Component.translatable("gui.twilightforest.progression_end.message", Component.translatable("gui.twilightforest.progression_end.discord").withStyle(style -> style.withColor(ChatFormatting.BLUE).applyFormat(ChatFormatting.UNDERLINE).withClickEvent(new ClickEvent.OpenUrl(URI.create("https://discord.experiment115.com/"))))), false);
		}

		for (var criteria : advancement.value().criteria().entrySet()) {
			if (criteria.getValue().trigger() instanceof DrinkFromFlaskTrigger) {
				TFDataAttachments.get(player, TFDataAttachments.FLASK_DOSES).resetDoses();
				break;
			}
		}
	}

	public static InteractionResult handleWroughtFenceLead(Player player, Level level, InteractionHand hand, BlockHitResult hit) {
		ItemStack stack = player.getItemInHand(hand);
		if (stack.is(Items.LEAD)) {
			BlockPos pos = hit.getBlockPos();
			BlockState state = level.getBlockState(pos);
			if (state.is(TFBlocks.WROUGHT_IRON_FENCE.get()) && state.getValue(WroughtIronFenceBlock.POST) != WroughtIronFenceBlock.PostState.NONE) {
				if (!level.isClientSide()) {
					LeadItem.bindPlayerMobs(player, level, pos);
				}
				return InteractionResult.SUCCESS;
			}
		}
		return InteractionResult.PASS;
	}

	public static InteractionResult handleGoldenDandelionUse(Player player, Level level, InteractionHand hand, Entity entity) {
		if (!(entity instanceof Animal animal) || !(animal instanceof AgeableMob ageable)) {
			return InteractionResult.PASS;
		}

		Identifier entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
		if (entityId == null || !TwilightForestMod.ID.equals(entityId.getNamespace())) {
			return InteractionResult.PASS;
		}

		ItemStack stack = player.getItemInHand(hand);
		int particleTimer = ((AgeableMobAccessor) ageable).twilightforest$getAgeLockParticleTimer();
		if (!AgeableMob.canUseGoldenDandelion(stack, ageable.isBaby(), particleTimer, animal)) {
			return InteractionResult.PASS;
		}

		if (!level.isClientSide()) {
			AgeableMob.setAgeLocked(animal, ageable::isAgeLocked, player, stack,
				mob -> ((AgeableMobAccessor) mob).twilightforest$invokeSetAgeLocked(!((AgeableMob) mob).isAgeLocked()));
		}

		return InteractionResult.SUCCESS;
	}

	public static void handleAfterDamage(LivingEntity living, DamageSource source, float finalDamage) {
		Entity trueSource = source.getEntity();

		// fire react and chill aura
		if (trueSource != null && finalDamage > 0) {
			int fireLevel = getGearCoverage(living, false) * 5;
			int chillLevel = getGearCoverage(living, true);

			if (fireLevel > 0 && living.getRandom().nextInt(25) < fireLevel && !trueSource.fireImmune()) {
				trueSource.igniteForSeconds(fireLevel / 2);
			}

			if (trueSource instanceof LivingEntity target) {
				ApplyFrostedEffect.doChillAuraEffect(target, chillLevel * 5 + 5, chillLevel, chillLevel > 0);
			}
		}

		// triple bow strips invulnerableTime
		if ("arrow".equals(source.getMsgId()) && trueSource instanceof Player player) {
			if (player.getItemInHand(player.getUsedItemHand()).is(TFItems.TRIPLE_BOW.get())) {
				living.invulnerableTime = 0;
			}
		}
	}

	public static boolean shouldCancelCasketBreak(Player player, BlockState state, BlockEntity blockEntity) {
		Block block = state.getBlock();
		if (block != TFBlocks.SKULL_CHEST.get() && block != TFBlocks.KEEPSAKE_CASKET.get()) {
			return false;
		}
		if (!(blockEntity instanceof SkullChestBlockEntity casket)) {
			return false;
		}
		ResolvableProfile checker = casket.owner;
		if (checker != null && !casket.isEmpty()) {
			boolean canBypass = player instanceof ServerPlayer serverPlayer && serverPlayer.permissions().hasPermission(Permissions.COMMANDS_ADMIN);
			return !canBypass && !player.getGameProfile().equals(checker.partialProfile());
		}
		return false;
	}

	public static void handleCrafting(Player player, ItemStack crafted, net.minecraft.world.inventory.CraftingContainer craftingInventory) {
		// if we've crafted 64 planks from a giant log, sneak 192 more planks into the player's inventory or drop them nearby
		if (crafted.is(Items.OAK_PLANKS) && crafted.getCount() == 64 && craftingInventory.countItem(TFBlocks.GIANT_LOG.get().asItem()) > 0) {
			giveItemToPlayer(player, new ItemStack(Items.OAK_PLANKS, 64));
			giveItemToPlayer(player, new ItemStack(Items.OAK_PLANKS, 64));
			giveItemToPlayer(player, new ItemStack(Items.OAK_PLANKS, 64));
		}
	}

	@SuppressWarnings("UnstableApiUsage")
	public static float applyFrostyDamageModifiers(LivingEntity living, DamageSource source, float amount) {
		var frostyHolder = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(TFMobEffects.FROSTY.get());
		var mobEffectInstance = living.getEffect(frostyHolder);
		if (mobEffectInstance == null) {
			return amount;
		}
		if (source.typeHolder().is(DamageTypes.FREEZE)) {
			return amount + (float) (mobEffectInstance.getAmplifier() / 2);
		}
		if (source.typeHolder().is(DamageTypeTags.IS_FIRE)) {
			living.removeEffect(frostyHolder);
			int newAmplifier = mobEffectInstance.getAmplifier() - 1;
			if (newAmplifier >= 0) {
				living.addEffect(new MobEffectInstance(mobEffectInstance.getEffect(), mobEffectInstance.getDuration(), newAmplifier, mobEffectInstance.isAmbient(), mobEffectInstance.isVisible(), mobEffectInstance.showIcon()));
			}
		}
		return amount;
	}

	// Parrying
	public static boolean handleParryProjectile(Projectile projectile, EntityHitResult result) {
		if (projectile.level().isClientSide()) {
			return false;
		}
		if (SHIELD_PARRY_MOD_LOADED) {
			return false;
		}
		if (!TFConfig.parryNonTwilightAttacks && !(projectile instanceof ITFProjectile)) {
			return false;
		}

		Entity entity = result.getEntity();
		if (entity instanceof LivingEntity entityBlocking) {
			if (entityBlocking.isBlocking() && entityBlocking.getUseItem().getUseDuration(entityBlocking) - entityBlocking.getUseItemRemainingTicks() <= TFConfig.shieldParryTicks) {
				projectile.deflect(ProjectileDeflection.AIM_DEFLECT, entityBlocking, EntityReference.of(entityBlocking), true);
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks if the player is attempting to create a skull candle
	 */
	// I wanted to make sure absolutely nothing broke, so I also check against the namespaces of the item to make sure theyre vanilla.
	// Worst case some stupid mod adds their own stuff to the minecraft namespace and breaks this, then you can disable this via config.
	public static InteractionResult handleSkullCandleUse(Player player, Level level, InteractionHand hand, BlockHitResult hit) {
		ItemStack stack = player.getItemInHand(hand);
		BlockPos pos = hit.getBlockPos();
		BlockState state = level.getBlockState(pos);
		if (TFConfig.disableSkullCandles) {
			return InteractionResult.PASS;
		}
		if (!stack.is(ItemTags.CANDLES) || !"minecraft".equals(BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace()) || player.isShiftKeyDown()) {
			return InteractionResult.PASS;
		}
		if (state.getBlock() instanceof AbstractSkullBlock skull && "minecraft".equals(BuiltInRegistries.BLOCK.getKey(state.getBlock()).getNamespace())) {
			SkullBlock.Types type = (SkullBlock.Types) skull.getType();
			boolean wall = state.getBlock() instanceof WallSkullBlock;
			switch (type) {
				case SKELETON -> {
					if (wall) makeWallSkull(level, pos, stack, TFBlocks.SKELETON_WALL_SKULL_CANDLE.get());
					else makeFloorSkull(level, pos, stack, TFBlocks.SKELETON_SKULL_CANDLE.get());
				}
				case WITHER_SKELETON -> {
					if (wall) makeWallSkull(level, pos, stack, TFBlocks.WITHER_SKELE_WALL_SKULL_CANDLE.get());
					else makeFloorSkull(level, pos, stack, TFBlocks.WITHER_SKELE_SKULL_CANDLE.get());
				}
				case PLAYER -> {
					if (wall) makeWallSkull(level, pos, stack, TFBlocks.PLAYER_WALL_SKULL_CANDLE.get());
					else makeFloorSkull(level, pos, stack, TFBlocks.PLAYER_SKULL_CANDLE.get());
				}
				case ZOMBIE -> {
					if (wall) makeWallSkull(level, pos, stack, TFBlocks.ZOMBIE_WALL_SKULL_CANDLE.get());
					else makeFloorSkull(level, pos, stack, TFBlocks.ZOMBIE_SKULL_CANDLE.get());
				}
				case CREEPER -> {
					if (wall) makeWallSkull(level, pos, stack, TFBlocks.CREEPER_WALL_SKULL_CANDLE.get());
					else makeFloorSkull(level, pos, stack, TFBlocks.CREEPER_SKULL_CANDLE.get());
				}
				case PIGLIN -> {
					if (wall) makeWallSkull(level, pos, stack, TFBlocks.PIGLIN_WALL_SKULL_CANDLE.get());
					else makeFloorSkull(level, pos, stack, TFBlocks.PIGLIN_SKULL_CANDLE.get());
				}
				default -> {
					return InteractionResult.PASS;
				}
			}
			stack.consume(1, player);
			player.swing(hand);
			if (player instanceof ServerPlayer) {
				player.awardStat(TFStats.SKULL_CANDLES_MADE.get());
			}
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}

	// TODO Merge method with below
	private static void makeFloorSkull(Level level, BlockPos pos, ItemStack stack, Block newBlock) {
		ResolvableProfile profile = null;
		if (level.getBlockEntity(pos) instanceof SkullBlockEntity skull)
			profile = skull.getOwnerProfile();
		level.playSound(null, pos, SoundEvents.CANDLE_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
		level.setBlockAndUpdate(pos, newBlock.defaultBlockState()
			.setValue(AbstractSkullCandleBlock.LIGHTING, LightableBlock.Lighting.NONE)
			.setValue(SkullCandleBlock.ROTATION, level.getBlockState(pos).getValue(SkullBlock.ROTATION)));
		level.setBlockEntity(new SkullCandleBlockEntity(pos,
			newBlock.defaultBlockState()
				.setValue(AbstractSkullCandleBlock.LIGHTING, LightableBlock.Lighting.NONE)
				.setValue(SkullCandleBlock.ROTATION, level.getBlockState(pos).getValue(SkullBlock.ROTATION)),
			AbstractSkullCandleBlock.candleToCandleColor(stack.getItem()).getValue()));
		if (level.getBlockEntity(pos) instanceof SkullCandleBlockEntity sc && profile != null) {
			((SkullBlockEntityAccessor) sc).twilightforest$setOwnerProfile(profile);
		}
	}

	// TODO Merge method with above
	private static void makeWallSkull(Level level, BlockPos pos, ItemStack stack, Block newBlock) {
		ResolvableProfile profile = null;
		if (level.getBlockEntity(pos) instanceof SkullBlockEntity skull)
			profile = skull.getOwnerProfile();
		level.playSound(null, pos, SoundEvents.CANDLE_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
		level.setBlockAndUpdate(pos, newBlock.defaultBlockState()
			.setValue(AbstractSkullCandleBlock.LIGHTING, LightableBlock.Lighting.NONE)
			.setValue(WallSkullCandleBlock.FACING, level.getBlockState(pos).getValue(WallSkullBlock.FACING)));
		level.setBlockEntity(new SkullCandleBlockEntity(pos,
			newBlock.defaultBlockState()
				.setValue(AbstractSkullCandleBlock.LIGHTING, LightableBlock.Lighting.NONE)
				.setValue(WallSkullCandleBlock.FACING, level.getBlockState(pos).getValue(WallSkullBlock.FACING)),
			AbstractSkullCandleBlock.candleToCandleColor(stack.getItem()).getValue()));
		if (level.getBlockEntity(pos) instanceof SkullCandleBlockEntity sc && profile != null) {
			((SkullBlockEntityAccessor) sc).twilightforest$setOwnerProfile(profile);
		}
	}

	/**
	 * Add up the number of armor pieces the player is wearing (either fiery or yeti)
	 */
	public static int getGearCoverage(LivingEntity entity, boolean yeti) {
		int amount = 0;

		for (EquipmentSlot slot : EquipmentSlot.VALUES) {
			if (!slot.isArmor()) {
				continue;
			}
			ItemStack armor = entity.getItemBySlot(slot);
			if (!armor.isEmpty() && (yeti ? armor.getItem() instanceof YetiArmorItem : armor.getItem() instanceof FieryArmorItem)) {
				amount++;
			}
		}

		return amount;
	}

	public static void handleLivingJump(LivingEntity living) {
		if (living != null && living.level().isClientSide() && !living.isSpectator() && living.level().getBlockState(living.getOnPos()).getBlock() instanceof CloudBlock) {
			for (int i = 0; i < 12; i++) {
				CloudBlock.addEntityMovementParticles(living.level(), living.getOnPos(), living, true);
			}
		}
	}

	private static int getSpawnListIndexAt(StructureStart start, BlockPos pos) {
		int highestFoundIndex = -1;
		for (StructurePiece component : start.getPieces()) {
			if (component.getBoundingBox().isInside(pos) && component instanceof SpawnIndexProvider indexProvider && indexProvider.getSpawnIndex() > highestFoundIndex) {
				highestFoundIndex = indexProvider.getSpawnIndex();
			}
		}
		return highestFoundIndex;
	}

	@Nullable
	public static WeightedList<MobSpawnSettings.SpawnerData> gatherPotentialSpawns(ServerLevel level, MobCategory classification, BlockPos pos) {
		List<StructureStart> structureStarts = level.structureManager().startsForStructure(ChunkPos.containing(pos), s -> s instanceof ControlledSpawns);

		// This is wretched FIXME make this method return void instead, make one of parameters the SpawnerData consumer (eg LevelEvent.PotentialSpawns::addSpawnerData or List::add)
		for (StructureStart start : structureStarts) {
			if (start.getStructure() instanceof ControlledSpawns landmark) {

				if (!start.isValid())
					continue;

				if (classification != MobCategory.MONSTER)
					return landmark.getSpawnableList(classification);

				var key = level.registryAccess().lookup(Registries.STRUCTURE).flatMap(registry -> registry.getResourceKey(start.getStructure())).orElse(null);
				if (key != null && StructureConqueredData.get(level).isConquered(key, start.getChunkPos()))
					return null;

				// FIXME Make interface for this method?
				if (landmark instanceof HollowHillStructure hollowHill && !hollowHill.canSpawnMob(pos, start.getBoundingBox()))
					return null;

				final int index = getSpawnListIndexAt(start, pos);
				if (index < 0)
					return null;
				return landmark.getSpawnableMonsterList(index);
			}
		}

		return null;
	}

	public static @Nullable WeightedList<MobSpawnSettings.SpawnerData> getStructureSpawns(ServerLevel level, MobCategory category, BlockPos pos) {
		return gatherPotentialSpawns(level, category, pos);
	}

	public static InteractionResult handleAttackEntity(Entity target) {
		// For clearing our Display text entities at the Final Castle Gazebo, there's no other way to remove them otherwise
		// The tag distinguishes our Interaction entities from other Mods' utilization
		if (target.level() instanceof ServerLevel level && target instanceof Interaction interaction
			&& interaction.entityTags().contains(FinalCastleBossGazeboComponent.INTERACTION_TAG)) {
			AABB bounds = interaction.getBoundingBox();
			level.getEntities(interaction, bounds, e -> e instanceof Display).forEach(Entity::discard);
			interaction.discard();
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}

	private static final Identifier GROUP_HEALTH_ID = TwilightForestMod.prefix("group_health_boost");

	public static void adjustEntityHealthInMultiplayerFights(Entity entity, ServerLevel level) {
		if (!entity.getType().builtInRegistryHolder().is(TFEntityTypeTags.MULTIPLAYER_INCLUSIVE_ENTITIES)) {
			return;
		}
		if (!TFConfig.multiplayerFightAdjuster.adjustsHealth()) {
			return;
		}
		if (!(entity instanceof LivingEntity living) || living.getAttribute(Attributes.MAX_HEALTH) == null) {
			return;
		}
		if (living.getAttribute(Attributes.MAX_HEALTH).hasModifier(GROUP_HEALTH_ID)) {
			return;
		}

		List<ServerPlayer> nearbyPlayers = level.getEntitiesOfClass(ServerPlayer.class, living.getBoundingBox().inflate(32, 10, 32), player -> EntitySelector.NO_CREATIVE_OR_SPECTATOR.and(EntitySelector.ENTITY_STILL_ALIVE).test(player));
		if (nearbyPlayers.size() > 1) {
			living.getAttribute(Attributes.MAX_HEALTH).addPermanentModifier(new AttributeModifier(GROUP_HEALTH_ID, getHealthBasedOnDifficulty(level.getDifficulty()) * (nearbyPlayers.size() - 1), AttributeModifier.Operation.ADD_VALUE));
		}
	}

	private static double getHealthBasedOnDifficulty(Difficulty difficulty) {
		return switch (difficulty) {
			default -> 0.0D;
			case EASY -> 20.0D;
			case NORMAL -> 40.0D;
			case HARD -> 60.0D;
		};
	}

	public static void handleMultiplayerDamage(LivingEntity target, DamageSource source) {
		if (!target.getType().builtInRegistryHolder().is(TFEntityTypeTags.MULTIPLAYER_INCLUSIVE_ENTITIES)) {
			return;
		}
		var data = TFDataAttachments.get(target, TFDataAttachments.MULTIPLAYER_FIGHT);
		if (source.getEntity() != null) {
			data.maybeAddQualifiedPlayer(source.getEntity());
		}
	}

	public static void handleMultiplayerDeath(LivingEntity target) {
		if (TFDataAttachments.has(target, TFDataAttachments.MULTIPLAYER_FIGHT)) {
			TFDataAttachments.get(target, TFDataAttachments.MULTIPLAYER_FIGHT).grantGroupAdvancement(target);
		}
	}

	public static boolean shouldFilterExplosionEntity(net.minecraft.world.level.Explosion explosion, Entity entity) {
		return explosion.getDirectSourceEntity() instanceof LichBomb && (entity instanceof ItemEntity || entity instanceof LichBomb);
	}

	public static void handleQuestSync(ServerPlayer player) {
		PacketDistributor.sendToPlayer(player, new SyncQuestsPacket(questingRamCurrentContext.getContext()));
	}

	public static void handleEntityLoad(Entity entity) {
		MultipartEntityUtil.trackMultipartEntity(entity);
		if (!(entity instanceof PathfinderMob mob && TFDataAttachments.has(mob, TFDataAttachments.LEASH_PATHFINDER_OVERRIDE))) {
			return;
		}
		if (!mob.mayBeLeashed()) {
			TFDataAttachments.remove(mob, TFDataAttachments.LEASH_PATHFINDER_OVERRIDE);
		}
	}

	private static void giveItemToPlayer(Player player, ItemStack stack) {
		if (!player.getInventory().add(stack)) {
			player.drop(stack, false);
		}
	}
}
