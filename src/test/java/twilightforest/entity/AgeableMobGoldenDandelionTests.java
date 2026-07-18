package twilightforest.entity;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AgeableMobGoldenDandelionTests {
	@BeforeAll
	static void bindGoldenDandelionComponents() {
		var holder = Items.GOLDEN_DANDELION.builtInRegistryHolder();
		if (!holder.areComponentsBound()) {
			holder.bindComponents(DataComponentMap.EMPTY);
		}
	}

	@Test
	void vanillaInteractionLocksAndUnlocksABabyWithTheFullStateTransition() {
		Level level = serverLevel();
		TestAnimal animal = new TestAnimal(level);
		animal.setAge(-200);
		ItemStack dandelions = new ItemStack(Items.GOLDEN_DANDELION, 3);
		Player player = playerHolding(dandelions, false);

		assertSame(InteractionResult.SUCCESS, animal.mobInteract(player, InteractionHand.MAIN_HAND));
		assertTrue(animal.isAgeLocked());
		assertEquals(AgeableMob.BABY_START_AGE, animal.getAge(),
			"Vanilla lock data must reset a partially grown baby to its start age");
		assertEquals(AgeableMob.AGE_LOCK_COOLDOWN_TICKS, animal.ageLockParticleTimer());
		assertEquals(2, dandelions.getCount());
		assertTrue(animal.isPersistenceRequired(), "A successfully locked baby must become persistent");
		assertFalse(animal.canAgeUp(), "The vanilla age tick and feeding path must reject a locked baby");
		verify(level).playSound(isNull(), any(net.minecraft.core.BlockPos.class), org.mockito.ArgumentMatchers.eq(SoundEvents.GOLDEN_DANDELION_USE),
			org.mockito.ArgumentMatchers.eq(SoundSource.PLAYERS), org.mockito.ArgumentMatchers.eq(1.0F),
			org.mockito.ArgumentMatchers.eq(1.0F));

		assertSame(InteractionResult.PASS, animal.mobInteract(player, InteractionHand.MAIN_HAND),
			"The particle timer is also the vanilla interaction cooldown");
		assertEquals(2, dandelions.getCount(), "A cooldown-blocked interaction must not consume an item");
		assertTrue(animal.isAgeLocked());

		animal.clearAgeLockCooldown();
		animal.setAge(-1200);
		assertSame(InteractionResult.SUCCESS, animal.mobInteract(player, InteractionHand.MAIN_HAND));
		assertFalse(animal.isAgeLocked());
		assertEquals(AgeableMob.BABY_START_AGE, animal.getAge(),
			"Vanilla unlock data must also restart the baby's growth period");
		assertEquals(1, dandelions.getCount());
		assertTrue(animal.canAgeUp());
		verify(level).playSound(isNull(), any(net.minecraft.core.BlockPos.class), org.mockito.ArgumentMatchers.eq(SoundEvents.GOLDEN_DANDELION_UNUSE),
			org.mockito.ArgumentMatchers.eq(SoundSource.PLAYERS), org.mockito.ArgumentMatchers.eq(1.0F),
			org.mockito.ArgumentMatchers.eq(1.0F));
	}

	@Test
	void creativePlayerDoesNotConsumeTheGoldenDandelion() {
		TestAnimal animal = new TestAnimal(serverLevel());
		animal.setAge(-1);
		ItemStack dandelion = new ItemStack(Items.GOLDEN_DANDELION);

		assertSame(InteractionResult.SUCCESS,
			animal.mobInteract(playerHolding(dandelion, true), InteractionHand.MAIN_HAND));
		assertTrue(animal.isAgeLocked());
		assertEquals(1, dandelion.getCount());
	}

	@Test
	void vanillaGateRejectsEntitiesInTheCannotBeAgeLockedTag() {
		ItemStack dandelion = new ItemStack(Items.GOLDEN_DANDELION);
		Mob mob = mock(Mob.class);
		when(mob.is(EntityTypeTags.CANNOT_BE_AGE_LOCKED)).thenReturn(true);

		assertFalse(AgeableMob.canUseGoldenDandelion(dandelion, true, 0, mob));

		when(mob.is(EntityTypeTags.CANNOT_BE_AGE_LOCKED)).thenReturn(false);
		assertTrue(AgeableMob.canUseGoldenDandelion(dandelion, true, 0, mob));
	}

	@Test
	void adultPassesWithoutConsumptionOrStateChanges() {
		Level level = serverLevel();
		TestAnimal animal = new TestAnimal(level);
		ItemStack dandelion = new ItemStack(Items.GOLDEN_DANDELION);

		assertSame(InteractionResult.PASS,
			animal.mobInteract(playerHolding(dandelion, false), InteractionHand.MAIN_HAND));
		assertEquals(1, dandelion.getCount());
		assertEquals(0, animal.getAge());
		assertFalse(animal.isAgeLocked());
		assertEquals(0, animal.ageLockParticleTimer());
		assertFalse(animal.isPersistenceRequired());
		verify(level, never()).playSound(isNull(), any(net.minecraft.core.BlockPos.class),
			any(net.minecraft.sounds.SoundEvent.class), any(SoundSource.class), anyFloat(), anyFloat());
	}

	@Test
	void ageLockIsWrittenAndRestoredByVanillaPersistence() {
		TestAnimal animal = new TestAnimal(serverLevel());
		animal.setAge(AgeableMob.BABY_START_AGE);
		animal.setAgeLockedForTest(true);
		ValueOutput output = mock(ValueOutput.class, RETURNS_DEEP_STUBS);

		animal.writeAdditionalData(output);

		verify(output).putInt("Age", AgeableMob.BABY_START_AGE);
		verify(output).putBoolean("AgeLocked", true);

		CompoundTag savedAgeData = new CompoundTag();
		savedAgeData.putInt("Age", AgeableMob.BABY_START_AGE);
		savedAgeData.putInt("ForcedAge", 0);
		savedAgeData.putBoolean("AgeLocked", true);
		ValueInput input = TagValueInput.create(ProblemReporter.DISCARDING, RegistryAccess.EMPTY, savedAgeData);
		TestAnimal restored = new TestAnimal(serverLevel());

		restored.readAdditionalData(input);

		assertEquals(AgeableMob.BABY_START_AGE, restored.getAge());
		assertTrue(restored.isAgeLocked());
		assertFalse(restored.canAgeUp());
	}

	private static Level serverLevel() {
		Level level = mock(Level.class);
		when(level.getNextEntityId()).thenReturn(1);
		when(level.isClientSide()).thenReturn(false);
		return level;
	}

	private static Player playerHolding(ItemStack stack, boolean infiniteMaterials) {
		Player player = mock(Player.class);
		when(player.getItemInHand(InteractionHand.MAIN_HAND)).thenReturn(stack);
		when(player.hasInfiniteMaterials()).thenReturn(infiniteMaterials);
		return player;
	}

	private static final class TestAnimal extends Animal {
		private TestAnimal(Level level) {
			super(EntityTypes.COW, level);
		}

		@Override
		public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
			return null;
		}

		@Override
		public boolean isFood(ItemStack stack) {
			return false;
		}

		private int ageLockParticleTimer() {
			return this.ageLockParticleTimer;
		}

		private void clearAgeLockCooldown() {
			this.ageLockParticleTimer = 0;
		}

		private void setAgeLockedForTest(boolean locked) {
			this.setAgeLocked(locked);
		}

		private void writeAdditionalData(ValueOutput output) {
			this.addAdditionalSaveData(output);
		}

		private void readAdditionalData(ValueInput input) {
			this.readAdditionalSaveData(input);
		}
	}
}
