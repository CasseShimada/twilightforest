package twilightforest.advancements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.predicates.ContextAwarePredicate;
import net.minecraft.advancements.predicates.entity.EntityPredicate;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import twilightforest.init.TFAdvancements;

import java.util.Optional;

public class AddModifierTrigger extends SimpleCriterionTrigger<AddModifierTrigger.TriggerInstance> {
	@Override
	public Codec<TriggerInstance> codec() {
		return TriggerInstance.CODEC;
	}

	public void trigger(ServerPlayer player, Identifier modifier) {
		this.trigger(player, instance -> instance.test(modifier));
	}

	public record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<Identifier> modifier) implements SimpleInstance {
		public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
			Identifier.CODEC.optionalFieldOf("modifier").forGetter(TriggerInstance::modifier)
		).apply(instance, TriggerInstance::new));

		public static Criterion<TriggerInstance> addedAnyModifier() {
			return TFAdvancements.ADD_MODIFIER.createCriterion(new TriggerInstance(Optional.empty(), Optional.empty()));
		}

		public static Criterion<TriggerInstance> addedModifier(Identifier modifier) {
			return TFAdvancements.ADD_MODIFIER.createCriterion(new TriggerInstance(Optional.empty(), Optional.of(modifier)));
		}

		private boolean test(Identifier modifier) {
			return this.modifier.isEmpty() || this.modifier.get().equals(modifier);
		}
	}
}
