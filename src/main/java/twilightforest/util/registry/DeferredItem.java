package twilightforest.util.registry;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.function.Supplier;

public final class DeferredItem<T extends Item> extends DeferredHolder<Item, T> implements ItemLike {
	DeferredItem(Identifier id, Supplier<? extends T> factory) {
		super(id, factory);
	}

	public ItemStack toStack() {
		return new ItemStack(this.get());
	}

	@Override
	public Item asItem() {
		return this.get();
	}
}
