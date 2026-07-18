package twilightforest.components.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import twilightforest.TFRegistries;
import twilightforest.TwilightForestMod;
import twilightforest.init.custom.ItemDisplays;
import twilightforest.inventory.InventoryUtil;
import twilightforest.item.travellers_gear.modifiers.display.ItemDisplayType;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.function.BiConsumer;

public final class ItemDisplayContents implements TooltipComponent {
	public static final Identifier MAP_ID = TwilightForestMod.prefix("map");
	public static final Identifier COMPASS_ID = TwilightForestMod.prefix("compass");
	public static final Identifier CLOCK_ID = TwilightForestMod.prefix("clock");
	public static final Identifier MOON_DIAL_ID = TwilightForestMod.prefix("moon_dial");
	public static final List<Identifier> LAYOUT = List.of(
		MAP_ID,
		MAP_ID,
		MAP_ID,
		COMPASS_ID,
		CLOCK_ID,
		MOON_DIAL_ID
	);
	private static final int FIRST_MAP_SLOT_INDEX = LAYOUT.indexOf(MAP_ID);
	public static final ItemDisplayContents EMPTY = new ItemDisplayContents(LAYOUT.size(), FIRST_MAP_SLOT_INDEX);
	public static final Codec<ItemDisplayContents> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		DisplaySlot.CODEC.listOf().fieldOf("slots").forGetter(ItemDisplayContents::asSlots),
		Codec.INT.fieldOf("chosen_map_slot").forGetter(ItemDisplayContents::findActiveMapSlot)
	).apply(instance, ItemDisplayContents::fromSlots));
	public static final StreamCodec<RegistryFriendlyByteBuf, ItemDisplayContents> STREAM_CODEC = StreamCodec.composite(
		ItemStack.OPTIONAL_STREAM_CODEC.apply(ByteBufCodecs.list()), contents -> new ArrayList<>(contents.items),
		ByteBufCodecs.VAR_INT, contents -> contents.chosenMapSlot,
		ItemDisplayContents::new
	);

	private final NonNullList<ItemStack> items;
	private final int chosenMapSlot;

	private ItemDisplayContents(int size, int chosenMapSlot) {
		this.items = NonNullList.withSize(size, ItemStack.EMPTY);
		this.chosenMapSlot = chosenMapSlot;
	}

	private ItemDisplayContents(List<ItemStack> items, int chosenMapSlot) {
		this.items = NonNullList.withSize(items.size(), ItemStack.EMPTY);
		for (int i = 0; i < items.size(); i++) {
			this.items.set(i, items.get(i).copy());
		}
		this.chosenMapSlot = chosenMapSlot;
	}

	private void copyInto(NonNullList<ItemStack> target) {
		for (int i = 0; i < target.size(); i++) {
			target.set(i, i < this.items.size() ? this.items.get(i).copy() : ItemStack.EMPTY);
		}
	}

	private static ItemDisplayContents fromSlots(List<DisplaySlot> slots, int chosenMapSlot) {
		OptionalInt maxSlot = slots.stream().mapToInt(DisplaySlot::index).max();
		if (maxSlot.isEmpty()) {
			return new ItemDisplayContents(LAYOUT.size(), chosenMapSlot);
		}

		ItemDisplayContents contents = new ItemDisplayContents(Math.max(LAYOUT.size(), maxSlot.getAsInt() + 1), chosenMapSlot);
		for (DisplaySlot slot : slots) {
			contents.items.set(slot.index(), slot.item());
		}
		return contents;
	}

	private List<DisplaySlot> asSlots() {
		List<DisplaySlot> slots = new ArrayList<>();
		for (int i = 0; i < this.items.size(); i++) {
			ItemStack stack = this.items.get(i);
			if (!stack.isEmpty()) {
				slots.add(new DisplaySlot(i, stack));
			}
		}
		return slots;
	}

	public int findActiveMapSlot() {
		return this.chosenMapSlot;
	}

	public NonNullList<ItemStack> items() {
		return this.items;
	}

	public int size() {
		return this.items.size();
	}

	public boolean isEmpty() {
		return this.items.stream().allMatch(ItemStack::isEmpty);
	}

	@Override
	public boolean equals(Object other) {
		return this == other || other instanceof ItemDisplayContents contents
			&& this.chosenMapSlot == contents.chosenMapSlot
			&& ItemStack.listMatches(this.items, contents.items);
	}

	@Override
	public int hashCode() {
		int result = 0;
		for (ItemStack stack : this.items) {
			result = result * 31 + ItemStack.hashItemAndComponents(stack);
		}
		return 31 * result + this.chosenMapSlot;
	}

	@Override
	public String toString() {
		return "ItemDisplayContents" + this.items + "chosenMapSlot" + this.chosenMapSlot;
	}

	private static ItemDisplayType layoutType(int index) {
		return TFRegistries.ITEM_DISPLAY_TYPE.getValue(LAYOUT.get(index));
	}

	public static final class Mutable {
		private final NonNullList<ItemStack> items;
		private int chosenMapSlot;

		public Mutable(ItemDisplayContents contents) {
			this.items = NonNullList.withSize(LAYOUT.size(), ItemStack.EMPTY);
			this.chosenMapSlot = contents.chosenMapSlot;
			contents.copyInto(this.items);
		}

		public int chosenMapSlot() {
			return this.chosenMapSlot;
		}

		private int findSwapSlot(ItemStack stack) {
			for (int i = 0; i < LAYOUT.size(); i++) {
				ItemDisplayType type = layoutType(i);
				if (type != null && type.validItems().test(stack)) {
					return i;
				}
			}
			return -1;
		}

		private int findInsertSlot(ItemStack stack) {
			for (int i = 0; i < LAYOUT.size(); i++) {
				ItemDisplayType type = layoutType(i);
				if (type != null && type.validItems().test(stack) && this.items.get(i).isEmpty()) {
					return i;
				}
			}
			return -1;
		}

		public boolean trySwap(SlotAccess source, Player player) {
			return this.trySwap(source, player, (stack, owner) -> InventoryUtil.giveItemToPlayer(owner, stack));
		}

		public boolean trySwap(SlotAccess source, Player player, BiConsumer<ItemStack, Player> remainder) {
			ItemStack slottedStack = source.get();
			if (slottedStack.isEmpty() || !slottedStack.getItem().canFitInsideContainerItems()) {
				return false;
			}

			int slotForStack = this.findInsertSlot(slottedStack);
			if (slotForStack < 0) {
				slotForStack = this.findSwapSlot(slottedStack);
			}
			if (slotForStack < 0) {
				return false;
			}

			ItemStack targetStack = this.items.get(slotForStack);
			if (!targetStack.isEmpty() && ItemStack.isSameItemSameComponents(slottedStack, targetStack)) {
				return false;
			}

			ItemStack insert = slottedStack.split(1);
			ItemStack replaced = this.items.set(slotForStack, insert);
			if (replaced.isEmpty()) {
				this.tryResetChosenMapSlot(slotForStack);
				return source.set(slottedStack);
			}

			boolean result = source.set(replaced);
			remainder.accept(slottedStack, player);
			return result;
		}

		@Nullable
		public ItemStack removeFirstFree(@Nullable Slot slot) {
			for (int i = 0; i < this.items.size(); i++) {
				ItemStack stack = this.items.get(i);
				if (!stack.isEmpty() && (slot == null || slot.mayPlace(stack))) {
					if (i == this.chosenMapSlot) {
						this.cycleChosenMapSlot();
					}
					return this.items.set(i, ItemStack.EMPTY);
				}
			}
			return null;
		}

		public ItemDisplayContents toImmutable() {
			return new ItemDisplayContents(this.items, this.chosenMapSlot);
		}

		public int cycleChosenMapSlot() {
			for (int index = this.chosenMapSlot + 1; index < this.items.size(); index++) {
				if (LAYOUT.get(index).equals(MAP_ID) && !this.items.get(index).isEmpty()) {
					this.chosenMapSlot = index;
					return this.chosenMapSlot;
				}
			}
			this.chosenMapSlot = -1;
			return this.chosenMapSlot;
		}

		private void tryResetChosenMapSlot(int index) {
			if (index < LAYOUT.size() && LAYOUT.get(index).equals(MAP_ID) && !this.hasOtherMaps(index)) {
				this.chosenMapSlot = index;
			}
		}

		private boolean hasOtherMaps(int mapIndex) {
			for (int i = 0; i < Math.min(this.items.size(), LAYOUT.size()); i++) {
				if (!this.items.get(i).isEmpty() && LAYOUT.get(i).equals(MAP_ID) && mapIndex != i) {
					return true;
				}
			}
			return false;
		}
	}

	private record DisplaySlot(int index, ItemStack item) {
		private static final Codec<DisplaySlot> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.fieldOf("slot").forGetter(DisplaySlot::index),
			ItemStack.CODEC.fieldOf("item").forGetter(DisplaySlot::item)
		).apply(instance, DisplaySlot::new));
	}
}
