package twilightforest.compat.wthit;

import mcp.mobius.waila.api.IBlockComponentProvider;
import mcp.mobius.waila.api.IBlockAccessor;
import mcp.mobius.waila.api.IEntityComponentProvider;
import mcp.mobius.waila.api.IEntityAccessor;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.component.ItemListComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import twilightforest.compat.OverlayCompatLogic;

final class WthitTooltipProviders {
	static final IBlockComponentProvider DRYING_RACK = DryingRack.INSTANCE;
	static final IEntityComponentProvider QUEST_RAM = QuestRam.INSTANCE;
	static final IBlockComponentProvider CANOPY_SHELF = CanopyShelf.INSTANCE;

	private enum DryingRack implements IBlockComponentProvider {
		INSTANCE;

		@Override
		public void appendBody(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
			if (!config.getBoolean(WthitConstants.DRYING_RACK)) {
				return;
			}
			var raw = accessor.getData().raw();
			if (!raw.getBooleanOr(WthitConstants.DRYING_PRESENT, false)) {
				return;
			}
			OverlayCompatLogic.dryingStatus(
				raw.getBooleanOr(WthitConstants.DRYING_ACTIVE, false),
				raw.getIntOr(WthitConstants.DRYING_PROGRESS, 0),
				raw.getIntOr(WthitConstants.DRYING_TOTAL, 0)
			).ifPresent(status -> tooltip.addLine(status.remaining() == 0
				? Component.translatable("wthit.twilightforest.drying_rack.complete")
				: Component.translatable("wthit.twilightforest.drying_rack.remaining",
					OverlayCompatLogic.formatDryingTime(status.remaining()))));
		}
	}

	private enum QuestRam implements IEntityComponentProvider {
		INSTANCE;

		@Override
		public void appendBody(ITooltip tooltip, IEntityAccessor accessor, IPluginConfig config) {
			if (!config.getBoolean(WthitConstants.QUEST_RAM_WOOL)) {
				return;
			}
			var raw = accessor.getData().raw();
			if (!raw.contains(WthitConstants.QUEST_RAM_FLAGS)) {
				return;
			}
			var missing = OverlayCompatLogic.missingQuestWools(raw.getIntOr(WthitConstants.QUEST_RAM_FLAGS, 0));
			if (missing.isEmpty()) {
				tooltip.addLine(Component.translatable("wthit.twilightforest.quest_ram.complete"));
				return;
			}
			tooltip.addLine(Component.translatable("wthit.twilightforest.quest_ram.missing"));
			tooltip.addLine(new ItemListComponent(missing.stream().map(ItemStack::new).toList(), 2, 0.75F));
		}
	}

	private enum CanopyShelf implements IBlockComponentProvider {
		INSTANCE;

		@Override
		public void appendBody(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
			if (!config.getBoolean(WthitConstants.CANOPY_SHELF_SPAWNER)) {
				return;
			}
			String value = accessor.getData().raw().getStringOr(WthitConstants.SHELF_ENTITY, "");
			Identifier id = Identifier.tryParse(value);
			if (id == null) {
				return;
			}
			BuiltInRegistries.ENTITY_TYPE.getOptional(id).ifPresent(type -> tooltip.addLine(
				Component.translatable("wthit.twilightforest.canopy_shelf.spawns", type.getDescription())));
		}
	}

	private WthitTooltipProviders() {
	}
}
