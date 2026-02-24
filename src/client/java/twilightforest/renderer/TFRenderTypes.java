package twilightforest.client.renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.rendertype.TextureTransform;
import org.joml.Matrix4f;
import twilightforest.TwilightForestMod;
import twilightforest.client.renderer.entity.LichRenderer;

public final class TFRenderTypes {
	private TFRenderTypes() {
	}

	private static final TextureTransform PROTECTION_BOX_TEXTURING = new TextureTransform("protection_offset_texturing", () -> {
		float tick = (float) (Minecraft.getInstance().getCameraEntity() != null ? Minecraft.getInstance().getCameraEntity().tickCount : 0)
			+ Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
		return new Matrix4f()
			.translation((-tick * 0.06F) % 1.0F, (-tick * 0.035F) % 1.0F, 0.0F)
			.scale(0.5F);
	});

	public static final RenderType PROTECTION_BOX = RenderType.create(
		"protection_box",
		RenderSetup.builder(RenderPipelines.ENERGY_SWIRL)
			.withTexture("Sampler0", TwilightForestMod.getModelTexture("protectionbox.png"))
			.setTextureTransform(PROTECTION_BOX_TEXTURING)
			.useLightmap()
			.useOverlay()
			.sortOnUpload()
			.createRenderSetup()
	);

	public static final RenderType SHADOW_CLONE = RenderTypes.itemEntityTranslucentCull(LichRenderer.TEXTURE);
}
