package twilightforest.client.renderer;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.BindGroupLayout;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.RenderPipelines;
import twilightforest.TwilightForestMod;

public final class TFRenderPipelines {
	public static final RenderPipeline AURORA = RenderPipelines.register(
		RenderPipeline.builder(RenderPipelines.MATRICES_FOG_SNIPPET)
			.withLocation(TwilightForestMod.prefix("pipeline/aurora"))
			.withVertexShader(TwilightForestMod.prefix("core/aurora/aurora"))
			.withFragmentShader(TwilightForestMod.prefix("core/aurora/aurora"))
			.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
			.withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false))
			.withCull(false)
			.withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
			.withPrimitiveTopology(PrimitiveTopology.QUADS)
			.withBindGroupLayout(BindGroupLayout.builder().withUniform("AuroraContext", UniformType.UNIFORM_BUFFER).build())
			.build()
	);

	public static void init() {
	}

	private TFRenderPipelines() {
	}
}
