package twilightforest.block.entity;

import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.impl.transfer.item.ItemVariantImpl;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity.WobbleStyle;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import twilightforest.components.item.SkullCandles;
import twilightforest.test.MinecraftBootstrapExtension;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MinecraftBootstrapExtension.class)
class BlockEntityPersistenceTests {
	private static final UUID LEGACY_CASKET_OWNER = UUID.fromString("12345678-1234-5678-9abc-def012345678");

	@BeforeAll
	static void bindFixtureItemComponents() {
		// 26.2 normally binds item prototypes during server resource loading, which plain JUnit does not run.
		DataComponentMap fixtureDefaults = DataComponentMap.builder()
			.set(DataComponents.MAX_STACK_SIZE, 64)
			.build();
		for (var item : new net.minecraft.world.item.Item[] {Items.GLOWSTONE, Items.EMERALD, Items.DIAMOND}) {
			if (!item.builtInRegistryHolder().areComponentsBound()) {
				item.builtInRegistryHolder().bindComponents(fixtureDefaults);
			}
		}
	}

	@Test
	void beanstalkReadsForgeCurrentAndSnakeCasePersistenceKeysWithoutResettingGrowth() throws Exception {
		CompoundTag fixture = readFixture();

		assertEquals(new GrowingBeanstalkBlockEntity.PersistentState(false, 147, 61, 0.3125F, 0.1875F, 192, 9),
			GrowingBeanstalkBlockEntity.readPersistentState(input(fixture.getCompoundOrEmpty("beanstalk_forge_1_20_1"))));
		assertEquals(new GrowingBeanstalkBlockEntity.PersistentState(true, 151, 42, 0.25F, 0.375F, 205, 3),
			GrowingBeanstalkBlockEntity.readPersistentState(input(fixture.getCompoundOrEmpty("beanstalk_current_keys_take_precedence"))));
		assertEquals(new GrowingBeanstalkBlockEntity.PersistentState(false, 164, 73, 0.15625F, 0.21875F, 226, 11),
			GrowingBeanstalkBlockEntity.readPersistentState(input(fixture.getCompoundOrEmpty("beanstalk_26_1_aliases"))));
	}

	@Test
	void beanstalkMissingKeysUseSafeHistoricalGrowthDefaults() {
		assertEquals(new GrowingBeanstalkBlockEntity.PersistentState(true, 0, 100, 0.1F, 0.1F, 175, 0),
			GrowingBeanstalkBlockEntity.readPersistentState(input(new CompoundTag())));
	}

	@Test
	void skullCandleReadsCurrentInfoBeforeLegacyFieldsAndPreservesLegacyAmount() throws Exception {
		CompoundTag fixture = readFixture();

		assertEquals(new SkullCandles(14, 4), SkullCandleBlockEntity.readCandleInfo(
			input(fixture.getCompoundOrEmpty("skull_candle_forge_1_20_1")), 1));
		assertEquals(new SkullCandles(5, 3), SkullCandleBlockEntity.readCandleInfo(
			input(fixture.getCompoundOrEmpty("skull_candle_current")), 1));
		assertEquals(new SkullCandles(7, 2), SkullCandleBlockEntity.readCandleInfo(
			input(fixture.getCompoundOrEmpty("skull_candle_color_only")), 2));
	}

	@Test
	void skullCandleCountIsSanitizedAndCurrentCodecWritesInfoShape() {
		CompoundTag invalidLegacy = new CompoundTag();
		invalidLegacy.putInt("CandleColor", 9);
		invalidLegacy.putInt("CandleAmount", 12);
		assertEquals(new SkullCandles(9, 4), SkullCandleBlockEntity.readCandleInfo(input(invalidLegacy), 1));

		Tag encoded = SkullCandles.CODEC.encodeStart(NbtOps.INSTANCE, new SkullCandles(11, 3)).getOrThrow();
		assertTrue(encoded instanceof CompoundTag);
		CompoundTag info = (CompoundTag) encoded;
		assertEquals(11, info.getIntOr("color", -1));
		assertEquals(3, info.getIntOr("count", -1));
	}

	@Test
	void masonJarReadsNeoForgeStackListAndPrefersCurrentItemKey() throws Exception {
		CompoundTag fixture = readFixture();
		ItemStack legacy = MasonJarBlockEntity.readStoredItem(
			input(fixture.getCompoundOrEmpty("mason_jar_neoforge_26_1")));
		assertTrue(legacy.is(Items.GLOWSTONE));
		assertEquals(23, legacy.getCount());

		ItemStack current = MasonJarBlockEntity.readStoredItem(
			input(fixture.getCompoundOrEmpty("mason_jar_current_keys_take_precedence")));
		assertTrue(current.is(Items.EMERALD));
		assertEquals(5, current.getCount());
	}

	@Test
	void masonJarRejectsLegacyExtraSlotsInsteadOfSilentlyDeletingItems() throws Exception {
		CompoundTag fixture = readFixture();
		assertThrows(IllegalStateException.class, () -> MasonJarBlockEntity.readStoredItem(
			input(fixture.getCompoundOrEmpty("mason_jar_invalid_extra_slot"))));
	}

	@Test
	void masonJarStorageOnlyNotifiesAfterTheOuterTransactionCommits() {
		MasonJarBlockEntity jar = mock(MasonJarBlockEntity.class);
		MasonJarBlockEntity.MasonJarItemStackHandler storage = new MasonJarBlockEntity.MasonJarItemStackHandler(jar);
		// ItemVariant.of uses a Fabric Loader Mixin cache on Item; plain JUnit does not apply that Mixin.
		ItemVariant glowstone = new ItemVariantImpl(Items.GLOWSTONE, DataComponentPatch.EMPTY);

		try (Transaction transaction = Transaction.openOuter()) {
			assertEquals(4, storage.insert(glowstone, 4, transaction));
			transaction.abort();
		}
		assertTrue(storage.isEmpty());
		verifyNoInteractions(jar);

		try (Transaction transaction = Transaction.openOuter()) {
			assertEquals(4, storage.insert(glowstone, 4, transaction));
			transaction.commit();
		}
		assertEquals(4, storage.getItem().getCount());
		verify(jar).wobble(WobbleStyle.POSITIVE);
		verify(jar).setChanged();

		clearInvocations(jar);
		try (Transaction transaction = Transaction.openOuter()) {
			assertEquals(2, storage.extract(glowstone, 2, transaction));
			transaction.abort();
		}
		assertEquals(4, storage.getItem().getCount());
		verifyNoInteractions(jar);

		try (Transaction transaction = Transaction.openOuter()) {
			assertEquals(4, storage.extract(glowstone, 4, transaction));
			transaction.commit();
		}
		assertTrue(storage.isEmpty());
		verify(jar).wobble(WobbleStyle.NEGATIVE);
		verify(jar).setChanged();
	}

	@Test
	void carminiteReactorPersistsItsExactCountdownAndBurstCenters() throws Exception {
		CompoundTag fixture = readFixture();
		CarminiteReactorBlockEntity.PersistentState fallback = new CarminiteReactorBlockEntity.PersistentState(
			0, 3, 3, 3, -3, -3, -3);
		assertEquals(new CarminiteReactorBlockEntity.PersistentState(217, 3, -3, 3, -3, 3, -3),
			CarminiteReactorBlockEntity.readPersistentState(input(fixture.getCompoundOrEmpty("carminite_reactor_current")), fallback));
		assertEquals(fallback, CarminiteReactorBlockEntity.readPersistentState(input(new CompoundTag()), fallback));
		assertThrows(IllegalStateException.class, () -> CarminiteReactorBlockEntity.readPersistentState(
			input(fixture.getCompoundOrEmpty("carminite_reactor_invalid_duplicate_burst")), fallback));
		CompoundTag incomplete = new CompoundTag();
		incomplete.putInt("counter", 100);
		assertThrows(IllegalStateException.class,
			() -> CarminiteReactorBlockEntity.readPersistentState(input(incomplete), fallback));
	}

	@Test
	void reactorDebrisReadsHistoricalTimeAliveAndPrefersCurrentKey() throws Exception {
		CompoundTag fixture = readFixture();
		assertEquals((byte) 42, ReactorDebrisBlockEntity.readTimeAlive(
			input(fixture.getCompoundOrEmpty("reactor_debris_2024")), (byte) 0));
		assertEquals((byte) 17, ReactorDebrisBlockEntity.readTimeAlive(
			input(fixture.getCompoundOrEmpty("reactor_debris_current_keys_take_precedence")), (byte) 0));
	}

	@Test
	void keepsakeCasketImportsLegacyUuidWithoutLockingNameOnlyCaskets() throws Exception {
		CompoundTag fixture = readFixture();
		ResolvableProfile legacy = KeepsakeCasketBlockEntity.readCompatibleOwner(
			input(fixture.getCompoundOrEmpty("keepsake_casket_forge_1_20_1")));
		assertNotNull(legacy);
		assertEquals(LEGACY_CASKET_OWNER, legacy.partialProfile().id());
		assertEquals("LegacyOwner", legacy.name().orElseThrow());

		assertNull(KeepsakeCasketBlockEntity.readCompatibleOwner(
			input(fixture.getCompoundOrEmpty("keepsake_casket_unlocked_name_only"))));
		ResolvableProfile uuidOnly = KeepsakeCasketBlockEntity.readCompatibleOwner(
			input(fixture.getCompoundOrEmpty("keepsake_casket_uuid_only")));
		assertNotNull(uuidOnly);
		assertTrue(SkullChestBlockEntity.profileMatches(
			new GameProfile(LEGACY_CASKET_OWNER, "CurrentUntruncatedName"), uuidOnly));
		assertFalse(SkullChestBlockEntity.profileMatches(
			new GameProfile(UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"), "LegacyOwner"), uuidOnly));
	}

	@Test
	void keepsakeCasketPrefersCurrentOwnerAndDoesNotMaskMalformedCurrentData() throws Exception {
		CompoundTag legacy = readFixture().getCompoundOrEmpty("keepsake_casket_forge_1_20_1").copy();
		UUID currentId = UUID.fromString("fedcba98-7654-3210-ffff-eeee11112222");
		ResolvableProfile current = ResolvableProfile.createResolved(new GameProfile(currentId, "CurrentOwner"));
		legacy.put("owner", ResolvableProfile.CODEC.encodeStart(NbtOps.INSTANCE, current).getOrThrow());
		ResolvableProfile decoded = KeepsakeCasketBlockEntity.readCompatibleOwner(input(legacy));
		assertNotNull(decoded);
		assertEquals(currentId, decoded.partialProfile().id());

		CompoundTag malformed = readFixture().getCompoundOrEmpty("keepsake_casket_forge_1_20_1").copy();
		CompoundTag malformedOwner = new CompoundTag();
		malformedOwner.putInt("name", 4);
		malformed.put("owner", malformedOwner);
		assertNull(KeepsakeCasketBlockEntity.readCompatibleOwner(input(malformed)));
	}

	@Test
	void keepsakeCasketDualWritesLegacyOwnerKeys() {
		ResolvableProfile owner = ResolvableProfile.createResolved(new GameProfile(LEGACY_CASKET_OWNER, "LegacyOwner"));
		TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING,
			RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));
		KeepsakeCasketBlockEntity.writeLegacyOwner(output, owner);
		CompoundTag result = output.buildResult();

		assertEquals(LEGACY_CASKET_OWNER,
			UUIDUtil.CODEC.parse(NbtOps.INSTANCE, result.get(KeepsakeCasketBlockEntity.LEGACY_OWNER_UUID_TAG)).getOrThrow());
		assertEquals("LegacyOwner", result.getStringOr(KeepsakeCasketBlockEntity.LEGACY_OWNER_NAME_TAG, ""));
	}

	private static ValueInput input(CompoundTag tag) {
		return TagValueInput.create(ProblemReporter.DISCARDING,
			RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY), tag);
	}

	private static CompoundTag readFixture() throws Exception {
		return TagParser.parseCompoundFully(readResource());
	}

	private static String readResource() throws IOException {
		try (InputStream input = Objects.requireNonNull(BlockEntityPersistenceTests.class.getResourceAsStream(
			"/twilightforest/block/entity/legacy-block-entity-persistence.snbt"))) {
			return new String(input.readAllBytes(), StandardCharsets.UTF_8);
		}
	}
}
