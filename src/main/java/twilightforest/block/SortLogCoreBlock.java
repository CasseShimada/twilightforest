package twilightforest.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.fabricmc.fabric.api.transfer.v1.item.ContainerStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import twilightforest.network.PacketDistributor;
import twilightforest.config.TFConfig;
import twilightforest.tags.TFEntityTypeTags;
import twilightforest.init.TFParticleType;
import twilightforest.network.ParticlePacket;
import twilightforest.util.BlockCapabilityDirectionalCache;
import twilightforest.util.WorldUtil;

import java.util.*;

public class SortLogCoreBlock extends SpecialMagicLogBlock {

	private final BlockCapabilityDirectionalCache<Storage<ItemVariant>> capabilityCache = new BlockCapabilityDirectionalCache<>();

	public SortLogCoreBlock(Properties properties) {
		super(properties);
	}

	@Override
	public boolean doesCoreFunction() {
		return !TFConfig.disableSortingCore;
	}

	@Override
	void performTreeEffect(ServerLevel level, BlockPos pos, RandomSource rand) {
		Map<List<Storage<ItemVariant>>, Vec3> inputMap = new HashMap<>();
		Map<Storage<ItemVariant>, Vec3> outputMap = new HashMap<>();

		for (BlockPos blockPos : WorldUtil.getAllAround(pos, TFConfig.sortingCoreRange)) { // Get every itemHandler from every block in the area
			if (!blockPos.equals(pos)) {
				BlockEntity blockEntity = level.getBlockEntity(blockPos);
				if (blockEntity != null) {
					// Put it in the input if its within 2 blocks
					if (Math.abs(blockPos.getX() - pos.getX()) <= 2 && Math.abs(blockPos.getY() - pos.getY()) <= 2 && Math.abs(blockPos.getZ() - pos.getZ()) <= 2) {
						List<Storage<ItemVariant>> storages = new ArrayList<>();
						for (Direction side : Direction.values()) {
							Storage<ItemVariant> storage = this.capabilityCache.get(ItemStorage.SIDED, level, blockPos, side);
							if (storage != null) storages.add(storage);
						}
						if (!storages.isEmpty()) {
							inputMap.put(storages, Vec3.upFromBottomCenterOf(blockPos, 1.9D));
						}
					} else { // Output if its outside that range
						for (Direction side : Direction.values()) {
							Storage<ItemVariant> storage = this.capabilityCache.get(ItemStorage.SIDED, level, blockPos, side);
							if (storage != null) outputMap.put(storage, Vec3.upFromBottomCenterOf(blockPos, 1.9D));
						}
					}
				}
			}
		}

		List<Entity> alreadyUsedForInput = new ArrayList<>(); // Keep track of entities we already have for inputs, so we can skip over them when looking for outputs

		level.getEntities((Entity) null, new AABB(pos).inflate(2), entity -> entity.isAlive() && entity.getType().builtInRegistryHolder().is(TFEntityTypeTags.SORTABLE_ENTITIES)).forEach(entity -> {
			if (entity instanceof Container container) {
				List<Storage<ItemVariant>> storages = new ArrayList<>();
				for (Direction side : Direction.values()) {
					storages.add(ContainerStorage.of(container, side));
				}
				inputMap.put(storages, entity.position().add(0D, entity.getBbHeight() + 0.9D, 0D));
				alreadyUsedForInput.add(entity);
			}
		});

		if (inputMap.isEmpty()) return; // No input

		level.getEntities((Entity) null, new AABB(pos).inflate(16), entity -> entity.isAlive() && !alreadyUsedForInput.contains(entity) && entity.getType().builtInRegistryHolder().is(TFEntityTypeTags.SORTABLE_ENTITIES)).forEach(entity -> {
			if (entity instanceof Container container) {
				for (Direction side : Direction.values()) {
					outputMap.put(ContainerStorage.of(container, side), entity.position().add(0D, entity.getBbHeight() + 0.9D, 0D));
				}
			}
		});

		if (outputMap.isEmpty()) return; // No output

		for (Map.Entry<List<Storage<ItemVariant>>, Vec3> inputHandlers : inputMap.entrySet()) {
			boolean transferred = false;
			for (Storage<ItemVariant> inputStorage : inputHandlers.getKey()) {
				ItemVariant extractedVariant;
				// Extract and insert must be atomic; use a transaction so we can roll back cleanly.
				try (Transaction tx = Transaction.openOuter()) {
					ResourceAmount<ItemVariant> extractable = StorageUtil.findExtractableContent(inputStorage, tx);
					if (extractable == null || extractable.resource() == null || extractable.resource().isBlank() || extractable.amount() <= 0) {
						continue;
					}
					extractedVariant = extractable.resource();

					if (inputStorage.extract(extractedVariant, 1, tx) != 1) {
						continue;
					}

					// Find the output storage that already has the most of this item.
					List<Map.Entry<Storage<ItemVariant>, Long>> outputsByCount = new ArrayList<>();
					for (Storage<ItemVariant> outputStorage : outputMap.keySet()) {
						long count = countItem(outputStorage, extractedVariant.getItem());
						if (count > 0) {
							outputsByCount.add(Map.entry(outputStorage, count));
						}
					}

					outputsByCount.sort(Map.Entry.<Storage<ItemVariant>, Long>comparingByValue().reversed());

					for (Map.Entry<Storage<ItemVariant>, Long> entry : outputsByCount) {
						Storage<ItemVariant> outputStorage = entry.getKey();
						long inserted = StorageUtil.tryInsertStacking(outputStorage, extractedVariant, 1, tx);
						if (inserted == 1) {
							tx.commit();
							transferred = true;

							Vec3 xyz = outputMap.get(outputStorage);
							if (xyz != null) {
								Vec3 diff = inputHandlers.getValue().subtract(xyz);
								double len = diff.length();
								if (len > 1.0E-6D) {
									ParticlePacket particlePacket = new ParticlePacket();
									double x = diff.x - 0.25D + rand.nextDouble() * 0.5D;
									double y = diff.y - 1.75D + rand.nextDouble() * 0.5D;
									double z = diff.z - 0.25D + rand.nextDouble() * 0.5D;
									particlePacket.queueParticle(TFParticleType.SORTING_PARTICLE, xyz, new Vec3(x, y, z).scale(1D / len));
									PacketDistributor.sendToPlayersNear(level, null, xyz.x(), xyz.y(), xyz.z(), 64.0D, particlePacket);
								}
							}
							break;
						}
					}
				}

				if (transferred) break; // Again, since we only transfer once per source, break
			}
		}
	}

	private static long countItem(Storage<ItemVariant> storage, net.minecraft.world.item.Item item) {
		long total = 0;
		for (StorageView<ItemVariant> view : storage) {
			if (!view.isResourceBlank() && view.getResource().getItem() == item) {
				total += view.getAmount();
			}
		}
		return total;
	}
}
