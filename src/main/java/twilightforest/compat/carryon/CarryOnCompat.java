package twilightforest.compat.carryon;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import tschipp.carryon.api.CarryActionContext;
import tschipp.carryon.api.CarryDecision;
import tschipp.carryon.api.CarryOnEvents;
import twilightforest.TwilightForestMod;
import twilightforest.compat.CompatTags;
import twilightforest.events.EntityEvents;
import twilightforest.events.ProgressionEvents;

/** Server-authoritative Carry On API 1 policy. */
public final class CarryOnCompat {
	private CarryOnCompat() {
	}

	public static void init() {
		CarryOnEvents.BEFORE_PICKUP.register(CarryOnCompat::decide);
		CarryOnEvents.BEFORE_PLACE.register(CarryOnCompat::decide);
		CarryOnEvents.BEFORE_STACK.register(CarryOnCompat::decide);
	}

	static CarryDecision decide(CarryActionContext context) {
		return switch (context.operation()) {
			case PICKUP_BLOCK -> pickupBlock(context);
			case PLACE_BLOCK -> placeBlock(context);
			case PICKUP_ENTITY -> entityPickup(context);
			case PLACE_ENTITY -> entityPlace(context);
			case STACK_ENTITY -> entityStack(context);
			default -> CarryDecision.PASS;
		};
	}

	private static CarryDecision pickupBlock(CarryActionContext context) {
		BlockPos pos = context.position().orElse(null);
		BlockState supplied = context.blockState().orElse(null);
		ServerLevel level = context.player().level();
		if (pos == null || supplied == null || !level.hasChunkAt(pos)) {
			return CarryDecision.DENY;
		}

		BlockState state = level.getBlockState(pos);
		if (state != supplied || ProgressionEvents.shouldCancelBlockBreak(level, context.player(), pos)
			|| state.is(CompatTags.CARRY_ON_UNSAFE_BLOCKS)) {
			return CarryDecision.DENY;
		}

		BlockEntity blockEntity = level.getBlockEntity(pos);
		if (EntityEvents.shouldCancelCasketBreak(context.player(), state, blockEntity)) {
			return CarryDecision.DENY;
		}
		if (state.is(CompatTags.CARRY_ON_SAFE_BLOCKS)) {
			return CarryDecision.ALLOW;
		}
		if (context.hasBlockEntity() || blockEntity != null) {
			return CarryDecision.DENY;
		}
		return CarryDecision.PASS;
	}

	private static CarryDecision placeBlock(CarryActionContext context) {
		BlockPos pos = context.position().orElse(null);
		BlockState state = context.blockState().or(() -> context.carried().blockState()).orElse(null);
		ServerLevel level = context.player().level();
		if (pos == null || state == null || !level.hasChunkAt(pos)
			|| ProgressionEvents.shouldCancelBlockPlacement(level, context.player(), pos)
			|| state.is(CompatTags.CARRY_ON_UNSAFE_BLOCKS)) {
			return CarryDecision.DENY;
		}
		if (state.is(CompatTags.CARRY_ON_SAFE_BLOCKS)) {
			return CarryDecision.ALLOW;
		}
		return context.carried().hasBlockEntityData() ? CarryDecision.DENY : CarryDecision.PASS;
	}

	private static CarryDecision entityPickup(CarryActionContext context) {
		if (context.hasPassengerData()) {
			return CarryDecision.DENY;
		}
		return classifyEntity(context.subjectId().orElse(null));
	}

	private static CarryDecision entityPlace(CarryActionContext context) {
		if (context.carried().hasPassengerData()) {
			return CarryDecision.DENY;
		}
		return classifyEntity(context.carried().contentId().or(() -> context.subjectId()).orElse(null));
	}

	private static CarryDecision entityStack(CarryActionContext context) {
		Identifier carried = context.carried().contentId().or(() -> context.subjectId()).orElse(null);
		Identifier target = context.targetId().orElse(null);
		return isTwilightForest(carried) || isTwilightForest(target) ? CarryDecision.DENY : CarryDecision.PASS;
	}

	static CarryDecision classifyEntity(Identifier id) {
		if (!isTwilightForest(id)) {
			return CarryDecision.PASS;
		}
		return switch (id.getPath()) {
			case "bighorn_sheep", "boar", "deer", "dwarf_rabbit", "penguin", "raven", "squirrel", "tiny_bird" -> CarryDecision.ALLOW;
			default -> CarryDecision.DENY;
		};
	}

	private static boolean isTwilightForest(Identifier id) {
		return id != null && TwilightForestMod.ID.equals(id.getNamespace());
	}
}
