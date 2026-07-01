package twilightforest.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.ARGB;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import twilightforest.components.item.PotionFlaskComponent;
import twilightforest.inventory.InventoryUtil;
import twilightforest.init.TFDamageTypes;
import twilightforest.init.TFDataAttachments;
import twilightforest.init.TFDataComponents;
import twilightforest.init.TFSounds;

import java.util.Optional;
import java.util.function.Consumer;

public class BrittleFlaskItem extends Item {

	public static final int DOSES = 3;

	public BrittleFlaskItem(Properties properties) {
		super(properties);
	}

	@Override
	public ItemStack getDefaultInstance() {
		ItemStack itemstack = super.getDefaultInstance();
		itemstack.set(TFDataComponents.POTION_FLASK_CONTENTS.get(), PotionFlaskComponent.EMPTY);
		updateMaxStackSize(itemstack);
		return itemstack;
	}

	@Override
	public boolean isBarVisible(ItemStack stack) {
		return stack.getOrDefault(TFDataComponents.POTION_FLASK_CONTENTS.get(), PotionFlaskComponent.EMPTY).potion().potion().isPresent();
	}

	@Override
	public int getBarColor(ItemStack stack) {
		return ARGB.opaque(stack.getOrDefault(TFDataComponents.POTION_FLASK_CONTENTS.get(), PotionFlaskComponent.EMPTY).potion().getColor());
	}

	@Override
	public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack other, Slot slot, ClickAction action, Player player, SlotAccess access) {
		return this.tryFillFlask(stack, other, action, player);
	}

	@Override
	public boolean overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction action, Player player) {
		return this.tryFillFlask(stack, slot.getItem(), action, player);
	}

	private boolean tryFillFlask(ItemStack stack, ItemStack other, ClickAction action, Player player) {
		PotionFlaskComponent flaskContents = stack.getOrDefault(TFDataComponents.POTION_FLASK_CONTENTS.get(), PotionFlaskComponent.EMPTY);
		PotionContents potionContents = other.get(DataComponents.POTION_CONTENTS);

		if (action == ClickAction.SECONDARY && potionContents != null) {
			if ((flaskContents.potion().potion().isEmpty() || flaskContents.potion().equals(potionContents)) && flaskContents.doses() < DOSES - flaskContents.breakage()) {
				if (!player.getAbilities().instabuild) {
					other.shrink(1);
					InventoryUtil.giveItemToPlayer(player, new ItemStack(Items.GLASS_BOTTLE));
				}

				this.changeAndConsumeFlask(stack, player, flask -> {
					flask.update(TFDataComponents.POTION_FLASK_CONTENTS.get(), flaskContents, component -> component.tryAddDose(potionContents));
					updateMaxStackSize(flask);
				});
				player.playSound(TFSounds.FLASK_FILL.get(), (flaskContents.doses() + 1) * 0.25F, player.level().getRandom().nextFloat() * 0.1F + 0.9F);
				return true;
			}
		}
		return false;
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		PotionFlaskComponent flaskContents = stack.getOrDefault(TFDataComponents.POTION_FLASK_CONTENTS.get(), PotionFlaskComponent.EMPTY);

		if (flaskContents.potion() == PotionContents.EMPTY) {
			return InteractionResult.FAIL;
		}

		if (flaskContents.doses() > 0) {
			return ItemUtils.startUsingInstantly(level, player, hand);
		}

		return InteractionResult.FAIL;
	}

	@Override
	public int getUseDuration(ItemStack stack, LivingEntity entity) {
		return 32;
	}

	@Override
	public ItemUseAnimation getUseAnimation(ItemStack stack) {
		return ItemUseAnimation.DRINK;
	}

	@Override
	public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
		PotionFlaskComponent flaskContents = stack.getOrDefault(TFDataComponents.POTION_FLASK_CONTENTS.get(), PotionFlaskComponent.EMPTY);
		if (flaskContents.potion() != PotionContents.EMPTY) {
			if (entity instanceof Player player) {
				if (level instanceof ServerLevel serverLevel) {
					for (MobEffectInstance mobeffectinstance : flaskContents.potion().getAllEffects()) {
						if (mobeffectinstance.is(MobEffects.INSTANT_DAMAGE) != entity.isInvertedHealAndHarm() && mobeffectinstance.getAmplifier() > 0) {
							//custom harming death message for the advancement
							entity.hurt(entity.damageSources().source(TFDamageTypes.FAILED_CHALLENGE), (float)(6 << mobeffectinstance.getAmplifier()));
						} else if (mobeffectinstance.getEffect().value().isInstantaneous()) {
							mobeffectinstance.getEffect().value().applyInstantaneousEffect(serverLevel, player, player, player, mobeffectinstance.getAmplifier(), 1.0D);
						} else {
							player.addEffect(new MobEffectInstance(mobeffectinstance));
						}
					}
					if (!player.isCreative() && !player.isSpectator() && player instanceof ServerPlayer serverPlayer) {
						flaskContents.potion().potion().ifPresent(potion -> TFDataAttachments.get(player, TFDataAttachments.FLASK_DOSES).trackDrink(potion, serverPlayer));
					}
				}
				player.awardStat(Stats.ITEM_USED.get(this));
				if (!player.getAbilities().instabuild) {

					this.changeAndConsumeFlask(stack, player, flask -> {
						flask.update(TFDataComponents.POTION_FLASK_CONTENTS.get(), flaskContents, component -> {
							component = component.removeDose();
							if (component.breakable()) {
								if (component.breakage() >= DOSES) {
									flask.shrink(1);
									level.playSound(null, player, TFSounds.BRITTLE_FLASK_BREAK.get(), player.getSoundSource(), 1.5F, 0.7F);
								} else {
									level.playSound(null, player, TFSounds.BRITTLE_FLASK_CRACK.get(), player.getSoundSource(), 1.5F, 2.0F);
								}
							}
							return component;
						});
						updateMaxStackSize(flask);
					});
				}
			}
		}
		return super.finishUsingItem(stack, level, entity);
	}

	private void changeAndConsumeFlask(ItemStack stack, Player player, Consumer<ItemStack> onDrink) {
		//if we have a stack of flasks, separate one when filling/emptying
		if (stack.getCount() > 1) {
			var copy = stack.copyWithCount(1);
			stack.shrink(1);

			onDrink.accept(copy);
			InventoryUtil.giveItemToPlayer(player, copy);
		} else {
			//otherwise just use the existing stack. Having it jump around the inventory is weird
			onDrink.accept(stack);
		}
	}

	@Override
	public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
		return Optional.of(new Tooltip(stack.getOrDefault(TFDataComponents.POTION_FLASK_CONTENTS.get(), PotionFlaskComponent.EMPTY), DOSES));
	}

	//copied from Item.getBarWidth, but reversed the "durability" check so it increments up, not down
	@Override
	public int getBarWidth(ItemStack stack) {
		return Math.round(13.0F - Math.abs(stack.getOrDefault(TFDataComponents.POTION_FLASK_CONTENTS.get(), PotionFlaskComponent.EMPTY).doses() - DOSES) * 13.0F / DOSES);
	}

	private void updateMaxStackSize(ItemStack stack) {
		boolean hasPotion = stack.getOrDefault(TFDataComponents.POTION_FLASK_CONTENTS.get(), PotionFlaskComponent.EMPTY).potion().potion().isPresent();
		stack.set(DataComponents.MAX_STACK_SIZE, hasPotion ? 1 : this.getDefaultMaxStackSize());
	}

	public record Tooltip(PotionFlaskComponent component, int maxDoses) implements TooltipComponent {
	}
}
