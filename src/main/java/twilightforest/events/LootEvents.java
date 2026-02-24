package twilightforest.events;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.jetbrains.annotations.Nullable;
import twilightforest.block.GiantBlock;
import twilightforest.components.entity.GiantPickaxeMiningAttachment;
import twilightforest.init.TFDataAttachments;
import twilightforest.init.TFItems;
import twilightforest.item.GiantPickItem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class LootEvents {
	public static final Map<Block, Item> GIANT_PICK_CONVERSIONS = new HashMap<>();

	private LootEvents() {
	}

	public static void register() {
		LootTableEvents.MODIFY_DROPS.register(LootEvents::modifyDrops);
	}

	public static boolean tryHandleGiantPickBreak(ServerLevel level, ServerPlayer player, BlockPos pos, BlockState state) {
		if (!(player.getMainHandItem().getItem() instanceof GiantPickItem)) {
			return false;
		}
		if (!player.hasCorrectToolForDrops(state)) {
			return false;
		}

		GiantPickaxeMiningAttachment attachment = TFDataAttachments.get(player, TFDataAttachments.GIANT_PICKAXE_MINING);
		if (!shouldBreakGiantBlock(player, attachment)) {
			return false;
		}

		attachment.setBreaking(true);

		boolean allTheSame = GIANT_PICK_CONVERSIONS.containsKey(state.getBlock());
		for (BlockPos offsetPos : GiantBlock.getVolume(pos)) {
			if (allTheSame && !player.level().getBlockState(offsetPos).is(state.getBlock())) {
				allTheSame = false;
			}
		}
		attachment.setGiantBlockConversion(allTheSame ? 64 : 0);

		level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(state));
		player.gameMode.destroyBlock(pos);

		for (BlockPos offsetPos : GiantBlock.getVolume(pos)) {
			if (!offsetPos.equals(pos) && player.level().getBlockState(offsetPos).is(state.getBlock())) {
				BlockPos newPos = new BlockPos(offsetPos);
				level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, newPos, Block.getId(player.level().getBlockState(newPos)));
				player.gameMode.destroyBlock(newPos);
			}
		}

		attachment.setBreaking(false);
		return true;
	}

	private static boolean shouldBreakGiantBlock(Player player, GiantPickaxeMiningAttachment attachment) {
		return attachment.getMining() == player.level().getGameTime() && !attachment.getBreaking();
	}

	private static void modifyDrops(@Nullable Holder<LootTable> entry, LootContext context, List<ItemStack> drops) {
		applyFierySmelting(context, drops);
		applyGiantPickGrouping(context, drops);
	}

	private static void applyFierySmelting(LootContext context, List<ItemStack> drops) {
		ItemStack tool = context.getOptionalParameter(LootContextParams.TOOL);
		if (tool == null || !tool.is(TFItems.FIERY_PICKAXE.get())) {
			return;
		}

		List<Pair<ItemStack, Float>> list = drops.stream().map(stack -> {
			var input = new SingleRecipeInput(stack);
			return context.getLevel().recipeAccess().getRecipeFor(RecipeType.SMELTING, input, context.getLevel())
				.map(holder -> {
					ItemStack result = holder.value().assemble(input, context.getLevel().registryAccess()).copy();
					result.setCount(stack.getCount() * result.getCount());
					return Pair.of(result, holder.value().experience());
				})
				.filter(pair -> !pair.getFirst().isEmpty())
				.orElse(Pair.of(stack, 0.0F));
		}).toList();

		float xp = (float) list.stream().mapToDouble(Pair::getSecond).sum();
		if (xp > 0.0F && context.getOptionalParameter(LootContextParams.THIS_ENTITY) != null) {
			ExperienceOrb.award(context.getLevel(), context.getParameter(LootContextParams.THIS_ENTITY).position(), Math.round(xp));
		}

		drops.clear();
		drops.addAll(list.stream().map(Pair::getFirst).toList());
	}

	private static void applyGiantPickGrouping(LootContext context, List<ItemStack> drops) {
		if (!(context.getOptionalParameter(LootContextParams.THIS_ENTITY) instanceof Player player)) {
			return;
		}
		BlockState state = context.getOptionalParameter(LootContextParams.BLOCK_STATE);
		if (state == null || !GIANT_PICK_CONVERSIONS.containsKey(state.getBlock())) {
			return;
		}

		var attachment = TFDataAttachments.get(player, TFDataAttachments.GIANT_PICKAXE_MINING);
		int blockConversion = attachment.getGiantBlockConversion();
		if (blockConversion <= 0) {
			return;
		}

		attachment.setGiantBlockConversion(blockConversion - 1);
		drops.clear();
		if (blockConversion == 64) {
			drops.add(new ItemStack(GIANT_PICK_CONVERSIONS.get(state.getBlock())));
		}
	}
}
