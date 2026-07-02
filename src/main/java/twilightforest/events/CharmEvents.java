package twilightforest.events;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityEquipment;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.minecraft.world.damagesource.DamageSource;
import twilightforest.network.PacketDistributor;
import twilightforest.TwilightForestMod;
import twilightforest.block.KeepsakeCasketBlock;
import twilightforest.block.entity.SkullChestBlockEntity;
import twilightforest.config.TFConfig;
import twilightforest.tags.TFItemTags;
import twilightforest.enums.BlockLoggingEnum;
import twilightforest.init.TFBlocks;
import twilightforest.init.TFDataAttachments;
import twilightforest.init.TFItems;
import twilightforest.init.TFSounds;
import twilightforest.init.TFStats;
import twilightforest.network.SpawnCharmPacket;
import twilightforest.mixin.accessor.BaseContainerBlockEntityAccessor;
import twilightforest.util.TFItemStackUtils;

import java.util.ArrayList;
import java.util.List;

public class CharmEvents {

	public static final String CHARM_INV_TAG = "TFCharmInventory";
	public static final String CASKET_DAMAGE_TAG = "CasketDamage";
	public static final String CONSUMED_CHARM_TAG = "CharmStack";

	// Check for charm of life first to stop a player from dying
	public static boolean tryPreventDeath(ServerPlayer player, DamageSource source) {
		if (player.level().isClientSide() || player instanceof FakePlayer || player.isCreative() || player.isSpectator()) {
			return false;
		}
		return charmOfLife(player);
	}

	// Then check if the player should keep any items through death
	public static void handleDeath(ServerPlayer player) {
		if (!(player.level() instanceof ServerLevel level) || player instanceof FakePlayer || player.isCreative() || player.isSpectator()) {
			return;
		}
		if (!level.getGameRules().get(GameRules.KEEP_INVENTORY)) {
			// Did the player recover? No? Let's give them their stuff based on the keeping charms
			charmOfKeeping(player);

			// Then let's store the rest of their stuff in the casket
			keepsakeCasket(player);
		}
	}

	public static void onPlayerRespawn(ServerPlayer player, boolean alive) {
		if (!alive) {
			returnStoredItems(player);
		}
	}

	private static boolean charmOfLife(Player player) {
		boolean charm2 = TFItemStackUtils.consumeInventoryItem(player, TFItems.CHARM_OF_LIFE_2.get(), getPlayerData(player), false) || hasCharmCurio(TFItems.CHARM_OF_LIFE_2.get(), player);
		boolean charm1 = !charm2 && (TFItemStackUtils.consumeInventoryItem(player, TFItems.CHARM_OF_LIFE_1.get(), getPlayerData(player), false) || hasCharmCurio(TFItems.CHARM_OF_LIFE_1.get(), player));

		if (charm2 || charm1) {
			if (charm1) {
				player.setHealth(8);
				player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0));
			}

			if (charm2) {
				player.setHealth(player.getMaxHealth());

				player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 600, 3));
				player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 600, 0));
				player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 600, 0));
			}

			if (player instanceof ServerPlayer serverPlayer) {
				PacketDistributor.sendToPlayer(serverPlayer, new SpawnCharmPacket(new ItemStack(charm1 ? TFItems.CHARM_OF_LIFE_1.get() : TFItems.CHARM_OF_LIFE_2.get()), ResourceKey.create(Registries.SOUND_EVENT, TFSounds.CHARM_LIFE.location())));
				serverPlayer.awardStat(TFStats.LIFE_CHARMS_ACTIVATED);
			}

			return true;
		}

		return false;
	}

	private static void charmOfKeeping(Player player) {
		//create a fake inventory to organize our kept inventory in
		Inventory keepInventory = new Inventory(player, new EntityEquipment());
		Inventory playerInventory = player.getInventory();
		List<Integer> allSlots = slotRange(0, playerInventory.getNonEquipmentItems().size());
		List<Integer> hotbarSlots = slotRange(0, Inventory.getSelectionSize());

		if (!applyCharm(TFItems.CHARM_OF_KEEPING_3.get(), keepInventory, player, allSlots)) {
			if (!applyCharm(TFItems.CHARM_OF_KEEPING_2.get(), keepInventory, player, hotbarSlots)) {
				int selected = playerInventory.getSelectedSlot();
				if (Inventory.isHotbarSlot(selected)) {
					applyCharm(TFItems.CHARM_OF_KEEPING_1.get(), keepInventory, player, List.of(selected));
				}
			}
		}

		//keep all items in the kept_on_death tag. This allows modpacks to support other items to keep on death
		for (int i = 0; i < playerInventory.getContainerSize(); i++) {
			ItemStack stack = playerInventory.getItem(i);
			if (stack.is(TFItemTags.KEPT_ON_DEATH)) {
				keepInventory.setItem(i, stack.copy());
				playerInventory.setItem(i, ItemStack.EMPTY);
			}
		}

		//take our fake inventory and save it to the persistent player data.
		//by saving it there we can guarantee we will always get all of our items back, even if the player logs out and back in.
		if (!keepInventory.isEmpty()) {
			getPlayerData(player).put(CHARM_INV_TAG, TFItemStackUtils.saveInventory(player.registryAccess(), keepInventory));
		}
	}

	private static boolean applyCharm(Item charm, Inventory keptInventory, Player player, List<Integer> inventorySlots) {
		Inventory playerInventory = player.getInventory();
		List<Integer> checkSlots = new ArrayList<>(inventorySlots);
		checkSlots.addAll(equipmentSlotIndices());

		boolean hasItems = false;
		for (int slot : checkSlots) {
			ItemStack stack = playerInventory.getItem(slot);
			if (!stack.isEmpty() && !stack.is(charm)) {
				hasItems = true;
				break;
			}
		}

		//first, check all affected slots to make sure they arent empty.
		//filter out the charm so it doesnt count towards keeping items if its the only thing we are holding
		if (!hasItems) {
			return false;
		}

		//do we even have a charm? No? Then stop operation
		if (!TFItemStackUtils.consumeInventoryItem(player, charm, getPlayerData(player), true) && !hasCharmCurio(charm, player)) {
			return false;
		}

		boolean keptACasket = keepSlotsAndCheckCasket(keptInventory, playerInventory, inventorySlots, charm == TFItems.CHARM_OF_KEEPING_3.get());
		keepSlotsAndCheckCasket(keptInventory, playerInventory, equipmentSlotIndices(), keptACasket);

		return true;
	}

	private static void keepsakeCasket(Player player) {
		//make sure we are still actually holding onto items before trying to place a casket
		if (player.getInventory().contains(stack -> !stack.isEmpty() && !stack.is(TFItems.KEEPSAKE_CASKET.get()))) {
			boolean casketConsumed = TFItemStackUtils.consumeInventoryItem(player, TFItems.KEEPSAKE_CASKET.get(), getPlayerData(player), false);

			if (!casketConsumed)
				return;

			Level level = player.level();
			BlockPos.MutableBlockPos pos = player.blockPosition().mutable();

			if (pos.getY() < level.dimensionType().minY() + 2) {
				pos.setY(level.dimensionType().minY() + 2);
			} else {
				int logicalHeight = player.level().dimensionType().logicalHeight();

				if (pos.getY() > logicalHeight) {
					pos.setY(logicalHeight - 1);
				}
			}

			pos.move(0, -1, 0);

			do {
				pos.move(0, 1, 0);
			} while (!level.getBlockState(pos).canBeReplaced());

			BlockPos immutablePos = pos.immutable();
			FluidState fluidState = level.getFluidState(immutablePos);

			int damage = getPlayerData(player).getIntOr(CASKET_DAMAGE_TAG, 0);
			BlockState setState = TFBlocks.KEEPSAKE_CASKET.get().defaultBlockState()
				.setValue(BlockLoggingEnum.MULTILOGGED, BlockLoggingEnum.getFromFluid(fluidState.getType()))
				.setValue(KeepsakeCasketBlock.BREAKAGE, damage)
				.setValue(KeepsakeCasketBlock.FACING, Direction.from2DDataValue(level.getRandom().nextInt(3)));

			if (player.getRandom().nextFloat() <= 0.15F) {
				if (damage >= 2) {
					setState = TFBlocks.SKULL_CHEST.get().withPropertiesOf(setState);
					TwilightForestMod.LOGGER.debug("{}'s Casket damage value was too high, placing Skull Chest instead", player.getName().getString());
				} else {
					damage = damage + 1;
					setState = TFBlocks.KEEPSAKE_CASKET.get().withPropertiesOf(setState).setValue(KeepsakeCasketBlock.BREAKAGE, damage);
					TwilightForestMod.LOGGER.debug("{}'s Casket was randomly damaged, applying new damage", player.getName().getString());
				}
			}

			if (!level.setBlockAndUpdate(immutablePos, setState)) {
				TwilightForestMod.LOGGER.error("Could not place Keepsake Casket at {}", pos);
				return;
			}

			if (!(level.getBlockEntity(immutablePos) instanceof SkullChestBlockEntity casket)) {
				TwilightForestMod.LOGGER.error("Failed to set Keepsake Casket data at {}", pos);
				return;
			}

			if (TFConfig.casketUUIDLocking) {
				//make it so only the player who died can open the chest if our config allows us
				casket.owner = ResolvableProfile.createResolved(player.getGameProfile());
			} else {
				casket.owner = null;
			}

			//some names are way too long for the casket so we'll cut them down
			String modifiedName = player.getName().getString().substring(0, Math.min(12, player.getName().getString().length()));
			((BaseContainerBlockEntityAccessor) casket).twilightforest$setName(Component.literal(modifiedName + "'s " + (level.getRandom().nextInt(1000) == 0 ? "Costco Casket" : casket.getDisplayName().getString())));

			int casketCapacity = casket.getContainerSize();
			List<ItemStack> list = new ArrayList<>(casketCapacity);
			NonNullList<ItemStack> filler = NonNullList.withSize(4, ItemStack.EMPTY);

			// lets add our inventory exactly how it was on us
			list.addAll(TFItemStackUtils.sortArmorForCasket(player));
			player.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
			player.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
			player.setItemSlot(EquipmentSlot.LEGS, ItemStack.EMPTY);
			player.setItemSlot(EquipmentSlot.FEET, ItemStack.EMPTY);
			list.addAll(filler);
			list.add(player.getItemBySlot(EquipmentSlot.OFFHAND));
			player.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
			list.addAll(TFItemStackUtils.sortInvForCasket(player));
			NonNullList<ItemStack> nonEquipmentItems = player.getInventory().getNonEquipmentItems();
			for (int i = 0; i < nonEquipmentItems.size(); i++) {
				nonEquipmentItems.set(i, ItemStack.EMPTY);
			}

			casket.setItems(NonNullList.of(ItemStack.EMPTY, list.toArray(new ItemStack[casketCapacity])));
			getPlayerData(player).remove(CASKET_DAMAGE_TAG);
		} else {
			//inventory is empty minus the casket: put the casket into the kept inventory
			for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
				if (player.getInventory().getItem(i).is(TFItems.KEEPSAKE_CASKET.get())) {
					Inventory tmp = new Inventory(player, new EntityEquipment());
					TFItemStackUtils.loadNoClear(player.registryAccess(), getPlayerData(player).getListOrEmpty(CHARM_INV_TAG), tmp);
					tmp.add(player.getInventory().getItem(i).copy());
					player.getInventory().setItem(i, ItemStack.EMPTY);
					getPlayerData(player).put(CHARM_INV_TAG, TFItemStackUtils.saveInventory(player.registryAccess(), tmp));
				}
			}
		}
	}

	/**
	 * Maybe we kept some stuff for the player!
	 */
	private static void returnStoredItems(Player player) {

		TwilightForestMod.LOGGER.debug("Player {} ({}) respawned and received items held in storage", player.getName().getString(), player.getUUID());

		//check if our tag is in the persistent player data. If so, copy that inventory over to our own. Cloud storage at its finest!
		CompoundTag playerData = getPlayerData(player);
		if (!player.level().isClientSide() && playerData.contains(CHARM_INV_TAG)) {
			ListTag tagList = playerData.getListOrEmpty(CHARM_INV_TAG);
			TFItemStackUtils.loadNoClear(player.registryAccess(), tagList, player.getInventory());
			playerData.remove(CHARM_INV_TAG);
		}

		// spawn effect thingers
		if (playerData.contains(CONSUMED_CHARM_TAG)) {
			ItemStack stack = TFItemStackUtils.loadItem(player.registryAccess(), playerData.getCompoundOrEmpty(CONSUMED_CHARM_TAG));

			if (player instanceof ServerPlayer serverPlayer) {
				PacketDistributor.sendToPlayer(serverPlayer, new SpawnCharmPacket(stack, ResourceKey.create(Registries.SOUND_EVENT, TFSounds.CHARM_KEEP.location())));
				serverPlayer.awardStat(TFStats.KEEPING_CHARMS_ACTIVATED);
			}
			playerData.remove(CONSUMED_CHARM_TAG);
		}
	}

	public static CompoundTag getPlayerData(Player player) {
		return TFDataAttachments.get(player, TFDataAttachments.CHARM_DATA);
	}

	//transfers a list of items to another
	private static boolean keepSlotsAndCheckCasket(Inventory transferTo, Inventory transferFrom, List<Integer> slots, boolean skipCasketCheck) {
		boolean keptCasket = false;
		for (int slot : slots) {
			var item = transferFrom.getItem(slot);
			if (item.isEmpty()) {
				continue;
			}
			var copy = item.copy();
			if (skipCasketCheck || (!copy.is(TFItems.KEEPSAKE_CASKET.get()) || keptCasket)) {
				transferTo.setItem(slot, copy);
				transferFrom.setItem(slot, ItemStack.EMPTY);
			} else {
				keptCasket = true;
				if (copy.getCount() > 1) {
					copy.shrink(1);
					transferTo.setItem(slot, copy);
					transferFrom.setItem(slot, copy.copyWithCount(1));
				}
			}
		}
		return keptCasket || skipCasketCheck;
	}

	private static List<Integer> equipmentSlotIndices() {
		List<Integer> indices = new ArrayList<>();
		indices.add(TFItemStackUtils.equipmentSlotIndex(EquipmentSlot.HEAD));
		indices.add(TFItemStackUtils.equipmentSlotIndex(EquipmentSlot.CHEST));
		indices.add(TFItemStackUtils.equipmentSlotIndex(EquipmentSlot.LEGS));
		indices.add(TFItemStackUtils.equipmentSlotIndex(EquipmentSlot.FEET));
		indices.add(TFItemStackUtils.equipmentSlotIndex(EquipmentSlot.OFFHAND));
		indices.removeIf(index -> index == Inventory.NOT_FOUND_INDEX);
		return indices;
	}

	private static List<Integer> slotRange(int startInclusive, int endExclusive) {
		List<Integer> slots = new ArrayList<>();
		for (int i = startInclusive; i < endExclusive; i++) {
			slots.add(i);
		}
		return slots;
	}

	private static boolean hasCharmCurio(Item item, Player player) {
//		if (ModList.get().isLoaded("curios")) {
//			return CuriosCompat.findAndConsumeCurio(item, player);
//		}

		return false;
	}
}
