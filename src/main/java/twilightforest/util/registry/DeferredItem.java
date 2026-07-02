package twilightforest.util.registry;

import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.function.Supplier;

public final class DeferredItem<T extends Item> extends DeferredHolder<Item, T> implements ItemLike {
	DeferredItem(Identifier id, Supplier<? extends T> factory) {
		super(id, factory);
	}

	public static <T extends Item> DeferredItem<T> create(Identifier id, Supplier<? extends T> factory) {
		return new DeferredItem<>(id, factory);
	}

	public void register(Registry<Item> registry) {
		T value = this.factory.get();
		this.bind(Registry.register(registry, this.getId(), value));
	}

	public ItemStack toStack() {
		return new ItemStack(this.get());
	}

	@Override
	public Item asItem() {
		return this.get();
	}
}
