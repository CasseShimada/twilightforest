package twilightforest.item;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

import java.time.LocalDate;

public class MoonDialItem extends Item {
	public MoonDialItem(Properties properties) {
		super(properties);
	}

	public static MutableComponent getMoonPhase(@Nullable Level level) {
		String phaseType;
		if (level != null && !level.dimensionType().hasFixedTime()) {
			phaseType = Integer.toString(level.environmentAttributes()
				.getDimensionValue(EnvironmentAttributes.MOON_PHASE).index());
		} else {
			LocalDate today = LocalDate.now();
			phaseType = today.getMonthValue() == 4 && today.getDayOfMonth() == 1 ? "unknown_fools" : "unknown";
		}
		return Component.translatable("item.twilightforest.moon_dial.phase_" + phaseType);
	}
}
