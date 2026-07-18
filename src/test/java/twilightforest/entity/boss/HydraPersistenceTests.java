package twilightforest.entity.boss;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HydraPersistenceTests {
	@Test
	void legacyHeadCountsExpandToCanonicalLeadingHeadMasks() throws Exception {
		CompoundTag fixture = TagParser.parseCompoundFully(readFixture());

		assertEquals(0b0001111, decode(fixture.getCompoundOrEmpty("legacy_four_heads")));
		assertEquals(0b1111111, decode(fixture.getCompoundOrEmpty("legacy_seven_heads")));
	}

	@Test
	void currentSparseMasksRemainExactAndDoNotKeepConstructorDefaults() throws Exception {
		CompoundTag fixture = TagParser.parseCompoundFully(readFixture());

		assertEquals(0b1001001, decode(fixture.getCompoundOrEmpty("current_sparse_mask")));
		assertEquals(0b0000111, decode(fixture.getCompoundOrEmpty("current_low_mask")));
	}

	private static int decode(CompoundTag data) {
		return Hydra.decodeSavedHeadMask(data.getByteOr("NumHeads", (byte) 0), data.contains("HeadNames"));
	}

	private static String readFixture() throws IOException {
		try (InputStream input = Objects.requireNonNull(HydraPersistenceTests.class.getResourceAsStream(
			"/twilightforest/entity/boss/legacy-hydra-heads.snbt"))) {
			return new String(input.readAllBytes(), StandardCharsets.UTF_8);
		}
	}
}
