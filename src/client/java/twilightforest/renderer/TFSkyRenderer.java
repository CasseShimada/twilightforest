package twilightforest.client.renderer;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.OptionalDouble;
import java.util.OptionalInt;
import twilightforest.TwilightForestMod;
import twilightforest.mixin.client.accessor.ClientLevelDataAccessor;
import twilightforest.mixin.client.accessor.LevelRendererAccessor;

public final class TFSkyRenderer implements AutoCloseable {
	private static final long STAR_SEED = 10842L;
	private static final int STAR_COUNT = 3000;
	private static long lastSkyTraceTick = Long.MIN_VALUE;

	private static GpuBuffer starVertexBuffer;
	private static int starIndexCount;

	private TFSkyRenderer() {}

	// [VanillaCopy] LevelRenderer.addSkyPass's overworld branch, without sun/moon/sunrise/sunset.
	// TF: Uses our own stars at full brightness, and lowers void horizon threshold height from getHorizonHeight (63) to 0.
	public static boolean renderSky(ClientLevel level, float partialTicks, Camera camera, Runnable setupFog) {
		LevelRenderer levelRenderer = Minecraft.getInstance().levelRenderer;
		var skyRenderer = ((LevelRendererAccessor) levelRenderer).twilightforest$getSkyRenderer();
		setupFog.run();

		PoseStack posestack = new PoseStack();
		net.minecraft.client.renderer.state.level.SkyRenderState skyState = new net.minecraft.client.renderer.state.level.SkyRenderState();
		skyRenderer.extractRenderState(level, partialTicks, camera, skyState);
		int k = skyState.skyColor;
		boolean darkDisc = shouldDarkenSky(level, camera, partialTicks);
		logSkyState(level, partialTicks, camera, skyState, darkDisc);
		skyRenderer.renderSkyDisc(k);

		// TF: replace sun, moon, and vanilla star rendering with our own star renderer.
		// Rotation is from SkyRenderer.renderSunMoonAndStars.
		posestack.pushPose();
		posestack.mulPose(Axis.YP.rotationDegrees(-90.0F));
		renderStars(setupFog, posestack);
		posestack.popPose();

		// TF: use custom height checks for the void sky as vanilla hardcodes to 63.
		if (darkDisc) {
			skyRenderer.renderDarkDisc();
		}

		return true;
	}

	public static void renderTwilightStars(PoseStack stack) {
		renderStars(() -> {}, stack);
	}

	private static boolean shouldDarkenSky(ClientLevel level, Camera camera, float partialTicks) {
		return camera.entity().getEyePosition(partialTicks).y - level.getMinY() < 0.0;
	}

	private static void logSkyState(ClientLevel level, float partialTicks, Camera camera, net.minecraft.client.renderer.state.level.SkyRenderState skyState, boolean darkDisc) {
		long gameTime = level.getGameTime();
		long dayTime = ((ClientLevelDataAccessor) level.getLevelData()).twilightforest$getDayTime();
		if (gameTime % 40L != 0L || gameTime == lastSkyTraceTick) {
			return;
		}
		lastSkyTraceTick = gameTime;

		TwilightForestMod.LOGGER.info(
			"TF sky trace: render sky dim={} gameTime={} dayTime={} cycleTime={} partial={} skyColor=0x{} sunriseColor=0x{} cameraPos={} eyeY={} minY={} darkDisc={}",
			level.dimension().identifier(),
			gameTime,
			dayTime,
			dayTime % 24000L,
			partialTicks,
			String.format("%08X", skyState.skyColor),
			String.format("%08X", skyState.sunriseAndSunsetColor),
			camera.position(),
			camera.position().y,
			level.getMinY(),
			darkDisc
		);
	}

	private static void ensureStarsBuilt() {
		if (starVertexBuffer != null && !starVertexBuffer.isClosed()) return;
		if (RenderSystem.tryGetDevice() == null) return;

		RandomSource random = RandomSource.create(STAR_SEED);
		int bytes = DefaultVertexFormat.POSITION.getVertexSize() * STAR_COUNT * 4;
		ByteBufferBuilder byteBuffer = ByteBufferBuilder.exactlySized(bytes);
		BufferBuilder builder = new BufferBuilder(byteBuffer, VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);

		buildStars(builder, random);

		try (MeshData mesh = builder.buildOrThrow()) {
			starIndexCount = mesh.drawState().indexCount();
			starVertexBuffer = RenderSystem.getDevice().createBuffer(
				() -> "twilightforest_stars",
				GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST,
				mesh.vertexBuffer()
			);
		}
	}

	// [VanillaCopy] SkyRenderer.buildStars, but with double the number of stars.
	private static void buildStars(VertexConsumer consumer, RandomSource random) {
		for (int i = 0; i < STAR_COUNT; ++i) {
			float f1 = random.nextFloat() * 2.0F - 1.0F;
			float f2 = random.nextFloat() * 2.0F - 1.0F;
			float f3 = random.nextFloat() * 2.0F - 1.0F;
			float f4 = 0.15F + random.nextFloat() * 0.1F;
			float f5 = Mth.lengthSquared(f1, f2, f3);
			if (!(f5 <= 0.010000001F) && !(f5 >= 1.0F)) {
				Vector3f vector3f = new Vector3f(f1, f2, f3).normalize(100.0F);
				float f6 = (float) (random.nextDouble() * (float) Math.PI * 2.0);
				Matrix3f matrix3f = new Matrix3f()
					.rotateTowards(new Vector3f(vector3f).negate(), new Vector3f(0.0F, 1.0F, 0.0F))
					.rotateZ(-f6);

				consumer.addVertex(vector3f.add(new Vector3f(f4, -f4, 0.0F).mul(matrix3f).add(vector3f)));
				consumer.addVertex(vector3f.add(new Vector3f(f4, f4, 0.0F).mul(matrix3f).add(vector3f)));
				consumer.addVertex(vector3f.add(new Vector3f(-f4, f4, 0.0F).mul(matrix3f).add(vector3f)));
				consumer.addVertex(vector3f.add(new Vector3f(-f4, -f4, 0.0F).mul(matrix3f).add(vector3f)));
			}
		}
	}

	// [VanillaCopy] SkyRenderer.renderStars, adapted to use our own cached vertex buffer and full brightness.
	private static void renderStars(Runnable setupFog, PoseStack stack) {
		ensureStarsBuilt();
		if (starVertexBuffer == null || starVertexBuffer.isClosed() || starIndexCount <= 0) return;

		Matrix4fStack modelView = RenderSystem.getModelViewStack();
		modelView.pushMatrix();
		modelView.mul(stack.last().pose());

		RenderTarget target = Minecraft.getInstance().getMainRenderTarget();
		var color = target.getColorTextureView();
		var depth = target.getDepthTextureView();

		var quadIndices = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
		GpuBuffer indexBuffer = quadIndices.getBuffer(starIndexCount);

		GpuBufferSlice transforms = RenderSystem.getDynamicUniforms().writeTransform(
			modelView,
			new Vector4f(1.0F, 1.0F, 1.0F, 1.0F),
			new Vector3f(),
			new Matrix4f()
		);

		CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
		try (RenderPass pass = encoder.createRenderPass(() -> "twilightforest_stars", color, OptionalInt.empty(), depth, OptionalDouble.empty())) {
			pass.setPipeline(RenderPipelines.STARS);
			RenderSystem.bindDefaultUniforms(pass);
			pass.setUniform("DynamicTransforms", transforms);
			pass.setVertexBuffer(0, starVertexBuffer);
			pass.setIndexBuffer(indexBuffer, quadIndices.type());
			pass.drawIndexed(0, 0, starIndexCount, 1);
		}

		setupFog.run();
		modelView.popMatrix();
	}

	@Override
	public void close() {
		if (starVertexBuffer != null) {
			starVertexBuffer.close();
			starVertexBuffer = null;
			starIndexCount = 0;
		}
	}
}
