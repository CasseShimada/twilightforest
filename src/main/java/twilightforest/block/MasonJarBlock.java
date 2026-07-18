package twilightforest.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import twilightforest.block.entity.MasonJarBlockEntity;
import twilightforest.init.TFSounds;
import twilightforest.util.PlayerMessaging;

import java.util.List;

public class MasonJarBlock extends JarBlock implements SimpleWaterloggedBlock {
	public static final MapCodec<MasonJarBlock> CODEC = simpleCodec(MasonJarBlock::new);
	public static final IntegerProperty LIGHT_LEVEL = IntegerProperty.create("light_level", 0, 15);

	public MasonJarBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.defaultBlockState().setValue(LIGHT_LEVEL, 0));
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec() {
		return CODEC;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new MasonJarBlockEntity(pos, state);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(LIGHT_LEVEL);
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
		InteractionResult jarResult = super.useItemOn(stack, state, level, pos, player, hand, hitResult);
		if (jarResult != InteractionResult.TRY_WITH_EMPTY_HAND) return jarResult;
		if (!(level.getBlockEntity(pos) instanceof MasonJarBlockEntity blockEntity)) return InteractionResult.PASS;
		if (!(level instanceof ServerLevel serverLevel)) return InteractionResult.SUCCESS;

		MasonJarBlockEntity.MasonJarItemStackHandler handler = blockEntity.getItemHandler();
		if (handler.insertItem(0, stack, true).getCount() >= stack.getCount()) {
			wiggle(serverLevel, pos, blockEntity);
			return InteractionResult.SUCCESS;
		}

		blockEntity.setItemRotation(RotationSegment.convertToSegment(player.getYRot() + 180.0F));
		player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
		ItemStack inserted = stack.copy();
		ItemStack returned = handler.insertItem(0, stack, false);
		player.setItemInHand(hand, player.hasInfiniteMaterials() ? inserted : returned);
		float pitch = (float) (inserted.getCount() - returned.getCount()) / (float) inserted.getMaxStackSize();
		serverLevel.playSound(null, pos, TFSounds.JAR_INSERT, SoundSource.BLOCKS, 1.0F, 0.7F + 0.5F * pitch);
		serverLevel.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
		return InteractionResult.SUCCESS;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
		if (!(level.getBlockEntity(pos) instanceof MasonJarBlockEntity blockEntity)) return InteractionResult.PASS;
		if (!(level instanceof ServerLevel serverLevel)) return InteractionResult.SUCCESS;

		MasonJarBlockEntity.MasonJarItemStackHandler handler = blockEntity.getItemHandler();
		ItemStack preview = handler.extractItem(0, Integer.MAX_VALUE, true);
		if (preview.isEmpty()) {
			wiggle(serverLevel, pos, blockEntity);
		} else if (player.isSecondaryUseActive()) {
			PlayerMessaging.displayClientMessage(player, Component.literal(preview.getItemName().getString() + " x" + preview.getCount()), true);
			wiggle(serverLevel, pos, blockEntity);
		} else {
			player.setItemInHand(InteractionHand.MAIN_HAND, handler.extractItem(0, Integer.MAX_VALUE, false));
			serverLevel.playSound(null, pos, TFSounds.JAR_REMOVE, SoundSource.BLOCKS, 1.0F, 1.0F);
			serverLevel.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
		}
		return InteractionResult.SUCCESS;
	}

	private static void wiggle(ServerLevel level, BlockPos pos, MasonJarBlockEntity blockEntity) {
		level.playSound(null, pos, TFSounds.JAR_WIGGLE, SoundSource.BLOCKS, 1.0F, 1.0F);
		blockEntity.wobble(DecoratedPotBlockEntity.WobbleStyle.NEGATIVE);
	}

	@Override
	protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
		if (params.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof MasonJarBlockEntity jarBlockEntity) {
			params = params.withDynamicDrop(ShulkerBoxBlock.CONTENTS, stackConsumer ->
				stackConsumer.accept(jarBlockEntity.getItemHandler().getItem()));
		}

		return super.getDrops(state, params);
	}

	@Override
	@SuppressWarnings("deprecation")
	protected boolean hasAnalogOutputSignal(BlockState state) {
		return true;
	}

	@Override
	@SuppressWarnings("deprecation")
	protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
		if (level.getBlockEntity(pos) instanceof MasonJarBlockEntity jarBlockEntity) {
			ItemStack itemstack = jarBlockEntity.getItemHandler().getItem();
			return Mth.lerpDiscrete(itemstack.isEmpty() ? 0 : (float) itemstack.getCount() / (float) itemstack.getMaxStackSize(), 0, 15);
		}
		return 0;
	}
}
