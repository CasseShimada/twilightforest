package twilightforest.command;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import twilightforest.TFRegistries;
import twilightforest.TwilightForestMod;
import twilightforest.item.travellers_gear.modifiers.TravellersModifier;
import twilightforest.test.MinecraftBootstrapExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MinecraftBootstrapExtension.class)
class CommandCompatibilityTests {
	private static final Set<String> RESTORED_TRANSLATIONS = Set.of(
		"commands.tffeature.ability_modifier",
		"commands.tffeature.added_modifier",
		"commands.tffeature.has_modifier",
		"commands.tffeature.invalid_modifier",
		"commands.tffeature.no_modifier",
		"commands.tffeature.not_travellers_gear",
		"commands.tffeature.removed_modifier",
		"commands.tffeature.teleport.dimension_missing",
		"commands.tffeature.teleport.player_only",
		"commands.tffeature.teleport.success",
		"commands.tffeature.too_many_modifiers",
		"commands.tffeature.wrong_modifier_slot"
	);

	@Test
	void restoredSubtreesKeepExactShapeAndGamemasterPermission() {
		LiteralCommandNode<CommandSourceStack> teleport = new TFTeleportCommand().register().build();
		LiteralCommandNode<CommandSourceStack> travellers = new TravellersGearCommand().register().build();
		CommandSourceStack source = mock(CommandSourceStack.class);

		assertEquals("tp", teleport.getName());
		assertTrue(teleport.getCommand() != null);
		assertEquals(Set.of("add_modifier", "remove_modifier"), Set.of(
			travellers.getChildren().stream().map(node -> node.getName()).toArray(String[]::new)
		));
		for (String operation : Set.of("add_modifier", "remove_modifier")) {
			assertEquals(Set.of("modifier"), Set.of(
				travellers.getChild(operation).getChildren().stream().map(node -> node.getName()).toArray(String[]::new)
			));
			assertTrue(travellers.getChild(operation).getChild("modifier").getCommand() != null);
		}

		when(source.permissions()).thenReturn(PermissionSet.NO_PERMISSIONS);
		assertFalse(teleport.canUse(source));
		assertFalse(travellers.canUse(source));
		when(source.permissions()).thenReturn(PermissionSet.ALL_PERMISSIONS);
		assertTrue(teleport.canUse(source));
		assertTrue(travellers.canUse(source));
	}

	@Test
	void rootCommandAttachesBothRestoredSubtrees() throws IOException {
		String source = Files.readString(Path.of("src/main/java/twilightforest/command/TFCommand.java"));
		assertTrue(source.contains(".then(tfTeleportCommand.register())"));
		assertTrue(source.contains(".then(travellersGearCommand.register())"));
		String travellers = Files.readString(Path.of("src/main/java/twilightforest/command/TravellersGearCommand.java"));
		assertTrue(travellers.contains("modifier.isAbility()"));
		assertTrue(travellers.contains("instanceof InsertableTravellersModifier"));
		assertTrue(travellers.contains("player instanceof FakePlayer"));
	}

	@Test
	void teleportPreservesRelativeBuildHeightAndLocaleIndependentFeedback() {
		assertEquals(-64.0D, TFTeleportCommand.convertY(0.0D, 0, 256, -64, 320));
		assertEquals(128.0D, TFTeleportCommand.convertY(128.0D, 0, 256, -64, 320));
		assertEquals(320.0D, TFTeleportCommand.convertY(256.0D, 0, 256, -64, 320));

		Locale previous = Locale.getDefault();
		try {
			Locale.setDefault(Locale.GERMANY);
			assertEquals("12.5", TFTeleportCommand.formatCoordinate(12.5D));
		} finally {
			Locale.setDefault(previous);
		}
	}

	@Test
	void travellersValidationKeepsPlayerGearAndAbilityFailures() {
		TravellersGearCommand command = new TravellersGearCommand();
		CommandSourceStack source = mock(CommandSourceStack.class);
		assertTranslation("commands.tffeature.not_player", assertThrows(CommandSyntaxException.class, () -> command.validate(source, null)));

		ServerPlayer player = mock(ServerPlayer.class);
		when(source.getEntity()).thenReturn(player);
		var stoneHolder = Items.STONE.builtInRegistryHolder();
		if (!stoneHolder.areComponentsBound()) {
			stoneHolder.bindComponents(DataComponentMap.builder().set(DataComponents.MAX_STACK_SIZE, 64).build());
		}
		ItemStack stone = new ItemStack(Items.STONE);
		when(player.getMainHandItem()).thenReturn(stone);
		assertTranslation("commands.tffeature.not_travellers_gear", assertThrows(CommandSyntaxException.class, () -> command.validate(source, null)));

		assertTranslation("commands.tffeature.ability_modifier", TravellersGearCommand.ERROR_ABILITY.create());
	}

	@Test
	void travellersAddAndRemoveFailuresKeepHistoricalPrecedence() {
		Component modifierName = Component.literal("modifier");
		assertTranslation("commands.tffeature.too_many_modifiers", assertThrows(CommandSyntaxException.class,
			() -> TravellersGearCommand.validateAdd(2, 2, true, false, modifierName)));
		assertTranslation("commands.tffeature.has_modifier", assertThrows(CommandSyntaxException.class,
			() -> TravellersGearCommand.validateAdd(0, 2, true, false, modifierName)));
		assertTranslation("commands.tffeature.wrong_modifier_slot", assertThrows(CommandSyntaxException.class,
			() -> TravellersGearCommand.validateAdd(0, 2, false, false, modifierName)));
		assertTranslation("commands.tffeature.no_modifier", assertThrows(CommandSyntaxException.class,
			() -> TravellersGearCommand.validateRemove(false, modifierName)));
	}

	@Test
	void dynamicModifierResolutionRejectsUnknownKeys() throws CommandSyntaxException {
		HolderLookup.Provider registries = mock(HolderLookup.Provider.class);
		@SuppressWarnings("unchecked") HolderLookup.RegistryLookup<TravellersModifier> lookup = mock(HolderLookup.RegistryLookup.class);
		@SuppressWarnings("unchecked") Holder.Reference<TravellersModifier> expected = mock(Holder.Reference.class);
		ResourceKey<TravellersModifier> key = ResourceKey.create(TFRegistries.Keys.TRAVELLERS_MODIFIERS, TwilightForestMod.prefix("test"));
		when(registries.lookupOrThrow(TFRegistries.Keys.TRAVELLERS_MODIFIERS)).thenReturn(lookup);
		when(lookup.get(key)).thenReturn(Optional.empty());
		assertTranslation("commands.tffeature.invalid_modifier", assertThrows(CommandSyntaxException.class,
			() -> TravellersGearCommand.resolveModifier(registries, key)));

		when(lookup.get(key)).thenReturn(Optional.of(expected));
		assertSame(expected, TravellersGearCommand.resolveModifier(registries, key));
	}

	@Test
	void restoredEnglishAndUpsideDownTranslationsArePackaged() throws IOException {
		for (String locale : Set.of("en_us", "en_ud")) {
			Path path = Path.of("src/generated/resources/assets/twilightforest/lang/" + locale + ".json");
			JsonObject language = JsonParser.parseString(Files.readString(path)).getAsJsonObject();
			for (String key : RESTORED_TRANSLATIONS) {
				assertTrue(language.has(key), () -> locale + " is missing " + key);
				assertFalse(language.get(key).getAsString().isBlank(), () -> locale + " has a blank " + key);
			}
		}
	}

	private static void assertTranslation(String expectedKey, CommandSyntaxException exception) {
		Component component = (Component) exception.getRawMessage();
		TranslatableContents contents = (TranslatableContents) component.getContents();
		assertEquals(expectedKey, contents.getKey());
	}
}
