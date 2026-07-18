package twilightforest.api;

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.level.ItemLike;

final class ApiTestItems {
	private ApiTestItems() {
	}

	static void bindStackable(ItemLike... items) {
		bind(DataComponentMap.builder()
			.set(DataComponents.MAX_STACK_SIZE, 64)
			.build(), items);
	}

	static void bindDamageable(ItemLike... items) {
		bind(DataComponentMap.builder()
			.set(DataComponents.MAX_STACK_SIZE, 1)
			.set(DataComponents.MAX_DAMAGE, 100)
			.set(DataComponents.DAMAGE, 0)
			.build(), items);
	}

	private static void bind(DataComponentMap components, ItemLike... items) {
		for (ItemLike item : items) {
			var holder = item.asItem().builtInRegistryHolder();
			if (!holder.areComponentsBound()) {
				holder.bindComponents(components);
			}
		}
	}
}
