package twilightforest.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import twilightforest.block.entity.MasonJarBlockEntity;
import twilightforest.client.MissingAdvancementToast;
import twilightforest.client.MovingCicadaSoundInstance;
import twilightforest.client.ClientHandHelper;
import twilightforest.client.renderer.TFWeatherRenderer;
import twilightforest.config.TFConfig;
import twilightforest.entity.CharmEffect;
import twilightforest.entity.ProtectionBox;
import twilightforest.entity.TFMultipartEntity;
import twilightforest.entity.TFPart;
import twilightforest.entity.boss.bar.ClientTFBossBar;
import twilightforest.entity.passive.quest.ram.QuestingRamCurrentContext;
import twilightforest.init.TFDataAttachments;
import twilightforest.init.TFEntities;
import twilightforest.init.TFParticleType;
import twilightforest.inventory.UncraftingMenu;
import twilightforest.item.MagicMapItem;
import twilightforest.item.MazeMapItem;
import twilightforest.item.mapdata.TFMagicMapData;
import twilightforest.item.mapdata.TFMazeMapData;
import twilightforest.mixin.client.accessor.BossHealthOverlayAccessor;
import twilightforest.particle.data.LeafParticleData;

import java.util.Arrays;
import java.util.Random;

public final class TFClientNetworking {
	private TFClientNetworking() {
	}

	public static void init() {
		registerReceivers();
	}

	private static void registerReceivers() {
		ClientPlayNetworking.registerGlobalReceiver(EnforceProgressionStatusPacket.TYPE, TFClientNetworking::handleEnforceProgressionStatus);
		ClientPlayNetworking.registerGlobalReceiver(MovePlayerPacket.TYPE, TFClientNetworking::handleMovePlayer);
		ClientPlayNetworking.registerGlobalReceiver(ParticlePacket.TYPE, TFClientNetworking::handleParticles);
		ClientPlayNetworking.registerGlobalReceiver(SetMasonJarItemPacket.TYPE, TFClientNetworking::handleSetMasonJarItem);
		ClientPlayNetworking.registerGlobalReceiver(SyncQuestsPacket.TYPE, TFClientNetworking::handleSyncQuests);
		ClientPlayNetworking.registerGlobalReceiver(SyncUncraftingTableConfigPacket.TYPE, TFClientNetworking::handleSyncUncraftingTableConfig);
		ClientPlayNetworking.registerGlobalReceiver(UpdateDeathTimePacket.TYPE, TFClientNetworking::handleUpdateDeathTime);
		ClientPlayNetworking.registerGlobalReceiver(UpdateFeatherFanFallPacket.TYPE, TFClientNetworking::handleUpdateFeatherFanFall);
		ClientPlayNetworking.registerGlobalReceiver(UpdateShieldPacket.TYPE, TFClientNetworking::handleUpdateShield);
		ClientPlayNetworking.registerGlobalReceiver(UpdateTFMultipartPacket.TYPE, TFClientNetworking::handleUpdateTFMultipart);
		ClientPlayNetworking.registerGlobalReceiver(UpdateThrownPacket.TYPE, TFClientNetworking::handleUpdateThrown);
		ClientPlayNetworking.registerGlobalReceiver(UpdateUncraftingCostPacket.TYPE, TFClientNetworking::handleUpdateUncraftingCost);
		ClientPlayNetworking.registerGlobalReceiver(GogglesZoomPacket.TYPE, TFClientNetworking::handleGogglesZoom);
		ClientPlayNetworking.registerGlobalReceiver(GradualGlidePacket.TYPE, TFClientNetworking::handleGradualGlide);
		ClientPlayNetworking.registerGlobalReceiver(TravellersWingsStatePacket.TYPE, TFClientNetworking::handleTravellersWingsState);

		ClientPlayNetworking.registerGlobalReceiver(AreaProtectionPacket.TYPE, TFClientNetworking::handleAreaProtection);
		ClientPlayNetworking.registerGlobalReceiver(CreateMovingCicadaSoundPacket.TYPE, TFClientNetworking::handleMovingCicadaSound);
		ClientPlayNetworking.registerGlobalReceiver(LifedrainParticlePacket.TYPE, TFClientNetworking::handleLifedrainParticles);
		ClientPlayNetworking.registerGlobalReceiver(MagicMapPacket.TYPE, TFClientNetworking::handleMagicMap);
		ClientPlayNetworking.registerGlobalReceiver(MazeMapPacket.TYPE, TFClientNetworking::handleMazeMap);
		ClientPlayNetworking.registerGlobalReceiver(MissingAdvancementToastPacket.TYPE, TFClientNetworking::handleMissingAdvancementToast);
		ClientPlayNetworking.registerGlobalReceiver(SpawnCharmPacket.TYPE, TFClientNetworking::handleSpawnCharm);
		ClientPlayNetworking.registerGlobalReceiver(SpawnFallenLeafFromPacket.TYPE, TFClientNetworking::handleSpawnFallenLeaf);
		ClientPlayNetworking.registerGlobalReceiver(StructureProtectionPacket.TYPE, TFClientNetworking::handleStructureProtection);
		ClientPlayNetworking.registerGlobalReceiver(TFBossBarPacket.AddTFBossBarPacket.TYPE, TFClientNetworking::handleBossBarAdd);
		ClientPlayNetworking.registerGlobalReceiver(TFBossBarPacket.UpdateTFBossBarStylePacket.TYPE, TFClientNetworking::handleBossBarStyle);
	}

	private static void handleGogglesZoom(GogglesZoomPacket packet, ClientPlayNetworking.Context context) {
		context.client().execute(() -> {
			Player player = context.player().level().getPlayerByUUID(packet.playerUUID());
			if (player != null) {
				TFDataAttachments.set(player, TFDataAttachments.IS_USING_GOGGLES_ZOOM_MODIFIER, packet.isUsingZoom());
			}
		});
	}

	private static void handleGradualGlide(GradualGlidePacket packet, ClientPlayNetworking.Context context) {
		context.client().execute(() -> {
			Player player = context.player().level().getPlayerByUUID(packet.playerUUID());
			if (player != null) {
				TFDataAttachments.set(player, TFDataAttachments.IS_GRADUALLY_GLIDING, packet.isGraduallyGliding());
			}
		});
	}

	private static void handleTravellersWingsState(TravellersWingsStatePacket packet, ClientPlayNetworking.Context context) {
		context.client().execute(() -> {
			Entity entity = context.player().level().getEntity(packet.entityId());
			if (entity instanceof LivingEntity living) {
				var attachment = TFDataAttachments.get(living, TFDataAttachments.TRAVELLERS_WINGS);
				attachment.state = packet.state();
				attachment.sidestepLeft = packet.sidestepLeft();
				attachment.doubleJumpTimer = packet.doubleJumpTimer();
				attachment.sidestepTimer = packet.sidestepTimer();
			}
		});
	}

	private static void handleEnforceProgressionStatus(EnforceProgressionStatusPacket packet, ClientPlayNetworking.Context context) {
		context.client().execute(() -> EnforceProgressionStatusPacket.enforcedProgression = packet.enforce());
	}

	private static void handleMovePlayer(MovePlayerPacket packet, ClientPlayNetworking.Context context) {
		context.client().execute(() -> context.player().push(packet.motionX(), packet.motionY(), packet.motionZ()));
	}

	private static void handleParticles(ParticlePacket packet, ClientPlayNetworking.Context context) {
		context.client().execute(() -> {
			for (ParticlePacket.QueuedParticle particle : packet.queuedParticles()) {
				context.player().level().addParticle(particle.particleOptions(), particle.x(), particle.y(), particle.z(), particle.x2(), particle.y2(), particle.z2());
			}
		});
	}

	private static void handleSetMasonJarItem(SetMasonJarItemPacket packet, ClientPlayNetworking.Context context) {
		context.client().execute(() -> {
			if (context.player().level().getBlockEntity(packet.pos()) instanceof MasonJarBlockEntity blockEntity) {
				blockEntity.getItemHandler().setItem(packet.stack());
				blockEntity.setItemRotation(packet.rotation());
				blockEntity.setChanged();
			}
		});
	}

	private static void handleSyncQuests(SyncQuestsPacket packet, ClientPlayNetworking.Context context) {
		context.client().execute(() -> QuestingRamCurrentContext.INSTANCE.setContext(packet.ram()));
	}

	private static void handleSyncUncraftingTableConfig(SyncUncraftingTableConfigPacket packet, ClientPlayNetworking.Context context) {
		context.client().execute(() -> {
			TFConfig.uncraftingXpCostMultiplier = packet.uncraftingMultiplier();
			TFConfig.repairingXpCostMultiplier = packet.repairingMultiplier();
			TFConfig.allowShapelessUncrafting = packet.allowShapeless();
			TFConfig.disableIngredientSwitching = packet.disableIngredientSwitching();
			TFConfig.disableUncraftingOnly = packet.disabledUncrafting();
			TFConfig.disableEntireTable = packet.disabledTable();
			TFConfig.disableUncraftingRecipes = packet.disabledRecipes();
			TFConfig.reverseRecipeBlacklist = packet.flipRecipeList();
			TFConfig.blacklistedUncraftingModIds = packet.disabledModids();
			TFConfig.flipUncraftingModIdList = packet.flipModidList();
		});
	}

	private static void handleUpdateDeathTime(UpdateDeathTimePacket packet, ClientPlayNetworking.Context context) {
		context.client().execute(() -> {
			Entity entity = context.player().level().getEntity(packet.entityID());
			if (entity instanceof LivingEntity living) {
				living.deathTime = packet.deathTime();
			}
		});
	}

	private static void handleUpdateFeatherFanFall(UpdateFeatherFanFallPacket packet, ClientPlayNetworking.Context context) {
		context.client().execute(() -> {
			Entity entity = context.player().level().getEntity(packet.entityID());
			if (entity instanceof Player) {
				TFDataAttachments.set(entity, TFDataAttachments.FEATHER_FAN, packet.falling());
			}
		});
	}

	private static void handleUpdateShield(UpdateShieldPacket packet, ClientPlayNetworking.Context context) {
		context.client().execute(() -> {
			Entity entity = context.player().level().getEntity(packet.entityID());
			if (entity instanceof LivingEntity living) {
				var attachment = TFDataAttachments.get(living, TFDataAttachments.FORTIFICATION_SHIELDS);
				attachment.setShields(living, packet.temporaryShields(), true);
				attachment.setShields(living, packet.permanentShields(), false);
			}
		});
	}

	private static void handleUpdateTFMultipart(UpdateTFMultipartPacket packet, ClientPlayNetworking.Context context) {
		context.client().execute(() -> {
			int entityId = packet.entity() != null && packet.entityId() <= 0 ? packet.entity().getId() : packet.entityId();
			Entity entity = context.player().level().getEntity(entityId);
			if (!(entity instanceof TFMultipartEntity multipart)) return;

			TFPart.assignPartIDs(entity);
			TFPart<?>[] parts = multipart.getParts();
			if (parts == null) return;

			for (TFPart<?> part : parts) {
				if (part == null) continue;

				if (packet.data() == null && packet.entity() instanceof TFMultipartEntity singleplayerMultipart) {
					Arrays.stream(singleplayerMultipart.getParts())
						.filter(singleplayerPart -> singleplayerPart.getId() == part.getId())
						.findFirst()
						.ifPresent(singleplayerPart -> part.readData(singleplayerPart.writeData()));
				} else if (packet.data() != null) {
					UpdateTFMultipartPacket.PartDataHolder data = packet.data().get(part.getId());
					if (data != null) {
						part.readData(data);
					}
				}
			}
		});
	}

	private static void handleUpdateThrown(UpdateThrownPacket packet, ClientPlayNetworking.Context context) {
		context.client().execute(() -> {
			Level level = context.player().level();
			Entity entity = level.getEntity(packet.entityID());
			if (entity instanceof Player player) {
				var attachment = TFDataAttachments.get(player, TFDataAttachments.YETI_THROWING);
				LivingEntity thrower = packet.thrower() != 0 ? (LivingEntity) level.getEntity(packet.thrower()) : null;
				attachment.setThrown(player, packet.thrown(), thrower);
				attachment.setThrowCooldown(player, packet.throwCooldown());
			}
		});
	}

	private static void handleUpdateUncraftingCost(UpdateUncraftingCostPacket packet, ClientPlayNetworking.Context context) {
		context.client().execute(() -> {
			AbstractContainerMenu container = context.player().containerMenu;
			if (container instanceof UncraftingMenu uncrafting) {
				uncrafting.updateCosts(packet.uncraftingCost(), packet.recraftingCost());
			}
		});
	}

	private static void handleAreaProtection(AreaProtectionPacket packet, ClientPlayNetworking.Context context) {
		context.client().execute(() -> {
			ClientLevel level = context.client().level;
			if (level == null) return;
			packet.boxes().forEach(box -> {
				for (Entity entity : level.entitiesForRendering()) {
					if (entity instanceof ProtectionBox prot && prot.lifeTime > 0 && prot.matches(box)) {
						prot.resetLifetime();
						return;
					}
				}

				level.addEntity(new ProtectionBox(level, box));
			});

			for (int i = 0; i < 20; i++) {
				double vx = level.getRandom().nextGaussian() * 0.02D;
				double vy = level.getRandom().nextGaussian() * 0.02D;
				double vz = level.getRandom().nextGaussian() * 0.02D;

				double x = packet.pos().getX() + 0.5D + level.getRandom().nextFloat() - level.getRandom().nextFloat();
				double y = packet.pos().getY() + 0.5D + level.getRandom().nextFloat() - level.getRandom().nextFloat();
				double z = packet.pos().getZ() + 0.5D + level.getRandom().nextFloat() - level.getRandom().nextFloat();

				level.addParticle(TFParticleType.PROTECTION, x, y, z, vx, vy, vz);
			}
		});
	}

	private static void handleMovingCicadaSound(CreateMovingCicadaSoundPacket packet, ClientPlayNetworking.Context context) {
		context.client().execute(() -> {
			Entity entity = context.player().level().getEntity(packet.entityID());
			if (entity instanceof LivingEntity living) {
				context.client().getSoundManager().queueTickingSound(new MovingCicadaSoundInstance(living));
			}
		});
	}

	private static void handleLifedrainParticles(LifedrainParticlePacket packet, ClientPlayNetworking.Context context) {
		context.client().execute(() -> {
			Entity entity = context.player().level().getEntity(packet.entityID());
			if (entity instanceof LivingEntity living) {
				ClientHandHelper.makeRedMagicTrail(living.level(), living, packet.victimPos());
			}
		});
	}

	private static void handleMagicMap(MagicMapPacket packet, ClientPlayNetworking.Context context) {
		context.client().execute(() -> {
			Level level = context.player().level();
			String name = MagicMapItem.getMapName(packet.inner().mapId().id());
			TFMagicMapData mapdata = TFMagicMapData.getMagicMapData(level, name);
			if (mapdata == null) {
				mapdata = new TFMagicMapData(0, 0, packet.inner().scale(), false, false, packet.inner().locked(), level.dimension());
				TFMagicMapData.registerMagicMapData(level, mapdata, name);
			}

			packet.inner().applyToMap(mapdata);
			mapdata.conqueredStructures.clear();
			mapdata.conqueredStructures.addAll(packet.conqueredStructures());
			context.client().getMapTextureManager().update(packet.inner().mapId(), mapdata);
		});
	}

	private static void handleMazeMap(MazeMapPacket packet, ClientPlayNetworking.Context context) {
		context.client().execute(() -> {
			Level level = context.player().level();
			String name = MazeMapItem.getMapName(packet.inner().mapId().id());
			TFMazeMapData mapdata = TFMazeMapData.getMazeMapData(level, name);
			if (mapdata == null) {
				mapdata = new TFMazeMapData(0, 0, packet.inner().scale(), false, false, packet.inner().locked(), level.dimension());
				TFMazeMapData.registerMazeMapData(level, mapdata, name);
			}

			mapdata.ore = packet.ore();
			mapdata.yCenter = packet.yCenter();
			packet.inner().applyToMap(mapdata);
			context.client().getMapTextureManager().update(packet.inner().mapId(), mapdata);
		});
	}

	private static void handleMissingAdvancementToast(MissingAdvancementToastPacket packet, ClientPlayNetworking.Context context) {
		context.client().execute(() -> context.client().gui.toastManager().addToast(new MissingAdvancementToast(packet.title(), packet.icon())));
	}

	private static void handleSpawnCharm(SpawnCharmPacket packet, ClientPlayNetworking.Context context) {
		context.client().execute(() -> {
			Player player = context.player();
			ClientLevel level = context.client().level;
			if (level == null) return;
			Entity camera = context.client().getCameraEntity();
			if (TFConfig.spawnCharmAnimationAsTotem) {
				context.client().gameRenderer.displayItemActivation(packet.charm());
				context.client().particleEngine.createTrackingEmitter(camera != null ? camera : player, new ItemParticleOption(ParticleTypes.ITEM, ItemStackTemplate.fromNonEmptyStack(packet.charm())), 20);
			} else {
				CharmEffect effect = new CharmEffect(TFEntities.CHARM_EFFECT, player.level(), player, packet.charm());
				effect.offset = (float) Math.PI;
				level.addEntity(effect);
			}
			SoundEvent event = BuiltInRegistries.SOUND_EVENT.getValue(packet.event());
			if (camera != null && event != null) {
				level.playLocalSound(camera.getX(), camera.getY(), camera.getZ(), event, player.getSoundSource(), 1.5F, 1.0F, false);
			}
		});
	}

	private static void handleSpawnFallenLeaf(SpawnFallenLeafFromPacket packet, ClientPlayNetworking.Context context) {
		context.client().execute(() -> {
			ClientLevel level = context.client().level;
			if (level == null) return;
			Random rand = new Random();
			int color = context.client().getBlockColors().getTintSource(Blocks.OAK_LEAVES.defaultBlockState(), 0).colorInWorld(Blocks.OAK_LEAVES.defaultBlockState(), level, packet.pos());
			int r = Mth.clamp(((color >> 16) & 0xFF) + rand.nextInt(0x22) - 0x11, 0x00, 0xFF);
			int g = Mth.clamp(((color >> 8) & 0xFF) + rand.nextInt(0x22) - 0x11, 0x00, 0xFF);
			int b = Mth.clamp((color & 0xFF) + rand.nextInt(0x22) - 0x11, 0x00, 0xFF);
			level.addParticle(new LeafParticleData(r, g, b),
				packet.pos().getX() + level.getRandom().nextFloat(),
				packet.pos().getY(),
				packet.pos().getZ() + level.getRandom().nextFloat(),
				(level.getRandom().nextFloat() * -0.5F) * packet.motion().x(),
				level.getRandom().nextFloat() * 0.5F + 0.25F,
				(level.getRandom().nextFloat() * -0.5F) * packet.motion().z()
			);
		});
	}

	private static void handleStructureProtection(StructureProtectionPacket packet, ClientPlayNetworking.Context context) {
		context.client().execute(() -> TFWeatherRenderer.setProtectedBoxes(packet.boxes().orElse(null)));
	}

	private static void handleBossBarAdd(TFBossBarPacket.AddTFBossBarPacket packet, ClientPlayNetworking.Context context) {
		context.client().execute(() -> {
			Minecraft minecraft = context.client();
			BossHealthOverlayAccessor overlay = (BossHealthOverlayAccessor) minecraft.gui.hud.getBossOverlay();
			overlay.twilightforest$getEvents().put(packet.id(), new ClientTFBossBar(packet.id(), packet.name(), packet.progress(), packet.color(), packet.overlay(), packet.darkenScreen(), packet.playMusic(), packet.createWorldFog()));
		});
	}

	private static void handleBossBarStyle(TFBossBarPacket.UpdateTFBossBarStylePacket packet, ClientPlayNetworking.Context context) {
		context.client().execute(() -> {
			Minecraft minecraft = context.client();
			BossHealthOverlayAccessor overlay = (BossHealthOverlayAccessor) minecraft.gui.hud.getBossOverlay();
			if (overlay.twilightforest$getEvents().get(packet.id()) instanceof ClientTFBossBar bossEvent) {
				bossEvent.setBarColor(packet.color());
				bossEvent.setOverlay(packet.overlay());
				if (!packet.allowLerp()) bossEvent.setSetTime(bossEvent.getSetTime() - 200L);
			}
		});
	}

}
