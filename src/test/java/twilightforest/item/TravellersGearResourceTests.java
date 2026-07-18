package twilightforest.item;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TravellersGearResourceTests {
	private static final Path ASSETS = Path.of("src/generated/resources/assets/twilightforest");
	private static final Path MAIN_ASSETS = Path.of("src/main/resources/assets/twilightforest");
	private static final Path FABRIC_ASSETS = Path.of("src/generated/fabric/assets/twilightforest");
	private static final Path FABRIC_DATA = Path.of("src/generated/fabric/data/twilightforest");
	private static final Path DATA = Path.of("src/generated/resources/data/twilightforest");
	private static final List<String> ITEMS = List.of(
		"travellers_goggles", "travellers_vest", "travellers_gloves",
		"travellers_wings", "travellers_belt", "travellers_boots"
	);
	private static final Map<String, String> MODIFIER_DIRECTORIES = Map.of(
		"travellers_goggles", "goggles",
		"travellers_vest", "vest",
		"travellers_wings", "wings",
		"travellers_boots", "boots"
	);

	@Test
	void keepsEveryPersistentItemIdAndDefaultComponentDeclaration() throws IOException {
		String itemSource = Files.readString(Path.of("src/main/java/twilightforest/init/TFItems.java"));
		String armorSource = Files.readString(Path.of("src/main/java/twilightforest/item/travellers_gear/TravellersArmorItem.java"));
		String beltSource = Files.readString(Path.of("src/main/java/twilightforest/item/travellers_gear/TravellersArmorBeltItem.java"));
		String allSources = itemSource + armorSource + beltSource;
		Map<String, String> componentMarkers = Map.of(
			"travellers_goggles", "ZOOM_ABILITY_MODIFIER",
			"travellers_vest", "TRAVELLERS_HAS_CHESTPLATE",
			"travellers_gloves", "TRAVELLERS_HAS_GLOVES",
			"travellers_wings", "TRAVELLERS_HAS_WINGS",
			"travellers_belt", "TRAVELLERS_HAS_BELT",
			"travellers_boots", "TRAVELLERS_HAS_BOOTS"
		);

		for (String id : ITEMS) {
			assertTrue(itemSource.contains("register(\"" + id + "\""), "Missing persistent item ID " + id);
			assertTrue(allSources.contains(componentMarkers.get(id)), id + " is missing its historical default component");
		}
	}

	@Test
	void everyItemHasACompleteVanillaOrFabricItemModel() throws IOException {
		JsonObject language = read(ASSETS.resolve("lang/en_us.json"));
		for (String id : ITEMS) {
			assertTrue(language.has("item.twilightforest." + id), id + " is missing its English fallback name");
			assertTrue(Files.isRegularFile(MAIN_ASSETS.resolve("textures/item/" + id + ".png")), id + " texture is missing");

			JsonObject definition = read(ASSETS.resolve("items/" + id + ".json"));
			JsonObject itemModel = definition.getAsJsonObject("model");
			JsonObject model = read(ASSETS.resolve("models/item/" + id + ".json"));
			assertEquals("minecraft:item/generated", model.get("parent").getAsString());
			assertEquals("twilightforest:item/" + id, model.getAsJsonObject("textures").get("layer0").getAsString());

			if (MODIFIER_DIRECTORIES.containsKey(id)) {
				String directory = MODIFIER_DIRECTORIES.get(id);
				assertEquals("twilightforest:travellers_gear", itemModel.get("type").getAsString());
				assertEquals("travellers_modifiers/" + directory + "/", itemModel.get("modifier_directory").getAsString());
				assertEquals("travellers_modifiers/" + directory + "/broken/", itemModel.get("broken_modifier_directory").getAsString());
				assertEquals("twilightforest:item/" + id, itemModel.getAsJsonObject("base_model").get("model").getAsString());
				assertEquals("twilightforest:item/" + id + "_broken", itemModel.getAsJsonObject("broken_model").get("model").getAsString());
				assertTrue(Files.isRegularFile(MAIN_ASSETS.resolve("textures/item/" + id + "_broken.png")));
				read(ASSETS.resolve("models/item/" + id + "_broken.json"));
			} else {
				assertEquals("minecraft:model", itemModel.get("type").getAsString());
				assertEquals("twilightforest:item/" + id, itemModel.get("model").getAsString());
			}
		}
	}

	@Test
	void modifierRegistryDataContainsAllHistoricalEntriesAndTypes() throws IOException {
		Path modifiers = FABRIC_DATA.resolve("twilight/travellers_modifiers");
		Set<String> expected = Set.of(
			"agile_ranger", "all_night_goggles", "aquatic_agility", "arrow_magnetism", "auto_repair",
			"double_jump", "efficient_eater", "gradual_glide", "haste", "high_jump", "item_display",
			"perfect_dodge", "red_thread_vision", "side_step", "slimy_soles", "stealth", "step_up",
			"straight_ahead", "swap_hotbar", "swap_hotbar_ability", "swift_swim", "unrestrained",
			"water_walk", "zoom"
		);
		Set<String> allowedTypes = Set.of(
			"twilightforest:attribute", "twilightforest:builtin",
			"twilightforest:component", "twilightforest:transferable_component"
		);
		try (Stream<Path> files = Files.list(modifiers)) {
			List<Path> jsonFiles = files.filter(path -> path.getFileName().toString().endsWith(".json")).toList();
			assertEquals(expected.size(), jsonFiles.size());
			for (Path path : jsonFiles) {
				String id = path.getFileName().toString().replace(".json", "");
				assertTrue(expected.contains(id), "Unexpected modifier ID " + id);
				assertTrue(allowedTypes.contains(read(path).get("type").getAsString()), "Unexpected modifier type for " + id);
			}
		}
	}

	@Test
	void soundEventsKeepTheirIdsSourcesAndSubtitles() throws IOException {
		List<String> events = List.of(
			"item.twilightforest.travellers_gear.cycle_maps",
			"item.twilightforest.travellers_gear.cycle_maps_empty",
			"item.twilightforest.travellers_gear.double_jump",
			"item.twilightforest.travellers_gear.perfect_dodge",
			"item.twilightforest.travellers_gear.side_step",
			"item.twilightforest.travellers_gear.side_step_ready",
			"item.twilightforest.travellers_gear.swap_hotbar",
			"item.twilightforest.travellers_goggles.zoom_in",
			"item.twilightforest.travellers_goggles.zoom_out"
		);
		JsonObject sounds = read(ASSETS.resolve("sounds.json"));
		JsonObject language = read(ASSETS.resolve("lang/en_us.json"));
		String soundSource = Files.readString(Path.of("src/main/java/twilightforest/init/TFSounds.java"));
		for (String event : events) {
			assertTrue(sounds.has(event), "Missing sound definition " + event);
			assertTrue(soundSource.contains("createEvent(\"" + event + "\")"), "Missing registered sound event " + event);
			String subtitle = sounds.getAsJsonObject(event).get("subtitle").getAsString();
			assertTrue(language.has(subtitle), "Missing subtitle " + subtitle);
		}

		for (String sound : List.of("double_jump", "perfect_dodge", "side_step", "side_step_ready")) {
			assertTrue(Files.isRegularFile(MAIN_ASSETS.resolve("sounds/random/travellers/" + sound + ".ogg")));
		}
	}

	@Test
	void restoresTravellerParticleIdsResourcesAndFabricProviders() throws IOException {
		String particleRegistry = Files.readString(Path.of("src/main/java/twilightforest/init/TFParticleType.java"));
		String clientRegistration = Files.readString(Path.of("src/client/java/twilightforest/event/RegistrationEvents.java"));
		String generator = Files.readString(Path.of("src/datagen/java/twilightforest/datagen/assets/ParticleGenerator.java"));
		for (String id : List.of("double_jump", "perfect_dodge")) {
			assertTrue(particleRegistry.contains("register(\"" + id + "\""), "Missing particle ID " + id);
			JsonObject description = read(FABRIC_ASSETS.resolve("particles/" + id + ".json"));
			assertEquals(8, description.getAsJsonArray("textures").size());
			assertEquals("minecraft:generic_7", description.getAsJsonArray("textures").get(0).getAsString());
			assertEquals("minecraft:generic_0", description.getAsJsonArray("textures").get(7).getAsString());
		}
		assertTrue(clientRegistration.contains("TFParticleType.DOUBLE_JUMP, DoubleJumpParticle.Provider::new"));
		assertTrue(clientRegistration.contains("TFParticleType.PERFECT_DODGE, PerfectDodgeParticle.Provider::new"));
		assertTrue(generator.contains("TFParticleType.DOUBLE_JUMP"));
		assertTrue(generator.contains("TFParticleType.PERFECT_DODGE"));
	}

	@Test
	void restoresTypedPayloadIdsDirectionsAndKeyMappings() throws IOException {
		String networking = Files.readString(Path.of("src/main/java/twilightforest/network/TFNetworking.java"));
		String clientNetworking = Files.readString(Path.of("src/client/java/twilightforest/network/TFClientNetworking.java"));
		Map<String, String> payloads = Map.of(
			"CycleMapSlotPacket", "cycle_map_slot_packet",
			"GogglesZoomPacket", "goggles_zoom_packet",
			"GradualGlidePacket", "gradual_glide_packet",
			"PerformDoubleJumpPacket", "perform_double_jump_packet",
			"PerformSidestepPacket", "perform_sidestep_packet",
			"SwapHotbarPacket", "swap_hotbar",
			"TravellersWingsStatePacket", "travellers_wings_state"
		);
		for (Map.Entry<String, String> payload : payloads.entrySet()) {
			String source = Files.readString(Path.of("src/main/java/twilightforest/network/" + payload.getKey() + ".java"));
			assertTrue(source.contains("prefix(\"" + payload.getValue() + "\")"), "Missing payload ID " + payload.getValue());
			assertTrue(networking.contains("register(" + payload.getKey() + ".TYPE"), "Payload is not registered: " + payload.getKey());
		}
		for (String serverbound : List.of("CycleMapSlotPacket", "GogglesZoomPacket", "GradualGlidePacket", "PerformDoubleJumpPacket", "PerformSidestepPacket", "SwapHotbarPacket")) {
			assertTrue(networking.contains("serverboundPlay().register(" + serverbound + ".TYPE"), "Missing serverbound registration " + serverbound);
			assertTrue(networking.contains("registerGlobalReceiver(" + serverbound + ".TYPE"), "Missing server receiver " + serverbound);
		}
		for (String clientbound : List.of("GogglesZoomPacket", "GradualGlidePacket", "TravellersWingsStatePacket")) {
			assertTrue(networking.contains("clientboundPlay().register(" + clientbound + ".TYPE"), "Missing clientbound registration " + clientbound);
			assertTrue(clientNetworking.contains("registerGlobalReceiver(" + clientbound + ".TYPE"), "Missing client receiver " + clientbound);
		}
		assertTrue(networking.contains("context.player()"));
		assertTrue(networking.contains("player.getUUID().equals(packet.playerUUID())"));

		String keys = Files.readString(Path.of("src/client/java/twilightforest/init/TFKeyBinds.java"));
		JsonObject language = read(ASSETS.resolve("lang/en_us.json"));
		for (String key : List.of("red_thread_vision", "item_display_map_cycle", "zoom", "swap_hotbar")) {
			assertTrue(keys.contains("\"" + key + "\""));
			assertTrue(language.has("key.twilightforest." + key));
		}
		assertTrue(keys.contains("KeyMappingHelper.registerKeyMapping"));
	}

	@Test
	void waterWalkAndEfficientEaterHaveReachableRuntimeHooks() throws IOException {
		String livingMixin = Files.readString(Path.of("src/main/java/twilightforest/mixin/LivingEntityMixin.java")).replaceAll("\\s+", "");
		String serverPlayerMixin = Files.readString(Path.of("src/main/java/twilightforest/mixin/ServerPlayerMixin.java")).replaceAll("\\s+", "");
		String mixinConfig = Files.readString(Path.of("src/main/resources/twilightforest.mixins.json"));
		String logic = Files.readString(Path.of("src/main/java/twilightforest/item/travellers_gear/TravellersGearLogic.java")).replaceAll("\\s+", "");

		assertTrue(livingMixin.contains("method=\"canStandOnFluid\",at=@At(\"RETURN\"),cancellable=true"));
		assertTrue(livingMixin.contains("TravellersGearLogic.canStandOnWater(living,fluidState,cir.getReturnValueZ())"));
		assertTrue(logic.contains("TravellersModifiersManager.WATER_WALK_MODIFIER"));
		assertTrue(logic.contains("returnexhaustion/divisor;"));
		assertTrue(serverPlayerMixin.contains("method={\"checkMovementStatistics\",\"jumpFromGround\"}"));
		assertTrue(serverPlayerMixin.contains("target=\"Lnet/minecraft/server/level/ServerPlayer;causeFoodExhaustion(F)V\""));
		assertTrue(mixinConfig.contains("\"ServerPlayerMixin\""));
	}

	@Test
	void unrestrainedNeutralizesBlockMovementPenaltiesAcrossServerAndClientPaths() throws IOException {
		String entityMixin = Files.readString(Path.of("src/main/java/twilightforest/mixin/EntityMixin.java")).replaceAll("\\s+", "");
		String livingMixin = Files.readString(Path.of("src/main/java/twilightforest/mixin/LivingEntityMixin.java")).replaceAll("\\s+", "");
		String slimeMixin = Files.readString(Path.of("src/main/java/twilightforest/mixin/SlimeBlockMixin.java")).replaceAll("\\s+", "");
		String localPlayerMixin = Files.readString(Path.of("src/client/java/twilightforest/mixin/client/LocalPlayerMixin.java")).replaceAll("\\s+", "");
		String mixinConfig = Files.readString(Path.of("src/main/resources/twilightforest.mixins.json"));

		assertTrue(entityMixin.contains("method={\"getBlockJumpFactor\",\"getBlockSpeedFactor\"}"));
		assertTrue(entityMixin.contains("cir.setReturnValue(1.0F);"));
		assertTrue(livingMixin.contains("target=\"Lnet/minecraft/world/level/block/Block;getFriction()F\""));
		assertTrue(livingMixin.contains("?0.6F:original"));
		assertTrue(slimeMixin.contains("method=\"stepOn\""));
		assertTrue(slimeMixin.contains("original||TravellersModifiersManager.isModifierActive"));
		assertTrue(localPlayerMixin.contains("method=\"isSprintingPossible\""));
		assertTrue(localPlayerMixin.contains("isInShallowWater()Z"));
		assertTrue(mixinConfig.contains("\"SlimeBlockMixin\""));
	}

	@Test
	void wiresTravellerClientInputAndCameraHooksToFabric() throws IOException {
		String clientEvents = Files.readString(Path.of("src/client/java/twilightforest/event/TravellersClientEvents.java"));
		String clientInitializer = Files.readString(Path.of("src/client/java/twilightforest/client/TwilightForestClient.java"));
		String keyboardMixin = Files.readString(Path.of("src/client/java/twilightforest/mixin/client/KeyboardInputMixin.java"));
		String cameraMixin = Files.readString(Path.of("src/client/java/twilightforest/mixin/client/GameRendererMixin.java"));
		String mouseMixin = Files.readString(Path.of("src/client/java/twilightforest/mixin/client/MouseHandlerMixin.java"));
		String fovMixin = Files.readString(Path.of("src/client/java/twilightforest/mixin/client/AbstractClientPlayerMixin.java"));
		String mixinConfig = Files.readString(Path.of("src/main/resources/twilightforest.mixins.json"));

		assertTrue(clientInitializer.contains("TravellersClientEvents.register()"));
		assertTrue(clientEvents.contains("ClientTickEvents.END_CLIENT_TICK.register"));
		for (String packet : List.of("CycleMapSlotPacket", "GogglesZoomPacket", "GradualGlidePacket", "PerformDoubleJumpPacket", "PerformSidestepPacket", "SwapHotbarPacket")) {
			assertTrue(clientEvents.contains("ClientPlayNetworking.send(" + (packet.startsWith("PerformSidestep") || packet.startsWith("GogglesZoom") || packet.startsWith("GradualGlide") ? "new " : "") + packet),
				"Missing client send path for " + packet);
		}
		assertTrue(keyboardMixin.contains("@Mixin(KeyboardInput.class)"));
		assertTrue(keyboardMixin.contains("TravellersClientEvents.modifyMovementInput"));
		assertTrue(cameraMixin.contains("@Mixin(Camera.class)"));
		assertTrue(cameraMixin.contains("method = \"calculateFov\""));
		assertTrue(mouseMixin.contains("method = \"turnPlayer\""));
		assertTrue(fovMixin.contains("method = \"getFieldOfViewModifier\""));
		assertTrue(fovMixin.contains("TFAttributeModifiers.STRAIGHT_AHEAD_ATTRIBUTE_MODIFIER_LOCATION"));
		for (String mixin : List.of("client.AbstractClientPlayerMixin", "client.GameRendererMixin", "client.KeyboardInputMixin", "client.MouseHandlerMixin", "client.accessor.ClientInputAccessor")) {
			assertTrue(mixinConfig.contains("\"" + mixin + "\""), "Missing required client mixin " + mixin);
		}
		assertTrue(!clientEvents.contains("net.neoforged"));
		assertTrue(!clientEvents.contains("net.minecraftforge"));
	}

	@Test
	void wiresTravellerDurabilityMenusAndMobRulesToRequiredMixins() throws IOException {
		String events = Files.readString(Path.of("src/main/java/twilightforest/events/TravellersGearEvents.java"));
		String livingMixin = Files.readString(Path.of("src/main/java/twilightforest/mixin/LivingEntityMixin.java"));
		String itemStackMixin = Files.readString(Path.of("src/main/java/twilightforest/mixin/ItemStackMixin.java"));
		String anvilMixin = Files.readString(Path.of("src/main/java/twilightforest/mixin/AnvilMenuMixin.java"));
		String grindstoneMixin = Files.readString(Path.of("src/main/java/twilightforest/mixin/GrindstoneMenuMixin.java"));
		String resultSlotMixin = Files.readString(Path.of("src/main/java/twilightforest/mixin/GrindstoneResultSlotMixin.java"));
		String phantomMixin = Files.readString(Path.of("src/main/java/twilightforest/mixin/PhantomSpawnerMixin.java"));
		String enderManMixin = Files.readString(Path.of("src/main/java/twilightforest/mixin/EnderManMixin.java"));
		String mixinConfig = Files.readString(Path.of("src/main/resources/twilightforest.mixins.json"));

		assertTrue(livingMixin.contains("method = \"doHurtEquipment\""));
		assertTrue(livingMixin.contains("TravellersGearEvents.damageArmor"));
		assertTrue(itemStackMixin.contains("ItemAttributeModifiers;forEach"));
		assertTrue(itemStackMixin.contains("TravellersGearEvents.activeAttributeModifiers"));
		assertTrue(anvilMixin.contains("method = \"createResult\""));
		assertTrue(grindstoneMixin.contains("method = \"computeResult\""));
		assertTrue(resultSlotMixin.contains("GrindstoneMenu$4"));
		assertTrue(resultSlotMixin.contains("returnGrindstoneContents"));
		assertTrue(phantomMixin.contains("ServerPlayer;isSpectator()Z"));
		assertTrue(phantomMixin.contains("ALL_NIGHT_GOGGLES_MODIFIER"));
		assertTrue(enderManMixin.contains("method = \"isBeingStaredBy\""));
		assertTrue(enderManMixin.contains("ALL_NIGHT_GOGGLES_MODIFIER"));
		assertTrue(events.contains("STORED_BROKEN_ATTRIBUTES"));
		assertTrue(events.contains("LAST_DAMAGE_ARMOR_TIME"));
		assertTrue(events.contains("nonEmptyItemCopyStream"));
		assertTrue(events.contains("ItemDisplayContents::items"));

		for (String mixin : List.of(
			"AnvilMenuMixin", "EnderManMixin", "GrindstoneMenuMixin", "GrindstoneResultSlotMixin",
			"ItemStackMixin", "LivingEntityMixin", "PhantomSpawnerMixin"
		)) {
			assertTrue(mixinConfig.contains("\"" + mixin + "\""), "Missing required common mixin " + mixin);
		}
		String commonSources = events + livingMixin + itemStackMixin + anvilMixin + grindstoneMixin + resultSlotMixin + phantomMixin + enderManMixin;
		assertTrue(!commonSources.contains("net.neoforged"));
		assertTrue(!commonSources.contains("net.minecraftforge"));
	}

	@Test
	void restoresPowderSnowWalkingAndPiglinNeutralityWithVanillaData() throws IOException {
		JsonObject powderSnow = read(FABRIC_DATA.resolve("tags/item/powder_snow_walkable_boots.json"));
		Set<String> powderSnowBoots = powderSnow.getAsJsonArray("values").asList().stream()
			.map(element -> element.getAsString())
			.collect(java.util.stream.Collectors.toSet());
		assertEquals(Set.of(
			"twilightforest:arctic_boots",
			"twilightforest:yeti_boots",
			"twilightforest:travellers_boots"
		), powderSnowBoots);

		JsonObject piglinSafeArmor = read(Path.of("src/generated/fabric/data/minecraft/tags/item/piglin_safe_armor.json"));
		Set<String> piglinSafeItems = piglinSafeArmor.getAsJsonArray("values").asList().stream()
			.map(element -> element.getAsString())
			.collect(java.util.stream.Collectors.toSet());
		assertEquals(Set.of("twilightforest:travellers_goggles", "twilightforest:travellers_wings"), piglinSafeItems);

		String powderSnowMixin = Files.readString(Path.of("src/main/java/twilightforest/mixin/PowderSnowBlockMixin.java"));
		String mixinConfig = Files.readString(Path.of("src/main/resources/twilightforest.mixins.json"));
		assertTrue(powderSnowMixin.contains("@Mixin(PowderSnowBlock.class)"));
		assertTrue(powderSnowMixin.contains("method = \"canEntityWalkOnPowderSnow\""));
		assertTrue(powderSnowMixin.contains("TFItemTags.POWDER_SNOW_WALKABLE_BOOTS"));
		assertTrue(mixinConfig.contains("\"PowderSnowBlockMixin\""));
		assertTrue(!powderSnowMixin.contains("net.neoforged"));
		assertTrue(!powderSnowMixin.contains("net.minecraftforge"));
	}

	@Test
	void keepsShiftDescriptionsAndKeybindInterpolationClientOnly() throws IOException {
		String armorItem = Files.readString(Path.of("src/main/java/twilightforest/item/travellers_gear/TravellersArmorItem.java"));
		String tooltipContext = Files.readString(Path.of("src/main/java/twilightforest/item/travellers_gear/modifiers/TravellersTooltipContext.java"));
		String manager = Files.readString(Path.of("src/main/java/twilightforest/init/custom/TravellersModifiersManager.java"));
		String interpolator = Files.readString(Path.of("src/client/java/twilightforest/item/travellers_gear/modifiers/TooltipStringInterpolator.java"));
		String clientEntry = Files.readString(Path.of("src/client/java/twilightforest/client/TwilightForestClient.java"));

		assertTrue(armorItem.contains("TravellersTooltipContext.hasShiftDown()"));
		assertTrue(armorItem.contains("travellers_gear.shift_info"));
		assertTrue(armorItem.contains("Stream.concat(abilities.stream(), insertable.stream())"));
		assertTrue(manager.contains("TravellersTooltipContext.translate"));
		assertTrue(tooltipContext.contains("BooleanSupplier"));
		assertTrue(!tooltipContext.contains("net.minecraft.client"));
		assertTrue(interpolator.contains("tfkeybinds"));
		assertTrue(interpolator.contains("KeyMapping.get(keyName)"));
		assertTrue(interpolator.contains("getTranslatedKeyMessage"));
		assertTrue(clientEntry.contains("InputConstants.KEY_LSHIFT"));
		assertTrue(clientEntry.contains("InputConstants.KEY_RSHIFT"));
		assertTrue(clientEntry.contains("TooltipStringInterpolator::render"));
	}

	@Test
	void wiresTravellerArmorModelsAnimationAndFirstPersonGlovesToFabric() throws IOException {
		String layers = Files.readString(Path.of("src/client/java/twilightforest/model/TFModelLayers.java"));
		String registration = Files.readString(Path.of("src/client/java/twilightforest/event/RegistrationEvents.java"));
		String renderer = Files.readString(Path.of("src/client/java/twilightforest/renderer/TravellersArmorRenderer.java"));
		String wingsModel = Files.readString(Path.of("src/client/java/twilightforest/model/armor/TravellersWingsModel.java"));
		String avatarMixin = Files.readString(Path.of("src/client/java/twilightforest/mixin/client/AvatarRendererMixin.java"));
		String humanoidMixin = Files.readString(Path.of("src/client/java/twilightforest/mixin/client/HumanoidMobRendererMixin.java"));
		String mixinConfig = Files.readString(Path.of("src/main/resources/twilightforest.mixins.json"));

		for (String layer : List.of("HELMET", "CHEST_GLOVES", "CHEST_GLOVES_SLIM", "LEGGINGS", "BOOTS")) {
			assertTrue(layers.contains("TRAVELLERS_ARMOR_" + layer), "Missing Traveller model layer " + layer);
			assertTrue(registration.contains("TFModelLayers.TRAVELLERS_ARMOR_" + layer), "Unregistered Traveller model layer " + layer);
		}
		for (String item : List.of("GOGGLES", "VEST", "GLOVES", "WINGS", "BELT", "BOOTS")) {
			assertTrue(registration.contains("TFItems.TRAVELLERS_" + item), "Traveller armor renderer missing " + item);
		}
		assertTrue(registration.contains("ArmorRenderer.register(TravellersArmorRenderer.INSTANCE"));
		assertTrue(renderer.contains("EquipmentLayerRenderer"));
		assertTrue(renderer.contains("TRAVELLERS_HAS_CHESTPLATE"));
		assertTrue(renderer.contains("TRAVELLERS_HAS_GLOVES"));
		assertTrue(renderer.contains("TRAVELLERS_HAS_WINGS"));
		assertTrue(renderer.contains("TRAVELLERS_HAS_BELT"));
		assertTrue(renderer.contains("firstPersonGloveOverlay"));
		assertTrue(wingsModel.contains("TravellersWingsAttachment.WingState.SIDESTEP"));
		assertTrue(wingsModel.contains("RenderStateDataKey<AnimationState>"));
		assertTrue(humanoidMixin.contains("@Mixin(HumanoidMobRenderer.class)"));
		assertTrue(humanoidMixin.contains("method = \"extractHumanoidRenderState\""));
		assertTrue(humanoidMixin.contains("TravellersArmorRenderer.extractRenderState"));
		assertTrue(avatarMixin.contains("@Mixin(AvatarRenderer.class)"));
		assertTrue(avatarMixin.contains("method = \"renderRightHand\""));
		assertTrue(avatarMixin.contains("method = \"renderLeftHand\""));
		assertTrue(mixinConfig.contains("\"client.AvatarRendererMixin\""));
		assertTrue(mixinConfig.contains("\"client.HumanoidMobRendererMixin\""));

		Path legacyArmor = MAIN_ASSETS.resolve("textures/models/armor");
		Path equipment = MAIN_ASSETS.resolve("textures/entity/equipment");
		assertEquals(-1L, Files.mismatch(legacyArmor.resolve("travellers_layer_1.png"), equipment.resolve("humanoid/travellers.png")));
		assertEquals(-1L, Files.mismatch(legacyArmor.resolve("travellers_layer_1_down.png"), equipment.resolve("humanoid/travellers_down.png")));
		assertEquals(-1L, Files.mismatch(legacyArmor.resolve("travellers_layer_2.png"), equipment.resolve("humanoid_leggings/travellers.png")));

		JsonObject normalAsset = read(FABRIC_ASSETS.resolve("equipment/travellers_gear.json"));
		JsonObject downAsset = read(FABRIC_ASSETS.resolve("equipment/travellers_gear_down.json"));
		assertTrue(normalAsset.toString().contains("twilightforest:travellers"));
		assertTrue(downAsset.toString().contains("twilightforest:travellers_down"));

		String clientSources = registration + renderer + wingsModel + avatarMixin + humanoidMixin;
		assertTrue(!clientSources.contains("net.neoforged"));
		assertTrue(!clientSources.contains("net.minecraftforge"));
	}

	@Test
	void wiresItemDisplayHudAndTooltipsWithoutClientTypesInCommonCode() throws IOException {
		String clientEntry = Files.readString(Path.of("src/client/java/twilightforest/client/TwilightForestClient.java"));
		String overlayEvents = Files.readString(Path.of("src/client/java/twilightforest/event/OverlayHandler.java"));
		String rendererRegistry = Files.readString(Path.of("src/client/java/twilightforest/client/overlay/display/ItemDisplayRenderers.java"));
		String simpleTextDisplay = Files.readString(Path.of("src/client/java/twilightforest/client/overlay/display/SimpleTextDisplay.java"));
		String clockDisplay = Files.readString(Path.of("src/client/java/twilightforest/client/overlay/display/ClockDisplay.java"));
		String moonDialDisplay = Files.readString(Path.of("src/client/java/twilightforest/client/overlay/display/MoonDialDisplay.java"));
		String tooltipRegistration = Files.readString(Path.of("src/client/java/twilightforest/event/RegistrationEvents.java"));
		String beltTooltip = Files.readString(Path.of("src/client/java/twilightforest/client/renderer/TravellersBeltTooltipComponent.java"));

		assertTrue(clientEntry.contains("ItemDisplayRenderers.init()"));
		assertTrue(overlayEvents.contains("HudElementRegistry.attachElementAfter"));
		assertTrue(overlayEvents.contains("prefix(\"item_display_overlay\")"));
		assertTrue(overlayEvents.contains("ItemDisplayOverlay.render"));
		assertTrue(rendererRegistry.contains("ItemDisplayContents.MAP_ID, MapDisplay::new"));
		assertTrue(rendererRegistry.contains("ItemDisplayContents.COMPASS_ID, CompassDisplay::new"));
		assertTrue(rendererRegistry.contains("ItemDisplayContents.CLOCK_ID, ClockDisplay::new"));
		assertTrue(rendererRegistry.contains("ItemDisplayContents.MOON_DIAL_ID, MoonDialDisplay::new"));
		assertTrue(simpleTextDisplay.contains("0xFFFFFFFF"));
		assertTrue(clockDisplay.contains("0xFFFFFFFF"));
		assertTrue(moonDialDisplay.contains("0xFFFFFFFF"));
		assertTrue(tooltipRegistration.contains("new ItemDisplayTooltipComponent(tooltip)"));
		assertTrue(tooltipRegistration.contains("new TravellersBeltTooltipComponent(tooltip)"));
		assertTrue(beltTooltip.contains("container.copyInto(stacks)"));

		for (String path : List.of(
			"src/main/java/twilightforest/components/item/ItemDisplayContents.java",
			"src/main/java/twilightforest/init/custom/ItemDisplays.java",
			"src/main/java/twilightforest/item/travellers_gear/modifiers/display/ItemDisplayType.java"
		)) {
			String commonSource = Files.readString(Path.of(path));
			assertFalse(commonSource.contains("twilightforest.client"), path + " references a client-only type");
		}
	}

	@Test
	void restoresEveryHistoricalRecipeSerializerAndResourceId() throws IOException {
		String recipeRegistry = Files.readString(Path.of("src/main/java/twilightforest/init/TFRecipes.java"));
		for (String serializer : List.of(
			"travellers_gear_modifier_shaped_recipe",
			"travellers_gear_modifier_shapeless_recipe",
			"travellers_vest_gloves_merge_recipe"
		)) {
			assertTrue(recipeRegistry.contains("registerSerializer(\"" + serializer + "\""), "Missing serializer " + serializer);
		}

		Path recipeRoot = DATA.resolve("recipe");
		Path advancementRoot = DATA.resolve("advancement");
		try (Stream<Path> paths = Stream.concat(
			Files.walk(recipeRoot),
			Files.walk(advancementRoot)
		)) {
			List<Path> resources = paths
				.filter(Files::isRegularFile)
				.filter(path -> path.toString().contains("traveller"))
				.toList();
			assertEquals(41, resources.size());
			assertEquals(33, resources.stream().filter(path -> path.startsWith(recipeRoot)).count());
			assertEquals(8, resources.stream().filter(path -> path.startsWith(advancementRoot)).count());
		}

		for (String item : ITEMS) {
			JsonObject recipe = read(DATA.resolve("recipe/equipment/" + item + ".json"));
			assertEquals("twilightforest:" + item, recipe.getAsJsonObject("result").get("id").getAsString());
			read(DATA.resolve("advancement/recipes/transportation/equipment/" + item + ".json"));
		}
	}

	@Test
	void modifierRecipesUseFabricIngredientsAndKeepEveryModifierKey() throws IOException {
		Path root = DATA.resolve("recipe/add_modifier_to_travellers_gear");
		Set<String> expectedModifiers = Set.of(
			"agile_ranger", "all_night_goggles", "aquatic_agility", "arrow_magnetism", "auto_repair",
			"double_jump", "efficient_eater", "gradual_glide", "haste", "item_display", "perfect_dodge",
			"red_thread_vision", "side_step", "slimy_soles", "stealth", "straight_ahead", "swap_hotbar",
			"unrestrained", "water_walk"
		);
		Set<String> foundModifiers;
		try (Stream<Path> paths = Files.walk(root)) {
			List<Path> recipes = paths.filter(Files::isRegularFile).toList();
			assertEquals(26, recipes.size());
			foundModifiers = recipes.stream().map(path -> {
				try {
					JsonObject recipe = read(path);
					String type = recipe.get("type").getAsString();
					assertTrue(type.equals("twilightforest:travellers_gear_modifier_shaped_recipe")
						|| type.equals("twilightforest:travellers_gear_modifier_shapeless_recipe"));
					assertTrue(!recipe.toString().contains("neoforge:"), "NeoForge ingredient remains in " + path);
					return recipe.get("modifier_key").getAsString().replace("twilightforest:", "");
				} catch (IOException exception) {
					throw new java.io.UncheckedIOException(exception);
				}
			}).collect(java.util.stream.Collectors.toSet());
		}
		assertEquals(expectedModifiers, foundModifiers);

		for (String recipe : List.of(
			"travellers_goggles/aquatic_agility_modifier.json",
			"travellers_vest/stealth_modifier.json",
			"travellers_wings/agile_ranger_modifier.json",
			"travellers_wings/double_jump_modifier.json",
			"travellers_boots/straight_ahead_modifier.json"
		)) {
			String json = read(root.resolve(recipe)).toString();
			assertTrue(json.contains("fabric:components"), recipe + " lost its exact potion component predicate");
			assertTrue(json.contains("fabric:any"), recipe + " lost its allowed potion alternatives");
		}

		String belt = read(DATA.resolve("recipe/equipment/travellers_belt.json")).toString();
		assertTrue(belt.contains("fabric:difference"));
		assertTrue(belt.contains("#c:chests/wooden"));
		assertTrue(belt.contains("#c:chests/trapped"));
	}

	@Test
	void vestGlovesMergeAndModifierAdvancementKeepTheirComponentContract() throws IOException {
		JsonObject merge = read(DATA.resolve("recipe/travellers_vest_gloves_merge_recipe.json"));
		assertEquals("twilightforest:travellers_vest_gloves_merge_recipe", merge.get("type").getAsString());
		String mergeSource = Files.readString(Path.of("src/main/java/twilightforest/item/recipe/travellers/TravellersVestGlovesMergeRecipe.java"));
		assertTrue(mergeSource.contains("TRAVELLERS_HAS_GLOVES"));
		assertTrue(mergeSource.contains("TRAVELLERS_VEST"));
		assertTrue(mergeSource.contains("TRAVELLERS_GLOVES"));

		JsonObject craftAdvancement = read(DATA.resolve("advancement/craft_travellers_gear.json"));
		assertTrue(craftAdvancement.toString().contains("twilightforest:equipment/travellers_vest"));
		assertTrue(!craftAdvancement.toString().contains("twilightforest:equipment/travellers_chest"));
		JsonObject modifierAdvancement = read(DATA.resolve("advancement/modify_travellers_gear.json"));
		assertTrue(modifierAdvancement.toString().contains("twilightforest:add_modifier"));

		String triggers = Files.readString(Path.of("src/main/java/twilightforest/init/TFAdvancements.java"));
		String craftingEvent = Files.readString(Path.of("src/main/java/twilightforest/events/EntityEvents.java"));
		assertTrue(triggers.contains("register(\"add_modifier\""));
		assertTrue(craftingEvent.contains("ADD_MODIFIER.trigger"));
		assertTrue(craftingEvent.contains("findAllInsertableModifiers"));
	}

	private static JsonObject read(Path path) throws IOException {
		assertTrue(Files.isRegularFile(path), "Missing resource: " + path);
		try (Reader reader = Files.newBufferedReader(path)) {
			return JsonParser.parseReader(reader).getAsJsonObject();
		}
	}
}
