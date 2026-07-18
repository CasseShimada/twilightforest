package twilightforest.entity.boss;

import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.entity.EntityReference;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LichPersistenceTests {
	private static final Set<UUID> CLONES = Set.of(
		UUID.fromString("00112233-4455-6677-8899-aabbccddeeff"),
		UUID.fromString("fedcba98-7654-3210-0123-456789abcdef"));

	@Test
	void readsBothHistoricalSummonedCloneShapesAndWritesTheLatestNestedShape() throws Exception {
		CompoundTag fixture = TagParser.parseCompoundFully(readFixture());

		assertEquals(CLONES, Lich.COMPATIBLE_SUMMONED_CLONES_CODEC
			.parse(NbtOps.INSTANCE, fixture.get("direct_list")).getOrThrow());
		assertEquals(CLONES, Lich.COMPATIBLE_SUMMONED_CLONES_CODEC
			.parse(NbtOps.INSTANCE, fixture.get("nested_set")).getOrThrow());

		Tag encoded = Lich.CURRENT_SUMMONED_CLONES_CODEC.encodeStart(NbtOps.INSTANCE, CLONES).getOrThrow();
		assertTrue(encoded instanceof CompoundTag);
		CompoundTag encodedCompound = (CompoundTag) encoded;
		assertTrue(encodedCompound.contains("UUIDs"));
		assertEquals(CLONES, UUIDUtil.CODEC_SET.parse(NbtOps.INSTANCE, encodedCompound.get("UUIDs")).getOrThrow());
	}

	@Test
	void entityReferenceMasterCodecRemainsByteCompatibleWithTheHistoricalUuidCodec() {
		UUID master = UUID.fromString("00112233-4455-6677-8899-aabbccddeeff");
		Tag uuid = UUIDUtil.CODEC.encodeStart(NbtOps.INSTANCE, master).getOrThrow();
		Tag reference = EntityReference.codec().encodeStart(NbtOps.INSTANCE, EntityReference.of(master)).getOrThrow();

		assertEquals(uuid, reference);
	}

	private static String readFixture() throws IOException {
		try (InputStream input = Objects.requireNonNull(LichPersistenceTests.class.getResourceAsStream(
			"/twilightforest/entity/boss/legacy-lich-clones.snbt"))) {
			return new String(input.readAllBytes(), StandardCharsets.UTF_8);
		}
	}
}
