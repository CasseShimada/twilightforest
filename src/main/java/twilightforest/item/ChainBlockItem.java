package twilightforest.item;

import net.minecraft.server.MinecraftServer;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import twilightforest.entity.projectile.ChainBlock;
import twilightforest.init.TFDataComponents;
import twilightforest.init.TFEnchantments;
import twilightforest.init.TFEntities;
import twilightforest.init.TFSounds;
import twilightforest.network.PacketDistributor;
import twilightforest.tags.TFBlockTags;

import java.util.UUID;

public class ChainBlockItem extends Item {

	public ChainBlockItem(Properties properties) {
		super(properties);
	}

	@Override
	public void inventoryTick(ItemStack stack, ServerLevel level, Entity holder, EquipmentSlot slot) {
		if (stack.get(TFDataComponents.THROWN_PROJECTILE.get()) != null && this.getThrownEntity(level, stack) == null) {
			stack.remove(TFDataComponents.THROWN_PROJECTILE.get());
		}
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);

		if (stack.get(TFDataComponents.THROWN_PROJECTILE.get()) != null || !level.getWorldBorder().isWithinBounds(player.blockPosition()))
			return InteractionResult.PASS;

		player.playSound(TFSounds.BLOCK_AND_CHAIN_FIRED, 0.5F, 1.0F / (level.getRandom().nextFloat() * 0.4F + 1.2F));

		if (level instanceof ServerLevel serverLevel) {
			Projectile.spawnProjectileFromRotation((lev, entity, stacc) -> {
				ChainBlock launchedBlock = new ChainBlock(TFEntities.CHAIN_BLOCK.get(), lev, entity, hand, stacc);
				stack.set(TFDataComponents.THROWN_PROJECTILE.get(), launchedBlock.getUUID());
				return launchedBlock;
			}, serverLevel, stack, player, 0.0F, 1.5F, 1.0F);
		}

		player.startUsingItem(hand);
		return InteractionResult.SUCCESS;
	}

	@Nullable
	private ChainBlock getThrownEntity(Level level, ItemStack stack) {
		if (level instanceof ServerLevel server) {
			UUID id = stack.get(TFDataComponents.THROWN_PROJECTILE.get());
			if (id != null) {
				Entity e = server.getEntity(id);
				if (e instanceof ChainBlock) {
					return (ChainBlock) e;
				}
			}
		}

		return null;
	}

	@Override
	public int getUseDuration(ItemStack stack, LivingEntity user) {
		return 72000;
	}

	@Override
	public ItemUseAnimation getUseAnimation(ItemStack stack) {
		return ItemUseAnimation.BLOCK;
	}

	@Override
	public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
		//dont try to check harvest level if we arent thrown
		if (stack.get(TFDataComponents.THROWN_PROJECTILE.get()) == null || !state.is(TFBlockTags.MINEABLE_WITH_BLOCK_AND_CHAIN)) return false;
		MinecraftServer server = PacketDistributor.getServer();
		if (server != null) {
			int destruction = EnchantmentHelper.getItemEnchantmentLevel(server.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(TFEnchantments.DESTRUCTION), stack);
			if (destruction > 0) return !state.is(this.getHarvestLevel(destruction).incorrectBlocksForDrops()); //FIXME 1.21.3
		}
		return false;
	}

	public ToolMaterial getHarvestLevel(int destruction) {
		return switch (destruction) {
			case 1 -> ToolMaterial.WOOD;
			case 2 -> ToolMaterial.STONE;
			default -> ToolMaterial.IRON;
		};
	}
}
