package twilightforest.util;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HolderMatcherTests {

	private HolderMatcher instance;

	@BeforeEach
	public void setup() {
		instance = new HolderMatcher();
	}

	@Test
	public void matchesReference() {
		Holder<Item> holder = BuiltInRegistries.ITEM.getOrThrow(BuiltInRegistries.ITEM.getResourceKey(Items.OAK_PLANKS).orElseThrow());

		boolean result = instance.match(holder, holder);

		assertTrue(result);
	}

	@Test
	public void matchesReferenceFailed() {
		Holder<Item> holder = BuiltInRegistries.ITEM.getOrThrow(BuiltInRegistries.ITEM.getResourceKey(Items.OAK_PLANKS).orElseThrow());
		Holder<Item> other = BuiltInRegistries.ITEM.getOrThrow(BuiltInRegistries.ITEM.getResourceKey(Items.BIRCH_PLANKS).orElseThrow());

		boolean result = instance.match(holder, other);

		assertFalse(result);
	}

	@Test
	public void matchesDirect() {
		Holder<Item> holder = Holder.direct(Items.OAK_PLANKS);

		boolean result = instance.match(holder, holder);

		assertTrue(result);
	}

	@Test
	public void matchesDirectFailed() {
		Holder<Item> holder = Holder.direct(Items.OAK_PLANKS);
		Holder<Item> other = Holder.direct(Items.BIRCH_PLANKS);

		boolean result = instance.match(holder, other);

		assertFalse(result);
	}

	@Test
	public void matchesDeferred() {
		Holder<Item> holder = BuiltInRegistries.ITEM.getOrThrow(BuiltInRegistries.ITEM.getResourceKey(Items.IRON_INGOT).orElseThrow());

		boolean result = instance.match(holder, holder);

		assertTrue(result);
	}

	@Test
	public void matchesDeferredFailed() {
		Holder<Item> holder = BuiltInRegistries.ITEM.getOrThrow(BuiltInRegistries.ITEM.getResourceKey(Items.IRON_INGOT).orElseThrow());
		Holder<Item> other = BuiltInRegistries.ITEM.getOrThrow(BuiltInRegistries.ITEM.getResourceKey(Items.GOLD_INGOT).orElseThrow());

		boolean result = instance.match(holder, other);

		assertFalse(result);
	}

	@Test
	public void matchesMixed() {
		Holder<Item> ref = BuiltInRegistries.ITEM.getOrThrow(BuiltInRegistries.ITEM.getResourceKey(Items.IRON_INGOT).orElseThrow());
		Holder<Item> direct = Holder.direct(Items.IRON_INGOT);
		Holder<Item> deferred = BuiltInRegistries.ITEM.getOrThrow(BuiltInRegistries.ITEM.getResourceKey(Items.IRON_INGOT).orElseThrow());

		assertTrue(instance.match(ref, direct));
		assertTrue(instance.match(ref, deferred));
		assertTrue(instance.match(direct, deferred));
	}

}
