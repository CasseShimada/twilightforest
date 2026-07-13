package twilightforest.datagen.data.tags;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.BlockFamily;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import twilightforest.init.TFBlocks;
import twilightforest.tags.TFBlockTags;
import twilightforest.util.TFBlockFamilies;

import java.util.Objects;
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

		this.builder(TFBlockTags.TOWERWOOD)
			.add(key(TFBlocks.TOWERWOOD), key(TFBlocks.MOSSY_TOWERWOOD), key(TFBlocks.CRACKED_TOWERWOOD), key(TFBlocks.INFESTED_TOWERWOOD));
		this.addWoodFamilyTags();
	}

	private void addWoodFamilyTags() {
		TFBlockFamilies.getAllFamilies().forEach(family -> {
			this.builder(BlockTags.PLANKS).add(key(family.getBaseBlock()));
			this.addFamilyVariant(BlockTags.WOODEN_BUTTONS, family, BlockFamily.Variant.BUTTON);
			this.addFamilyVariant(BlockTags.WOODEN_DOORS, family, BlockFamily.Variant.DOOR);
			this.addFamilyVariant(BlockTags.WOODEN_FENCES, family, BlockFamily.Variant.FENCE);
			this.addFamilyVariant(BlockTags.FENCE_GATES, family, BlockFamily.Variant.FENCE_GATE);
			this.addFamilyVariant(BlockTags.WOODEN_PRESSURE_PLATES, family, BlockFamily.Variant.PRESSURE_PLATE);
			this.addFamilyVariant(BlockTags.WOODEN_SLABS, family, BlockFamily.Variant.SLAB);
			this.addFamilyVariant(BlockTags.WOODEN_STAIRS, family, BlockFamily.Variant.STAIRS);
			this.addFamilyVariant(BlockTags.WOODEN_TRAPDOORS, family, BlockFamily.Variant.TRAPDOOR);
			this.addFamilyVariant(BlockTags.STANDING_SIGNS, family, BlockFamily.Variant.SIGN);
			this.addFamilyVariant(BlockTags.WALL_SIGNS, family, BlockFamily.Variant.WALL_SIGN);
			this.addFamilyVariant(BlockTags.CEILING_HANGING_SIGNS, family, BlockFamily.Variant.HANGING_SIGN);
			this.addFamilyVariant(BlockTags.WALL_HANGING_SIGNS, family, BlockFamily.Variant.WALL_HANGING_SIGN);
		});
		this.builder(BlockTags.PLANKS).addTag(TFBlockTags.TOWERWOOD);
	}

	private void addFamilyVariant(TagKey<Block> tag, BlockFamily family, BlockFamily.Variant variant) {
		Block block = Objects.requireNonNull(family.get(variant), () -> family.getBaseBlock() + " is missing " + variant);
		this.builder(tag).add(key(block));
	}

	private static ResourceKey<Block> key(Block block) {
		return BuiltInRegistries.BLOCK.getResourceKey(block).orElseThrow();
	}
}
