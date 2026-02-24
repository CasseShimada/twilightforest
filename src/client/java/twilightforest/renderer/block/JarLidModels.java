package twilightforest.client.renderer.block;

import net.fabricmc.fabric.api.client.model.loading.v1.ExtraModelKey;
import net.fabricmc.fabric.api.client.model.loading.v1.FabricBakedModelManager;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.SimpleUnbakedExtraModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import twilightforest.TwilightForestMod;
import twilightforest.init.TFBlocks;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class JarLidModels {
	private record LidResource(Item lid, Identifier modelId) {}

	private static final List<LidResource> LIDS = List.of(
		lid(TFBlocks.MANGROVE_LOG.get().asItem(), "mangrove_log"),
		lid(TFBlocks.CANOPY_LOG.get().asItem(), "canopy_log"),
		lid(TFBlocks.DARK_LOG.get().asItem(), "dark_log"),
		lid(TFBlocks.MINING_LOG.get().asItem(), "mining_log"),
		lid(TFBlocks.SORTING_LOG.get().asItem(), "sorting_log"),
		lid(TFBlocks.TIME_LOG.get().asItem(), "time_log"),
		lid(TFBlocks.TRANSFORMATION_LOG.get().asItem(), "transformation_log"),
		lid(TFBlocks.TWILIGHT_OAK_LOG.get().asItem(), "twilight_oak_log"),
		lid(Items.ACACIA_LOG, "acacia_log"),
		lid(Items.BIRCH_LOG, "birch_log"),
		lid(Items.CHERRY_LOG, "cherry_log"),
		lid(Items.DARK_OAK_LOG, "dark_oak_log"),
		lid(Items.JUNGLE_LOG, "jungle_log"),
		lid(Items.MANGROVE_LOG, "vanilla_mangrove_log"),
		lid(Items.OAK_LOG, "oak_log"),
		lid(Items.PALE_OAK_LOG, "pale_oak_log"),
		lid(Items.SPRUCE_LOG, "spruce_log"),
		lid(Items.CRIMSON_STEM, "crimson_stem"),
		lid(Items.WARPED_STEM, "warped_stem"),
		lid(TFBlocks.STRIPPED_MANGROVE_LOG.get().asItem(), "stripped_mangrove_log"),
		lid(TFBlocks.STRIPPED_CANOPY_LOG.get().asItem(), "stripped_canopy_log"),
		lid(TFBlocks.STRIPPED_DARK_LOG.get().asItem(), "stripped_dark_log"),
		lid(TFBlocks.STRIPPED_MINING_LOG.get().asItem(), "stripped_mining_log"),
		lid(TFBlocks.STRIPPED_SORTING_LOG.get().asItem(), "stripped_sorting_log"),
		lid(TFBlocks.STRIPPED_TIME_LOG.get().asItem(), "stripped_time_log"),
		lid(TFBlocks.STRIPPED_TRANSFORMATION_LOG.get().asItem(), "stripped_transformation_log"),
		lid(TFBlocks.STRIPPED_TWILIGHT_OAK_LOG.get().asItem(), "stripped_twilight_oak_log"),
		lid(Items.STRIPPED_ACACIA_LOG, "stripped_acacia_log"),
		lid(Items.STRIPPED_BIRCH_LOG, "stripped_birch_log"),
		lid(Items.STRIPPED_CHERRY_LOG, "stripped_cherry_log"),
		lid(Items.STRIPPED_DARK_OAK_LOG, "stripped_dark_oak_log"),
		lid(Items.STRIPPED_JUNGLE_LOG, "stripped_jungle_log"),
		lid(Items.STRIPPED_MANGROVE_LOG, "vanilla_stripped_mangrove_log"),
		lid(Items.STRIPPED_OAK_LOG, "stripped_oak_log"),
		lid(Items.STRIPPED_PALE_OAK_LOG, "stripped_pale_oak_log"),
		lid(Items.STRIPPED_SPRUCE_LOG, "stripped_spruce_log"),
		lid(Items.STRIPPED_CRIMSON_STEM, "stripped_crimson_stem"),
		lid(Items.STRIPPED_WARPED_STEM, "stripped_warped_stem"),
		lid(TFBlocks.CINDER_LOG.get().asItem(), "cinder_log"),
		lid(Items.PUMPKIN, "pumpkin"),
		lid(Items.BAMBOO_BLOCK, "bamboo_block"),
		lid(Items.STRIPPED_BAMBOO_BLOCK, "stripped_bamboo_block")
	);

	private static final Map<Item, ExtraModelKey<BlockStateModel>> MODEL_KEYS = new HashMap<>();

	public static void register() {
		for (LidResource resource : LIDS) {
			MODEL_KEYS.computeIfAbsent(resource.lid(), lid -> ExtraModelKey.create(resource.modelId()::toString));
		}

		ModelLoadingPlugin.register(context -> {
			for (LidResource resource : LIDS) {
				ExtraModelKey<BlockStateModel> key = MODEL_KEYS.get(resource.lid());
				context.addModel(key, SimpleUnbakedExtraModel.blockStateModel(resource.modelId()));
			}
		});
	}

	public static @Nullable BlockStateModel getModel(Item item) {
		ExtraModelKey<BlockStateModel> key = MODEL_KEYS.get(item);
		if (key == null) {
			return null;
		}

		return ((FabricBakedModelManager) Minecraft.getInstance().getModelManager()).getModel(key);
	}

	private static LidResource lid(Item lid, String path) {
		return new LidResource(lid, TwilightForestMod.prefix("block/lid/" + path));
	}

	private JarLidModels() {}
}
