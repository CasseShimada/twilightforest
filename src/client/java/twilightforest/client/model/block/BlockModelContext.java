package twilightforest.client.model.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class BlockModelContext {
	private static final ThreadLocal<Context> CONTEXT = new ThreadLocal<>();

	private BlockModelContext() {
	}

	public static void set(BlockAndTintGetter level, BlockPos pos, BlockState state) {
		CONTEXT.set(new Context(level, pos, state));
	}

	public static void clear() {
		CONTEXT.remove();
	}

	public static @Nullable Context get() {
		return CONTEXT.get();
	}

	public record Context(BlockAndTintGetter level, BlockPos pos, BlockState state) {
	}
}
