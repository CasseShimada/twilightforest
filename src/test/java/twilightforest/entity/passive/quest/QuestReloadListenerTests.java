package twilightforest.entity.passive.quest;

import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import twilightforest.entity.passive.quest.ram.QuestingRamContext;
import twilightforest.entity.passive.quest.ram.QuestingRamCurrentContext;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

class QuestReloadListenerTests {
	private final QuestReloadListener listener = new QuestReloadListener();

	@AfterEach
	void resetContext() {
		QuestingRamCurrentContext.INSTANCE.setContext(QuestingRamContext.FALLBACK);
	}

	@Test
	void loadsQuestFromCanonicalResourceId() {
		QuestingRamContext context = testContext();

		this.listener.apply(Map.of(QuestReloadListener.QUESTING_RAM, context), mock(ResourceManager.class), mock(ProfilerFiller.class));

		assertSame(context, QuestingRamCurrentContext.INSTANCE.getContext());
	}

	@Test
	void fallsBackWhenCanonicalResourceIsMissing() {
		Identifier unrelatedQuest = Identifier.parse("othermod:questing_ram");

		this.listener.apply(Map.of(unrelatedQuest, testContext()), mock(ResourceManager.class), mock(ProfilerFiller.class));

		assertSame(QuestingRamContext.FALLBACK, QuestingRamCurrentContext.INSTANCE.getContext());
	}

	private static QuestingRamContext testContext() {
		return new QuestingRamContext(QuestingRamContext.FALLBACK.questItems(), QuestingRamContext.FALLBACK.lootTable());
	}
}
