package twilightforest.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import twilightforest.util.ClientLevelHelper;

import java.time.LocalDate;
import java.util.function.Consumer;

public class MoonDialItem extends Item {
	public MoonDialItem(Properties properties) {
		super(properties);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
		boolean aprilFools = LocalDate.of(LocalDate.now().getYear(), 4, 1).equals(LocalDate.now());
		var level = ClientLevelHelper.getClientLevel();
		String phaseType = (level != null && level.dimensionType().hasSkyLight()
			? String.valueOf(Math.floorMod(level.getDayTime() / 24000L, 8))
			: aprilFools ? "unknown_fools" : "unknown");
		tooltip.accept(Component.translatable("item.twilightforest.moon_dial.phase_" + phaseType).withStyle(ChatFormatting.GRAY));
	}
}
