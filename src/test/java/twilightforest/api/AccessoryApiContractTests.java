package twilightforest.api;

import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import twilightforest.test.MinecraftBootstrapExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MinecraftBootstrapExtension.class)
class AccessoryApiContractTests {
	@BeforeAll
	static void bindFixtureItemComponents() {
		ApiTestItems.bindStackable(Items.STICK, Items.STONE);
	}

	@AfterEach
	void resetRegistrations() {
		TwilightForestApi.resetForTests();
	}

	@Test
	void emptyRegistryPasses() {
		assertFalse(AccessoryApi.tryConsume(context()).wasConsumed());
	}

	@Test
	void contextSignatureMakesTheLogicalServerBoundaryCompileTimeVisible() throws NoSuchMethodException {
		assertEquals(ServerPlayer.class, AccessoryConsumptionContext.class.getRecordComponents()[0].getType());
		AccessoryConsumptionContext.class.getConstructor(ServerPlayer.class, Item.class);
		assertThrows(NoSuchMethodException.class,
			() -> AccessoryConsumptionContext.class.getConstructor(Player.class, Item.class));
	}

	@Test
	void offThreadDispatchFailsBeforeInvokingProviders() {
		ServerPlayer player = mock(ServerPlayer.class);
		ServerLevel level = mock(ServerLevel.class);
		MinecraftServer server = mock(MinecraftServer.class);
		when(player.level()).thenReturn(level);
		when(level.getServer()).thenReturn(server);
		when(server.isSameThread()).thenReturn(false);
		AtomicInteger calls = new AtomicInteger();
		AccessoryApi.registerItemConsumer(id("must_not_run_off_thread"), context -> {
			calls.incrementAndGet();
			return AccessoryConsumptionResult.pass();
		});

		assertThrows(IllegalStateException.class,
			() -> AccessoryApi.tryConsume(new AccessoryConsumptionContext(player, Items.STICK)));
		assertEquals(0, calls.get());
	}

	@Test
	void successfulProviderReturnsAnIndependentPreConsumptionSnapshot() {
		ItemStack stored = new ItemStack(Items.STICK, 3);
		AccessoryApi.registerItemConsumer(id("storage"), context -> {
			ItemStack before = stored.copy();
			stored.shrink(1);
			return AccessoryConsumptionResult.consumed(before);
		});

		AccessoryConsumptionResult result = AccessoryApi.tryConsume(context());
		assertTrue(result.wasConsumed());
		assertEquals(2, stored.getCount());
		assertEquals(3, result.consumedStack().getCount());

		ItemStack callerCopy = result.consumedStack();
		callerCopy.setCount(1);
		assertEquals(3, result.consumedStack().getCount());
	}

	@Test
	void providersRunInIdOrderUntilOneConsumes() {
		List<String> calls = new ArrayList<>();
		AccessoryApi.registerItemConsumer(id("zeta"), context -> {
			calls.add("zeta");
			return AccessoryConsumptionResult.consumed(new ItemStack(Items.STICK, 2));
		});
		AccessoryApi.registerItemConsumer(id("alpha"), context -> {
			calls.add("alpha");
			return AccessoryConsumptionResult.pass();
		});

		assertTrue(AccessoryApi.tryConsume(context()).wasConsumed());
		assertEquals(List.of("alpha", "zeta"), calls);
	}

	@Test
	void providerExceptionIsIsolatedAndDispatchContinues() {
		AccessoryApi.registerItemConsumer(id("alpha_failure"), context -> {
			throw new IllegalStateException("expected contract-test failure");
		});
		AccessoryApi.registerItemConsumer(id("beta_success"), context ->
			AccessoryConsumptionResult.consumed(new ItemStack(Items.STICK)));

		assertTrue(AccessoryApi.tryConsume(context()).wasConsumed());
	}

	@Test
	void recursiveDispatchPassesWithoutReinvokingProviders() {
		AtomicInteger calls = new AtomicInteger();
		AtomicReference<AccessoryConsumptionResult> nestedResult = new AtomicReference<>();
		AccessoryApi.registerItemConsumer(id("recursive"), context -> {
			calls.incrementAndGet();
			nestedResult.set(AccessoryApi.tryConsume(context));
			return AccessoryConsumptionResult.consumed(new ItemStack(Items.STICK));
		});

		assertTrue(AccessoryApi.tryConsume(context()).wasConsumed());
		assertEquals(1, calls.get());
		assertFalse(nestedResult.get().wasConsumed());
	}

	@Test
	void invalidConsumedStackDoesNotStopLaterProviders() {
		AccessoryApi.registerItemConsumer(id("alpha_wrong_item"), context ->
			AccessoryConsumptionResult.consumed(new ItemStack(Items.STONE)));
		AccessoryApi.registerItemConsumer(id("beta_requested_item"), context ->
			AccessoryConsumptionResult.consumed(new ItemStack(Items.STICK)));

		assertTrue(AccessoryApi.tryConsume(context()).wasConsumed());
	}

	@Test
	void resultFactoriesEnforcePassAndConsumedInvariants() {
		assertThrows(IllegalArgumentException.class,
			() -> new AccessoryConsumptionResult(AccessoryConsumptionResult.Outcome.PASS, new ItemStack(Items.STICK)));
		assertThrows(IllegalArgumentException.class,
			() -> AccessoryConsumptionResult.consumed(ItemStack.EMPTY));
	}

	private static AccessoryConsumptionContext context() {
		ServerPlayer player = mock(ServerPlayer.class);
		ServerLevel level = mock(ServerLevel.class);
		MinecraftServer server = mock(MinecraftServer.class);
		when(player.level()).thenReturn(level);
		when(level.getServer()).thenReturn(server);
		when(server.isSameThread()).thenReturn(true);
		return new AccessoryConsumptionContext(player, Items.STICK);
	}

	private static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath("api_contract_test", path);
	}
}
