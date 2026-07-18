package twilightforest.api;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import twilightforest.events.CharmEvents;
import twilightforest.init.TFDataComponents;
import twilightforest.test.MinecraftBootstrapExtension;
import twilightforest.util.TFItemStackUtils;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MinecraftBootstrapExtension.class)
class TFItemStackUtilsAccessoryTests {
	@BeforeAll
	static void bindFixtureItemComponents() {
		ApiTestItems.bindStackable(Items.STICK);
	}

	@AfterEach
	void resetRegistrations() {
		TwilightForestApi.resetForTests();
	}

	@Test
	void accessoryConsumptionRunsAfterAllNativeSlotsAndPreservesThePreConsumptionSnapshot() {
		RegistryAccess registryAccess = registryAccess();
		ServerPlayer player = mock(ServerPlayer.class);
		stubEmptyNativeStorage(player, registryAccess);
		ItemStack externallyStored = new ItemStack(Items.STICK, 3);
		externallyStored.set(TFDataComponents.CASKET_DAMAGE, 2);
		AtomicReference<AccessoryConsumptionContext> receivedContext = new AtomicReference<>();
		AccessoryApi.registerItemConsumer(id("external_storage"), context -> {
			receivedContext.set(context);
			ItemStack beforeConsumption = externallyStored.copy();
			externallyStored.shrink(1);
			return AccessoryConsumptionResult.consumed(beforeConsumption);
		});
		CompoundTag persistentTag = new CompoundTag();

		assertTrue(TFItemStackUtils.consumeInventoryItem(player, Items.STICK, persistentTag, true));

		assertSame(player, receivedContext.get().player());
		assertSame(Items.STICK, receivedContext.get().requestedItem());
		assertEquals(2, externallyStored.getCount(), "the bridge must not consume a second item");
		assertEquals(2, persistentTag.getIntOr(CharmEvents.CASKET_DAMAGE_TAG, -1));
		ItemStack savedStack = TFItemStackUtils.loadItem(
			registryAccess,
			persistentTag.getCompoundOrEmpty(CharmEvents.CONSUMED_CHARM_TAG));
		assertTrue(savedStack.is(Items.STICK));
		assertEquals(3, savedStack.getCount(), "CharmStack must contain the complete pre-consumption stack");
		assertEquals(2, savedStack.getOrDefault(TFDataComponents.CASKET_DAMAGE, -1));
	}

	@Test
	void nativeInventoryConsumptionShortCircuitsAccessoryProviders() {
		RegistryAccess registryAccess = registryAccess();
		ServerPlayer player = mock(ServerPlayer.class);
		Inventory inventory = mock(Inventory.class);
		ItemStack nativeStack = new ItemStack(Items.STICK, 2);
		NonNullList<ItemStack> stacks = NonNullList.create();
		stacks.add(nativeStack);
		when(player.getInventory()).thenReturn(inventory);
		when(player.registryAccess()).thenReturn(registryAccess);
		when(inventory.getNonEquipmentItems()).thenReturn(stacks);
		AtomicInteger providerCalls = registerCountingProvider();

		assertTrue(TFItemStackUtils.consumeInventoryItem(player, Items.STICK, new CompoundTag(), false));
		assertEquals(1, nativeStack.getCount());
		assertEquals(0, providerCalls.get());
	}

	@Test
	void offhandConsumptionShortCircuitsAccessoryProviders() {
		RegistryAccess registryAccess = registryAccess();
		ServerPlayer player = mock(ServerPlayer.class);
		stubEmptyNativeStorage(player, registryAccess);
		ItemStack offhandStack = new ItemStack(Items.STICK, 2);
		when(player.getItemBySlot(EquipmentSlot.OFFHAND)).thenReturn(offhandStack);
		AtomicInteger providerCalls = registerCountingProvider();

		assertTrue(TFItemStackUtils.consumeInventoryItem(player, Items.STICK, new CompoundTag(), false));
		assertEquals(1, offhandStack.getCount());
		assertEquals(0, providerCalls.get());
		verify(player).setItemSlot(EquipmentSlot.OFFHAND, offhandStack);
	}

	@Test
	void nonServerPlayersNeverDispatchAccessoryConsumers() {
		Player player = mock(Player.class);
		stubEmptyNativeStorage(player, registryAccess());
		AtomicInteger providerCalls = registerCountingProvider();

		assertFalse(TFItemStackUtils.consumeInventoryItem(player, Items.STICK, new CompoundTag(), false));
		assertEquals(0, providerCalls.get());
	}

	@Test
	void serverPlayerWithNoAccessoryConsumerStillReturnsFalse() {
		ServerPlayer player = mock(ServerPlayer.class);
		stubEmptyNativeStorage(player, registryAccess());

		assertFalse(TFItemStackUtils.consumeInventoryItem(player, Items.STICK, new CompoundTag(), false));
	}

	private static AtomicInteger registerCountingProvider() {
		AtomicInteger calls = new AtomicInteger();
		AccessoryApi.registerItemConsumer(id("must_not_run"), context -> {
			calls.incrementAndGet();
			return AccessoryConsumptionResult.consumed(new ItemStack(context.requestedItem()));
		});
		return calls;
	}

	private static void stubEmptyNativeStorage(Player player, RegistryAccess registryAccess) {
		Inventory inventory = mock(Inventory.class);
		when(player.getInventory()).thenReturn(inventory);
		when(player.registryAccess()).thenReturn(registryAccess);
		when(inventory.getNonEquipmentItems()).thenReturn(NonNullList.create());
		when(player.getItemBySlot(any(EquipmentSlot.class))).thenReturn(ItemStack.EMPTY);
		if (player instanceof ServerPlayer serverPlayer) {
			ServerLevel level = mock(ServerLevel.class);
			MinecraftServer server = mock(MinecraftServer.class);
			when(serverPlayer.level()).thenReturn(level);
			when(level.getServer()).thenReturn(server);
			when(server.isSameThread()).thenReturn(true);
		}
	}

	private static RegistryAccess registryAccess() {
		return RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
	}

	private static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath("tf_item_stack_utils_test", path);
	}
}
