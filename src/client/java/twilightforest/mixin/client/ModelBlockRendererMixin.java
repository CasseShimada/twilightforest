package twilightforest.mixin.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.renderer.block.BlockQuadOutput;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import twilightforest.client.model.block.BlockModelContext;

@Mixin(ModelBlockRenderer.class)
public class ModelBlockRendererMixin {
	@WrapMethod(method = "tesselateBlock")
	private void twilightforest$withBlockContext(BlockQuadOutput output, float x, float y, float z, BlockAndTintGetter level, BlockPos pos, BlockState state, BlockStateModel model, long seed, Operation<Void> original) {
		BlockModelContext.set(level, pos, state);
		try {
			original.call(output, x, y, z, level, pos, state, model, seed);
		} finally {
			BlockModelContext.clear();
		}
	}
}
