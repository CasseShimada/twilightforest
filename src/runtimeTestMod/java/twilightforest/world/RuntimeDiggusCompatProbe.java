package twilightforest.world;

import net.kyrptonaught.diggusmaximus.api.DiggusMaximusApi;
import net.kyrptonaught.diggusmaximus.api.ExcavationPattern;
import net.kyrptonaught.diggusmaximus.api.ExcavationRequest;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import twilightforest.init.TFBlocks;
import twilightforest.init.TFItems;

/** Real API-facade excavation transaction checks; loaded only when Diggus Maximus is present. */
final class RuntimeDiggusCompatProbe {
	private RuntimeDiggusCompatProbe() {
	}

	static Evidence run(ServerLevel level, ServerPlayer player) {
		BlockPos seed = new BlockPos(1, 120, 1);
		AABB evidenceBounds = new AABB(seed).inflate(4.0D);
		level.getEntitiesOfClass(ItemEntity.class, evidenceBounds).forEach(ItemEntity::discard);
		for (int y = -1; y <= 4; y++) {
			level.setBlockAndUpdate(seed.offset(0, y, 0), y < 0 ? Blocks.STONE.defaultBlockState() : Blocks.AIR.defaultBlockState());
		}
		for (int y = 0; y < 3; y++) {
			level.setBlockAndUpdate(seed.above(y), TFBlocks.TWILIGHT_OAK_LOG.defaultBlockState());
		}
		player.teleportTo(level, 1.5D, 120.0D, 5.5D, java.util.Set.of(), 180.0F, 0.0F, false);
		ItemStack axe = new ItemStack(Items.IRON_AXE);
		player.setItemInHand(InteractionHand.MAIN_HAND, axe);

		var result = DiggusMaximusApi.requestExcavation(new ExcavationRequest(player, seed, ExcavationPattern.VEIN));
		check(result.successful(), "TF log excavation failed: " + result);
		int broken = result.completion().orElseThrow().excavation().brokenBlocks();
		check(broken == 3, "TF log excavation broke " + broken + " blocks instead of 3");
		for (int y = 0; y < 3; y++) {
			check(level.getBlockState(seed.above(y)).isAir(), "TF log remained after excavation at " + seed.above(y));
		}
		check(axe.getDamageValue() == 3, "iron axe durability changed by " + axe.getDamageValue() + " instead of 3");
		BlockPos giantPickTarget = seed.offset(2, 0, 0);
		level.setBlockAndUpdate(giantPickTarget.below(), Blocks.STONE.defaultBlockState());
		level.setBlockAndUpdate(giantPickTarget, TFBlocks.TWILIGHT_OAK_LOG.defaultBlockState());
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(TFItems.GIANT_PICKAXE));
		var giantResult = DiggusMaximusApi.requestExcavation(
			new ExcavationRequest(player, giantPickTarget, ExcavationPattern.VEIN));
		check(!giantResult.successful(), "Giant Pick recursively started Diggus excavation");
		check(level.getBlockState(giantPickTarget).is(TFBlocks.TWILIGHT_OAK_LOG), "denied Giant Pick seed was removed");

		BlockPos unsafeTarget = seed.offset(4, 0, 0);
		level.setBlockAndUpdate(unsafeTarget.below(), Blocks.STONE.defaultBlockState());
		level.setBlockAndUpdate(unsafeTarget, TFBlocks.TWILIGHT_PORTAL.defaultBlockState());
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_AXE));
		var unsafeResult = DiggusMaximusApi.requestExcavation(
			new ExcavationRequest(player, unsafeTarget, ExcavationPattern.VEIN));
		check(!unsafeResult.successful(), "portal started Diggus excavation");
		check(level.getBlockState(unsafeTarget).is(TFBlocks.TWILIGHT_PORTAL), "denied portal seed was removed");
		player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
		return new Evidence(evidenceBounds);
	}

	record Evidence(AABB dropBounds) {
		String verifyAfterEntityTick(ServerLevel level, ServerPlayer player) {
			int worldDrops = level.getEntitiesOfClass(ItemEntity.class, this.dropBounds).stream()
				.filter(item -> item.getItem().is(TFBlocks.TWILIGHT_OAK_LOG.asItem()))
				.mapToInt(item -> item.getItem().getCount())
				.sum();
			int autoPickedUp = player.getInventory().countItem(TFBlocks.TWILIGHT_OAK_LOG.asItem());
			check(worldDrops + autoPickedUp == 3, "TF log excavation produced " + worldDrops
				+ " world drops and " + autoPickedUp + " auto-picked-up logs instead of 3 total");
			return "logs=3 drops=3(world=" + worldDrops + ",pickup=" + autoPickedUp
				+ ") durability=3 giant_pick=denied portal=denied";
		}
	}

	private static void check(boolean condition, String message) {
		if (!condition) {
			throw new IllegalStateException(message);
		}
	}
}
