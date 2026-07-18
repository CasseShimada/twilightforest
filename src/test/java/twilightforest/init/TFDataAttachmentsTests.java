package twilightforest.init;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Test;
import twilightforest.components.entity.SlimySolesAttachment;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class TFDataAttachmentsTests {
	@Test
	void declaresEveryTravellersGearAttachmentUnderItsPersistentId() throws IOException {
		String source = Files.readString(Path.of("src/main/java/twilightforest/init/TFDataAttachments.java"));
		List<String> expected = List.of(
			"travellers_wings",
			"travellers_wings_anim",
			"is_using_goggles_zoom_modifier",
			"travellers_goggles_red_thread_vision",
			"last_tick_water_walking",
			"has_double_jump",
			"double_jump_validator",
			"double_jump_validator_last_check",
			"temporary_saved_straight_ahead",
			"last_damage_armor_time",
			"last_jump_key_press_time",
			"last_horizontal_impulse",
			"last_non_horizontal_impulse",
			"last_horizontal_walking_time",
			"sidestep_validator",
			"sidestep_validator_last_check",
			"is_gradually_gliding",
			"slimy_soles_bounce_info"
		);

		assertEquals(18, expected.size());
		expected.forEach(path -> assertTrue(source.contains("\"" + path + "\""), path));
	}

	@Test
	void declaresPersistenceAndSyncContracts() throws IOException {
		String source = Files.readString(Path.of("src/main/java/twilightforest/init/TFDataAttachments.java"));
		assertTrue(source.contains("LegacyAttachmentDataFix.wrappedScalarCodec(Codec.BOOL, \"zooming\"), ByteBufCodecs.BOOL"));
		assertTrue(source.contains("LegacyAttachmentDataFix.wrappedScalarCodec(Codec.BOOL, \"gliding\"), ByteBufCodecs.BOOL"));
		assertTrue(source.contains("SlimySolesAttachment.CODEC.codec()"));
		assertTrue(source.contains("syncWith(streamCodec, AttachmentSyncPredicate.all())"));
		assertTrue(source.contains("persistent(GiantPickaxeMiningAttachment.CODEC)"));
		assertTrue(source.contains("persistent(YetiThrowAttachment.CODEC)"));
	}

	@Test
	void charmDataIsPersistentAndCopiedAcrossPlayerDeath() throws IOException {
		String source = Files.readString(Path.of("src/main/java/twilightforest/init/TFDataAttachments.java"));
		Pattern contract = Pattern.compile(
			"CHARM_DATA\\s*=\\s*AttachmentRegistry\\.create\\(TwilightForestMod\\.prefix\\(\"charm_data\"\\),\\s*builder\\s*->\\s*"
				+ "builder\\.initializer\\(CompoundTag::new\\)\\.persistent\\(CompoundTag\\.CODEC\\)\\.copyOnDeath\\(\\)\\)",
			Pattern.DOTALL);

		assertTrue(contract.matcher(source).find(), "charm_data must keep its ID and codec and opt into copy-on-death");
	}

	@Test
	void legacyFrameworkBackupIsPersistentAndCopiedAcrossPlayerDeath() throws IOException {
		String source = Files.readString(Path.of("src/main/java/twilightforest/init/TFDataAttachments.java"));
		Pattern contract = Pattern.compile(
			"LEGACY_FRAMEWORK_DATA\\s*=\\s*AttachmentRegistry\\.create\\(TwilightForestMod\\.prefix\\(\"legacy_framework_data\"\\),\\s*builder\\s*->\\s*"
				+ "builder\\.initializer\\(CompoundTag::new\\)\\.persistent\\(CompoundTag\\.CODEC\\)\\.copyOnDeath\\(\\)\\)",
			Pattern.DOTALL);

		assertTrue(contract.matcher(source).find(), "legacy framework backups must survive the player death-copy lifecycle");
	}

	@Test
	void slimySolesCodecRetainsLegacyFieldNamesAndValues() {
		SlimySolesAttachment source = new SlimySolesAttachment(0.75D, 0.25D, true, true);
		JsonObject encoded = SlimySolesAttachment.CODEC.codec().encodeStart(JsonOps.INSTANCE, source).getOrThrow().getAsJsonObject();

		assertEquals(0.75D, encoded.get("bounce_velocity").getAsDouble());
		assertEquals(0.25D, encoded.get("double_jump_boost_velocity").getAsDouble());
		assertTrue(encoded.get("force_bounce").getAsBoolean());
		assertTrue(encoded.get("bounce").getAsBoolean());

		SlimySolesAttachment decoded = SlimySolesAttachment.CODEC.codec().parse(JsonOps.INSTANCE, encoded).getOrThrow();
		assertEquals(0.75D, decoded.bounceVelocity);
		assertEquals(0.25D, decoded.doubleJumpBoostVelocity);
		assertTrue(decoded.forceBounce);
		assertTrue(decoded.hasBounced);
	}
}
