package twilightforest.item.recipe.travellers;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import twilightforest.init.custom.TravellersModifiersManager;
import twilightforest.item.travellers_gear.modifiers.TravellersModifiable;
import twilightforest.item.travellers_gear.modifiers.TravellersModifier;

import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public abstract class TravellersGearModifierRecipe extends CustomRecipe {
	protected final Holder<TravellersModifier> travellersModifier;

	protected TravellersGearModifierRecipe(Holder<TravellersModifier> travellersModifier) {
		this.travellersModifier = travellersModifier;
	}

	@Override
	public boolean matches(CraftingInput input, Level level) {
		ItemStack stack = getModifiableArmor(input);
		if (stack == null) {
			return false;
		}

		int slots = stack.getItem() instanceof TravellersModifiable modifiable ? modifiable.getModifierSlots() : 0;
		TravellersModifier modifier = this.travellersModifier.value();
		return TravellersModifiersManager.countInsertableModifiers(level.registryAccess(), stack) < slots
			&& !modifier.hasModifier(stack)
			&& TravellersModifiersManager.getModifierDataComponentProviders(input.items(), modifier) <= 1;
	}

	@Override
	public ItemStack assemble(CraftingInput input) {
		ItemStack travellerArmorStack = getModifiableArmor(input);
		if (travellerArmorStack == null) {
			return ItemStack.EMPTY;
		}

		ItemStack stack = travellerArmorStack.copy();
		return applyModifier(stack, input.items());
	}

	public ItemStack applyModifier(ItemStack stack, List<ItemStack> inputs) {
		TravellersModifier modifier = this.travellersModifier.value();
		if (TravellersModifiersManager.transferModifier(stack, inputs, modifier)) {
			return stack;
		}
		return TravellersModifiersManager.addModifier(stack, modifier) ? stack : ItemStack.EMPTY;
	}

	public abstract boolean isShapeless();

	public abstract int getWidth();

	public abstract int getHeight();

	protected abstract Stream<Ingredient> ingredientValues();

	protected static @Nullable ItemStack getModifiableArmor(CraftingInput input) {
		return getModifiableArmor(input.items());
	}

	protected static @Nullable ItemStack getModifiableArmor(Iterable<ItemStack> items) {
		return StreamSupport.stream(items.spliterator(), false)
			.filter(stack -> stack.getItem() instanceof TravellersModifiable modifiable && modifiable.getModifierSlots() > 0)
			.findFirst()
			.orElse(null);
	}

	public static ItemStack getModifiableArmorFromIngredients(Iterable<Ingredient> ingredients) {
		return StreamSupport.stream(ingredients.spliterator(), false)
			.flatMap(Ingredient::items)
			.map(ItemStack::new)
			.filter(stack -> stack.getItem() instanceof TravellersModifiable)
			.findFirst()
			.orElseThrow();
	}

	public Identifier getId() {
		String itemPath = StringUtils.substringAfterLast(
			getModifiableArmorFromIngredients(this.ingredientValues()::iterator).getItem().getDescriptionId(), '.'
		);
		return getTravellersModifierKey().identifier()
			.withPrefix(itemPath + "/")
			.withPrefix("add_modifier_to_travellers_gear/")
			.withSuffix("_modifier");
	}

	public ResourceKey<TravellersModifier> getTravellersModifierKey() {
		return this.travellersModifier.unwrapKey().orElseThrow();
	}

}
