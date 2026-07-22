package twilightforest.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import tschipp.carryon.api.CarryOnApi;
import twilightforest.block.entity.DryingRackBlockEntity;
import twilightforest.init.TFBlocks;

/** Real API-facade pickup/rollback/place checks; loaded only when Carry On is present. */
final class RuntimeCarryOnCompatProbe {
	private RuntimeCarryOnCompatProbe() {
	}

	static String run(ServerLevel level, ServerPlayer player) {
		BlockPos source = new BlockPos(1, 120, 13);
		BlockPos destination = new BlockPos(5, 120, 13);
		BlockPos unsafe = new BlockPos(10, 120, 13);
		for (BlockPos pos : java.util.List.of(source, destination, unsafe)) {
			level.setBlockAndUpdate(pos.below(), Blocks.STONE.defaultBlockState());
			level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
		}
		level.setBlockAndUpdate(source, TFBlocks.OAK_DRYING_RACK.defaultBlockState());
		DryingRackBlockEntity sourceRack = requireRack(level, source, "source");
		sourceRack.setTheItem(new ItemStack(Items.BEEF));
		player.getInventory().clearContent();
		player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
		player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
		player.teleportTo(level, 2.5D, 120.0D, 15.0D, java.util.Set.of(), 180.0F, 0.0F, false);

		var pickup = CarryOnApi.requestPickupBlock(player, source);
		check(pickup.successful(), "safe drying rack pickup failed: " + pickup);
		check(level.getBlockState(source).isAir(), "successful drying rack pickup left the source block");
		check(CarryOnApi.isCarrying(player), "successful drying rack pickup did not commit carried data");

		player.tickCount++;
		var failedPlace = CarryOnApi.requestPlace(player, new BlockPos(1_000_000, 120, 1_000_000), Direction.UP);
		check(!failedPlace.successful(), "unloaded Carry On placement unexpectedly succeeded");
		check(CarryOnApi.isCarrying(player), "failed Carry On placement discarded carried data");

		player.tickCount++;
		player.teleportTo(level, 4.5D, 120.0D, 15.0D, java.util.Set.of(), 180.0F, 0.0F, false);
		var place = CarryOnApi.requestPlace(player, destination.below(), Direction.UP);
		check(place.successful(), "drying rack placement failed: " + place);
		check(level.getBlockState(destination).is(TFBlocks.OAK_DRYING_RACK), "placed drying rack has the wrong block state");
		DryingRackBlockEntity destinationRack = requireRack(level, destination, "destination");
		check(destinationRack.getTheItem().is(Items.BEEF), "placed drying rack lost its stored item");
		check(!CarryOnApi.isCarrying(player), "successful placement retained duplicate carried data");

		level.setBlockAndUpdate(unsafe, TFBlocks.TWILIGHT_PORTAL.defaultBlockState());
		player.teleportTo(level, 9.5D, 120.0D, 15.0D, java.util.Set.of(), 180.0F, 0.0F, false);
		var denied = CarryOnApi.requestPickupBlock(player, unsafe);
		check(!denied.successful(), "relocation_not_supported portal pickup unexpectedly succeeded");
		check(level.getBlockState(unsafe).is(TFBlocks.TWILIGHT_PORTAL), "denied portal pickup removed the block");
		check(!CarryOnApi.isCarrying(player), "denied portal pickup created carried data");
		return "drying_rack=round_trip failed_place=retained portal=denied stored_item=preserved";
	}

	private static DryingRackBlockEntity requireRack(ServerLevel level, BlockPos pos, String description) {
		if (level.getBlockEntity(pos) instanceof DryingRackBlockEntity rack) {
			return rack;
		}
		throw new IllegalStateException("Missing " + description + " drying rack at " + pos);
	}

	private static void check(boolean condition, String message) {
		if (!condition) {
			throw new IllegalStateException(message);
		}
	}
}
