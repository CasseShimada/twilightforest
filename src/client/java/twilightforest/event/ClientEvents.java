package twilightforest.client.event;

import com.ibm.icu.text.RuleBasedNumberFormat;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
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
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.state.BlockOutlineRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
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
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;
import twilightforest.TwilightForestMod;
import twilightforest.block.GiantBlock;
import twilightforest.block.MiniatureStructureBlock;
import twilightforest.block.entity.GrowingBeanstalkBlockEntity;
import twilightforest.client.BugModelAnimationHelper;
import twilightforest.client.OptifineWarningScreen;
import twilightforest.client.renderer.TFRenderPipelines;
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
import twilightforest.mixin.client.accessor.BiomeManagerAccessor;
import twilightforest.tags.TFItemTags;
import twilightforest.util.HolderMatcher;

import java.time.LocalDate;
import java.time.Month;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.OptionalDouble;
import java.util.OptionalInt;

public class ClientEvents {
	private static final VoxelShape GIANT_BLOCK = Shapes.box(0.0D, 0.0D, 0.0D, 4.0D, 4.0D, 4.0D);
	private static final MutableComponent WIP_TEXT = Component.translatable("misc.twilightforest.wip").withStyle(ChatFormatting.RED);
	private static final MutableComponent EMPERORS_CLOTH_TOOLTIP = Component.translatable("item.twilightforest.emperors_cloth.desc").withStyle(ChatFormatting.GRAY);
	public static final RenderStateDataKey<Boolean> HEAD_KEY = RenderStateDataKey.create(() -> TwilightForestMod.prefix("wearing_trophy").toString());
	private static final int AURORA_UNIFORM_SIZE = 32;

	private static boolean firstTitleScreenShown = false;

	public static int time = 0;
	private static float shakeIntensity = 0.0F;

	private static int aurora = 0;
	private static int lastAurora = 0;
	private static GpuBuffer auroraUniformBuffer;

	private static final HolderMatcher HOLDER_MATCHER = new HolderMatcher();

	public static void register() {
		ScreenEvents.AFTER_INIT.register(ClientEvents::onScreenInit);
		ItemTooltipCallback.EVENT.register(ClientEvents::addCustomTooltips);
		ItemTooltipCallback.EVENT.register(ClientEvents::translateBookAuthor);
		ClientTickEvents.END_CLIENT_TICK.register(ClientEvents::clientTick);
		WorldRenderEvents.BEFORE_TRANSLUCENT.register(ClientEvents::renderAurora);
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

			lastAurora = aurora;
			if (mc.level != null && mc.getCameraEntity() != null && !TFConfig.getValidAuroraBiomes(mc.level.registryAccess()).isEmpty()) {
				RegistryAccess access = mc.level.registryAccess();
				Holder<Biome> biome = mc.level.getBiome(mc.getCameraEntity().blockPosition());
				if (TFConfig.getValidAuroraBiomes(access).stream().anyMatch(c -> HOLDER_MATCHER.match(c, biome)))
					aurora++;
				else
					aurora--;
				aurora = Mth.clamp(aurora, 0, 60);
			} else {
				aurora = 0;
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

	private static void renderAurora(WorldRenderContext context) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null) return;

		float partialTick = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
		float alpha = (Mth.lerp(partialTick, lastAurora, aurora)) / 60F * 0.5F;
		if (alpha <= 0.001F) return;

		final float scale = 2048F * (mc.gameRenderer.getRenderDistance() / 32F);
		Vec3 pos = getCameraPosition();
		float y = (float) (256F - pos.y());

		BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
		addAuroraVertex(buffer, -scale, y, scale, 0.0F, 1.0F, alpha);
		addAuroraVertex(buffer, -scale, y, -scale, 0.0F, 0.0F, alpha);
		addAuroraVertex(buffer, scale, y, -scale, 1.0F, 0.0F, alpha);
		addAuroraVertex(buffer, scale, y, scale, 1.0F, 1.0F, alpha);

		MeshData mesh = buffer.build();
		if (mesh == null) {
			return;
		}

		int seed = 0;
		if (mc.level != null) {
			seed = Mth.abs((int) ((BiomeManagerAccessor) mc.level.getBiomeManager()).twilightforest$getBiomeZoomSeed());
		}
		renderAuroraMesh(mesh, pos, seed);
	}

	private static void renderAuroraMesh(MeshData mesh, Vec3 cameraPos, int seed) {
		RenderPipeline pipeline = TFRenderPipelines.AURORA;
		CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
		Matrix4f modelView = new Matrix4f(RenderSystem.getModelViewMatrix()).setTranslation(0.0F, 0.0F, 0.0F);
		GpuBufferSlice transformSlice = RenderSystem.getDynamicUniforms().writeTransform(
			modelView,
			new Vector4f(1.0F, 1.0F, 1.0F, 1.0F),
			new Vector3f(),
			new Matrix4f()
		);

		GpuBuffer auroraBuffer = getAuroraUniformBuffer();
		try (MemoryStack stack = MemoryStack.stackPush()) {
			Std140Builder builder = Std140Builder.onStack(stack, AURORA_UNIFORM_SIZE);
			builder.putInt(seed).putVec3((float) cameraPos.x, (float) cameraPos.y, (float) cameraPos.z);
			encoder.writeToBuffer(auroraBuffer.slice(), builder.get());
		}

		GpuBuffer vertexBuffer = pipeline.getVertexFormat().uploadImmediateVertexBuffer(mesh.vertexBuffer());
		GpuBuffer indexBuffer;
		VertexFormat.IndexType indexType;
		if (mesh.indexBuffer() == null) {
			RenderSystem.AutoStorageIndexBuffer indexBufferSource = RenderSystem.getSequentialBuffer(mesh.drawState().mode());
			indexBuffer = indexBufferSource.getBuffer(mesh.drawState().indexCount());
			indexType = indexBufferSource.type();
		} else {
			indexBuffer = pipeline.getVertexFormat().uploadImmediateIndexBuffer(mesh.indexBuffer());
			indexType = mesh.drawState().indexType();
		}

		GpuTextureView color = RenderSystem.outputColorTextureOverride;
		OutputTarget outputTarget = OutputTarget.MAIN_TARGET;
		var target = outputTarget.getRenderTarget();
		if (color == null) {
			color = target.getColorTextureView();
		}
		GpuTextureView depth = null;
		if (target.useDepth) {
			depth = RenderSystem.outputDepthTextureOverride != null ? RenderSystem.outputDepthTextureOverride : target.getDepthTextureView();
		}

		try (RenderPass pass = encoder.createRenderPass(() -> "twilightforest_aurora", color, OptionalInt.empty(), depth, OptionalDouble.empty())) {
			pass.setPipeline(pipeline);
			RenderSystem.bindDefaultUniforms(pass);
			pass.setUniform("DynamicTransforms", transformSlice);
			pass.setUniform("AuroraContext", auroraBuffer.slice());

			pass.setVertexBuffer(0, vertexBuffer);

			pass.setIndexBuffer(indexBuffer, indexType);
			pass.drawIndexed(0, 0, mesh.drawState().indexCount(), 1);
		} finally {
			mesh.close();
		}
	}

	private static GpuBuffer getAuroraUniformBuffer() {
		if (auroraUniformBuffer == null || auroraUniformBuffer.isClosed()) {
			auroraUniformBuffer = RenderSystem.getDevice().createBuffer(
				() -> "twilightforest_aurora_uniforms",
				GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST,
				AURORA_UNIFORM_SIZE
			);
		}
		return auroraUniformBuffer;
	}

	private static void addAuroraVertex(VertexConsumer consumer, float x, float y, float z, float u, float v, float alpha) {
		consumer.addVertex(x, y, z)
			.setUv(u, v)
			.setColor(1.0F, 1.0F, 1.0F, alpha)
			.setLight(LightTexture.FULL_BRIGHT)
			.setOverlay(OverlayTexture.NO_OVERLAY)
			.setNormal(0.0F, 1.0F, 0.0F);
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
				VertexConsumer consumer = context.consumers().getBuffer(RenderTypes.debugFilledBox());
				Vec3 xyz = Vec3.atLowerCornerOf(offsetPos).subtract(getCameraPosition());
				renderGiantOutlineBoxes(poseStack, consumer, xyz.x(), xyz.y(), xyz.z());
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

	private static void renderGiantOutlineBoxes(PoseStack poseStack, VertexConsumer consumer, double x, double y, double z) {
		final double minX = x;
		final double minY = y;
		final double minZ = z;
		final double maxX = x + 4.0D;
		final double maxY = y + 4.0D;
		final double maxZ = z + 4.0D;
		final double halfThickness = 0.015625D;
		final double extension = 0.001D;

		// Use thin filled boxes instead of the line pipeline so giant outlines stay visible
		// without depending on the 1.21.11 LineWidth vertex format or fragile driver support.
		addOutlineEdge(poseStack, consumer, minX - extension, minY - halfThickness, minZ - halfThickness, maxX + extension, minY + halfThickness, minZ + halfThickness);
		addOutlineEdge(poseStack, consumer, minX - extension, minY - halfThickness, maxZ - halfThickness, maxX + extension, minY + halfThickness, maxZ + halfThickness);
		addOutlineEdge(poseStack, consumer, minX - extension, maxY - halfThickness, minZ - halfThickness, maxX + extension, maxY + halfThickness, minZ + halfThickness);
		addOutlineEdge(poseStack, consumer, minX - extension, maxY - halfThickness, maxZ - halfThickness, maxX + extension, maxY + halfThickness, maxZ + halfThickness);

		addOutlineEdge(poseStack, consumer, minX - halfThickness, minY - extension, minZ - halfThickness, minX + halfThickness, maxY + extension, minZ + halfThickness);
		addOutlineEdge(poseStack, consumer, maxX - halfThickness, minY - extension, minZ - halfThickness, maxX + halfThickness, maxY + extension, minZ + halfThickness);
		addOutlineEdge(poseStack, consumer, minX - halfThickness, minY - extension, maxZ - halfThickness, minX + halfThickness, maxY + extension, maxZ + halfThickness);
		addOutlineEdge(poseStack, consumer, maxX - halfThickness, minY - extension, maxZ - halfThickness, maxX + halfThickness, maxY + extension, maxZ + halfThickness);

		addOutlineEdge(poseStack, consumer, minX - halfThickness, minY - halfThickness, minZ - extension, minX + halfThickness, minY + halfThickness, maxZ + extension);
		addOutlineEdge(poseStack, consumer, maxX - halfThickness, minY - halfThickness, minZ - extension, maxX + halfThickness, minY + halfThickness, maxZ + extension);
		addOutlineEdge(poseStack, consumer, minX - halfThickness, maxY - halfThickness, minZ - extension, minX + halfThickness, maxY + halfThickness, maxZ + extension);
		addOutlineEdge(poseStack, consumer, maxX - halfThickness, maxY - halfThickness, minZ - extension, maxX + halfThickness, maxY + halfThickness, maxZ + extension);
	}

	private static void addOutlineEdge(PoseStack poseStack, VertexConsumer consumer, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		PoseStack.Pose pose = poseStack.last();

		addQuad(consumer, pose, minX, minY, minZ, minX, maxY, minZ, maxX, maxY, minZ, maxX, minY, minZ);
		addQuad(consumer, pose, minX, minY, maxZ, maxX, minY, maxZ, maxX, maxY, maxZ, minX, maxY, maxZ);
		addQuad(consumer, pose, minX, minY, minZ, maxX, minY, minZ, maxX, minY, maxZ, minX, minY, maxZ);
		addQuad(consumer, pose, minX, maxY, minZ, minX, maxY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ);
		addQuad(consumer, pose, minX, minY, minZ, minX, minY, maxZ, minX, maxY, maxZ, minX, maxY, minZ);
		addQuad(consumer, pose, maxX, minY, minZ, maxX, maxY, minZ, maxX, maxY, maxZ, maxX, minY, maxZ);
	}

	private static void addQuad(VertexConsumer consumer, PoseStack.Pose pose, double x1, double y1, double z1, double x2, double y2, double z2, double x3, double y3, double z3, double x4, double y4, double z4) {
		addOutlineVertex(consumer, pose, x1, y1, z1);
		addOutlineVertex(consumer, pose, x2, y2, z2);
		addOutlineVertex(consumer, pose, x3, y3, z3);
		addOutlineVertex(consumer, pose, x4, y4, z4);
	}

	private static void addOutlineVertex(VertexConsumer consumer, PoseStack.Pose pose, double x, double y, double z) {
		consumer.addVertex(pose, (float) x, (float) y, (float) z).setColor(0.0F, 0.0F, 0.0F, 0.45F);
	}
}
