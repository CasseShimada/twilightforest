package twilightforest.components.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public final class SlimySolesAttachment {
	// The fields remain optional because older releases wrote incomplete or incorrectly named data.
	public static final MapCodec<SlimySolesAttachment> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		Codec.DOUBLE.optionalFieldOf("bounce_velocity", 0.0D).forGetter(value -> value.bounceVelocity),
		Codec.DOUBLE.optionalFieldOf("double_jump_boost_velocity", 0.0D).forGetter(value -> value.doubleJumpBoostVelocity),
		Codec.BOOL.optionalFieldOf("force_bounce", false).forGetter(value -> value.forceBounce),
		Codec.BOOL.optionalFieldOf("bounce", false).forGetter(value -> value.hasBounced)
	).apply(instance, SlimySolesAttachment::new));

	public double bounceVelocity;
	public double doubleJumpBoostVelocity;
	public boolean forceBounce;
	public boolean hasBounced;

	public SlimySolesAttachment() {
		this(0.0D, 0.0D, false, false);
	}

	public SlimySolesAttachment(double bounceVelocity, double doubleJumpBoostVelocity, boolean forceBounce, boolean hasBounced) {
		this.bounceVelocity = bounceVelocity;
		this.doubleJumpBoostVelocity = doubleJumpBoostVelocity;
		this.forceBounce = forceBounce;
		this.hasBounced = hasBounced;
	}
}
