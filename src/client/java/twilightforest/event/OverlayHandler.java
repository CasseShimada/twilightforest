package twilightforest.client.event;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.Window;
import com.mojang.datafixers.util.Pair;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudStatusBarHeightRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ARGB;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.HitResult;
import twilightforest.TwilightForestMod;
import twilightforest.components.entity.TFPortalAttachment;
import twilightforest.components.item.OreScannerData;
import twilightforest.config.TFConfig;
import twilightforest.entity.passive.QuestRam;
import twilightforest.entity.passive.quest.ram.QuestingRamCurrentContext;
import twilightforest.events.HostileMountEvents;
import twilightforest.init.TFBlocks;
import twilightforest.init.TFDataAttachments;
import twilightforest.init.TFDataComponents;
import twilightforest.init.TFItems;
import twilightforest.item.OreMeterItem;
import twilightforest.util.ComponentAlignment;

import java.text.DecimalFormat;
import java.lang.reflect.Method;
import java.util.*;

public class OverlayHandler {
	private static final Identifier QUESTING_RAM_CHECK_SPRITE = TwilightForestMod.prefix("questing_ram_check");
	private static final Identifier QUESTING_RAM_X_SPRITE = TwilightForestMod.prefix("questing_ram_x");
	private static final Identifier FORTIFICATION_SHIELD_SPRITE = TwilightForestMod.prefix("fortification_shield");
	private static final Identifier HOSTILE_MOUNT_HUNGER_BAR = TwilightForestMod.prefix("hostile_mount_hunger_bar");
	private static final Identifier FORTIFICATION_SHIELD_BAR = TwilightForestMod.prefix("fortification_shield_count");
	public static final Map<Long, OreMeterInfoCache> ORE_METER_STAT_CACHE = new HashMap<>();

	private static final QuestingRamCurrentContext questingRamCurrentContext = QuestingRamCurrentContext.INSTANCE;
	private static Method renderFoodMethod;
	private static Method canRenderCrosshairMethod;

	public static void registerOverlays() {
		HudElementRegistry.attachElementAfter(VanillaHudElements.CROSSHAIR, TwilightForestMod.prefix("quest_ram_indicator"), (graphics, tickCounter) -> {
			Minecraft minecraft = Minecraft.getInstance();
			LocalPlayer player = minecraft.player;
			Gui gui = minecraft.gui;
			if (player != null && !minecraft.options.hideGui && TFConfig.showQuestRamCrosshairIndicator) {
				renderIndicator(minecraft, graphics, gui, player, graphics.guiWidth(), graphics.guiHeight());
			}
		});

		HudStatusBarHeightRegistry.addRight(HOSTILE_MOUNT_HUNGER_BAR, player -> shouldRenderHostileMountBar(Minecraft.getInstance(), player) ? 10 : 0);
		HudElementRegistry.attachElementAfter(VanillaHudElements.MOUNT_HEALTH, HOSTILE_MOUNT_HUNGER_BAR, (graphics, tickCounter) -> {
			Minecraft minecraft = Minecraft.getInstance();
			LocalPlayer player = minecraft.player;
			Gui gui = minecraft.gui;
			if (player != null && shouldRenderHostileMountBar(minecraft, player)) {
				int xPos = graphics.guiWidth() / 2 + 91;
				int yPos = graphics.guiHeight() - HudStatusBarHeightRegistry.getHeight(HOSTILE_MOUNT_HUNGER_BAR);
				invokeRenderFood(gui, graphics, player, yPos, xPos);
			}
		});

		HudElementRegistry.attachElementAfter(VanillaHudElements.SUBTITLES, TwilightForestMod.prefix("ore_meter_stats"), (graphics, tickCounter) -> {
			Minecraft minecraft = Minecraft.getInstance();
			LocalPlayer player = minecraft.player;
			Gui gui = minecraft.gui;
			if (player != null && !minecraft.options.hideGui && !gui.getDebugOverlay().showDebugScreen() && minecraft.screen == null) {
				renderOreMeterStats(graphics, player);
			}
		});

		HudStatusBarHeightRegistry.addLeft(FORTIFICATION_SHIELD_BAR, player -> shouldRenderFortificationShieldBar(Minecraft.getInstance(), player) ? 10 : 0);
		HudElementRegistry.attachElementAfter(VanillaHudElements.ARMOR_BAR, FORTIFICATION_SHIELD_BAR, (graphics, tickCounter) -> {
			Minecraft minecraft = Minecraft.getInstance();
			LocalPlayer player = minecraft.player;
			Gui gui = minecraft.gui;
			if (player != null && shouldRenderFortificationShieldBar(minecraft, player)) {
				renderShieldCount(graphics, gui, graphics.guiWidth(), graphics.guiHeight(), TFDataAttachments.get(player, TFDataAttachments.FORTIFICATION_SHIELDS).shieldsLeft());
			}
		});

		HudElementRegistry.attachElementAfter(VanillaHudElements.SUBTITLES, TwilightForestMod.prefix("portal_overlay"), (graphics, tickCounter) -> {
			Minecraft minecraft = Minecraft.getInstance();
			Window window = minecraft.getWindow();
			LocalPlayer player = minecraft.player;

			if (player != null) {
				TFPortalAttachment portal = TFDataAttachments.get(player, TFDataAttachments.TF_PORTAL_COOLDOWN);
				if (portal.getPortalTimer() > 0) {
					TextureAtlasSprite textureatlassprite = minecraft.getModelManager().getBlockStateModelSet().getParticleMaterial(TFBlocks.TWILIGHT_PORTAL.get().defaultBlockState()).sprite();
					graphics.blitSprite(RenderPipelines.BLOCK_SCREEN_EFFECT, textureatlassprite, 0, 0, window.getGuiScaledWidth(), window.getGuiScaledHeight(), ARGB.white((float) portal.getPortalTimer() / (float) TFPortalAttachment.MAX_TICKS));
				}
			}
		});
	}

	private static void renderIndicator(Minecraft minecraft, GuiGraphicsExtractor graphics, Gui gui, Player player, int screenWidth, int screenHeight) {
		if (minecraft.options.getCameraType().isFirstPerson() && (minecraft.gameMode.getPlayerMode() != GameType.SPECTATOR || canRenderCrosshairForSpectator(gui, minecraft.hitResult)) && minecraft.crosshairPickEntity instanceof QuestRam ram) {
			ItemStack stack = player.getInventory().getSelectedItem();
			if (!stack.isEmpty()) {
				for (var questEntry : questingRamCurrentContext.getContext().questItems().entrySet()) {
					if (questEntry.getValue().test(stack)) {
						int j = ((screenHeight - 1) / 2) - 11;
						int k = ((screenWidth - 1) / 2) - 3;
						if (!ram.isColorPresent(questEntry.getKey())) {
							graphics.blitSprite(RenderPipelines.CROSSHAIR, QUESTING_RAM_X_SPRITE, k, j, 7, 7);
						} else {
							graphics.blitSprite(RenderPipelines.CROSSHAIR, QUESTING_RAM_CHECK_SPRITE, k, j, 7, 7);
						}
						break;
					}
				}
			}
		}
	}

	private static boolean canRenderCrosshairForSpectator(Gui gui, HitResult hitResult) {
		try {
			if (canRenderCrosshairMethod == null) {
				canRenderCrosshairMethod = Gui.class.getDeclaredMethod("canRenderCrosshairForSpectator", HitResult.class);
				canRenderCrosshairMethod.setAccessible(true);
			}
			return (boolean) canRenderCrosshairMethod.invoke(gui, hitResult);
		} catch (ReflectiveOperationException e) {
			return hitResult != null;
		}
	}

	private static boolean shouldRenderHostileMountBar(Minecraft minecraft, Player player) {
		return !minecraft.options.hideGui && minecraft.gameMode.canHurtPlayer() && HostileMountEvents.isRidingUnfriendly(player);
	}

	private static boolean shouldRenderFortificationShieldBar(Minecraft minecraft, Player player) {
		return !minecraft.options.hideGui
			&& (minecraft.gameMode.canHurtPlayer() || TFConfig.showFortificationShieldIndicatorInCreative)
			&& TFDataAttachments.has(player, TFDataAttachments.FORTIFICATION_SHIELDS)
			&& TFDataAttachments.get(player, TFDataAttachments.FORTIFICATION_SHIELDS).shieldsLeft() > 0
			&& TFConfig.showFortificationShieldIndicator;
	}

	private static void invokeRenderFood(Gui gui, GuiGraphicsExtractor graphics, Player player, int yPos, int xPos) {
		try {
			if (renderFoodMethod == null) {
				renderFoodMethod = Gui.class.getDeclaredMethod("renderFood", GuiGraphicsExtractor.class, Player.class, int.class, int.class);
				renderFoodMethod.setAccessible(true);
			}
			renderFoodMethod.invoke(gui, graphics, player, yPos, xPos);
		} catch (ReflectiveOperationException e) {
			// If reflection fails, skip rendering the bar.
		}
	}

	private static void renderShieldCount(GuiGraphicsExtractor graphics, Gui gui, int screenWidth, int screenHeight, int shieldCount) {
		int yPos = screenHeight - HudStatusBarHeightRegistry.getHeight(FORTIFICATION_SHIELD_BAR);
		for (int i = 0; i < Math.min(shieldCount, 10); i++) {
			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, FORTIFICATION_SHIELD_SPRITE, screenWidth / 2 - 91 + (i * 8), yPos, 9, 9);
		}
	}

	private static void renderOreMeterStats(GuiGraphicsExtractor graphics, Player player) {
		if (player.isHolding(TFItems.ORE_METER.get())) {
			InteractionHand handToUse = player.getItemInHand(InteractionHand.MAIN_HAND).is(TFItems.ORE_METER.get()) ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
			ItemStack selectedMeter = player.getItemInHand(handToUse);
			if (OreMeterItem.isLoading(selectedMeter)) {
				int dots = (OreMeterItem.getLoadProgress(selectedMeter) / 5) % 3;
				Component component = Component.translatable("misc.twilightforest.ore_meter_loading");
				for (int i = 0; i <= dots; i++) {
					component = component.copy().append(".");
				}
				graphics.fill(0, 0, 56, 16, 0x9b000000);
				graphics.text(Minecraft.getInstance().font, component, 4, 4, 16777215, false);
			} else {
				OreScannerData oreScannerData = selectedMeter.get(TFDataComponents.ORE_DATA.get());

				if (oreScannerData == null) return;

				long identifier = oreScannerData.universalId();
				if (identifier != 0L && !ORE_METER_STAT_CACHE.containsKey(identifier)) {
					initTooltips(identifier, selectedMeter.getOrDefault(TFDataComponents.ORE_RANGE.get(), 1), oreScannerData);
				}

				if (ORE_METER_STAT_CACHE.containsKey(identifier)) {
					OreMeterInfoCache info = ORE_METER_STAT_CACHE.get(identifier);

					if (info != null) {
						info.renderData(graphics);
					}
				}
			}
		}
	}

	private static final DecimalFormat FORMAT = new DecimalFormat("0.000");

	private static void initTooltips(long id, int range, OreScannerData data) {
		ChunkPos pos = data.scannedChunk();
		int totalScanned = data.totalScannedBlocks();

		List<Component> headerRowTexts = ImmutableList.of(
			Component.translatable("misc.twilightforest.ore_meter_range", range, pos.x(), pos.z()),
			Component.translatable("misc.twilightforest.ore_meter_total", totalScanned)
		);

		ArrayList<ComponentColumn> columns = new ArrayList<>();

		List<Pair<String, Integer>> scanData = data.counts().entrySet().stream()
			.map(e -> Pair.of(e.getKey(), e.getValue()))
			.sorted(Comparator.comparing(Pair::getSecond))
			.toList();

		if (TFConfig.prettifyOreMeterGui) {
			ComponentColumn padding = ComponentColumn.padding(1);
			List<Integer> counts = scanData.stream().map(Pair::getSecond).toList();

			columns.add(nameColumn(scanData.stream().map(Pair::getFirst).toList()));
			columns.add(padding);
			columns.add(dashColumn(scanData.size()));
			columns.add(padding);
			columns.add(countColumn(counts));
			columns.add(padding);
			columns.add(ratioColumn(totalScanned, counts));
		} else {
			columns.add(withoutPrettyPrinting(totalScanned, scanData));
		}

		ORE_METER_STAT_CACHE.put(id, OreMeterInfoCache.build(headerRowTexts, columns));
	}

	private static ComponentColumn withoutPrettyPrinting(int totalScanned, List<Pair<String, Integer>> entries) {
		List<Component> tooltips = new ArrayList<>();

		for (Pair<String, Integer> entry : entries) {
			String percentage = FORMAT.format(entry.getSecond() * 100.0F / totalScanned);
			Component formattedEntry = Component.translatable(entry.getFirst())
				.append(Component.literal(" "))
				.append(Component.translatable("misc.twilightforest.ore_meter_separator"))
				.append(Component.literal(" " + entry.getSecond() + " "))
				.append(Component.translatable("misc.twilightforest.ore_meter_ratio", percentage));

			tooltips.add(formattedEntry);
		}

		return ComponentColumn.build(tooltips, ComponentAlignment.LEFT);
	}

	private static ComponentColumn nameColumn(List<String> oreNameKeys) {
		ImmutableList.Builder<Component> toList = ImmutableList.builder();

		toList.add(Component.translatable("misc.twilightforest.ore_meter_header_block").withStyle(ChatFormatting.GRAY));

		for (String oreNameKey : oreNameKeys) {
			MutableComponent translatable = Component.translatable(oreNameKey);
			toList.add(translatable);
		}

		return ComponentColumn.build(toList.build(), ComponentAlignment.LEFT);
	}

	private static ComponentColumn dashColumn(int size) {
		ImmutableList.Builder<Component> toList = ImmutableList.builder();

		toList.add(Component.empty());

		MutableComponent dash = Component.translatable("misc.twilightforest.ore_meter_separator");
		for (int i = 0; i < size; i++)
			toList.add(dash);

		return ComponentColumn.build(toList.build(), ComponentAlignment.CENTER);
	}

	private static ComponentColumn countColumn(List<Integer> oreCounts) {
		ImmutableList.Builder<Component> toList = ImmutableList.builder();

		toList.add(Component.translatable("misc.twilightforest.ore_meter_header_count").withStyle(ChatFormatting.GRAY));

		oreCounts.stream().mapToInt(count -> count).mapToObj(count -> Component.literal(String.valueOf(count))).forEach(toList::add);

		return ComponentColumn.build(toList.build(), ComponentAlignment.RIGHT);
	}

	private static ComponentColumn ratioColumn(int totalScanned, List<Integer> oreCounts) {
		ImmutableList.Builder<Component> toList = ImmutableList.builder();

		toList.add(Component.translatable("misc.twilightforest.ore_meter_header_ratio").withStyle(ChatFormatting.GRAY));

		for (int count : oreCounts) {
			var percentage = FORMAT.format(count * 100.0F / totalScanned);
			toList.add(Component.translatable("misc.twilightforest.ore_meter_ratio", percentage));
		}

		return ComponentColumn.build(toList.build(), ComponentAlignment.RIGHT);
	}

	public record ComponentColumn(List<? extends Component> textRows, int maxPixelWidth,
								  ComponentAlignment textAlignment) {
		public static ComponentColumn build(List<? extends Component> rowTexts, ComponentAlignment textAlignment) {
			int maxColumnPixelWidth = rowTexts.stream().mapToInt(c -> Minecraft.getInstance().font.width(c)).max().orElse(0);
			return new ComponentColumn(rowTexts, maxColumnPixelWidth, textAlignment);
		}

		public static ComponentColumn padding(int forcedExtraMaxWidthBySpaces) {
			return new ComponentColumn(List.of(), forcedExtraMaxWidthBySpaces * Minecraft.getInstance().font.width(" "), ComponentAlignment.LEFT);
		}

		private int renderColumn(GuiGraphicsExtractor graphics, ComponentColumn column, int xOff, int yOff, int verticalTextPixelsAdvance) {
			for (Component rowText : column.textRows) {
				int textPixelWidth = Minecraft.getInstance().font.width(rowText);
				int textXPos = xOff + this.textAlignment.getTextOffset(textPixelWidth, this.maxPixelWidth);
				graphics.text(Minecraft.getInstance().font, rowText, textXPos, yOff, 0x00_ff_ff_ff, false);
				yOff += verticalTextPixelsAdvance;
			}

			return column.maxPixelWidth;
		}
	}

	public record OreMeterInfoCache(int totalPixelWidth, int totalRowCount, List<Component> headerRows, List<ComponentColumn> textColumns) {
		public static OreMeterInfoCache build(List<Component> headers, List<ComponentColumn> columns) {
			int summedColumnMaxWidths = columns.stream().mapToInt(ComponentColumn::maxPixelWidth).sum();
			int maxHeaderWidth = headers.stream().mapToInt(c -> Minecraft.getInstance().font.width(c)).max().orElse(0);

			int maxPixelWidth = Math.max(summedColumnMaxWidths, maxHeaderWidth);

			int totalRowCount = headers.size() + columns.stream().mapToInt(column -> column.textRows.size()).max().orElse(0);

			return new OreMeterInfoCache(maxPixelWidth, totalRowCount, ImmutableList.copyOf(headers), ImmutableList.copyOf(columns));
		}

		public void renderData(GuiGraphicsExtractor graphics) {
			int verticalTextPixelsAdvance = Minecraft.getInstance().font.lineHeight + 1;

			graphics.fill(0, 0, this.totalPixelWidth + 8, this.totalRowCount * verticalTextPixelsAdvance + 6, 0x9b_00_00_00);

			int xOff = 4;
			int yOff = 4;

			for (Component headerRowText : this.headerRows) {
				graphics.text(Minecraft.getInstance().font, headerRowText, xOff, yOff, 0x00_ff_ff_ff, false);
				yOff += verticalTextPixelsAdvance;
			}

			for (ComponentColumn column : this.textColumns) {
				xOff += column.renderColumn(graphics, column, xOff, yOff, verticalTextPixelsAdvance);
			}
		}
	}
}
