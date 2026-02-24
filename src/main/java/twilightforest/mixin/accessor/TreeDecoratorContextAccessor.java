package twilightforest.mixin.accessor;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.BiConsumer;

@Mixin(TreeDecorator.Context.class)
public interface TreeDecoratorContextAccessor {
	@Accessor("decorationSetter")
	BiConsumer<BlockPos, BlockState> twilightforest$getDecorationSetter();
}
