package twilightforest.mixin.client;

import net.minecraft.client.renderer.block.BlockQuadOutput;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twilightforest.client.model.block.BlockModelContext;

@Mixin(ModelBlockRenderer.class)
public class ModelBlockRendererMixin {
	@Inject(method = "tesselateBlock", at = @At("HEAD"))
	private void twilightforest$setBlockContext(BlockQuadOutput output, float x, float y, float z, BlockAndTintGetter level, BlockPos pos, BlockState state, BlockStateModel model, long seed, CallbackInfo ci) {
		BlockModelContext.set(level, pos, state);
	}

	@Inject(method = "tesselateBlock", at = @At("TAIL"))
	private void twilightforest$clearBlockContext(BlockQuadOutput output, float x, float y, float z, BlockAndTintGetter level, BlockPos pos, BlockState state, BlockStateModel model, long seed, CallbackInfo ci) {
		BlockModelContext.clear();
	}
}
