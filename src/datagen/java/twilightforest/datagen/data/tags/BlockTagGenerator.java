package twilightforest.datagen.data.tags;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import twilightforest.init.TFBlocks;

import java.util.concurrent.CompletableFuture;

public final class BlockTagGenerator extends FabricTagsProvider.BlockTagsProvider {
	public BlockTagGenerator(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, registries);
	}

	@Override
	protected void addTags(HolderLookup.Provider registries) {
		this.builder(BlockTags.ENCHANTMENT_POWER_PROVIDER)
			.add(key(TFBlocks.CANOPY_BOOKSHELF));

		this.builder(BlockTags.DIRT).add(key(TFBlocks.UBEROUS_SOIL));
		this.builder(BlockTags.OVERRIDES_MUSHROOM_LIGHT_REQUIREMENT).add(key(TFBlocks.UBEROUS_SOIL));
		this.builder(BlockTags.SUPPORTS_SMALL_DRIPLEAF).add(key(TFBlocks.UBEROUS_SOIL));

		this.builder(BlockTags.FROG_PREFER_JUMP_TO).add(key(TFBlocks.HUGE_LILY_PAD));
		this.builder(BlockTags.INSIDE_STEP_SOUND_BLOCKS).add(key(TFBlocks.HUGE_LILY_PAD));
		this.builder(BlockTags.SWORD_EFFICIENT).add(key(TFBlocks.HUGE_LILY_PAD));

		this.builder(BlockTags.INVALID_SPAWN_INSIDE).add(key(TFBlocks.TWILIGHT_PORTAL));
		this.builder(BlockTags.PORTALS).add(key(TFBlocks.TWILIGHT_PORTAL));

		this.builder(BlockTags.OCCLUDES_VIBRATION_SIGNALS).add(key(TFBlocks.ARCTIC_FUR_BLOCK));
		this.builder(BlockTags.STRIDER_WARM_BLOCKS).add(key(TFBlocks.FIERY_BLOCK));
		this.builder(BlockTags.WALLS).add(key(TFBlocks.WROUGHT_IRON_FENCE));
		this.builder(BlockTags.WOOL_CARPETS).add(key(TFBlocks.CORONATION_CARPET));

		this.builder(BlockTags.OVERWORLD_CARVER_REPLACEABLES).add(key(TFBlocks.TROLLSTEINN));
		this.builder(BlockTags.NEEDS_IRON_TOOL)
			.add(key(TFBlocks.FIERY_BLOCK), key(TFBlocks.KNIGHTMETAL_BLOCK));
		this.builder(BlockTags.MOSS_REPLACEABLE)
			.add(key(TFBlocks.ROOT_BLOCK), key(TFBlocks.LIVEROOT_BLOCK), key(TFBlocks.TROLLSTEINN));
	}

	private static ResourceKey<Block> key(Block block) {
		return BuiltInRegistries.BLOCK.getResourceKey(block).orElseThrow();
	}
}
