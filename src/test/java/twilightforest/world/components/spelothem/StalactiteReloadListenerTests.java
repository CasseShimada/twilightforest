package twilightforest.world.components.spelothem;

import com.mojang.datafixers.util.Either;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import twilightforest.TwilightForestMod;
import twilightforest.world.components.feature.BlockSpikeFeature;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

class StalactiteReloadListenerTests {
	private static final Identifier ENTRY_ID = TwilightForestMod.prefix("entries/test");
	private static final Identifier CONFIG_ID = TwilightForestMod.prefix("test_config");

	private final StalactiteReloadListener listener = new StalactiteReloadListener();

	@AfterEach
	void clearState() {
		this.reload(Map.of());
	}

	@Test
	void resolvesCodecLoadedEntriesFromConfigs() {
		Stalactite stalactite = BlockSpikeFeature.STONE_STALACTITE;
		SpeleothemVarietyConfig config = config(List.of(ENTRY_ID));

		this.reload(Map.of(ENTRY_ID, Either.left(stalactite), CONFIG_ID, Either.right(config)));

		assertSame(config, StalactiteReloadListener.HILL_CONFIGS.get("test"));
		assertEquals(List.of(stalactite), StalactiteReloadListener.STALACTITES_PER_HILL.get("test"));
	}

	@Test
	void clearsAllDerivedStateOnReload() {
		this.reload(Map.of(
			ENTRY_ID, Either.left(BlockSpikeFeature.STONE_STALACTITE),
			CONFIG_ID, Either.right(config(List.of(ENTRY_ID)))
		));

		this.reload(Map.of());

		assertEquals(Map.of(), StalactiteReloadListener.HILL_CONFIGS);
		assertEquals(Map.of(), StalactiteReloadListener.STALACTITES_PER_HILL);
		assertEquals(Map.of(), StalactiteReloadListener.ORE_STALACTITES_PER_HILL);
		assertEquals(Map.of(), StalactiteReloadListener.STALAGMITES_PER_HILL);
	}

	private void reload(Map<Identifier, Either<Stalactite, SpeleothemVarietyConfig>> resources) {
		this.listener.apply(resources, mock(ResourceManager.class), mock(ProfilerFiller.class));
	}

	private static SpeleothemVarietyConfig config(List<Identifier> entries) {
		return new SpeleothemVarietyConfig("test", entries, List.of(), List.of(), 0.0F, 1.0F, 0.0F, false);
	}
}
