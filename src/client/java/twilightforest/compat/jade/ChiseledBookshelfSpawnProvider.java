package twilightforest.compat.jade;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.SpawnData;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.theme.IThemeHelper;
import twilightforest.TwilightForestMod;
import twilightforest.block.ChiseledCanopyShelfBlock;
import twilightforest.block.entity.bookshelf.ChiseledCanopyShelfBlockEntity;

public enum ChiseledBookshelfSpawnProvider implements IBlockComponentProvider {
	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		if (!accessor.getPlayer().isCreative()
			|| !accessor.getBlockState().getValue(ChiseledCanopyShelfBlock.SPAWNER)
			|| !(accessor.getBlockEntity() instanceof ChiseledCanopyShelfBlockEntity shelf)) {
			return;
		}

		SpawnData spawnData = shelf.getSpawner().getNextSpawnData();
		if (spawnData == null) {
			return;
		}

		String entityId = spawnData.getEntityToSpawn().getStringOr("id", "");
		Identifier identifier = Identifier.tryParse(entityId);
		if (identifier == null) {
			return;
		}

		BuiltInRegistries.ENTITY_TYPE.getOptional(identifier).ifPresent(type -> replaceSpawnerName(tooltip, accessor, type));
	}

	private static void replaceSpawnerName(ITooltip tooltip, BlockAccessor accessor, EntityType<?> type) {
		Component name = Component.translatable("jade.spawner", accessor.getBlock().getName(), type.getDescription());
		tooltip.replace(JadeIds.CORE_OBJECT_NAME, IThemeHelper.get().title(name));
	}

	@Override
	public Identifier getUid() {
		return TwilightForestMod.prefix("chiseled_bookshelf_spawner");
	}
}
