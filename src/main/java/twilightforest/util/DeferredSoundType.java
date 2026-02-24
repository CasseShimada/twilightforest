package twilightforest.util;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.SoundType;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Lazy SoundType that resolves SoundEvents after registries are ready.
 */
public class DeferredSoundType extends SoundType {
	private final Supplier<SoundEvent> breakSound;
	private final Supplier<SoundEvent> stepSound;
	private final Supplier<SoundEvent> placeSound;
	private final Supplier<SoundEvent> hitSound;
	private final Supplier<SoundEvent> fallSound;

	public DeferredSoundType(float volume, float pitch,
		Supplier<SoundEvent> breakSound,
		Supplier<SoundEvent> stepSound,
		Supplier<SoundEvent> placeSound,
		Supplier<SoundEvent> hitSound,
		Supplier<SoundEvent> fallSound
	) {
		super(volume, pitch, SoundEvents.STONE_BREAK, SoundEvents.STONE_STEP, SoundEvents.STONE_PLACE, SoundEvents.STONE_HIT, SoundEvents.STONE_FALL);
		this.breakSound = Objects.requireNonNull(breakSound, "breakSound");
		this.stepSound = Objects.requireNonNull(stepSound, "stepSound");
		this.placeSound = Objects.requireNonNull(placeSound, "placeSound");
		this.hitSound = Objects.requireNonNull(hitSound, "hitSound");
		this.fallSound = Objects.requireNonNull(fallSound, "fallSound");
	}

	@Override
	public SoundEvent getBreakSound() {
		return breakSound.get();
	}

	@Override
	public SoundEvent getStepSound() {
		return stepSound.get();
	}

	@Override
	public SoundEvent getPlaceSound() {
		return placeSound.get();
	}

	@Override
	public SoundEvent getHitSound() {
		return hitSound.get();
	}

	@Override
	public SoundEvent getFallSound() {
		return fallSound.get();
	}
}
