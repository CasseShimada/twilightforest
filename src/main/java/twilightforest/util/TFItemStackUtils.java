package twilightforest.util;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ItemLike;
import twilightforest.api.AccessoryApi;
import twilightforest.api.AccessoryConsumptionContext;
import twilightforest.api.AccessoryConsumptionResult;
import twilightforest.block.KeepsakeCasketBlock;
import twilightforest.events.CharmEvents;
import twilightforest.init.TFDataComponents;
import twilightforest.TwilightForestMod;

import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class TFItemStackUtils {

	public static boolean consumeInventoryItem(final Player player, final ItemLike item, CompoundTag persistentTag, boolean saveItemToTag) {
		Inventory inventory = player.getInventory();
		if (consumeInventoryItem(inventory.getNonEquipmentItems(), item, persistentTag, saveItemToTag, player.registryAccess())) {
			return true;
		}

		boolean consumedFromEquipment = consumeEquipmentSlot(player, EquipmentSlot.HEAD, item, persistentTag, saveItemToTag)
			|| consumeEquipmentSlot(player, EquipmentSlot.CHEST, item, persistentTag, saveItemToTag)
			|| consumeEquipmentSlot(player, EquipmentSlot.LEGS, item, persistentTag, saveItemToTag)
			|| consumeEquipmentSlot(player, EquipmentSlot.FEET, item, persistentTag, saveItemToTag)
			|| consumeEquipmentSlot(player, EquipmentSlot.OFFHAND, item, persistentTag, saveItemToTag);
		if (consumedFromEquipment) {
			return true;
		}

		if (player instanceof ServerPlayer serverPlayer) {
			AccessoryConsumptionResult result = AccessoryApi.tryConsume(new AccessoryConsumptionContext(serverPlayer, item.asItem()));
			if (result.wasConsumed()) {
				recordPreConsumptionStack(player.registryAccess(), result.consumedStack(), persistentTag, saveItemToTag);
				return true;
			}
		}

		return false;
	}

	public static boolean consumeInventoryItem(final NonNullList<ItemStack> stacks, final ItemLike item, CompoundTag persistentTag, boolean saveItemToTag, RegistryAccess registryAccess) {
		for (ItemStack stack : stacks) {
			if (stack.is(item.asItem())) {
				recordPreConsumptionStack(registryAccess, stack, persistentTag, saveItemToTag);
				stack.shrink(1);
				return true;
			}
		}

		return false;
	}

	private static boolean consumeEquipmentSlot(Player player, EquipmentSlot slot, ItemLike item, CompoundTag persistentTag, boolean saveItemToTag) {
		ItemStack stack = player.getItemBySlot(slot);
		if (!stack.is(item.asItem())) {
			return false;
		}
		recordPreConsumptionStack(player.registryAccess(), stack, persistentTag, saveItemToTag);
		stack.shrink(1);
		player.setItemSlot(slot, stack);
		return true;
	}

	private static void recordPreConsumptionStack(RegistryAccess registryAccess, ItemStack stack, CompoundTag persistentTag, boolean saveItemToTag) {
		if (saveItemToTag) persistentTag.put(CharmEvents.CONSUMED_CHARM_TAG, saveItem(registryAccess, stack));
		BlockItemStateProperties blockItemStateProperties = stack.get(DataComponents.BLOCK_STATE);
		if (blockItemStateProperties != null && blockItemStateProperties.properties().containsKey(KeepsakeCasketBlock.BREAKAGE.getName())) {
			String propertyValueString = blockItemStateProperties.properties().get(KeepsakeCasketBlock.BREAKAGE.getName());
			persistentTag.putInt(CharmEvents.CASKET_DAMAGE_TAG, isNumeric(propertyValueString) ? Integer.parseInt(propertyValueString) : 0);
		} else if (stack.has(TFDataComponents.CASKET_DAMAGE)) {
			persistentTag.putInt(CharmEvents.CASKET_DAMAGE_TAG, stack.getOrDefault(TFDataComponents.CASKET_DAMAGE, 0));
		}
	}

	public static NonNullList<ItemStack> sortArmorForCasket(Player player) {
		NonNullList<ItemStack> armor = NonNullList.create();
		armor.add(player.getItemBySlot(EquipmentSlot.HEAD));
		armor.add(player.getItemBySlot(EquipmentSlot.CHEST));
		armor.add(player.getItemBySlot(EquipmentSlot.LEGS));
		armor.add(player.getItemBySlot(EquipmentSlot.FEET));
		return armor;
	}

	public static NonNullList<ItemStack> sortInvForCasket(Player player) {
		NonNullList<ItemStack> inv = player.getInventory().getNonEquipmentItems();
		NonNullList<ItemStack> sorted = NonNullList.create();
		//hotbar at the bottom
		sorted.addAll(inv.subList(9, 36));
		sorted.addAll(inv.subList(0, 9));

		return sorted;
	}

	public static NonNullList<ItemStack> splitToSize(ItemStack stack) {

		NonNullList<ItemStack> result = NonNullList.create();

		int size = stack.getMaxStackSize();

		while (!stack.isEmpty()) {
			result.add(stack.split(size));
		}

		return result;
	}

	public static boolean hasInfoTag(ItemStack stack, String key) {
		CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
		return customData != null && customData.copyTag().contains(key);
	}

	public static void addInfoTag(ItemStack stack, String key) {
		CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
		CompoundTag nbt = customData == null ? new CompoundTag() : customData.copyTag();
		nbt.putBoolean(key, true);
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
	}

	public static void clearInfoTag(ItemStack stack, String key) {
		CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
		if (customData != null) {
			CompoundTag nbt = customData.copyTag();
			nbt.remove(key);
			stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
		}
	}

	//[VanillaCopy] of Inventory.load, but removed clearing all slots
	//also add a handler to move items to the next available slot if the slot they want to go to isnt available
	public static void loadNoClear(RegistryAccess registryAccess, ListTag tag, Inventory inventory) {

		List<ItemStack> blockedItems = new ArrayList<>();

		for (int i = 0; i < tag.size(); ++i) {
			CompoundTag compoundtag = tag.getCompoundOrEmpty(i);
			int j = compoundtag.getInt("Slot").orElse(compoundtag.getByteOr("Slot", (byte) 0) & 255);
			ItemStack itemstack = loadItem(registryAccess, compoundtag);
			if (!itemstack.isEmpty()) {
				if (j < inventory.getContainerSize()) {
					if (inventory.getItem(j).isEmpty()) {
						inventory.setItem(j, itemstack);
					} else {
						blockedItems.add(itemstack);
					}
				}
			}
		}

		if (!blockedItems.isEmpty()) blockedItems.forEach(inventory::add);
	}

	public static ListTag saveInventory(RegistryAccess registryAccess, Inventory inventory) {
		ListTag tagList = new ListTag();
		for (int i = 0; i < inventory.getContainerSize(); i++) {
			ItemStack stack = inventory.getItem(i);
			if (!stack.isEmpty()) {
				CompoundTag itemTag = saveItem(registryAccess, stack);
				itemTag.putInt("Slot", i);
				tagList.add(itemTag);
			}
		}
		return tagList;
	}

	public static CompoundTag saveItem(RegistryAccess registryAccess, ItemStack stack) {
		RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, registryAccess);
		return ItemStack.CODEC.encodeStart(ops, stack)
			.resultOrPartial(TwilightForestMod.LOGGER::error)
			.filter(CompoundTag.class::isInstance)
			.map(CompoundTag.class::cast)
			.orElseGet(CompoundTag::new);
	}

	public static ItemStack loadItem(RegistryAccess registryAccess, CompoundTag tag) {
		RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, registryAccess);
		CompoundTag itemTag = tag.contains("Item") ? tag.getCompoundOrEmpty("Item") : tag.copy();
		itemTag.remove("Slot");
		return ItemStack.CODEC.parse(ops, itemTag)
			.resultOrPartial(TwilightForestMod.LOGGER::error)
			.orElse(ItemStack.EMPTY);
	}

	public static int equipmentSlotIndex(EquipmentSlot slot) {
		for (var entry : Inventory.EQUIPMENT_SLOT_MAPPING.int2ObjectEntrySet()) {
			if (entry.getValue() == slot) {
				return entry.getIntKey();
			}
		}
		return Inventory.NOT_FOUND_INDEX;
	}

	public static boolean isAtZeroDurability(ItemStack stack) {
		return stack.isDamageableItem() && stack.getDamageValue() >= stack.getMaxDamage();
	}

	public static void hurtWithoutBreaking(ItemStack stack, int amount, @Nullable Player player) {
		if (stack.isDamageableItem()) {
			if (player != null) {
				stack.hurtWithoutBreaking(amount, player);
			} else {
				int newDamage = Math.min(stack.getDamageValue() + amount, stack.getMaxDamage() - 1);
				stack.setDamageValue(newDamage);
			}
		}
	}

	private static boolean isNumeric(@Nullable String value) {
		if (value == null || value.isEmpty()) {
			return false;
		}
		for (int i = 0; i < value.length(); i++) {
			if (!Character.isDigit(value.charAt(i))) {
				return false;
			}
		}
		return true;
	}
}
