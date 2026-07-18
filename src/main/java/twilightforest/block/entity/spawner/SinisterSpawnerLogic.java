package twilightforest.block.entity.spawner;

import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntitySpawnRequest;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import twilightforest.init.TFBlocks;
import twilightforest.mixin.accessor.BaseSpawnerAccessor;
import twilightforest.util.BoundingBoxUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public abstract class SinisterSpawnerLogic extends BaseSpawner {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Codec<List<ParticleOptions>> PARTICLES_CODEC = ParticleTypes.CODEC.listOf();
	private static final String TAG_ENTITY_SCAN_RANGE = "EntityScanRange";

	private @Nullable BlockPos.MutableBlockPos checkPos;
	private final List<BlockPos> spawnBuffer = new ArrayList<>();
	private int countNextToSpawn;
	private int entityScanRange = 4;
	private final Set<ParticleOptions> particleOptions = new HashSet<>();

	protected abstract Either<BlockEntity, Entity> getOwner();

	private BaseSpawnerAccessor access() {
		return (BaseSpawnerAccessor) (Object) this;
	}

	public void setChanged() {
		Either<BlockEntity, Entity> owner = this.getOwner();
		if (owner != null) {
			owner.ifLeft(blockEntity -> {
				blockEntity.setChanged();
				if (blockEntity instanceof SinisterSpawnerBlockEntity sinisterSpawnerBlockEntity) {
					sinisterSpawnerBlockEntity.sendChanges();
				}
			});
		}
	}

	public boolean setParticles(Collection<ParticleOptions> particleOptions, boolean sendUpdate) {
		this.particleOptions.clear();
		boolean changed = this.particleOptions.addAll(particleOptions);
		if (sendUpdate && changed) this.setChanged();
		return changed;
	}

	public boolean addParticle(ParticleOptions particle, boolean sendUpdate) {
		boolean changed = this.particleOptions.add(particle);
		if (sendUpdate && changed) this.setChanged();
		return changed;
	}

	public boolean removeParticle(ParticleOptions particle, boolean sendUpdate) {
		boolean changed = this.particleOptions.remove(particle);
		if (sendUpdate && changed) this.setChanged();
		return changed;
	}

	public int getEntityScanRange() {
		return this.entityScanRange;
	}

	public void setEntityScanRange(int entityScanRange) {
		this.entityScanRange = entityScanRange;
	}

	// [VANILLA COPY] Regular flame/smoke particles are replaced with the configured sinister particles.
	@Override
	public void clientTick(Level level, BlockPos pos) {
		BaseSpawnerAccessor access = this.access();
		if (!access.twilightforest$isNearPlayer(level, pos)) {
			access.twilightforest$setOldSpin(access.twilightforest$getSpin());
		} else if (access.twilightforest$getDisplayEntity() != null) {
			RandomSource random = level.getRandom();
			double particleX = pos.getX() + random.nextDouble();
			double particleY = pos.getY() + random.nextDouble();
			double particleZ = pos.getZ() + random.nextDouble();
			for (ParticleOptions particle : this.particleOptions) {
				level.addParticle(particle, particleX, particleY, particleZ, 0.0, 0.0, 0.0);
			}

			int spawnDelay = access.twilightforest$getSpawnDelay();
			if (spawnDelay > 0) {
				access.twilightforest$setSpawnDelay(--spawnDelay);
			}

			double spin = access.twilightforest$getSpin();
			access.twilightforest$setOldSpin(spin);
			access.twilightforest$setSpin((spin + 1000.0F / (spawnDelay + 200.0F)) % 360.0);
		}
	}

	// Lazily scans for safe positions while charging, then spawns at the positions it found.
	@Override
	public void serverTick(ServerLevel level, BlockPos spawnerPos) {
		BaseSpawnerAccessor access = this.access();
		if (!access.twilightforest$isNearPlayer(level, spawnerPos) || !level.isSpawnerBlockEnabled()) {
			return;
		}

		if (this.countNextToSpawn < 1) {
			this.countNextToSpawn = level.getRandom().nextInt(1 + Math.max(0, access.twilightforest$getSpawnCount()));
		}

		int spawnDelay = access.twilightforest$getSpawnDelay();
		if (spawnDelay <= -1) {
			access.twilightforest$delay(level, spawnerPos);
			spawnDelay = access.twilightforest$getSpawnDelay();
		}

		this.scanSpawnPositions(level, spawnerPos, level.getRandom());

		if (spawnDelay > 0) {
			access.twilightforest$setSpawnDelay(spawnDelay - 1);
		} else if (!this.spawnBuffer.isEmpty()) {
			this.spawnBufferedEntities(level, spawnerPos, access);
		} else if (spawnDelay == 0) {
			access.twilightforest$setSpawnDelay(-1);
		}
	}

	private void spawnBufferedEntities(ServerLevel level, BlockPos spawnerPos, BaseSpawnerAccessor access) {
		boolean spawned = false;
		RandomSource random = level.getRandom();
		SpawnData spawnData = access.twilightforest$getOrCreateNextSpawnData(level, random, spawnerPos);

		for (BlockPos spawnAt : this.spawnBuffer) {
			CompoundTag entityData = spawnData.getEntityToSpawn();
			try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(this::toString, LOGGER)) {
				ValueInput input = TagValueInput.create(reporter, level.registryAccess(), entityData);
				Optional<EntityType<?>> entityType = EntityType.by(input);
				if (entityType.isEmpty()) {
					access.twilightforest$delay(level, spawnerPos);
					return;
				}

				double spawnX = spawnAt.getX() + 0.5;
				double spawnY = spawnAt.getY();
				double spawnZ = spawnAt.getZ() + 0.5;
				if (!level.noCollision(entityType.get().getSpawnAABB(spawnX, spawnY, spawnZ))) {
					continue;
				}

				if (spawnData.getCustomSpawnRules().isPresent()) {
					if (!entityType.get().getCategory().isFriendly() && level.getDifficulty() == Difficulty.PEACEFUL) {
						continue;
					}
					if (!spawnData.getCustomSpawnRules().orElseThrow().isValidPosition(spawnAt, level)) {
						continue;
					}
				} else if (!SpawnPlacements.checkSpawnRules(entityType.get(), level, EntitySpawnReason.SPAWNER, spawnAt, random)) {
					continue;
				}

				Entity entity = EntityType.loadEntityRecursive(entityData, level,
					new EntitySpawnRequest(EntitySpawnReason.SPAWNER, true), spawnedEntity -> {
						spawnedEntity.snapTo(spawnX, spawnY, spawnZ, spawnedEntity.getYRot(), spawnedEntity.getXRot());
						return spawnedEntity;
					});
				if (entity == null) {
					access.twilightforest$delay(level, spawnerPos);
					return;
				}

				int likeEntities = level.getEntities(
					EntityTypeTest.forExactClass(entity.getClass()),
					new AABB(spawnerPos).inflate(this.entityScanRange),
					EntitySelector.NO_SPECTATORS
				).size();
				if (likeEntities >= access.twilightforest$getMaxNearbyEntities()) {
					access.twilightforest$delay(level, spawnerPos);
					return;
				}

				entity.snapTo(entity.getX(), entity.getY(), entity.getZ(), random.nextFloat() * 360.0F, 0.0F);
				if (entity instanceof Mob mob) {
					boolean hasNoConfiguration = entityData.size() == 1 && entityData.getString("id").isPresent();
					if (hasNoConfiguration) {
						mob.finalizeSpawn(level, level.getCurrentDifficultyAt(entity.blockPosition()), EntitySpawnReason.SPAWNER, null);
					}
					spawnData.getEquipment().ifPresent(mob::equip);
					this.checkPos = spawnerPos.mutable();
				}

				if (!level.tryAddFreshEntityWithPassengers(entity)) {
					access.twilightforest$delay(level, spawnerPos);
					return;
				}

				for (ParticleOptions particle : this.particleOptions) {
					level.sendParticles(particle, entity.getX(), entity.getY(0.5F), entity.getZ(), 10,
						entity.getBbWidth() * 0.5F, entity.getBbHeight() * 0.5F, entity.getBbWidth() * 0.5F, 0);
				}
				level.gameEvent(entity, GameEvent.ENTITY_PLACE, spawnAt);
				if (entity instanceof Mob mob) {
					mob.spawnAnim();
				}
				spawned = true;
			}
		}

		this.spawnBuffer.clear();
		if (spawned) {
			for (ParticleOptions particle : this.particleOptions) {
				level.sendParticles(particle, spawnerPos.getX() + 0.5F, spawnerPos.getY() + 0.5F, spawnerPos.getZ() + 0.5F,
					10, 1, 1, 1, 0);
			}
			access.twilightforest$delay(level, spawnerPos);
		}
	}

	private void scanSpawnPositions(ServerLevel level, BlockPos spawnerPos, RandomSource random) {
		if (this.spawnBuffer.size() >= this.countNextToSpawn) return;

		int spawnRange = this.access().twilightforest$getSpawnRange();
		if (this.checkPos == null || !BoundingBoxUtils.isPosWithinBox(spawnerPos, this.checkPos, spawnRange)) {
			BlockPos.MutableBlockPos below = spawnerPos.mutable().move(Direction.DOWN);
			this.checkPos = level.getBlockState(below).isAir() ? below : below.move(Direction.UP, 2);
		}

		BlockState blockBelow = level.getBlockState(this.checkPos.below());
		if (blockBelow.isAir()) {
			this.checkPos.move(Direction.DOWN);
		} else {
			if ((blockBelow.isSolid() || blockBelow.is(TFBlocks.CORONATION_CARPET))
				&& random.nextBoolean() && !this.spawnBuffer.contains(this.checkPos) && level.getBlockState(this.checkPos).isAir()) {
				this.spawnBuffer.add(this.checkPos.immutable());
			}

			Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(random);
			if (!level.getBlockState(this.checkPos.relative(direction)).isSolid()) {
				this.checkPos.move(direction);
			} else {
				this.checkPos = null;
			}
		}
	}

	@Override
	public void load(@Nullable Level level, BlockPos pos, ValueInput input) {
		super.load(level, pos, input);
		this.particleOptions.clear();
		input.read("ParticleOptions", PARTICLES_CODEC).ifPresent(this.particleOptions::addAll);
		this.entityScanRange = readEntityScanRange(input, this.access().twilightforest$getSpawnRange());
	}

	static int readEntityScanRange(ValueInput input, int spawnRange) {
		return input.getIntOr(TAG_ENTITY_SCAN_RANGE, spawnRange);
	}

	@Override
	public void save(ValueOutput output) {
		super.save(output);
		output.store("ParticleOptions", PARTICLES_CODEC, List.copyOf(this.particleOptions));
		output.putInt(TAG_ENTITY_SCAN_RANGE, this.entityScanRange);
	}

	@Override
	public void broadcastEvent(Level level, BlockPos pos, int eventId) {
		level.blockEvent(pos, TFBlocks.SINISTER_SPAWNER, eventId, 0);
	}

	@Override
	protected void setNextSpawnData(@Nullable Level level, BlockPos pos, SpawnData nextSpawnData) {
		super.setNextSpawnData(level, pos, nextSpawnData);
		if (level != null) {
			BlockState blockState = level.getBlockState(pos);
			level.sendBlockUpdated(pos, blockState, blockState, 4);
		}
	}
}
