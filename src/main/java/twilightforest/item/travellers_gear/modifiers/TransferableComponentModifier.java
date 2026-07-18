package twilightforest.item.travellers_gear.modifiers;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import twilightforest.TwilightForestMod;

import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unchecked")
public record TransferableComponentModifier(
	EquipmentSlotGroup group,
	DataComponentType<Unit> markerComponent,
	TypedDataComponent<?> transferableComponent,
	List<Component> description
) implements TransferableTravellersModifier {
	public static final MapCodec<TransferableComponentModifier> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		EquipmentSlotGroup.CODEC.fieldOf("equipment_slots").validate(TravellersModifier::validateEquipment).forGetter(TransferableComponentModifier::group),
		DataComponentType.CODEC.fieldOf("component").forGetter(value -> value.markerComponent),
		DataComponentMap.CODEC.fieldOf("transferable_components").forGetter(value -> DataComponentMap.builder().set((DataComponentType<Object>) value.transferableComponent.type(), value.transferableComponent.value()).build()),
		ComponentSerialization.CODEC.listOf().optionalFieldOf("description", List.of()).forGetter(TransferableComponentModifier::description)
	).apply(instance, (group, marker, transferableComponents, description) -> {
		if (transferableComponents.size() != 1) {
			throw new IllegalArgumentException("Expected exactly one transferable component: " + transferableComponents);
		}
		return new TransferableComponentModifier(group, (DataComponentType<Unit>) marker, transferableComponents.stream().findFirst().orElseThrow(), description);
	}));

	public <T> TransferableComponentModifier(EquipmentSlotGroup group, DataComponentType<Unit> markerComponent, DataComponentType<T> transferableComponent, T defaultValue, List<Component> description) {
		this(group, markerComponent, new TypedDataComponent<>(transferableComponent, defaultValue), description);
	}

	@Override
	public MapCodec<? extends TravellersModifier> codec() {
		return CODEC;
	}

	@Override
	public boolean isAbility() {
		return false;
	}

	@Override
	public boolean addModifier(ItemStack stack) {
		stack.set(this.markerComponent, Unit.INSTANCE);
		stack.set((DataComponentType<Object>) this.transferableComponent.type(), this.transferableComponent.value());
		return true;
	}

	@Override
	public boolean hasModifier(ItemStack stack) {
		return stack.has(this.markerComponent) && stack.has(this.transferableComponent.type());
	}

	@Override
	public void removeModifier(ItemStack stack) {
		stack.remove(this.markerComponent);
		stack.remove(this.transferableComponent.type());
	}

	@Override
	public boolean transfer(ItemStack output, List<Ingredient> input) {
		List<ItemStack[]> providers = this.findDataComponentProviders(input);
		if (providers.isEmpty()) {
			return false;
		}
		if (providers.size() > 1) {
			TwilightForestMod.LOGGER.error("A travellers recipe matched more than one transferable component provider: {}", input);
			return false;
		}

		ItemStack provider = Arrays.stream(providers.getFirst())
			.filter(stack -> stack.has(this.transferableComponent.type()))
			.findFirst()
			.orElseThrow();
		output.set(this.markerComponent, Unit.INSTANCE);
		output.set((DataComponentType<Object>) this.transferableComponent.type(), provider.get(this.transferableComponent.type()));
		return true;
	}

	@Override
	public boolean transferFromStacks(ItemStack output, List<ItemStack> input) {
		List<ItemStack> providers = this.findDataComponentProviderStacks(input);
		if (providers.isEmpty()) {
			return false;
		}
		if (providers.size() > 1) {
			TwilightForestMod.LOGGER.error("A travellers recipe matched more than one transferable component provider: {}", input);
			return false;
		}

		ItemStack provider = providers.getFirst();
		output.set(this.markerComponent, Unit.INSTANCE);
		output.set((DataComponentType<Object>) this.transferableComponent.type(), provider.get(this.transferableComponent.type()));
		return true;
	}

	public List<ItemStack[]> findDataComponentProviders(List<Ingredient> input) {
		return input.stream()
			.map(ingredient -> ingredient.items().map(ItemStack::new).toArray(ItemStack[]::new))
			.filter(stacks -> Arrays.stream(stacks).anyMatch(stack -> stack.has(this.transferableComponent.type())))
			.toList();
	}

	public List<ItemStack> findDataComponentProviderStacks(List<ItemStack> input) {
		return input.stream()
			.filter(stack -> stack.has(this.transferableComponent.type()))
			.toList();
	}

	@Override
	public List<Component> getDescription() {
		return this.description;
	}
}
