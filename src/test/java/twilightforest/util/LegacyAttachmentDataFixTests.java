package twilightforest.util;

import com.mojang.serialization.Codec;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import org.junit.jupiter.api.Test;
import twilightforest.components.entity.FortificationShieldAttachment;
import twilightforest.components.entity.GiantPickaxeMiningAttachment;
import twilightforest.components.entity.YetiThrowAttachment;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LegacyAttachmentDataFixTests {
	@Test
	void readsNeoForge26RootAndRetainsTravellerFieldNames() throws Exception {
		CompoundTag fixture = readFixture("/twilightforest/attachment/legacy-neoforge-attachments.snbt");
		CompoundTag attachments = LegacyAttachmentDataFix.readAttachmentData(
			input(fixture), LegacyAttachmentDataFix.FABRIC_ROOT, CompoundTag.CODEC).orElseThrow();

		assertEquals(18, attachments.size());
		assertTrue(attachments.getCompoundOrEmpty("twilightforest:feather_fan_falling").getBooleanOr("feather_fan_falling", false));
		assertTrue(attachments.getCompoundOrEmpty("twilightforest:has_double_jump").getBooleanOr("double_jump", false));
		assertEquals(2, attachments.getCompoundOrEmpty("twilightforest:double_jump_validator").getIntOr("double_jump_count", -1));
		assertEquals(0.625D, attachments.getCompoundOrEmpty("twilightforest:temporary_saved_straight_ahead").getDoubleOr("straight_ahead", -1.0D));
		assertEquals(987654321L, attachments.getCompoundOrEmpty("twilightforest:last_damage_armor_time").getLongOr("last_armor_damage_timestamp", -1L));
		assertEquals(0.875D, attachments.getCompoundOrEmpty("twilightforest:slimy_soles_bounce_info").getDoubleOr("bounce_velocity", -1.0D));
		CompoundTag preserved = attachments.getCompoundOrEmpty(LegacyAttachmentDataFix.LEGACY_FRAMEWORK_ATTACHMENT);
		assertEquals(17, preserved.getCompoundOrEmpty(LegacyAttachmentDataFix.NEOFORGE_ROOT).size());
	}

	@Test
	void normalizesPre2026NeoForgeScalarPayloadsAndRenamedIds() throws Exception {
		CompoundTag fixture = readFixture("/twilightforest/attachment/legacy-neoforge-raw-attachments.snbt");
		CompoundTag oldRoot = fixture.getCompoundOrEmpty(LegacyAttachmentDataFix.NEOFORGE_ROOT);
		CompoundTag normalized = LegacyAttachmentDataFix.normalizeNeoForgeAttachments(oldRoot, null);

		assertEquals(0.25D, normalized.getDoubleOr("twilightforest:temporary_saved_straight_ahead", -1.0D));
		assertTrue(normalized.getBooleanOr("twilightforest:is_gradually_gliding", false));
		assertTrue(normalized.getBooleanOr("twilightforest:has_double_jump", false));
		assertFalse(normalized.contains("twilightforest:temporary_saved_forward_boost"));
		assertFalse(normalized.contains("twilightforest:is_controlled_falling"));
		assertFalse(normalized.contains("twilightforest:travellers_goggles_item_display"));
		CompoundTag preserved = normalized.getCompoundOrEmpty(LegacyAttachmentDataFix.LEGACY_FRAMEWORK_ATTACHMENT)
			.getCompoundOrEmpty(LegacyAttachmentDataFix.NEOFORGE_ROOT);
		assertEquals(0.25D, preserved.getDoubleOr("twilightforest:temporary_saved_forward_boost", -1.0D));
		assertTrue(preserved.contains("twilightforest:travellers_goggles_item_display"));
	}

	@Test
	void wrappedScalarCodecReadsBothShapesAndWritesCurrentShape() {
		Codec<Boolean> codec = LegacyAttachmentDataFix.wrappedScalarCodec(Codec.BOOL, "value");
		CompoundTag current = new CompoundTag();
		current.putBoolean("value", true);

		assertTrue(codec.parse(NbtOps.INSTANCE, current).result().orElseThrow());
		assertTrue(codec.parse(NbtOps.INSTANCE, ByteTag.ONE).result().orElseThrow());
		Tag encoded = codec.encodeStart(NbtOps.INSTANCE, true).result().orElseThrow();
		assertTrue(encoded instanceof CompoundTag);
		assertTrue(((CompoundTag) encoded).getBooleanOr("value", false));
	}

	@Test
	void migratesForge1201CapabilityAndPersistentDataWithoutDroppingOriginal() throws Exception {
		CompoundTag fixture = readFixture("/twilightforest/attachment/legacy-forge-1.20.1-entity.snbt");
		CompoundTag normalized = LegacyAttachmentDataFix.normalizeForgeData(
			fixture.getCompoundOrEmpty(LegacyAttachmentDataFix.FORGE_CAPS_ROOT),
			fixture.getCompoundOrEmpty(LegacyAttachmentDataFix.FORGE_DATA_ROOT));

		FortificationShieldAttachment shields = FortificationShieldAttachment.CODEC
			.parse(NbtOps.INSTANCE, normalized.get("twilightforest:fortification_shields")).result().orElseThrow();
		assertEquals(3, shields.temporaryShieldsLeft());
		assertEquals(2, shields.permanentShieldsLeft());

		GiantPickaxeMiningAttachment giantPick = GiantPickaxeMiningAttachment.CODEC
			.parse(NbtOps.INSTANCE, normalized.get("twilightforest:giant_pickaxe_mining")).result().orElseThrow();
		assertEquals(123456789L, giantPick.getMining());
		assertTrue(giantPick.getBreaking());
		assertEquals(4, giantPick.getGiantBlockConversion());

		YetiThrowAttachment yetiThrow = YetiThrowAttachment.CODEC
			.parse(NbtOps.INSTANCE, normalized.get("twilightforest:yeti_throwing")).result().orElseThrow();
		assertTrue(yetiThrow.getThrown());
		assertEquals(199, yetiThrow.getThrowCooldown());
		assertTrue(normalized.getCompoundOrEmpty("twilightforest:feather_fan_falling").getBooleanOr("feather_fan_falling", false));
		assertTrue(normalized.getCompoundOrEmpty("twilightforest:charm_data").getBooleanOr("twilightforest:keep_inventory_charm", false));
		assertTrue(normalized.contains("twilightforest:twilightforest_banished"));
		assertTrue(normalized.getBooleanOr("twilightforest:ender_bow_arrow", false));

		CompoundTag preserved = normalized.getCompoundOrEmpty(LegacyAttachmentDataFix.LEGACY_FRAMEWORK_ATTACHMENT);
		assertEquals("preserved", preserved.getCompoundOrEmpty(LegacyAttachmentDataFix.FORGE_CAPS_ROOT)
			.getCompoundOrEmpty("example:unknown_provider").getStringOr("do_not_drop", ""));
		assertEquals(42, preserved.getCompoundOrEmpty(LegacyAttachmentDataFix.FORGE_DATA_ROOT).getIntOr("unrelated_value", -1));
	}

	@Test
	void neoForgeRootWinsAndOlderForgeRootsOnlyFillAbsentAttachments() {
		CompoundTag neoForge = new CompoundTag();
		CompoundTag neoCharmData = new CompoundTag();
		neoCharmData.putString("source", "neoforge");
		neoForge.put("twilightforest:charm_data", neoCharmData);
		neoForge.putBoolean("twilightforest:ender_bow_arrow", false);
		CompoundTag neoShields = new CompoundTag();
		neoShields.putInt("tempshields", 7);
		neoShields.putInt("permshields", 6);
		neoForge.put("twilightforest:fortification_shields", neoShields);

		CompoundTag forgeCaps = new CompoundTag();
		CompoundTag staleShields = new CompoundTag();
		staleShields.putInt("tempshields", 1);
		staleShields.putInt("permshields", 2);
		forgeCaps.put("twilightforest:cap_shield", staleShields);
		CompoundTag yetiThrow = new CompoundTag();
		yetiThrow.putBoolean("yetiThrown", true);
		forgeCaps.put("twilightforest:cap_thrown", yetiThrow);

		CompoundTag forgeData = new CompoundTag();
		CompoundTag stalePlayerPersisted = new CompoundTag();
		stalePlayerPersisted.putString("source", "forge");
		stalePlayerPersisted.putBoolean("twilightforest_banished", true);
		forgeData.put("PlayerPersisted", stalePlayerPersisted);
		forgeData.putBoolean("twilightforest:ender", true);

		CompoundTag entityData = new CompoundTag();
		entityData.put(LegacyAttachmentDataFix.NEOFORGE_ROOT, neoForge);
		entityData.put(LegacyAttachmentDataFix.FORGE_CAPS_ROOT, forgeCaps);
		entityData.put(LegacyAttachmentDataFix.FORGE_DATA_ROOT, forgeData);
		CompoundTag normalized = LegacyAttachmentDataFix.readAttachmentData(
			input(entityData), LegacyAttachmentDataFix.FABRIC_ROOT, CompoundTag.CODEC).orElseThrow();

		assertEquals("neoforge", normalized.getCompoundOrEmpty("twilightforest:charm_data").getStringOr("source", ""));
		assertFalse(normalized.getBooleanOr("twilightforest:ender_bow_arrow", true));
		assertEquals(7, normalized.getCompoundOrEmpty("twilightforest:fortification_shields").getIntOr("tempshields", -1));
		assertTrue(normalized.contains("twilightforest:yeti_throwing"), "ForgeCaps should fill an attachment absent from NeoForge");
		assertTrue(normalized.contains("twilightforest:twilightforest_banished"), "ForgeData should fill an attachment absent from NeoForge");

		CompoundTag preserved = normalized.getCompoundOrEmpty(LegacyAttachmentDataFix.LEGACY_FRAMEWORK_ATTACHMENT);
		assertEquals(neoForge, preserved.getCompoundOrEmpty(LegacyAttachmentDataFix.NEOFORGE_ROOT));
		assertEquals(forgeCaps, preserved.getCompoundOrEmpty(LegacyAttachmentDataFix.FORGE_CAPS_ROOT));
		assertEquals(forgeData, preserved.getCompoundOrEmpty(LegacyAttachmentDataFix.FORGE_DATA_ROOT));
	}

	@Test
	void currentFabricRootAlwaysTakesPrecedence() throws Exception {
		CompoundTag fixture = readFixture("/twilightforest/attachment/legacy-neoforge-attachments.snbt");
		CompoundTag forgeFixture = readFixture("/twilightforest/attachment/legacy-forge-1.20.1-entity.snbt");
		fixture.put(LegacyAttachmentDataFix.FORGE_CAPS_ROOT, forgeFixture.getCompoundOrEmpty(LegacyAttachmentDataFix.FORGE_CAPS_ROOT).copy());
		fixture.put(LegacyAttachmentDataFix.FORGE_DATA_ROOT, forgeFixture.getCompoundOrEmpty(LegacyAttachmentDataFix.FORGE_DATA_ROOT).copy());
		CompoundTag current = new CompoundTag();
		current.putString("winner", "fabric");
		fixture.put(LegacyAttachmentDataFix.FABRIC_ROOT, current);

		CompoundTag selected = LegacyAttachmentDataFix.readAttachmentData(
			input(fixture), LegacyAttachmentDataFix.FABRIC_ROOT, CompoundTag.CODEC).orElseThrow();
		assertEquals("fabric", selected.getStringOr("winner", ""));
		CompoundTag preserved = selected.getCompoundOrEmpty(LegacyAttachmentDataFix.LEGACY_FRAMEWORK_ATTACHMENT);
		assertEquals(fixture.get(LegacyAttachmentDataFix.NEOFORGE_ROOT), preserved.get(LegacyAttachmentDataFix.NEOFORGE_ROOT));
		assertEquals(fixture.get(LegacyAttachmentDataFix.FORGE_CAPS_ROOT), preserved.get(LegacyAttachmentDataFix.FORGE_CAPS_ROOT));
		assertEquals(fixture.get(LegacyAttachmentDataFix.FORGE_DATA_ROOT), preserved.get(LegacyAttachmentDataFix.FORGE_DATA_ROOT));
	}

	@Test
	void wrongTypeCurrentFabricRootFailsWithoutReadingValidStaleRoot() throws Exception {
		CompoundTag fixture = readFixture("/twilightforest/attachment/legacy-neoforge-attachments.snbt");
		fixture.putString(LegacyAttachmentDataFix.FABRIC_ROOT, "not-an-attachment-map");
		ProblemReporter.Collector problems = new ProblemReporter.Collector();

		CompoundTag isolated = LegacyAttachmentDataFix.readAttachmentData(
			input(fixture, problems), LegacyAttachmentDataFix.FABRIC_ROOT, CompoundTag.CODEC).orElseThrow();
		assertFalse(problems.isEmpty());
		assertFalse(isolated.contains("twilightforest:has_double_jump"), "stale NeoForge values must not become live data");
		CompoundTag preserved = isolated.getCompoundOrEmpty(LegacyAttachmentDataFix.LEGACY_FRAMEWORK_ATTACHMENT);
		assertEquals("not-an-attachment-map", preserved.getStringOr(LegacyAttachmentDataFix.FABRIC_ROOT, ""));
		assertEquals(fixture.get(LegacyAttachmentDataFix.NEOFORGE_ROOT), preserved.get(LegacyAttachmentDataFix.NEOFORGE_ROOT));
	}

	@Test
	void wrongTypeNeoForgeRootIsIsolatedWithoutFallingBackToForge() {
		CompoundTag entityData = new CompoundTag();
		entityData.putString(LegacyAttachmentDataFix.NEOFORGE_ROOT, "corrupt-newer-root");
		CompoundTag forgeData = new CompoundTag();
		CompoundTag playerPersisted = new CompoundTag();
		playerPersisted.putString("source", "stale-forge");
		forgeData.put("PlayerPersisted", playerPersisted);
		entityData.put(LegacyAttachmentDataFix.FORGE_DATA_ROOT, forgeData);

		CompoundTag isolated = LegacyAttachmentDataFix.readAttachmentData(
			input(entityData), LegacyAttachmentDataFix.FABRIC_ROOT, CompoundTag.CODEC).orElseThrow();

		assertFalse(isolated.contains("twilightforest:charm_data"), "malformed NeoForge presence must block stale Forge fallback");
		CompoundTag preserved = isolated.getCompoundOrEmpty(LegacyAttachmentDataFix.LEGACY_FRAMEWORK_ATTACHMENT);
		assertEquals("corrupt-newer-root", preserved.getStringOr(LegacyAttachmentDataFix.NEOFORGE_ROOT, ""));
		assertEquals(forgeData, preserved.getCompoundOrEmpty(LegacyAttachmentDataFix.FORGE_DATA_ROOT));
	}

	@Test
	void malformedInnerCurrentFabricRootFailsWithoutReadingValidStaleRoot() throws Exception {
		CompoundTag fixture = readFixture("/twilightforest/attachment/legacy-neoforge-attachments.snbt");
		CompoundTag current = new CompoundTag();
		current.putInt("winner", 1);
		fixture.put(LegacyAttachmentDataFix.FABRIC_ROOT, current);
		ProblemReporter.Collector problems = new ProblemReporter.Collector();

		assertTrue(LegacyAttachmentDataFix.readAttachmentData(
			input(fixture, problems), LegacyAttachmentDataFix.FABRIC_ROOT,
			Codec.STRING.fieldOf("winner").codec()).isEmpty());
		assertFalse(problems.isEmpty());
	}

	@Test
	void declaresRequiredGenericFabricCompatibilityMixin() throws IOException {
		String mixinConfig = readResource("/twilightforest.mixins.json");
		String attachmentSource = java.nio.file.Files.readString(java.nio.file.Path.of("src/main/java/twilightforest/init/TFDataAttachments.java"));
		String fixSource = java.nio.file.Files.readString(java.nio.file.Path.of("src/main/java/twilightforest/util/LegacyAttachmentDataFix.java"));

		assertTrue(mixinConfig.contains("AttachmentSerializingImplMixin"));
		assertTrue(attachmentSource.contains("LegacyAttachmentDataFix.wrappedScalarCodec"));
		assertTrue(attachmentSource.contains("LegacyAttachmentDataFix.unitCodec()"));
		assertTrue(fixSource.contains("ForgeCaps"));
		assertTrue(fixSource.contains("legacy_framework_data"));
	}

	private static ValueInput input(CompoundTag tag) {
		return input(tag, ProblemReporter.DISCARDING);
	}

	private static ValueInput input(CompoundTag tag, ProblemReporter problems) {
		return TagValueInput.create(problems, RegistryAccess.EMPTY, tag);
	}

	private static CompoundTag readFixture(String path) throws Exception {
		return TagParser.parseCompoundFully(readResource(path));
	}

	private static String readResource(String path) throws IOException {
		try (InputStream input = Objects.requireNonNull(LegacyAttachmentDataFixTests.class.getResourceAsStream(path))) {
			return new String(input.readAllBytes(), StandardCharsets.UTF_8);
		}
	}
}
