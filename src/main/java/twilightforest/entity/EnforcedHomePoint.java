package twilightforest.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import com.mojang.serialization.Codec;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import twilightforest.entity.ai.goal.AttemptToGoHomeGoal;
import twilightforest.init.TFDimension;

public interface EnforcedHomePoint {

	default <T extends PathfinderMob & EnforcedHomePoint> void addRestrictionGoals(T entity, GoalSelector selector) {
		selector.addGoal(5, new AttemptToGoHomeGoal<>(entity, 1.25D));
	}

	default void saveHomePointToNbt(ValueOutput output) {
		if (this.getRestrictionPoint() != null) {
			output.store("HomePos", GlobalPos.CODEC, this.getRestrictionPoint());
		}
	}

	default void loadHomePointFromNbt(ValueInput input) {
		input.read("HomePos", GlobalPos.CODEC).ifPresentOrElse(this::setRestrictionPoint, () -> {
			// properly load old home points, just assume theyre set in TF
			input.read("Home", Codec.DOUBLE.listOf())
				.filter(list -> list.size() >= 3)
				.ifPresent(list -> this.setRestrictionPoint(GlobalPos.of(TFDimension.DIMENSION_KEY, BlockPos.containing(list.get(0), list.get(1), list.get(2)))));
		});
	}

	default boolean isMobWithinHomeArea(Entity entity) {
		if (!this.isRestrictionPointValid(entity.level().dimension())) return true;
		return this.getRestrictionPoint().pos().distSqr(entity.blockPosition()) < (double) (this.getHomeRadius() * this.getHomeRadius());
	}

	default boolean isRestrictionPointValid(ResourceKey<Level> currentMobLevel) {
		return this.getRestrictionPoint() != null && this.getRestrictionPoint().dimension().equals(currentMobLevel);
	}

	@Nullable
	GlobalPos getRestrictionPoint();

	void setRestrictionPoint(@Nullable GlobalPos pos);

	int getHomeRadius();
}
