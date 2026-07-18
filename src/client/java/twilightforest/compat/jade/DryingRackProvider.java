package twilightforest.compat.jade;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.JadeUI;

public enum DryingRackProvider implements IBlockComponentProvider {
	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		DryingRackDataProvider.INSTANCE.decodeFromData(accessor).ifPresent(data -> {
			int remainingTicks = Math.max(0, data.total() - data.progress());
			tooltip.add(JadeUI.spacer(10, 0));
			tooltip.append(JadeUI.text(Component.translatable("jade.drying_rack.remaining", JadeCompatLogic.formatDryingTime(remainingTicks))));
		});
	}

	@Override
	public Identifier getUid() {
		return DryingRackDataProvider.INSTANCE.getUid();
	}
}
