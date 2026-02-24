package twilightforest.client.event;

import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemTintSources;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.GrassColor;
import net.minecraft.world.level.block.Blocks;
import twilightforest.TwilightForestMod;
import twilightforest.block.ClimbableHollowLogBlock;
import twilightforest.client.properties.PotionFlaskTintSource;
import twilightforest.enums.HollowLogVariants;
import twilightforest.init.TFBlocks;
import twilightforest.util.ColorUtil;
import twilightforest.util.SimplexNoiseHelper;

public class ColorHandler {
	public static final Int2IntFunction CANOPY_COLORIZER = color -> 0xFF000000 | (((color & 0xFEFEFE) + 0x469A66) / 2);
	public static final Int2IntFunction MANGROVE_COLORIZER = color -> 0xFF000000 | (((color & 0xFEFEFE) + 0xC0E694) / 2);
	private static boolean simplexNoiseAvailable = true;

	public static void registerColors() {
		registerBlockColors();
		registerTintSources();
	}

	private static void registerBlockColors() {
		ColorProviderRegistry.BLOCK.register((state, getter, pos, tintIndex) -> {
			float hue = getter == null ? 0.45F : safeRippleNoise(2, 128.0F, pos != null ? pos.above(128) : BlockPos.ZERO, 0.37F, 0.67F, 1.5F, 0.45F);
			return 0xFF000000 | ColorUtil.hsvToRGB(hue, 1.0F, 1.0F);
		}, TFBlocks.AURORA_BLOCK.get());
		ColorProviderRegistry.BLOCK.register((state, getter, pos, tintIndex) -> {
			var blockColors = Minecraft.getInstance().getBlockColors();
			if (blockColors == null) {
				return 0xFFFFFFFF;
			}
			int normalColor = blockColors.getColor(TFBlocks.AURORA_BLOCK.get().defaultBlockState(), getter, pos, tintIndex);

			int red = (normalColor >> 16) & 255;
			int blue = normalColor & 255;
			int green = (normalColor >> 8) & 255;

			float[] hsb = ColorUtil.rgbToHSV(red, green, blue);

			return 0xFF000000 | ColorUtil.hsvToRGB(hsb[0], hsb[1] * 0.5F, Math.min(hsb[2] + 0.4F, 0.9F));
		}, TFBlocks.AURORA_PILLAR.get(), TFBlocks.AURORA_SLAB.get(), TFBlocks.AURORALIZED_GLASS.get());
		ColorProviderRegistry.BLOCK.register((state, getter, pos, tintIndex) -> {
			var blockColors = Minecraft.getInstance().getBlockColors();
			if (blockColors == null) {
				return GrassColor.getDefaultColor();
			}
			return blockColors.getColor(Blocks.SHORT_GRASS.defaultBlockState(), getter, pos, tintIndex);
		}, TFBlocks.SMOKER.get(), TFBlocks.FIRE_JET.get());
		ColorProviderRegistry.BLOCK.register((state, getter, pos, tintIndex) -> getter != null && pos != null ? 0xFF000000 | 2129968 : 0xFF000000 | 7455580, TFBlocks.HUGE_LILY_PAD.get());
		ColorProviderRegistry.BLOCK.register((state, getter, pos, tintIndex) -> {
			if (getter == null || pos == null) {
				return 0xFF000000 | 106 << 16 | 156 << 8 | 23;
			} else {
				int red, green, blue;

				int fade = pos.getX() * 16 + pos.getY() * 16 + pos.getZ() * 16;
				if ((fade & 256) != 0) {
					fade = 255 - (fade & 255);
				}
				fade &= 255;

				float spring = (255 - fade) / 255F;
				float fall = fade / 255F;

				red = (int) (spring * 106 + fall * 251);
				green = (int) (spring * 156 + fall * 108);
				blue = (int) (spring * 23 + fall * 27);

				return 0xFF000000 | red << 16 | green << 8 | blue;
			}
		}, TFBlocks.TIME_LEAVES.get());
		ColorProviderRegistry.BLOCK.register((state, getter, pos, tintIndex) -> {
			if (getter == null || pos == null) {
				return 0xFF000000 | 108 << 16 | 204 << 8 | 234;
			} else {
				int red, green, blue;

				int fade = pos.getX() * 27 + pos.getY() * 63 + pos.getZ() * 39;
				if ((fade & 256) != 0) {
					fade = 255 - (fade & 255);
				}
				fade &= 255;

				float spring = (255 - fade) / 255F;
				float fall = fade / 255F;

				red = (int) (spring * 108 + fall * 96);
				green = (int) (spring * 204 + fall * 107);
				blue = (int) (spring * 234 + fall * 121);

				return 0xFF000000 | red << 16 | green << 8 | blue;
			}
		}, TFBlocks.TRANSFORMATION_LEAVES.get());
		ColorProviderRegistry.BLOCK.register((state, getter, pos, tintIndex) -> {
			if (getter == null || pos == null) {
				return 0xFF000000 | 252 << 16 | 241 << 8 | 68;
			} else {
				int red, green, blue;

				int fade = pos.getX() * 31 + pos.getY() * 33 + pos.getZ() * 32;
				if ((fade & 256) != 0) {
					fade = 255 - (fade & 255);
				}
				fade &= 255;

				float spring = (255 - fade) / 255F;
				float fall = fade / 255F;

				red = (int) (spring * 252 + fall * 237);
				green = (int) (spring * 241 + fall * 172);
				blue = (int) (spring * 68 + fall * 9);

				return 0xFF000000 | red << 16 | green << 8 | blue;
			}
		}, TFBlocks.MINING_LEAVES.get());
		ColorProviderRegistry.BLOCK.register((state, getter, pos, tintIndex) -> {
			if (getter == null || pos == null) {
				return 0xFF000000 | 54 << 16 | 76 << 8 | 3;
			} else {
				int red, green, blue;

				int fade = pos.getX() * 63 + pos.getY() * 63 + pos.getZ() * 63;
				if ((fade & 256) != 0) {
					fade = 255 - (fade & 255);
				}
				fade &= 255;

				float spring = (255 - fade) / 255F;
				float fall = fade / 255F;

				red = (int) (spring * 54 + fall * 168);
				green = (int) (spring * 76 + fall * 199);
				blue = (int) (spring * 3 + fall * 43);

				return 0xFF000000 | red << 16 | green << 8 | blue;
			}
		}, TFBlocks.SORTING_LEAVES.get());
		ColorProviderRegistry.BLOCK.register((state, getter, pos, tintIndex) -> {
			if (getter == null || pos == null) {
				return -1;
			} else {
				float f = safeRippleNoise(2, 32.0f, pos, 0.4f, 1.0f, 2f, 0.7f);
				return 0xFF000000 | ColorUtil.hsvToRGB(0.1f, 1f - f, (f + 2f) / 3f);
			}
		}, TFBlocks.TOWERWOOD.get(), TFBlocks.CRACKED_TOWERWOOD.get(), TFBlocks.INFESTED_TOWERWOOD.get(), TFBlocks.MOSSY_TOWERWOOD.get());
		ColorProviderRegistry.BLOCK.register((state, getter, pos, tintIndex) -> getter != null && pos != null ? BiomeColors.getAverageFoliageColor(getter, pos) : FoliageColor.FOLIAGE_DEFAULT, TFBlocks.TWILIGHT_OAK_LEAVES.get(), TFBlocks.DARK_LEAVES.get(), TFBlocks.HARDENED_DARK_LEAVES.get(), TFBlocks.GIANT_LEAVES.get(), TFBlocks.FALLEN_LEAVES.get());
		ColorProviderRegistry.BLOCK.register((state, getter, pos, tintIndex) -> getter != null && pos != null ? CANOPY_COLORIZER.apply(BiomeColors.getAverageFoliageColor(getter, pos)) : FoliageColor.FOLIAGE_EVERGREEN, TFBlocks.CANOPY_LEAVES.get());
		ColorProviderRegistry.BLOCK.register((state, getter, pos, tintIndex) -> getter != null && pos != null ? MANGROVE_COLORIZER.apply(BiomeColors.getAverageFoliageColor(getter, pos)) : FoliageColor.FOLIAGE_BIRCH, TFBlocks.MANGROVE_LEAVES.get());
		ColorProviderRegistry.BLOCK.register((state, getter, pos, tintIndex) -> {
			if (getter == null || pos == null) {
				return FoliageColor.FOLIAGE_DEFAULT;
			} else {
				// RAINBOW!
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
		}, TFBlocks.RAINBOW_OAK_LEAVES.get());
		ColorProviderRegistry.BLOCK.register((state, getter, pos, tintIndex) -> FoliageColor.FOLIAGE_EVERGREEN, TFBlocks.BEANSTALK_LEAVES.get(), TFBlocks.THORN_LEAVES.get());
		ColorProviderRegistry.BLOCK.register((state, getter, pos, tintIndex) -> getter != null && pos != null ? BiomeColors.getAverageGrassColor(getter, pos) : GrassColor.getDefaultColor(), TFBlocks.FIDDLEHEAD.get(), TFBlocks.POTTED_FIDDLEHEAD.get());
		ColorProviderRegistry.BLOCK.register((state, getter, pos, tintIndex) -> {
				if (tintIndex != 0) {
					return getter != null && pos != null ? BiomeColors.getAverageGrassColor(getter, pos) : GrassColor.getDefaultColor();
				} else {
					return -1;
				}
			}, TFBlocks.HOLLOW_OAK_LOG_HORIZONTAL.get(), TFBlocks.HOLLOW_SPRUCE_LOG_HORIZONTAL.get(), TFBlocks.HOLLOW_BIRCH_LOG_HORIZONTAL.get(), TFBlocks.HOLLOW_JUNGLE_LOG_HORIZONTAL.get(),
			TFBlocks.HOLLOW_ACACIA_LOG_HORIZONTAL.get(), TFBlocks.HOLLOW_DARK_OAK_LOG_HORIZONTAL.get(), TFBlocks.HOLLOW_CRIMSON_STEM_HORIZONTAL.get(), TFBlocks.HOLLOW_WARPED_STEM_HORIZONTAL.get(),
			TFBlocks.HOLLOW_VANGROVE_LOG_HORIZONTAL.get(), TFBlocks.HOLLOW_CHERRY_LOG_HORIZONTAL.get(),
			TFBlocks.HOLLOW_TWILIGHT_OAK_LOG_HORIZONTAL.get(), TFBlocks.HOLLOW_CANOPY_LOG_HORIZONTAL.get(), TFBlocks.HOLLOW_MANGROVE_LOG_HORIZONTAL.get(), TFBlocks.HOLLOW_DARK_LOG_HORIZONTAL.get(),
			TFBlocks.HOLLOW_TIME_LOG_HORIZONTAL.get(), TFBlocks.HOLLOW_TRANSFORMATION_LOG_HORIZONTAL.get(), TFBlocks.HOLLOW_MINING_LOG_HORIZONTAL.get(), TFBlocks.HOLLOW_SORTING_LOG_HORIZONTAL.get());
		ColorProviderRegistry.BLOCK.register((state, getter, pos, tintIndex) -> {
				if (state.getValue(ClimbableHollowLogBlock.VARIANT) != HollowLogVariants.Climbable.VINE || tintIndex != 0) {
					return -1;
				} else {
					if (getter != null && pos != null) {
						return BiomeColors.getAverageFoliageColor(getter, pos);
					} else {
						return FoliageColor.FOLIAGE_DEFAULT;
					}
				}
			}, TFBlocks.HOLLOW_OAK_LOG_CLIMBABLE.get(), TFBlocks.HOLLOW_SPRUCE_LOG_CLIMBABLE.get(), TFBlocks.HOLLOW_BIRCH_LOG_CLIMBABLE.get(), TFBlocks.HOLLOW_JUNGLE_LOG_CLIMBABLE.get(),
			TFBlocks.HOLLOW_ACACIA_LOG_CLIMBABLE.get(), TFBlocks.HOLLOW_DARK_OAK_LOG_CLIMBABLE.get(), TFBlocks.HOLLOW_CRIMSON_STEM_CLIMBABLE.get(), TFBlocks.HOLLOW_WARPED_STEM_CLIMBABLE.get(), TFBlocks.HOLLOW_VANGROVE_LOG_CLIMBABLE.get(),
			TFBlocks.HOLLOW_TWILIGHT_OAK_LOG_CLIMBABLE.get(), TFBlocks.HOLLOW_CANOPY_LOG_CLIMBABLE.get(), TFBlocks.HOLLOW_MANGROVE_LOG_CLIMBABLE.get(), TFBlocks.HOLLOW_DARK_LOG_CLIMBABLE.get(),
			TFBlocks.HOLLOW_TIME_LOG_CLIMBABLE.get(), TFBlocks.HOLLOW_TRANSFORMATION_LOG_CLIMBABLE.get(), TFBlocks.HOLLOW_MINING_LOG_CLIMBABLE.get(), TFBlocks.HOLLOW_SORTING_LOG_CLIMBABLE.get());
		ColorProviderRegistry.BLOCK.register((state, getter, pos, tintIndex) -> GrassColor.getDefaultColor(),
			TFBlocks.TWILIGHT_PORTAL_MINIATURE_STRUCTURE.get(), TFBlocks.NAGA_COURTYARD_MINIATURE_STRUCTURE.get(), TFBlocks.LICH_TOWER_MINIATURE_STRUCTURE.get());
		ColorProviderRegistry.BLOCK.register((state, getter, pos, tintIndex) -> 0xFFFF00FF, TFBlocks.PINK_CASTLE_RUNE_BRICK.get(), TFBlocks.PINK_CASTLE_DOOR.get());
		ColorProviderRegistry.BLOCK.register((state, getter, pos, tintIndex) -> 0xFF00FFFF, TFBlocks.BLUE_CASTLE_RUNE_BRICK.get(), TFBlocks.BLUE_CASTLE_DOOR.get());
		ColorProviderRegistry.BLOCK.register((state, getter, pos, tintIndex) -> 0xFFFFFF00, TFBlocks.YELLOW_CASTLE_RUNE_BRICK.get(), TFBlocks.YELLOW_CASTLE_DOOR.get());
		ColorProviderRegistry.BLOCK.register((state, getter, pos, tintIndex) -> 0xFF4B0082, TFBlocks.VIOLET_CASTLE_RUNE_BRICK.get(), TFBlocks.VIOLET_CASTLE_DOOR.get());
		ColorProviderRegistry.BLOCK.register((state, getter, pos, tintIndex) -> 0xFF5C1074, TFBlocks.VIOLET_FORCE_FIELD.get());
		ColorProviderRegistry.BLOCK.register((state, getter, pos, tintIndex) -> 0xFFFA057E, TFBlocks.PINK_FORCE_FIELD.get());
		ColorProviderRegistry.BLOCK.register((state, getter, pos, tintIndex) -> 0xFFFF5B02, TFBlocks.ORANGE_FORCE_FIELD.get());
		ColorProviderRegistry.BLOCK.register((state, getter, pos, tintIndex) -> 0xFF89E701, TFBlocks.GREEN_FORCE_FIELD.get());
		ColorProviderRegistry.BLOCK.register((state, getter, pos, tintIndex) -> 0xFF0DDEFF, TFBlocks.BLUE_FORCE_FIELD.get());
	}

	private static void registerTintSources() {
		ItemTintSources.ID_MAPPER.put(TwilightForestMod.prefix("potion_flask"), PotionFlaskTintSource.TYPE);
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
}
