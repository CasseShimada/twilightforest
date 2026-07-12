package twilightforest.client.event;

import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.color.item.ItemTintSources;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.GrassColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import twilightforest.TwilightForestMod;
import twilightforest.block.ClimbableHollowLogBlock;
import twilightforest.client.properties.PotionFlaskTintSource;
import twilightforest.enums.HollowLogVariants;
import twilightforest.init.TFBlocks;
import twilightforest.util.ColorUtil;
import twilightforest.util.SimplexNoiseHelper;

import java.util.List;

public class ColorHandler {
	public static final Int2IntFunction CANOPY_COLORIZER = color -> 0xFF000000 | (((color & 0xFEFEFE) + 0x469A66) / 2);
	public static final Int2IntFunction MANGROVE_COLORIZER = color -> 0xFF000000 | (((color & 0xFEFEFE) + 0xC0E694) / 2);
	private static boolean simplexNoiseAvailable = true;

	public static void registerColors() {
		registerBlockColors();
		registerTintSources();
	}

	private static void registerBlockColors() {
		register(tint(
			state -> auroraBaseColor(BlockPos.ZERO),
			(state, getter, pos) -> auroraBaseColor(pos.above(128))
		), TFBlocks.AURORA_BLOCK);

		register(tint(
			state -> desaturateAuroraColor(auroraBaseColor(BlockPos.ZERO)),
			(state, getter, pos) -> desaturateAuroraColor(auroraBaseColor(pos.above(128)))
		), TFBlocks.AURORA_PILLAR, TFBlocks.AURORA_SLAB, TFBlocks.AURORALIZED_GLASS);

		register(tint(
			state -> GrassColor.getDefaultColor(),
			(state, getter, pos) -> BiomeColors.getAverageGrassColor(getter, pos)
		), TFBlocks.SMOKER, TFBlocks.FIRE_JET);

		register(tint(
			state -> 0xFF000000 | 7455580,
			(state, getter, pos) -> 0xFF000000 | 2129968
		), TFBlocks.HUGE_LILY_PAD);

		register(tint(
			state -> 0xFF000000 | 106 << 16 | 156 << 8 | 23,
			(state, getter, pos) -> seasonalLeafColor(pos, 16, 16, 16, 106, 156, 23, 251, 108, 27)
		), TFBlocks.TIME_LEAVES);

		register(tint(
			state -> 0xFF000000 | 108 << 16 | 204 << 8 | 234,
			(state, getter, pos) -> seasonalLeafColor(pos, 27, 63, 39, 108, 204, 234, 96, 107, 121)
		), TFBlocks.TRANSFORMATION_LEAVES);

		register(tint(
			state -> 0xFF000000 | 252 << 16 | 241 << 8 | 68,
			(state, getter, pos) -> seasonalLeafColor(pos, 31, 33, 32, 252, 241, 68, 237, 172, 9)
		), TFBlocks.MINING_LEAVES);

		register(tint(
			state -> 0xFF000000 | 54 << 16 | 76 << 8 | 3,
			(state, getter, pos) -> seasonalLeafColor(pos, 63, 63, 63, 54, 76, 3, 168, 199, 43)
		), TFBlocks.SORTING_LEAVES);

		register(tint(
			state -> -1,
			(state, getter, pos) -> {
				float f = safeRippleNoise(2, 32.0f, pos, 0.4f, 1.0f, 2f, 0.7f);
				return 0xFF000000 | ColorUtil.hsvToRGB(0.1f, 1f - f, (f + 2f) / 3f);
			}
		), TFBlocks.TOWERWOOD, TFBlocks.CRACKED_TOWERWOOD, TFBlocks.INFESTED_TOWERWOOD, TFBlocks.MOSSY_TOWERWOOD);

		register(tint(
			state -> FoliageColor.FOLIAGE_DEFAULT,
			(state, getter, pos) -> BiomeColors.getAverageFoliageColor(getter, pos)
		), TFBlocks.TWILIGHT_OAK_LEAVES, TFBlocks.DARK_LEAVES, TFBlocks.HARDENED_DARK_LEAVES, TFBlocks.GIANT_LEAVES, TFBlocks.FALLEN_LEAVES);

		register(tint(
			state -> FoliageColor.FOLIAGE_EVERGREEN,
			(state, getter, pos) -> CANOPY_COLORIZER.apply(BiomeColors.getAverageFoliageColor(getter, pos))
		), TFBlocks.CANOPY_LEAVES);

		register(tint(
			state -> FoliageColor.FOLIAGE_BIRCH,
			(state, getter, pos) -> MANGROVE_COLORIZER.apply(BiomeColors.getAverageFoliageColor(getter, pos))
		), TFBlocks.MANGROVE_LEAVES);

		register(tint(
			state -> FoliageColor.FOLIAGE_DEFAULT,
			(state, getter, pos) -> rainbowLeafColor(pos)
		), TFBlocks.RAINBOW_OAK_LEAVES);

		register(tint(state -> FoliageColor.FOLIAGE_EVERGREEN), TFBlocks.BEANSTALK_LEAVES, TFBlocks.THORN_LEAVES);

		register(tint(
			state -> GrassColor.getDefaultColor(),
			(state, getter, pos) -> BiomeColors.getAverageGrassColor(getter, pos)
		), TFBlocks.FIDDLEHEAD, TFBlocks.POTTED_FIDDLEHEAD);

		register(List.of(
			tint(
				state -> GrassColor.getDefaultColor(),
				(state, getter, pos) -> BiomeColors.getAverageGrassColor(getter, pos)
			),
			noTint()
		), TFBlocks.HOLLOW_OAK_LOG_HORIZONTAL.get(), TFBlocks.HOLLOW_SPRUCE_LOG_HORIZONTAL.get(), TFBlocks.HOLLOW_BIRCH_LOG_HORIZONTAL.get(), TFBlocks.HOLLOW_JUNGLE_LOG_HORIZONTAL.get(),
			TFBlocks.HOLLOW_ACACIA_LOG_HORIZONTAL.get(), TFBlocks.HOLLOW_DARK_OAK_LOG_HORIZONTAL.get(), TFBlocks.HOLLOW_CRIMSON_STEM_HORIZONTAL.get(), TFBlocks.HOLLOW_WARPED_STEM_HORIZONTAL.get(),
			TFBlocks.HOLLOW_VANGROVE_LOG_HORIZONTAL.get(), TFBlocks.HOLLOW_CHERRY_LOG_HORIZONTAL.get(),
			TFBlocks.HOLLOW_TWILIGHT_OAK_LOG_HORIZONTAL, TFBlocks.HOLLOW_CANOPY_LOG_HORIZONTAL.get(), TFBlocks.HOLLOW_MANGROVE_LOG_HORIZONTAL.get(), TFBlocks.HOLLOW_DARK_LOG_HORIZONTAL.get(),
			TFBlocks.HOLLOW_TIME_LOG_HORIZONTAL.get(), TFBlocks.HOLLOW_TRANSFORMATION_LOG_HORIZONTAL.get(), TFBlocks.HOLLOW_MINING_LOG_HORIZONTAL.get(), TFBlocks.HOLLOW_SORTING_LOG_HORIZONTAL.get());

		register(List.of(
			tint(
				state -> state.getValue(ClimbableHollowLogBlock.VARIANT) == HollowLogVariants.Climbable.VINE ? FoliageColor.FOLIAGE_DEFAULT : -1,
				(state, getter, pos) -> state.getValue(ClimbableHollowLogBlock.VARIANT) == HollowLogVariants.Climbable.VINE ? BiomeColors.getAverageFoliageColor(getter, pos) : -1
			),
			noTint()
		), TFBlocks.HOLLOW_OAK_LOG_CLIMBABLE.get(), TFBlocks.HOLLOW_SPRUCE_LOG_CLIMBABLE.get(), TFBlocks.HOLLOW_BIRCH_LOG_CLIMBABLE.get(), TFBlocks.HOLLOW_JUNGLE_LOG_CLIMBABLE.get(),
			TFBlocks.HOLLOW_ACACIA_LOG_CLIMBABLE.get(), TFBlocks.HOLLOW_DARK_OAK_LOG_CLIMBABLE.get(), TFBlocks.HOLLOW_CRIMSON_STEM_CLIMBABLE.get(), TFBlocks.HOLLOW_WARPED_STEM_CLIMBABLE.get(), TFBlocks.HOLLOW_VANGROVE_LOG_CLIMBABLE.get(),
			TFBlocks.HOLLOW_TWILIGHT_OAK_LOG_CLIMBABLE, TFBlocks.HOLLOW_CANOPY_LOG_CLIMBABLE.get(), TFBlocks.HOLLOW_MANGROVE_LOG_CLIMBABLE.get(), TFBlocks.HOLLOW_DARK_LOG_CLIMBABLE.get(),
			TFBlocks.HOLLOW_TIME_LOG_CLIMBABLE.get(), TFBlocks.HOLLOW_TRANSFORMATION_LOG_CLIMBABLE.get(), TFBlocks.HOLLOW_MINING_LOG_CLIMBABLE.get(), TFBlocks.HOLLOW_SORTING_LOG_CLIMBABLE.get());

		register(tint(state -> GrassColor.getDefaultColor()),
			TFBlocks.TWILIGHT_PORTAL_MINIATURE_STRUCTURE, TFBlocks.NAGA_COURTYARD_MINIATURE_STRUCTURE, TFBlocks.LICH_TOWER_MINIATURE_STRUCTURE);
		register(tint(state -> 0xFFFF00FF), TFBlocks.PINK_CASTLE_RUNE_BRICK, TFBlocks.PINK_CASTLE_DOOR);
		register(tint(state -> 0xFF00FFFF), TFBlocks.BLUE_CASTLE_RUNE_BRICK, TFBlocks.BLUE_CASTLE_DOOR);
		register(tint(state -> 0xFFFFFF00), TFBlocks.YELLOW_CASTLE_RUNE_BRICK, TFBlocks.YELLOW_CASTLE_DOOR);
		register(tint(state -> 0xFF4B0082), TFBlocks.VIOLET_CASTLE_RUNE_BRICK, TFBlocks.VIOLET_CASTLE_DOOR);
		register(tint(state -> 0xFF5C1074), TFBlocks.VIOLET_FORCE_FIELD);
		register(tint(state -> 0xFFFA057E), TFBlocks.PINK_FORCE_FIELD);
		register(tint(state -> 0xFFFF5B02), TFBlocks.ORANGE_FORCE_FIELD);
		register(tint(state -> 0xFF89E701), TFBlocks.GREEN_FORCE_FIELD);
		register(tint(state -> 0xFF0DDEFF), TFBlocks.BLUE_FORCE_FIELD);
	}

	private static void registerTintSources() {
		ItemTintSources.ID_MAPPER.put(TwilightForestMod.prefix("potion_flask"), PotionFlaskTintSource.TYPE);
	}

	private static void register(BlockTintSource source, Block... blocks) {
		register(List.of(source), blocks);
	}

	private static void register(List<BlockTintSource> sources, Block... blocks) {
		BlockColorRegistry.register(sources, blocks);
	}

	private static BlockTintSource noTint() {
		return tint(state -> -1);
	}

	private static BlockTintSource tint(StateTint fallback) {
		return new BlockTintSource() {
			@Override
			public int color(BlockState state) {
				return fallback.apply(state);
			}
		};
	}

	private static BlockTintSource tint(StateTint fallback, WorldTint world) {
		return new BlockTintSource() {
			@Override
			public int color(BlockState state) {
				return fallback.apply(state);
			}

			@Override
			public int colorInWorld(BlockState state, BlockAndTintGetter getter, BlockPos pos) {
				return world.apply(state, getter, pos);
			}
		};
	}

	private static int auroraBaseColor(BlockPos pos) {
		float hue = safeRippleNoise(2, 128.0F, pos, 0.37F, 0.67F, 1.5F, 0.45F);
		return 0xFF000000 | ColorUtil.hsvToRGB(hue, 1.0F, 1.0F);
	}

	private static int desaturateAuroraColor(int normalColor) {
		int red = (normalColor >> 16) & 255;
		int blue = normalColor & 255;
		int green = (normalColor >> 8) & 255;
		float[] hsb = ColorUtil.rgbToHSV(red, green, blue);
		return 0xFF000000 | ColorUtil.hsvToRGB(hsb[0], hsb[1] * 0.5F, Math.min(hsb[2] + 0.4F, 0.9F));
	}

	private static int seasonalLeafColor(BlockPos pos, int xMul, int yMul, int zMul, int springR, int springG, int springB, int fallR, int fallG, int fallB) {
		int fade = pos.getX() * xMul + pos.getY() * yMul + pos.getZ() * zMul;
		if ((fade & 256) != 0) {
			fade = 255 - (fade & 255);
		}
		fade &= 255;

		float spring = (255 - fade) / 255F;
		float fall = fade / 255F;

		int red = (int) (spring * springR + fall * fallR);
		int green = (int) (spring * springG + fall * fallG);
		int blue = (int) (spring * springB + fall * fallB);
		return 0xFF000000 | red << 16 | green << 8 | blue;
	}

	private static int rainbowLeafColor(BlockPos pos) {
		int red = pos.getX() * 32 + pos.getY() * 16;
		if ((red & 256) != 0) {
			red = 255 - (red & 255);
		}
		red &= 255;

		int green = pos.getY() * 32 + pos.getZ() * 16;
		if ((green & 256) != 0) {
			green = 255 - (green & 255);
		}
		green ^= 255;

		int blue = pos.getX() * 16 + pos.getZ() * 32;
		if ((blue & 256) != 0) {
			blue = 255 - (blue & 255);
		}
		blue &= 255;

		return 0xFF000000 | red << 16 | green << 8 | blue;
	}

	private static float safeRippleNoise(int iterations, float size, BlockPos pos, float minimum, float maximum, float frequency, float fallback) {
		if (!simplexNoiseAvailable) {
			return fallback;
		}
		try {
			return SimplexNoiseHelper.rippleFractalNoise(iterations, size, pos, minimum, maximum, frequency);
		} catch (Throwable t) {
			simplexNoiseAvailable = false;
			TwilightForestMod.LOGGER.warn("Failed to sample simplex noise; falling back to static colors.", t);
			return fallback;
		}
	}

	@FunctionalInterface
	private interface StateTint {
		int apply(BlockState state);
	}

	@FunctionalInterface
	private interface WorldTint {
		int apply(BlockState state, BlockAndTintGetter getter, BlockPos pos);
	}
}
