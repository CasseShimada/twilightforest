package twilightforest.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public record ItemStackRef(Holder<Item> item, int count) {
	public static final Codec<ItemStackRef> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		BuiltInRegistries.ITEM.holderByNameCodec().fieldOf("id").forGetter(ItemStackRef::item),
		ExtraCodecs.POSITIVE_INT.optionalFieldOf("count", 1).forGetter(ItemStackRef::count)
	).apply(instance, ItemStackRef::new));

	public static ItemStackRef of(ItemLike itemLike) {
		return new ItemStackRef(itemLike.asItem().builtInRegistryHolder(), 1);
	}

	public static ItemStackRef of(ItemStack stack) {
		return new ItemStackRef(stack.typeHolder(), stack.getCount());
	}

	public ItemStack create() {
		return new ItemStack(this.item, this.count);
	}
}
