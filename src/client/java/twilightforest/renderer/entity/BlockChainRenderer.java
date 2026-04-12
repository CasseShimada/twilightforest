package twilightforest.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import twilightforest.TwilightForestMod;
import twilightforest.client.model.TFModelLayers;
import twilightforest.client.model.entity.ChainModel;
import twilightforest.client.model.entity.SpikeBlockModel;
import twilightforest.client.state.ChainBlockRenderState;
import twilightforest.entity.projectile.ChainBlock;

public class BlockChainRenderer extends EntityRenderer<ChainBlock, ChainBlockRenderState> {

	private static final Identifier TEXTURE = TwilightForestMod.getModelTexture("block_and_chain.png");
	private final Model<Void> model;
	private final Model<Void> chainModel;

	public BlockChainRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.model = new SpikeBlockModel(context.bakeLayer(TFModelLayers.CHAIN_BLOCK));
		this.chainModel = new ChainModel(context.bakeLayer(TFModelLayers.CHAIN));
	}

	@Override
	public void submit(ChainBlockRenderState state, PoseStack stack, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState) {
		stack.pushPose();
		stack.mulPose(Axis.YP.rotationDegrees(state.yRot - 90.0F));
		stack.mulPose(Axis.ZP.rotationDegrees(state.xRot));

		stack.scale(-1.0F, -1.0F, 1.0F);
		nodeCollector.submitModel(this.model, null, stack, this.model.renderType(TEXTURE), state.lightCoords, net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY, -1, null);
		stack.popPose();
		if (state.chainStartPos != null) {
			stack.pushPose();
			stack.translate(0.0D, state.blockHeight * 0.5D, 0.0D);
			Vec3 xyz = state.chainStartPos;
			double linksPerMeter = 1.5F; // Defines how many chain links per meter. 2.0F there will be two per meter, 0.5F will be one per two meters, etc.
			double links = xyz.length() / linksPerMeter;
			Vec3 offset = xyz.normalize().scale(-linksPerMeter);
			for (int i = 1; i < links; i++) {
				renderChain(xyz.add(offset.scale(links - i)), stack, nodeCollector, Math.max(state.lightCoords, state.ownerLight), this.chainModel);
			}
			stack.popPose();
		}
		super.submit(state, stack, nodeCollector, cameraRenderState);
	}

	@Override
	protected AABB getBoundingBoxForCulling(ChainBlock chainBlock) {
		if (chainBlock.getOwner() != null) {
			AABB dis = super.getBoundingBoxForCulling(chainBlock);
			AABB owner = chainBlock.getOwner().getBoundingBox();
			return dis.minmax(owner);
		}
		return super.getBoundingBoxForCulling(chainBlock);
	}

	public static void renderChain(Vec3 offset, PoseStack stack, SubmitNodeCollector nodeCollector, int light, Model<Void> chainModel) {
		stack.pushPose();
		stack.translate(offset.x(), offset.y(), offset.z());

		stack.scale(-1.0F, -1.0F, 1.0F);
		nodeCollector.submitModel(chainModel, null, stack, chainModel.renderType(TEXTURE), light, net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY, -1, null);
		stack.popPose();
	}

	@Override
	public ChainBlockRenderState createRenderState() {
		return new ChainBlockRenderState();
	}

	@Override
	public void extractRenderState(ChainBlock entity, ChainBlockRenderState state, float partialTick) {
		super.extractRenderState(entity, state, partialTick);
		state.yRot = entity.getYRot(partialTick);
		state.xRot = entity.getXRot(partialTick);
		state.isFoil = entity.isFoil();
		state.chainStartPos = entity.getOwner() != null ? entity.getOwner().getEyePosition(partialTick).subtract(entity.getEyePosition(partialTick)).add(0.0D, entity.getBbHeight() * -0.5D, 0.0D) : null;
		state.ownerLight = entity.getOwner() != null ? Minecraft.getInstance().getEntityRenderDispatcher().getPackedLightCoords(entity.getOwner(), partialTick) : 0;
		state.blockHeight = entity.getBbHeight();
	}
}
