package twilightforest.util.registry;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

public final class DeferredBlock<T extends Block> extends DeferredHolder<Block, T> implements ItemLike {
	DeferredBlock(Identifier id, Supplier<? extends T> factory) {
		super(id, factory);
	}

	public static <T extends Block> DeferredBlock<T> create(Identifier id, Supplier<? extends T> factory) {
		return new DeferredBlock<>(id, factory);
	}

	public void register(Registry<Block> registry) {
		T value = this.factory.get();
		this.bind(Registry.register(registry, this.getId(), value));
	}

	public ItemStack toStack() {
		return new ItemStack(this.get());
	}

	public Item asItem() {
		return this.get().asItem();
	}

	public Holder<Block> asHolder() {
		return BuiltInRegistries.BLOCK.wrapAsHolder(this.get());
	}
}
