package twilightforest.block.entity;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import twilightforest.components.item.JarLid;
import twilightforest.test.MinecraftBootstrapExtension;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MinecraftBootstrapExtension.class)
class JarBlockEntityCompatibilityTests {
	@Test
	void missingBlockEntityLidRetainsTheCanonicalBlockSpecificFallback() {
		Item blockSpecificFallback = Items.JUNGLE_LOG;

		assertSame(blockSpecificFallback, JarBlockEntity.readLid(input(new CompoundTag()), blockSpecificFallback));
	}

	@Test
	void validCurrentBlockEntityLidTakesPrecedenceOverTheCanonicalFallback() {
		CompoundTag tag = new CompoundTag();
		tag.putString(JarBlockEntity.TAG_LID, "minecraft:birch_log");

		assertSame(Items.BIRCH_LOG, JarBlockEntity.readLid(input(tag), Items.JUNGLE_LOG));
	}

	@Test
	void malformedCurrentBlockEntityLidFailsWithoutReplacingTheDamageWithAFallback() {
		CompoundTag tag = new CompoundTag();
		tag.putInt(JarBlockEntity.TAG_LID, 42);

		IllegalStateException failure = assertThrows(IllegalStateException.class,
			() -> JarBlockEntity.readLid(input(tag), Items.JUNGLE_LOG));
		assertTrue(failure.getMessage().startsWith("Malformed jar lid 'lid':"));
	}

	@Test
	void missingItemComponentRetainsTheCanonicalBlockSpecificFallback() {
		Item blockSpecificFallback = Items.JUNGLE_LOG;

		assertSame(blockSpecificFallback, JarBlockEntity.readLid((JarLid) null, blockSpecificFallback));
	}

	@Test
	void validCurrentItemComponentTakesPrecedenceOverTheCanonicalFallback() {
		assertSame(Items.BIRCH_LOG,
			JarBlockEntity.readLid(new JarLid(Items.BIRCH_LOG), Items.JUNGLE_LOG));
	}

	private static ValueInput input(CompoundTag tag) {
		return TagValueInput.create(ProblemReporter.DISCARDING,
			RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY), tag);
	}
}
