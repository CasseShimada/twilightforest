package twilightforest.entity.monster;

import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoyalZombiePersistenceTests {
	@Test
	void restoresLegacyBabyFlagOnlyWhenExactAgeIsMissing() {
		CompoundTag legacyBaby = new CompoundTag();
		legacyBaby.putBoolean("IsBaby", true);
		assertTrue(LoyalZombie.LegacyBabyState.shouldRestore(input(legacyBaby)));

		CompoundTag currentBaby = new CompoundTag();
		currentBaby.putInt("Age", -12000);
		currentBaby.putBoolean("IsBaby", true);
		assertFalse(LoyalZombie.LegacyBabyState.shouldRestore(input(currentBaby)));

		CompoundTag currentAdult = new CompoundTag();
		currentAdult.putInt("Age", 0);
		currentAdult.putBoolean("IsBaby", true);
		assertFalse(LoyalZombie.LegacyBabyState.shouldRestore(input(currentAdult)));
	}

	@Test
	void doesNotInventBabyStateForLegacyAdults() {
		CompoundTag legacyAdult = new CompoundTag();
		legacyAdult.putBoolean("IsBaby", false);
		assertFalse(LoyalZombie.LegacyBabyState.shouldRestore(input(legacyAdult)));
	}

	private static ValueInput input(CompoundTag tag) {
		return TagValueInput.create(ProblemReporter.DISCARDING, RegistryAccess.EMPTY, tag);
	}
}
