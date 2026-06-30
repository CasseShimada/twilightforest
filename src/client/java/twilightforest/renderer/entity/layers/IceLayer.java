package twilightforest.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.fabricmc.fabric.api.client.rendering.v1.FabricRenderState;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import twilightforest.TwilightForestMod;
import twilightforest.client.model.entity.DeathTomeModel;
import twilightforest.client.renderer.RenderStateUtil;
import twilightforest.potions.FrostedEffect;

public class IceLayer<S extends LivingEntityRenderState, M extends EntityModel<S>> extends RenderLayer<S, M> {
	private final RandomSource random = RandomSource.create();

	public static final RenderStateDataKey<Double> FROST_COUNT_KEY = RenderStateDataKey.create(() -> TwilightForestMod.prefix("frost_count").toString());
	public static final RenderStateDataKey<Integer> FROST_ID_KEY = RenderStateDataKey.create(() -> TwilightForestMod.prefix("frost_id").toString());

	public IceLayer(RenderLayerParent<S, M> renderer) {
		super(renderer);
	}

	@Override
	public void submit(PoseStack poseStack, SubmitNodeCollector nodeCollector, int packedLight, S state, float netHeadYaw, float headPitch) {
		if (!(state instanceof FabricRenderState fabricState)) return;
		Double countValue = fabricState.getDataOrDefault(FROST_COUNT_KEY, 0.0D);
		if (countValue == null) return;
		double count = countValue;
		if (count <= 0.0D) return;
		Integer id = fabricState.getData(FROST_ID_KEY);
		if (id == null) return;

		this.random.setSeed(id * id * 3121L + id * 45238971L);

		int numCubes = (int) (state.boundingBoxHeight / 0.4F) + (int) (count / FrostedEffect.FROST_MULTIPLIER) + 1;
		float specialOffset = this.getParentModel() instanceof DeathTomeModel ? 1.0F : 0.0F;

		for (int i = 0; i < numCubes; i++) {
			poseStack.pushPose();
			float dx = ((this.random.nextFloat() * (state.boundingBoxWidth * 2.0F)) - state.boundingBoxWidth) * 0.1F;
			float dy = Math.max(1.5F - (this.random.nextFloat()) * (state.boundingBoxHeight - specialOffset), -0.1F) - specialOffset;
			float dz = ((this.random.nextFloat() * (state.boundingBoxWidth * 2.0F)) - state.boundingBoxWidth) * 0.1F;
			poseStack.translate(dx, dy, dz);
			poseStack.scale(0.5F, 0.5F, 0.5F);
			poseStack.mulPose(Axis.XP.rotationDegrees(this.random.nextFloat() * 360F));
			poseStack.mulPose(Axis.YP.rotationDegrees(this.random.nextFloat() * 360F));
			poseStack.mulPose(Axis.ZP.rotationDegrees(this.random.nextFloat() * 360F));
			poseStack.translate(-0.5F, -0.5F, -0.5F);

			MovingBlockRenderState iceState = new MovingBlockRenderState();
			BlockPos pos = BlockPos.containing(state.x, state.y, state.z);
			RenderStateUtil.populateMovingBlockRenderState(iceState, Blocks.ICE.defaultBlockState(), Minecraft.getInstance().level, pos, pos);
			nodeCollector.submitMovingBlock(poseStack, iceState, 0);
			poseStack.popPose();
		}
	}
}
