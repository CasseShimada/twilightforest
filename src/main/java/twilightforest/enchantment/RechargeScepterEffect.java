package twilightforest.enchantment;

import com.mojang.serialization.MapCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.phys.Vec3;
import twilightforest.init.TFItems;
import twilightforest.inventory.InventoryUtil;
import twilightforest.item.recipe.ScepterRepairRecipe;

import java.util.ArrayList;
import java.util.List;

public record RechargeScepterEffect() implements EnchantmentEntityEffect {

	public static final MapCodec<RechargeScepterEffect> CODEC = MapCodec.unit(RechargeScepterEffect::new);

	@Override
	public void apply(ServerLevel level, int enchantmentLevel, EnchantedItemInUse item, Entity entity, Vec3 vec3) {
		applyRecharge(level, item.itemStack(), entity);
	}

	public static void applyRecharge(ServerLevel level, ItemStack item, Entity entity) {
		if (entity instanceof Player player && item.getDamageValue() == item.getMaxDamage()) {
			List<ScepterRepairRecipe> recipes = level.recipeAccess().getRecipes().stream().filter(holder -> holder.value() instanceof ScepterRepairRecipe).map(RecipeHolder::value).map(ScepterRepairRecipe.class::cast).toList();
			for (var recipe : recipes) {
				if (item.is(recipe.getScepter())) {
					List<Integer> slotsToConsume = new ArrayList<>();
					var ingredientCopy = new ArrayList<>(recipe.getRepairItems());
					var inventory = player.getInventory().getNonEquipmentItems();
					scepterItemsCheck:
					for (int i = 0; i < inventory.size(); i++) {
						var stack = inventory.get(i);
						if (stack.isEmpty()) continue;
						if (stack.is(TFItems.EXANIMATE_ESSENCE.get())) {
							stack.shrink(1);
							item.setDamageValue(0);
							return;
						}
						for (int ingredientIndex = 0; ingredientIndex < ingredientCopy.size(); ingredientIndex++) {
							if (ingredientCopy.get(ingredientIndex).test(stack)) {
								ingredientCopy.remove(ingredientIndex);
								slotsToConsume.add(i);
								if (ingredientCopy.isEmpty()) break scepterItemsCheck;
								continue scepterItemsCheck;
							}
						}
					}

					if (slotsToConsume.size() == recipe.getRepairItems().size()) {
						for (int slot : slotsToConsume) {
							ItemStack stack = inventory.get(slot);
							ItemStackTemplate remainder = stack.getCraftingRemainder();
							stack.shrink(1);
							if (remainder != null) {
								InventoryUtil.giveItemToPlayer(player, remainder.create());
							}
						}
						item.setDamageValue(item.getDamageValue() - recipe.getRepairDurability());
					}
				}
			}
		}
	}

	@Override
	public MapCodec<? extends EnchantmentEntityEffect> codec() {
		return CODEC;
	}
}
