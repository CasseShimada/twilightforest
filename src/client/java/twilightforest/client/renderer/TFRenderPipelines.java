package twilightforest.client.renderer;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderPipelines;
import twilightforest.TwilightForestMod;

public final class TFRenderPipelines {
	public static final RenderPipeline AURORA = RenderPipelines.register(
		RenderPipeline.builder(RenderPipelines.MATRICES_FOG_SNIPPET, RenderPipelines.GLOBALS_SNIPPET)
			.withLocation(TwilightForestMod.prefix("pipeline/aurora"))
			.withVertexShader(TwilightForestMod.prefix("core/aurora/aurora"))
			.withFragmentShader(TwilightForestMod.prefix("core/aurora/aurora"))
			.withBlend(BlendFunction.TRANSLUCENT)
			.withDepthWrite(false)
			.withCull(false)
			.withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS)
			.withUniform("AuroraContext", UniformType.UNIFORM_BUFFER)
			.build()
	);

	public static void init() {
	}

	private TFRenderPipelines() {
	}
}
