package twilightforest.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.core.BlockPos;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twilightforest.client.model.block.BlockModelContext;

import java.util.List;

@Mixin(ModelBlockRenderer.class)
public class ModelBlockRendererMixin {
	@Inject(method = "tesselateBlock", at = @At("HEAD"))
	private void twilightforest$setBlockContext(BlockAndTintGetter level, List<BlockStateModelPart> parts, BlockState state, BlockPos pos, PoseStack poseStack, com.mojang.blaze3d.vertex.VertexConsumer consumer, boolean checkSides, int overlay, CallbackInfo ci) {
		BlockModelContext.set(level, pos, state);
	}

	@Inject(method = "tesselateBlock", at = @At("TAIL"))
	private void twilightforest$clearBlockContext(BlockAndTintGetter level, List<BlockStateModelPart> parts, BlockState state, BlockPos pos, PoseStack poseStack, com.mojang.blaze3d.vertex.VertexConsumer consumer, boolean checkSides, int overlay, CallbackInfo ci) {
		BlockModelContext.clear();
	}
}
