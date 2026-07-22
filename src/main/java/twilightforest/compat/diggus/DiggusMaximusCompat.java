package twilightforest.compat.diggus;

import net.kyrptonaught.diggusmaximus.api.ExcavationCandidateContext;
import net.kyrptonaught.diggusmaximus.api.ExcavationDecision;
import net.kyrptonaught.diggusmaximus.api.ExcavationEvents;
import net.kyrptonaught.diggusmaximus.api.ExcavationStartContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import twilightforest.compat.CompatTags;
import twilightforest.events.EntityEvents;
import twilightforest.events.ProgressionEvents;
import twilightforest.init.TFItems;

/** Server-authoritative Diggus Maximus API 1 policy. */
public final class DiggusMaximusCompat {
	private DiggusMaximusCompat() {
	}

	public static void init() {
		ExcavationEvents.TOOL_PARTICIPATION.register(context -> context.tool().is(TFItems.GIANT_PICKAXE)
			? ExcavationDecision.DENY : ExcavationDecision.DEFAULT);
		ExcavationEvents.BEFORE_START.register(DiggusMaximusCompat::beforeStart);
		ExcavationEvents.CANDIDATE.register(DiggusMaximusCompat::candidate);
		ExcavationEvents.BEFORE_BLOCK_BREAK.register(DiggusMaximusCompat::candidate);
	}

	private static ExcavationDecision beforeStart(ExcavationStartContext context) {
		return decide(context.player(), context.level(), context.origin(), context.originState());
	}

	private static ExcavationDecision candidate(ExcavationCandidateContext context) {
		return decide(context.player(), context.level(), context.position(), context.state());
	}

	static ExcavationDecision decide(ServerPlayer player, ServerLevel level, BlockPos pos, BlockState expectedState) {
		// Never turn a policy callback into an implicit chunk ticket.
		if (!level.hasChunkAt(pos)) {
			return ExcavationDecision.DENY;
		}

		BlockState currentState = level.getBlockState(pos);
		if (currentState != expectedState
			|| currentState.is(CompatTags.DIGGUS_UNSAFE_BLOCKS)
			|| ProgressionEvents.shouldCancelBlockBreak(level, player, pos)) {
			return ExcavationDecision.DENY;
		}

		BlockEntity blockEntity = level.getBlockEntity(pos);
		if (EntityEvents.shouldCancelCasketBreak(player, currentState, blockEntity) || blockEntity != null) {
			return ExcavationDecision.DENY;
		}

		// Inclusion/equivalence remains data-driven. DEFAULT retains Diggus Maximus hard safety,
		// tool durability and vanilla/Fabric break-event semantics.
		return ExcavationDecision.DEFAULT;
	}
}
