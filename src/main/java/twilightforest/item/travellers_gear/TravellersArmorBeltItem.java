package twilightforest.item.travellers_gear;

import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.TooltipDisplay;
import twilightforest.init.TFDataComponents;
import twilightforest.init.TFSounds;
import twilightforest.init.custom.TravellersModifiersManager;
import twilightforest.tags.TFItemTags;

import java.util.Optional;

public class TravellersArmorBeltItem extends TravellersArmorItem {
	public static final ItemContainerContents DEFAULT_EMPTY_BELT_CONTAINER = ItemContainerContents.fromItems(NonNullList.withSize(9, ItemStack.EMPTY));

	public TravellersArmorBeltItem(int insertableModifierSlots, Properties properties) {
		super(insertableModifierSlots, properties);
	}

	public static Properties beltProperties(Properties properties) {
		return properties
			.component(TFDataComponents.SWAP_HOTBAR_ABILITY, Unit.INSTANCE)
			.component(DataComponents.CONTAINER, DEFAULT_EMPTY_BELT_CONTAINER)
			.component(TFDataComponents.TRAVELLERS_HAS_BELT, Unit.INSTANCE);
	}

	@Override
	public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
		TooltipDisplay display = stack.getOrDefault(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT);
		return display.shows(DataComponents.CONTAINER)
			? Optional.ofNullable(stack.get(DataComponents.CONTAINER)).map(Tooltip::new)
			: Optional.empty();
	}

	@Override
	public boolean canFitInsideContainerItems() {
		return false;
	}

	public static void travellersTrySwapHotbar(Player player) {
		ItemStack legArmor = player.getItemBySlot(EquipmentSlot.LEGS);
		ItemContainerContents container = legArmor.get(DataComponents.CONTAINER);
		if (!hasSwapHotbar(player, legArmor) || container == null) {
			return;
		}

		NonNullList<ItemStack> hotbar = NonNullList.withSize(9, ItemStack.EMPTY);
		NonNullList<ItemStack> beltItems = NonNullList.withSize(9, ItemStack.EMPTY);
		container.copyInto(beltItems);
		Inventory inventory = player.getInventory();
		boolean active = isSwapHotbarActive(player, legArmor);
		boolean changed = false;
		for (int slot = 0; slot < 9; slot++) {
			ItemStack inventoryStack = inventory.getItem(slot);
			ItemStack beltStack = beltItems.get(slot);
			boolean canStore = inventoryStack.getItem().canFitInsideContainerItems()
				&& !inventoryStack.has(DataComponents.CONTAINER)
				&& !inventoryStack.is(TFItemTags.TRAVELLERS_BELT_BLACKLISTED);
			if (canStore && (active || inventoryStack.isEmpty())) {
				hotbar.set(slot, inventoryStack);
				inventory.setItem(slot, beltStack);
				changed |= !beltStack.equals(inventoryStack);
			} else {
				hotbar.set(slot, beltStack);
			}
		}
		legArmor.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(hotbar));
		if (changed) {
			player.level().playSound(null, player, TFSounds.SWAP_HOTBAR, SoundSource.PLAYERS, 1.0F, 1.0F);
		}
	}

	public static boolean isSwapHotbarActive(Player player, ItemStack stack) {
		return (TravellersModifiersManager.isModifierActive(player, TravellersModifiersManager.SWAP_HOTBAR_MODIFIER)
			|| TravellersModifiersManager.isModifierActive(player, TravellersModifiersManager.SWAP_HOTBAR_ABILITY))
			&& stack.has(DataComponents.CONTAINER);
	}

	public static boolean hasSwapHotbar(Player player, ItemStack stack) {
		return (TravellersModifiersManager.hasTravellersModifier(player.registryAccess(), stack, TravellersModifiersManager.SWAP_HOTBAR_MODIFIER)
			|| TravellersModifiersManager.hasTravellersModifier(player.registryAccess(), stack, TravellersModifiersManager.SWAP_HOTBAR_ABILITY))
			&& stack.has(DataComponents.CONTAINER);
	}

	public record Tooltip(ItemContainerContents contents) implements TooltipComponent {
	}
}
