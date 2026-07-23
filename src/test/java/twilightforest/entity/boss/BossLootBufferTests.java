package twilightforest.entity.boss;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import twilightforest.test.MinecraftBootstrapExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MinecraftBootstrapExtension.class)
class BossLootBufferTests {
	@BeforeAll
	static void bindFixtureItemComponents() {
		DataComponentMap components = DataComponentMap.builder()
			.set(DataComponents.MAX_STACK_SIZE, 64)
			.build();
		bind(components, Items.DIAMOND, Items.EMERALD, Items.IRON_INGOT);
	}

	@Test
	void generatedLootIsDistributedOnceWithExactOverflow() {
		TestBuffer buffer = new TestBuffer();
		ObjectArrayList<ItemStack> generated = new ObjectArrayList<>();
		for (int index = 0; index < 30; index++) {
			generated.add(new ItemStack(Items.DIAMOND));
		}
		List<ItemStack> overflow = new ArrayList<>();

		IBossLootBuffer.bufferGeneratedLoot(buffer, generated, RandomSource.create(1234L), overflow::add);

		assertEquals(27, occupiedSlots(buffer.items));
		assertEquals(27, itemCount(buffer.items));
		assertEquals(3, itemCount(overflow));
		assertTrue(buffer.hasBufferedLoot());
	}

	@Test
	void splittingPreservesEveryGeneratedItemWithoutOverstacking() {
		TestBuffer buffer = new TestBuffer();
		ObjectArrayList<ItemStack> generated = new ObjectArrayList<>();
		generated.add(new ItemStack(Items.DIAMOND, 64));
		generated.add(new ItemStack(Items.EMERALD, 64));
		generated.add(new ItemStack(Items.IRON_INGOT, 64));
		List<ItemStack> overflow = new ArrayList<>();

		IBossLootBuffer.bufferGeneratedLoot(buffer, generated, RandomSource.create(5678L), overflow::add);

		assertEquals(192, itemCount(buffer.items) + itemCount(overflow));
		assertTrue(buffer.items.stream().allMatch(stack -> stack.isEmpty() || stack.getCount() <= stack.getMaxStackSize()));
		assertTrue(overflow.stream().allMatch(stack -> stack.getCount() <= stack.getMaxStackSize()));
	}

	@Test
	void generatedLootPreservesExistingBufferSlotsAndOverflowsOnlyTheRemainder() {
		TestBuffer buffer = new TestBuffer();
		buffer.setItem(0, new ItemStack(Items.EMERALD, 9));
		ObjectArrayList<ItemStack> generated = new ObjectArrayList<>();
		for (int index = 0; index < 27; index++) {
			generated.add(new ItemStack(Items.DIAMOND));
		}
		List<ItemStack> overflow = new ArrayList<>();

		IBossLootBuffer.bufferGeneratedLoot(buffer, generated, RandomSource.create(9012L), overflow::add);

		assertTrue(buffer.getItem(0).is(Items.EMERALD));
		assertEquals(9, buffer.getItem(0).getCount());
		assertEquals(26, buffer.items.stream().filter(stack -> stack.is(Items.DIAMOND)).count());
		assertEquals(1, itemCount(overflow));
	}

	@Test
	void transferIsExactOnceAndRejectsContainersThatWouldTruncate() {
		TestBuffer buffer = new TestBuffer();
		buffer.setItem(0, new ItemStack(Items.DIAMOND, 3));
		buffer.setItem(26, new ItemStack(Items.EMERALD, 5));

		assertFalse(IBossLootBuffer.transferBufferedLoot(buffer, new SimpleContainer(26)));
		assertEquals(8, itemCount(buffer.items));
		SimpleContainer occupied = new SimpleContainer(IBossLootBuffer.CONTAINER_SIZE);
		occupied.setItem(4, new ItemStack(Items.IRON_INGOT));
		assertFalse(IBossLootBuffer.transferBufferedLoot(buffer, occupied));
		assertEquals(8, itemCount(buffer.items));
		assertEquals(1, itemCount(occupied.getItems()));

		SimpleContainer chest = new SimpleContainer(IBossLootBuffer.CONTAINER_SIZE);
		assertTrue(IBossLootBuffer.transferBufferedLoot(buffer, chest));
		assertEquals(8, itemCount(chest.getItems()));
		assertFalse(buffer.hasBufferedLoot());
		assertEquals(0, itemCount(buffer.items));
	}

	@Test
	void fixedSizeEmptyInventoryDoesNotPretendToContainLoot() {
		TestBuffer buffer = new TestBuffer();

		assertEquals(IBossLootBuffer.CONTAINER_SIZE, buffer.items.size());
		assertFalse(buffer.hasBufferedLoot());
	}

	private static int occupiedSlots(List<ItemStack> stacks) {
		return (int) stacks.stream().filter(stack -> !stack.isEmpty()).count();
	}

	private static int itemCount(List<ItemStack> stacks) {
		return stacks.stream().mapToInt(ItemStack::getCount).sum();
	}

	private static void bind(DataComponentMap components, ItemLike... items) {
		for (ItemLike item : items) {
			var holder = item.asItem().builtInRegistryHolder();
			if (!holder.areComponentsBound()) {
				holder.bindComponents(components);
			}
		}
	}

	private static final class TestBuffer implements IBossLootBuffer {
		private final NonNullList<ItemStack> items = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);

		@Override
		public NonNullList<ItemStack> getItemStacks() {
			return this.items;
		}
	}
}
