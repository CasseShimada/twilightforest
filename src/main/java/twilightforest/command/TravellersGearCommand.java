package twilightforest.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import twilightforest.TFRegistries;
import twilightforest.init.custom.TravellersModifiersManager;
import twilightforest.item.travellers_gear.modifiers.InsertableTravellersModifier;
import twilightforest.item.travellers_gear.modifiers.TravellersModifiable;
import twilightforest.item.travellers_gear.modifiers.TravellersModifier;

import java.util.function.Function;

public class TravellersGearCommand {
	static final DynamicCommandExceptionType ERROR_INVALID_MODIFIER = new DynamicCommandExceptionType(value -> Component.translatableEscape("commands.tffeature.invalid_modifier", value));
	static final SimpleCommandExceptionType ERROR_NOT_RUN_BY_PLAYER = new SimpleCommandExceptionType(Component.translatable("commands.tffeature.not_player"));
	static final SimpleCommandExceptionType ERROR_NOT_HOLDING_GEAR = new SimpleCommandExceptionType(Component.translatable("commands.tffeature.not_travellers_gear"));
	static final SimpleCommandExceptionType ERROR_TOO_MANY_MODIFIERS = new SimpleCommandExceptionType(Component.translatable("commands.tffeature.too_many_modifiers"));
	static final Function<Component, SimpleCommandExceptionType> ERROR_NO_MODIFIER = component -> new SimpleCommandExceptionType(Component.translatable("commands.tffeature.no_modifier", component));
	static final Function<Component, SimpleCommandExceptionType> ERROR_HAS_MODIFIER = component -> new SimpleCommandExceptionType(Component.translatable("commands.tffeature.has_modifier", component));
	static final Function<Component, SimpleCommandExceptionType> ERROR_WRONG_SLOT = component -> new SimpleCommandExceptionType(Component.translatable("commands.tffeature.wrong_modifier_slot", component));
	static final SimpleCommandExceptionType ERROR_ABILITY = new SimpleCommandExceptionType(Component.translatable("commands.tffeature.ability_modifier"));

	public LiteralArgumentBuilder<CommandSourceStack> register() {
		return Commands.literal("travellers_gear")
			.requires(source -> Commands.LEVEL_GAMEMASTERS.check(source.permissions()))
			.then(Commands.literal("add_modifier")
				.then(Commands.argument("modifier", ResourceKeyArgument.key(TFRegistries.Keys.TRAVELLERS_MODIFIERS))
					.executes(context -> this.addModifier(context.getSource(), resolveModifier(context, "modifier")))))
			.then(Commands.literal("remove_modifier")
				.then(Commands.argument("modifier", ResourceKeyArgument.key(TFRegistries.Keys.TRAVELLERS_MODIFIERS))
					.executes(context -> this.removeModifier(context.getSource(), resolveModifier(context, "modifier")))));
	}

	private Holder.Reference<TravellersModifier> resolveModifier(CommandContext<CommandSourceStack> context, String name) throws CommandSyntaxException {
		ResourceKey<TravellersModifier> key = ResourceKeyArgument.getRegistryKey(context, name, TFRegistries.Keys.TRAVELLERS_MODIFIERS, ERROR_INVALID_MODIFIER);
		return resolveModifier(context.getSource().registryAccess(), key);
	}

	static Holder.Reference<TravellersModifier> resolveModifier(HolderLookup.Provider registries, ResourceKey<TravellersModifier> key) throws CommandSyntaxException {
		return registries.lookupOrThrow(TFRegistries.Keys.TRAVELLERS_MODIFIERS).get(key)
			.orElseThrow(() -> ERROR_INVALID_MODIFIER.create(key.identifier()));
	}

	private int addModifier(CommandSourceStack source, Holder.Reference<TravellersModifier> modifier) throws CommandSyntaxException {
		Context context = validate(source, modifier);
		validateAdd(
			TravellersModifiersManager.countInsertableModifiers(source.registryAccess(), context.stack()),
			context.item().getModifierSlots(),
			TravellersModifiersManager.hasTravellersModifier(source.registryAccess(), context.stack(), modifier.key()),
			modifier.value().group().test(context.player().getEquipmentSlotForItem(context.stack())),
			context.modifierName()
		);

		context.modifier().addModifier(context.stack());
		source.sendSuccess(() -> Component.translatable("commands.tffeature.added_modifier", context.modifierName(), context.stack().getHoverName()), true);
		return Command.SINGLE_SUCCESS;
	}

	private int removeModifier(CommandSourceStack source, Holder.Reference<TravellersModifier> modifier) throws CommandSyntaxException {
		Context context = validate(source, modifier);
		validateRemove(TravellersModifiersManager.hasTravellersModifier(source.registryAccess(), context.stack(), modifier.key()), context.modifierName());

		context.modifier().removeModifier(context.stack());
		source.sendSuccess(() -> Component.translatable("commands.tffeature.removed_modifier", context.modifierName(), context.stack().getHoverName()), true);
		return Command.SINGLE_SUCCESS;
	}

	Context validate(CommandSourceStack source, Holder.Reference<TravellersModifier> modifier) throws CommandSyntaxException {
		if (!(source.getEntity() instanceof ServerPlayer player) || player instanceof FakePlayer) {
			throw ERROR_NOT_RUN_BY_PLAYER.create();
		}
		ItemStack stack = player.getMainHandItem();
		if (!(stack.getItem() instanceof TravellersModifiable item)) {
			throw ERROR_NOT_HOLDING_GEAR.create();
		}
		InsertableTravellersModifier insertable = requireInsertable(modifier.value());
		Component modifierName = TravellersModifiersManager.getModifierTooltipComponent(modifier);
		return new Context(player, stack, item, insertable, modifierName);
	}

	static InsertableTravellersModifier requireInsertable(TravellersModifier modifier) throws CommandSyntaxException {
		if (modifier.isAbility() || !(modifier instanceof InsertableTravellersModifier insertable)) {
			throw ERROR_ABILITY.create();
		}
		return insertable;
	}

	static void validateAdd(long modifierCount, int modifierSlots, boolean alreadyPresent, boolean correctSlot, Component modifierName) throws CommandSyntaxException {
		if (modifierCount >= modifierSlots) {
			throw ERROR_TOO_MANY_MODIFIERS.create();
		}
		if (alreadyPresent) {
			throw ERROR_HAS_MODIFIER.apply(modifierName).create();
		}
		if (!correctSlot) {
			throw ERROR_WRONG_SLOT.apply(modifierName).create();
		}
	}

	static void validateRemove(boolean present, Component modifierName) throws CommandSyntaxException {
		if (!present) {
			throw ERROR_NO_MODIFIER.apply(modifierName).create();
		}
	}

	record Context(ServerPlayer player, ItemStack stack, TravellersModifiable item, InsertableTravellersModifier modifier, Component modifierName) {
	}
}
