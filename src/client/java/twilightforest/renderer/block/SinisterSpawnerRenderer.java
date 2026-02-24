package twilightforest.client.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.SpawnerRenderer;
import net.minecraft.client.renderer.blockentity.state.SpawnerRenderState;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import twilightforest.block.entity.spawner.SinisterSpawnerBlockEntity;

// [VanillaCopy] SpawnerRenderer, bound to SinisterSpawnerBlockEntity
public class SinisterSpawnerRenderer implements BlockEntityRenderer<SinisterSpawnerBlockEntity, SpawnerRenderState> {
	private final EntityRenderDispatcher entityRenderer;

	public SinisterSpawnerRenderer(BlockEntityRendererProvider.Context context) {
		this.entityRenderer = context.entityRenderer();
	}

	@Override
	public SpawnerRenderState createRenderState() {
		return new SpawnerRenderState();
	}

	@Override
	public void extractRenderState(SinisterSpawnerBlockEntity blockEntity, SpawnerRenderState renderState, float partialTick, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
		BlockEntityRenderer.super.extractRenderState(blockEntity, renderState, partialTick, cameraPosition, breakProgress);
		renderState.displayEntity = null;
		renderState.spin = 0.0F;
		renderState.scale = 0.0F;

		Level level = blockEntity.getLevel();
		if (level == null) return;

		BaseSpawner spawner = blockEntity.getSpawner();
		Entity entity = spawner.getOrCreateDisplayEntity(level, blockEntity.getBlockPos());
		if (entity == null) return;

		renderState.displayEntity = this.entityRenderer.extractEntity(entity, partialTick);
		renderState.displayEntity.lightCoords = renderState.lightCoords;
		renderState.spin = (float) Mth.lerp((double) partialTick, spawner.getOSpin(), spawner.getSpin()) * 10.0F;
		renderState.scale = 0.53125F;

		float f = Math.max(entity.getBbWidth(), entity.getBbHeight());
		if (f > 1.0) {
			renderState.scale /= f;
		}
	}

	@Override
	public void submit(SpawnerRenderState renderState, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState) {
		if (renderState.displayEntity != null) {
			SpawnerRenderer.submitEntityInSpawner(poseStack, nodeCollector, renderState.displayEntity, this.entityRenderer, renderState.spin, renderState.scale, cameraRenderState);
		}
	}

}
