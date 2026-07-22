// SPDX-License-Identifier: MIT
package net.kyrptonaught.diggusmaximus.api;

import net.fabricmc.fabric.api.event.Event;

/** Declaration-only API 1 snapshot; runtime fields are supplied by Diggus Maximus. */
public final class ExcavationEvents {
	public static final Event<BeforeStart> BEFORE_START = null;
	public static final Event<Candidate> CANDIDATE = null;
	public static final Event<ToolParticipation> TOOL_PARTICIPATION = null;
	public static final Event<BeforeBlockBreak> BEFORE_BLOCK_BREAK = null;

	private ExcavationEvents() {
	}

	@FunctionalInterface
	public interface BeforeStart {
		ExcavationDecision allowStart(ExcavationStartContext context);
	}

	@FunctionalInterface
	public interface Candidate {
		ExcavationDecision allowCandidate(ExcavationCandidateContext context);
	}

	@FunctionalInterface
	public interface ToolParticipation {
		ExcavationDecision allowTool(ExcavationToolContext context);
	}

	@FunctionalInterface
	public interface BeforeBlockBreak {
		ExcavationDecision beforeBlockBreak(ExcavationCandidateContext context);
	}
}
