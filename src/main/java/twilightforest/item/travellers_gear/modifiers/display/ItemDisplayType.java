package twilightforest.item.travellers_gear.modifiers.display;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;
import java.util.function.Predicate;

public record ItemDisplayType(Predicate<ItemStack> validItems, Optional<Identifier> slotTexture) {
}
