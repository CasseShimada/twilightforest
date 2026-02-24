package twilightforest.entity.boss;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.Vec3;
import twilightforest.network.PacketDistributor;
import twilightforest.config.TFConfig;
import twilightforest.init.TFSounds;
import twilightforest.loot.TFLootTables;
import twilightforest.network.ParticlePacket;

import java.util.ArrayList;
import java.util.List;

public interface IBossLootBuffer {
	int CONTAINER_SIZE = 27;

	default ItemStack getItem(int slot) {
		return this.getItemStacks().get(slot);
	}

	default void setItem(int slot, ItemStack stack) {
		this.getItemStacks().set(slot, stack);
		if (!stack.isEmpty() && stack.getCount() > stack.getMaxStackSize()) {
			stack.setCount(stack.getMaxStackSize());
		}
	}

	default void addDeathItemsSaveData(ValueOutput output) {
		ContainerHelper.saveAllItems(output, this.getItemStacks());
	}

	default void readDeathItemsSaveData(ValueInput input) {
		ContainerHelper.loadAllItems(input, this.getItemStacks());
	}

	static <T extends LivingEntity & IBossLootBuffer> void saveDropsIntoBoss(T boss, LootParams params, ServerLevel serverLevel) {
		var loot = boss.getLootTable();
		if (TFConfig.bossDropChests && loot.isPresent()) {
			LootTable table = serverLevel.getServer().reloadableRegistries().getLootTable(loot.get());
			ObjectArrayList<ItemStack> stacks = table.getRandomItems(params);
			boss.fill(boss, params, table);

			//If our loot stack size is bigger than the inventory, drop everything else outside it. Don't want to lose any loot now do we?
			if (stacks.size() > CONTAINER_SIZE) {
				for (ItemStack stack : stacks.subList(28, stacks.size())) {
					ItemEntity item = new ItemEntity(serverLevel, boss.getX(), boss.getY(), boss.getZ(), stack);
					item.setExtendedLifetime();
					item.setNoPickUpDelay();
					serverLevel.addFreshEntity(item);
				}
			}
		}
	}

	static <T extends LivingEntity & IBossLootBuffer> void depositDropsIntoChest(T boss, BlockState chest, BlockPos pos, ServerLevel serverLevel) {
		if (TFConfig.bossDropChests && !boss.getItemStacks().isEmpty()) {
			if (!tryDeposit(boss, chest, pos, serverLevel)) {
				BlockPos.MutableBlockPos chestPos = pos.mutable();
				for (int y = pos.getY(); y < serverLevel.getMaxY(); y++) {
					chestPos.setY(y);
					if (tryDeposit(boss, chest, chestPos, serverLevel)) return;
				}
			} else return;

			for (int i = 0; i < CONTAINER_SIZE; i++) {
				Block.popResource(serverLevel, pos, boss.getItem(i));
			}
			celebrateAt(boss, pos.getCenter(), serverLevel);
		}
	}

	static <T extends LivingEntity & IBossLootBuffer> boolean tryDeposit(T boss, BlockState chest, BlockPos pos, ServerLevel serverLevel) {
		if ((serverLevel.getBlockState(pos).is(chest.getBlock()) ||
			((serverLevel.getBlockState(pos).canBeReplaced() || serverLevel.getBlockState(pos).getPistonPushReaction() != PushReaction.BLOCK) && serverLevel.getBlockEntity(pos) == null && serverLevel.setBlock(pos, chest, TFLootTables.DEFAULT_PLACE_FLAG))) &&
			serverLevel.getBlockEntity(pos) instanceof Container container) {

			for (int i = 0; i < CONTAINER_SIZE && i < container.getContainerSize(); i++) {
				container.setItem(i, boss.getItem(i));
			}
			celebrateAt(boss, pos.getCenter(), serverLevel);
			return true;
		}
		return false;
	}

	static <T extends LivingEntity & IBossLootBuffer> void celebrateAt(T boss, Vec3 vec3, ServerLevel serverLevel) {
		serverLevel.playSound(null, vec3.x, vec3.y, vec3.z, TFSounds.BOSS_CHEST_APPEAR.get(), boss.getSoundSource(), 128.0F, (boss.getRandom().nextFloat() - boss.getRandom().nextFloat()) * 0.175F + 0.5F);

		ParticlePacket particlePacket = new ParticlePacket();
		for (int i = 0; i < 40; i++) {
			double x = (boss.getRandom().nextDouble() - 0.5D) * 0.075D * i;
			double y = (boss.getRandom().nextDouble() - 0.5D) * 0.075D * i;
			double z = (boss.getRandom().nextDouble() - 0.5D) * 0.075D * i;
			particlePacket.queueParticle(ParticleTypes.POOF, vec3.add(x, y, z), Vec3.ZERO);
		}
		PacketDistributor.sendToPlayersTrackingEntity(boss, particlePacket);
	}

	default <T extends LivingEntity & IBossLootBuffer> void fill(T boss, LootParams context, LootTable table) {
		ObjectArrayList<ItemStack> items = table.getRandomItems(context);
		RandomSource randomsource = boss.getRandom();
		List<Integer> list = this.getAvailableSlots(randomsource);
		shuffleAndSplitItems(items, list.size(), randomsource);

		for (ItemStack itemstack : items) {
			if (!list.isEmpty()) {
				this.setItem(list.removeLast(), itemstack.isEmpty() ? ItemStack.EMPTY : itemstack);
			}
		}
	}

	default List<Integer> getAvailableSlots(RandomSource random) {
		ObjectArrayList<Integer> arrayList = new ObjectArrayList<>();
		for (int i = 0; i < CONTAINER_SIZE; ++i) arrayList.add(i);
		Util.shuffle(arrayList, random);
		return arrayList;
	}

	static void shuffleAndSplitItems(ObjectArrayList<ItemStack> stacks, int slots, RandomSource random) {
		List<ItemStack> splitStacks = new ArrayList<>();

		for (var iterator = stacks.iterator(); iterator.hasNext(); ) {
			ItemStack stack = iterator.next();
			if (stack.isEmpty()) {
				iterator.remove();
			} else if (stack.getCount() > 1) {
				splitStacks.add(stack);
				iterator.remove();
			}
		}

		while (slots - stacks.size() - splitStacks.size() > 0 && !splitStacks.isEmpty()) {
			ItemStack stack = splitStacks.remove(Mth.nextInt(random, 0, splitStacks.size() - 1));
			int splitCount = Mth.nextInt(random, 1, stack.getCount() / 2);
			ItemStack split = stack.split(splitCount);

			if (stack.getCount() > 1 && random.nextBoolean()) {
				splitStacks.add(stack);
			} else {
				stacks.add(stack);
			}

			if (split.getCount() > 1 && random.nextBoolean()) {
				splitStacks.add(split);
			} else {
				stacks.add(split);
			}
		}

		stacks.addAll(splitStacks);
		Util.shuffle(stacks, random);
	}

	NonNullList<ItemStack> getItemStacks();
}
