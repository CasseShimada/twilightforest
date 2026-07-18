package twilightforest.api;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiGameplayIntegrationTests {
	@Test
	void builtinsRegisterAfterItemsAndFreezeAtServerStart() throws IOException {
		String initializer = source("src/main/java/twilightforest/TwilightForestMod.java");
		int itemsInitialization = initializer.indexOf("TFItems.init();");
		int apiInitialization = initializer.indexOf("TwilightForestApiImplementation.registerBuiltins();");
		assertTrue(itemsInitialization >= 0 && itemsInitialization < apiInitialization);
		assertTrue(initializer.contains(
			"ServerLifecycleEvents.SERVER_STARTING.register(server -> TwilightForestApi.freezeRegistrations())"));

		String implementation = source("src/main/java/twilightforest/TwilightForestApiImplementation.java");
		assertTrue(implementation.contains("built_in_traveller_gear"));
		assertTrue(implementation.contains("built_in_armor"));
		assertTrue(implementation.contains("built_in_weapon"));
		assertTrue(implementation.contains("lookupOrThrow(TFRegistries.Keys.TRAVELLERS_MODIFIERS)"));
		assertTrue(implementation.contains("holder.value().isActive"));
		assertFalse(implementation.contains("net.minecraft.client"));
	}

	@Test
	void builtInClassifiersPreserveSubtypeBehaviorAndSlotScopedEffects() throws IOException {
		String implementation = source("src/main/java/twilightforest/TwilightForestApiImplementation.java");
		assertTrue(implementation.contains("stack.getItem() instanceof FieryArmorItem"));
		assertTrue(implementation.contains("stack.getItem() instanceof YetiArmorItem"));
		assertTrue(implementation.contains("item instanceof MinotaurAxeItem"));
		assertTrue(implementation.contains("item instanceof MazebreakerPickItem"));
		assertTrue(implementation.contains("CHARGE_WEAPONS.contains(item) || minotaurAxe"));
		assertTrue(implementation.contains("stack.is(TFItems.MAZEBREAKER_PICKAXE) || mazebreaker"));

		int groupFilter = implementation.indexOf("holder.value().group().test(part.equipmentSlot())");
		int activeCheck = implementation.indexOf("holder.value().isActive(equippedStack");
		assertTrue(groupFilter >= 0 && groupFilter < activeCheck);
	}

	@Test
	void publicTraitsDriveTheExistingGameplayHooks() throws IOException {
		String tools = source("src/main/java/twilightforest/events/ToolEvents.java");
		assertTrue(tools.contains("WeaponApi.IGNITES_TARGETS"));
		assertTrue(tools.contains("WeaponApi.BONUS_AGAINST_ARMORED"));
		assertTrue(tools.contains("WeaponApi.BONUS_AGAINST_UNARMORED"));
		assertTrue(tools.contains("WeaponApi.SPRINT_CHARGE_BONUS"));
		assertTrue(tools.contains("WeaponApi.MAZESTONE_WEAR_EXEMPT"));

		String loot = source("src/main/java/twilightforest/events/LootEvents.java");
		assertTrue(loot.contains("WeaponApi.SMELTS_BLOCK_DROPS"));

		String entities = source("src/main/java/twilightforest/events/EntityEvents.java");
		assertTrue(entities.contains("WeaponApi.RESETS_ARROW_INVULNERABILITY"));
		assertTrue(entities.contains("ArmorApi.FIERY_REACTIVE"));
		assertTrue(entities.contains("ArmorApi.CHILL_AURA"));

		String fieryBlock = source("src/main/java/twilightforest/block/FieryBlock.java");
		assertTrue(fieryBlock.contains("ArmorApi.FIERY_STEP_IMMUNITY"));
	}

	@Test
	void accessoryDispatchRemainsAThreadAndSlotSafeFallback() throws IOException {
		String utility = source("src/main/java/twilightforest/util/TFItemStackUtils.java");
		int offhand = utility.indexOf("consumeEquipmentSlot(player, EquipmentSlot.OFFHAND");
		int serverBoundary = utility.indexOf("player instanceof ServerPlayer serverPlayer");
		int dispatch = utility.indexOf("AccessoryApi.tryConsume");
		assertTrue(offhand >= 0 && offhand < serverBoundary && serverBoundary < dispatch);
		assertTrue(utility.contains("recordPreConsumptionStack(player.registryAccess(), result.consumedStack()"));
	}

	private static String source(String path) throws IOException {
		return Files.readString(Path.of(path));
	}
}
