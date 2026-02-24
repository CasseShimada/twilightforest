package twilightforest.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import tamaized.beanification.junit.MockitoFixer;
import twilightforest.enums.HollowLogVariants;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoFixer.class)
public class HollowLogVerticalTests {

	private VerticalHollowLogBlock instance;
	private ClimbableHollowLogBlock climbable;

	@BeforeAll
	public static void ensureBlockRegistryIntrusive() {
		try {
			var field = MappedRegistry.class.getDeclaredField("unregisteredIntrusiveHolders");
			field.setAccessible(true);
			Object current = field.get(BuiltInRegistries.BLOCK);
			if (current == null) {
				field.set(BuiltInRegistries.BLOCK, new java.util.IdentityHashMap<>());
			}
			var frozenField = MappedRegistry.class.getDeclaredField("frozen");
			frozenField.setAccessible(true);
			if (Boolean.TRUE.equals(frozenField.get(BuiltInRegistries.BLOCK))) {
				frozenField.set(BuiltInRegistries.BLOCK, false);
			}
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("Failed to enable intrusive block holders for tests.", e);
		}
	}

	@BeforeEach
	public void setup() {
		Identifier verticalId = Identifier.fromNamespaceAndPath("twilightforest", "test_vertical");
		Identifier climbableId = Identifier.fromNamespaceAndPath("twilightforest", "test_climbable");
		java.util.concurrent.atomic.AtomicReference<ClimbableHollowLogBlock> climbableRef = new java.util.concurrent.atomic.AtomicReference<>();
		BlockBehaviour.Properties verticalProps = BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, verticalId));
		BlockBehaviour.Properties climbableProps = BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, climbableId));
		instance = new VerticalHollowLogBlock(climbableRef::get, verticalProps);
		climbable = new ClimbableHollowLogBlock(verticalId, () -> instance, climbableProps);
		climbableRef.set(climbable);
	}

	@Test
	public void useItemOnNotInside() {
		ItemStack stack = new ItemStack(Items.STICK);
		BlockState state = instance.defaultBlockState();
		Level level = mock(Level.class);
		Player player = mock(Player.class);
		BlockHitResult hitResult = new BlockHitResult(Vec3.ZERO, Direction.UP, BlockPos.ZERO, false);

		InteractionResult result = instance.useItemOn(stack, state, level, BlockPos.ZERO, player, InteractionHand.MAIN_HAND, hitResult);

		assertSame(InteractionResult.TRY_WITH_EMPTY_HAND, result);
	}

	@Test
	public void useItemOnInside() {
		ItemStack stack = new ItemStack(Items.STICK);
		BlockState state = instance.defaultBlockState();
		Level level = mock(Level.class);
		Player player = mock(Player.class);
		BlockHitResult hitResult = new BlockHitResult(new Vec3(0.5, 0.5, 0.5), Direction.NORTH, BlockPos.ZERO, false);

		InteractionResult result = instance.useItemOn(stack, state, level, BlockPos.ZERO, player, InteractionHand.MAIN_HAND, hitResult);

		assertSame(InteractionResult.TRY_WITH_EMPTY_HAND, result);
	}

	@Test
	public void useItemOnVine() {
		ItemStack stack = new ItemStack(Blocks.VINE);
		BlockState state = instance.defaultBlockState();
		Level level = mock(Level.class);
		Player player = mock(Player.class);
		BlockHitResult hitResult = new BlockHitResult(new Vec3(0.5, 0.5, 0.5), Direction.NORTH, BlockPos.ZERO, false);

		when(player.getDirection()).thenReturn(Direction.NORTH);

		InteractionResult result = instance.useItemOn(stack, state, level, BlockPos.ZERO, player, InteractionHand.MAIN_HAND, hitResult);

		assertSame(InteractionResult.SUCCESS, result);

		ArgumentCaptor<BlockState> climbable = ArgumentCaptor.captor();
		verify(level, times(1)).setBlock(eq(BlockPos.ZERO), climbable.capture(), eq(Block.UPDATE_ALL));
		assertSame(HollowLogVariants.Climbable.VINE, climbable.getValue().getValue(ClimbableHollowLogBlock.VARIANT));
		assertSame(Direction.NORTH, climbable.getValue().getValue(ClimbableHollowLogBlock.FACING));
		verify(level, times(1)).playSound(null, BlockPos.ZERO, SoundEvents.VINE_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
		assertTrue(stack.isEmpty());
	}

	@Test
	public void useItemOnLadder() {
		ItemStack stack = new ItemStack(Blocks.LADDER);
		BlockState state = instance.defaultBlockState();
		Level level = mock(Level.class);
		Player player = mock(Player.class);
		BlockHitResult hitResult = new BlockHitResult(new Vec3(0.5, 0.5, 0.5), Direction.NORTH, BlockPos.ZERO, false);

		when(player.getDirection()).thenReturn(Direction.NORTH);

		InteractionResult result = instance.useItemOn(stack, state, level, BlockPos.ZERO, player, InteractionHand.MAIN_HAND, hitResult);

		assertSame(InteractionResult.SUCCESS, result);

		ArgumentCaptor<BlockState> climbable = ArgumentCaptor.captor();
		verify(level, times(1)).setBlock(eq(BlockPos.ZERO), climbable.capture(), eq(Block.UPDATE_ALL));
		assertSame(HollowLogVariants.Climbable.LADDER, climbable.getValue().getValue(ClimbableHollowLogBlock.VARIANT));
		assertSame(Direction.NORTH, climbable.getValue().getValue(ClimbableHollowLogBlock.FACING));
		verify(level, times(1)).playSound(null, BlockPos.ZERO, SoundEvents.LADDER_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
		assertTrue(stack.isEmpty());
	}

	@Test
	public void useItemOnLadderWaterlogged() {
		ItemStack stack = new ItemStack(Blocks.LADDER);
		BlockState state = instance.defaultBlockState().setValue(VerticalHollowLogBlock.WATERLOGGED, true);
		Level level = mock(Level.class);
		Player player = mock(Player.class);
		BlockHitResult hitResult = new BlockHitResult(new Vec3(0.5, 0.5, 0.5), Direction.NORTH, BlockPos.ZERO, false);

		when(player.getDirection()).thenReturn(Direction.NORTH);

		InteractionResult result = instance.useItemOn(stack, state, level, BlockPos.ZERO, player, InteractionHand.MAIN_HAND, hitResult);

		assertSame(InteractionResult.SUCCESS, result);

		ArgumentCaptor<BlockState> climbable = ArgumentCaptor.captor();
		verify(level, times(1)).setBlock(eq(BlockPos.ZERO), climbable.capture(), eq(Block.UPDATE_ALL));
		assertSame(HollowLogVariants.Climbable.LADDER_WATERLOGGED, climbable.getValue().getValue(ClimbableHollowLogBlock.VARIANT));
		assertSame(Direction.NORTH, climbable.getValue().getValue(ClimbableHollowLogBlock.FACING));
		verify(level, times(1)).playSound(null, BlockPos.ZERO, SoundEvents.LADDER_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
		assertTrue(stack.isEmpty());
	}
}
