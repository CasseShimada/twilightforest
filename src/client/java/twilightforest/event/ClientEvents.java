package twilightforest.client.event;

import com.ibm.icu.text.RuleBasedNumberFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.SplashRenderer;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.BlockOutlineRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.WrittenBookItem;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import twilightforest.TwilightForestMod;
import twilightforest.block.GiantBlock;
import twilightforest.block.MiniatureStructureBlock;
import twilightforest.block.entity.GrowingBeanstalkBlockEntity;
import twilightforest.client.BugModelAnimationHelper;
import twilightforest.client.OptifineWarningScreen;
import twilightforest.config.TFConfig;
import twilightforest.events.HostileMountEvents;
import twilightforest.init.TFDataAttachments;
import twilightforest.init.TFDataComponents;
import twilightforest.init.TFDimension;
import twilightforest.item.EnderBowItem;
import twilightforest.item.GiantPickItem;
import twilightforest.item.IceBowItem;
import twilightforest.item.SeekerBowItem;
import twilightforest.item.TripleBowItem;
import twilightforest.tags.TFItemTags;

import java.time.LocalDate;
import java.time.Month;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

public class ClientEvents {
	private static final VoxelShape GIANT_BLOCK = Shapes.box(0.0D, 0.0D, 0.0D, 4.0D, 4.0D, 4.0D);
	private static final MutableComponent WIP_TEXT = Component.translatable("misc.twilightforest.wip").withStyle(ChatFormatting.RED);
	private static final MutableComponent EMPERORS_CLOTH_TOOLTIP = Component.translatable("item.twilightforest.emperors_cloth.desc").withStyle(ChatFormatting.GRAY);
	public static final RenderStateDataKey<Boolean> HEAD_KEY = RenderStateDataKey.create(() -> TwilightForestMod.prefix("wearing_trophy").toString());

	private static boolean firstTitleScreenShown = false;

	public static int time = 0;
	private static float shakeIntensity = 0.0F;

	public static void register() {
		ScreenEvents.AFTER_INIT.register(ClientEvents::onScreenInit);
		ItemTooltipCallback.EVENT.register(ClientEvents::addCustomTooltips);
		ItemTooltipCallback.EVENT.register(ClientEvents::translateBookAuthor);
		ClientTickEvents.END_CLIENT_TICK.register(ClientEvents::clientTick);
		WorldRenderEvents.BEFORE_BLOCK_OUTLINE.register(ClientEvents::renderGiantBlockOutlines);

		HudElementRegistry.replaceElement(VanillaHudElements.MOUNT_HEALTH, original -> (context, tickCounter) -> {
			if (!HostileMountEvents.isRidingUnfriendly(Minecraft.getInstance().player)) {
				original.render(context, tickCounter);
			}
		});

		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> FogHandler.reset());
		CloudEvents.register();
	}

	private static void onScreenInit(Minecraft client, Screen screen, int scaledWidth, int scaledHeight) {
		handleGameBootup(screen);
		customizeSplashes(screen);
	}

	private static void handleGameBootup(Screen screen) {
		if (firstTitleScreenShown || !(screen instanceof TitleScreen)) return;

		if (RegistrationEvents.isOptifinePresent() && !TFConfig.disableOptifineNagScreen) {
			Minecraft.getInstance().setScreen(new OptifineWarningScreen(screen));
		}

		firstTitleScreenShown = true;
	}

	private static void customizeSplashes(Screen screen) {
		// TitleScreen splash text is private in 1.21.11; skip customization.
	}

	private static void clientTick(Minecraft mc) {
		if (!mc.isPaused()) {
			time++;

			if (mc.player != null) {
				TFDataAttachments.get(mc.player, TFDataAttachments.TF_PORTAL_COOLDOWN).tick(mc.player);
			}

			BugModelAnimationHelper.animate();

			if (TFConfig.firstPersonEffects && mc.level != null && mc.player != null) {
				HashSet<ChunkPos> chunksInRange = new HashSet<>();
				for (int x = -16; x <= 16; x += 16) {
					for (int z = -16; z <= 16; z += 16) {
						chunksInRange.add(new ChunkPos((int) (mc.player.getX() + x) >> 4, (int) (mc.player.getZ() + z) >> 4));
					}
				}
				for (ChunkPos pos : chunksInRange) {
					if (mc.level.getChunk(pos.x, pos.z, ChunkStatus.FULL, false) != null) {
						List<BlockEntity> beanstalksInChunk = mc.level.getChunk(pos.x, pos.z).getBlockEntities().values().stream()
							.filter(blockEntity -> blockEntity instanceof GrowingBeanstalkBlockEntity beanstalkBlock && beanstalkBlock.isBeanstalkRumbling())
							.toList();
						if (!beanstalksInChunk.isEmpty()) {
							BlockEntity beanstalk = beanstalksInChunk.getFirst();
							Player player = mc.player;
							shakeIntensity = (float) (1.0F - mc.player.distanceToSqr(Vec3.atCenterOf(beanstalk.getBlockPos())) / Math.pow(16, 2));
							if (shakeIntensity > 0) {
								player.setYRot(player.getYRot() + (player.getRandom().nextFloat() - 0.5F) * shakeIntensity);
								player.setXRot(player.getXRot() + (player.getRandom().nextFloat() * 2.5F - 1.25F) * shakeIntensity);
								shakeIntensity = 0.0F;
								break;
							}
						}
					}
				}
			}
		}

		if (mc.level != null && TFDimension.DIMENSION_KEY.equals(mc.level.dimension())) {
			mc.gui.vignetteBrightness = 0.0F;
		}

		if (mc.player != null && HostileMountEvents.isRidingUnfriendly(mc.player)) {
			mc.gui.setOverlayMessage(Component.empty(), false);
		}
	}

	private static void addCustomTooltips(ItemStack item, Item.TooltipContext context, TooltipFlag flag, List<Component> lines) {
		if (item.has(TFDataComponents.EMPERORS_CLOTH.get())) {
			lines.add(1, EMPERORS_CLOTH_TOOLTIP);
		}

		if (item.is(TFItemTags.WIP)) {
			lines.add(WIP_TEXT);
		}
	}

	private static void translateBookAuthor(ItemStack stack, Item.TooltipContext context, TooltipFlag flag, List<Component> lines) {
		if (stack.getItem() instanceof WrittenBookItem && stack.has(DataComponents.WRITTEN_BOOK_CONTENT)) {
			if (stack.has(TFDataComponents.TRANSLATABLE_BOOK.get())) {
				for (int i = 0; i < lines.size(); i++) {
					Component component = lines.get(i);
					if (component.toString().contains("book.byAuthor")) {
						lines.set(i, (Component.translatable("book.byAuthor", Component.translatable(TwilightForestMod.ID + ".book.author"))).withStyle(component.getStyle()));
					}
				}
			}
		}
	}

	private static boolean renderGiantBlockOutlines(WorldRenderContext context, BlockOutlineRenderState outlineState) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null) {
			return true;
		}

		BlockPos pos = outlineState.pos();
		BlockState state = mc.level.getBlockState(pos);

		if (state.getBlock() instanceof MiniatureStructureBlock) {
			return false;
		}

		LocalPlayer player = mc.player;
		if (player != null && (player.getMainHandItem().getItem() instanceof GiantPickItem || (player.getMainHandItem().getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof GiantBlock))) {
			if (!state.isAir() && player.level().getWorldBorder().isWithinBounds(pos)) {
				BlockPos offsetPos = new BlockPos(pos.getX() & ~0b11, pos.getY() & ~0b11, pos.getZ() & ~0b11);
				PoseStack poseStack = context.matrices();
				VertexConsumer consumer = context.consumers().getBuffer(RenderTypes.lines());
				Vec3 xyz = Vec3.atLowerCornerOf(offsetPos).subtract(getCameraPosition());
				PoseStack.Pose pose = poseStack.last();
				GIANT_BLOCK.forAllEdges((x1, y1, z1, x2, y2, z2) -> {
						float f = (float) (x2 - x1);
						float f1 = (float) (y2 - y1);
						float f2 = (float) (z2 - z1);
						float f3 = Mth.sqrt(f * f + f1 * f1 + f2 * f2);
						f /= f3;
						f1 /= f3;
						f2 /= f3;
						consumer.addVertex(pose, (float) (x1 + xyz.x()), (float) (y1 + xyz.y()), (float) (z1 + xyz.z())).setColor(0.0F, 0.0F, 0.0F, 0.45F).setNormal(pose, f, f1, f2);
						consumer.addVertex(pose, (float) (x2 + xyz.x()), (float) (y2 + xyz.y()), (float) (z2 + xyz.z())).setColor(0.0F, 0.0F, 0.0F, 0.45F).setNormal(pose, f, f1, f2);
					}
				);
			}
			return false;
		}

		return true;
	}

	public static boolean areCuriosEquipped(LivingEntity entity) {
//		if (ModList.get().isLoaded("curios")) {
//			return CuriosCompat.isCurioEquippedAndVisible(entity, stack -> stack.getItem() instanceof TrophyItem);
//		}
		return false;
	}

	public static float consumeShakeIntensity() {
		float current = shakeIntensity;
		shakeIntensity = 0.0F;
		return current;
	}

	private static Vec3 getCameraPosition() {
		Entity cameraEntity = Minecraft.getInstance().getCameraEntity();
		return cameraEntity != null ? cameraEntity.getEyePosition() : Vec3.ZERO;
	}
}
