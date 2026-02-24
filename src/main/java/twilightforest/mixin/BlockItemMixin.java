package twilightforest.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twilightforest.events.ProgressionEvents;

@Mixin(BlockItem.class)
public abstract class BlockItemMixin {
	@Shadow
	protected abstract BlockState getPlacementState(BlockPlaceContext context);

	@Inject(method = "place", at = @At("HEAD"), cancellable = true)
	private void twilightforest$preventProtectedPlacement(BlockPlaceContext context, CallbackInfoReturnable<InteractionResult> cir) {
		Level level = context.getLevel();
		if (!(level instanceof ServerLevel serverLevel)) {
			return;
		}
		Player player = context.getPlayer();
		if (player == null) {
			return;
		}
		BlockState state = this.getPlacementState(context);
		if (state == null) {
			return;
		}
		BlockPos pos = context.getClickedPos();
		if (ProgressionEvents.shouldCancelBlockPlacement(serverLevel, player, pos) || isAdditionalPlacementBlocked(serverLevel, player, state, pos)) {
			if (player instanceof ServerPlayer serverPlayer) {
				serverPlayer.inventoryMenu.sendAllDataToRemote();
			}
			cir.setReturnValue(InteractionResult.FAIL);
		}
	}

	private static boolean isAdditionalPlacementBlocked(ServerLevel level, Player player, BlockState state, BlockPos pos) {
		if (state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)) {
			BlockPos other = pos.above();
			if (ProgressionEvents.shouldCancelBlockPlacement(level, player, other)) {
				return true;
			}
		}

		Block block = state.getBlock();
		if (block instanceof BedBlock && state.hasProperty(BedBlock.FACING)) {
			Direction direction = state.getValue(BedBlock.FACING);
			BlockPos other = pos.relative(direction);
			if (ProgressionEvents.shouldCancelBlockPlacement(level, player, other)) {
				return true;
			}
		}

		return false;
	}
}
