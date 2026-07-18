package twilightforest.block.entity.bookshelf;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.ChiseledBookShelfBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.trialspawner.PlayerDetector;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;
import twilightforest.TwilightForestMod;
import twilightforest.block.ChiseledCanopyShelfBlock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public abstract class BookshelfSpawner {
	private static final String LEGACY_ENTITY_TYPE_TAG = "EntityType";
	private static final String LEGACY_REMAINING_TAG = "MobSpawnsLeft";
	private static final String LEGACY_SPAWN_DELAY_TAG = "SpawnDelay";
	private static final String LEGACY_PLAYER_RANGE_TAG = "MaxPlayerDistance";
	public static final List<Pair<Integer, BooleanProperty>> SLOT_PROPERTIES_AND_INDEXES = List.of(
		Pair.of(0, ChiseledBookShelfBlock.SLOT_0_OCCUPIED),
		Pair.of(1, ChiseledBookShelfBlock.SLOT_1_OCCUPIED),
		Pair.of(2, ChiseledBookShelfBlock.SLOT_2_OCCUPIED),
		Pair.of(3, ChiseledBookShelfBlock.SLOT_3_OCCUPIED),
		Pair.of(4, ChiseledBookShelfBlock.SLOT_4_OCCUPIED),
		Pair.of(5, ChiseledBookShelfBlock.SLOT_5_OCCUPIED));
	public int maxNearbyEntities = 4;
	public int spawnRange = 4;
	public int spawnCheckRange = 12;
	private int spawnDelay = 20;
	private WeightedList<SpawnData> spawnPotentials = WeightedList.of();
	@Nullable
	private SpawnData nextSpawnData;
	private int minSpawnDelay = 200;
	private int maxSpawnDelay = 400;
	private int requiredPlayerRange = 8;
	private final PlayerDetector detector = PlayerDetector.INCLUDING_CREATIVE_PLAYERS;

	public void setEntityId(EntityType<?> type, @Nullable Level level, RandomSource random, BlockPos pos) {
		this.getOrCreateNextSpawnData(level, random, pos)
			.getEntityToSpawn()
			.putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(type).toString());
	}

	private boolean isNearPlayer(ServerLevel level, BlockPos pos) {
		return !this.detector.detect(level, PlayerDetector.EntitySelector.SELECT_FROM_LEVEL, pos, this.requiredPlayerRange, true).isEmpty();
	}

	public void serverTick(ServerLevel level, BlockPos pos, BlockState state) {
		if (this.isNearPlayer(level, pos)) {
			if (this.spawnDelay == -1) {
				this.delay(level, pos);
			}

			if (this.spawnDelay > 0) {
				this.spawnDelay--;
				this.markOwnerChanged(level, pos);
			} else {
				List<Pair<Integer, BooleanProperty>> filledSlots = new ArrayList<>(SLOT_PROPERTIES_AND_INDEXES);
				filledSlots.removeIf(pair -> !state.getValue(pair.getSecond()));
				Collections.shuffle(filledSlots);

				for (Pair<Integer, BooleanProperty> filledSlot : filledSlots) {
					BooleanProperty property = filledSlot.getSecond();
					if (state.hasProperty(property) && state.getValue(property)) {
						if (this.attemptSpawnTome(filledSlot.getFirst(), level, pos,
							state.getValue(HorizontalDirectionalBlock.FACING), false, null, 0)) {
							this.delay(level, pos);
							break;
						}
					}
				}

				int fullSlots = 0;
				for (BooleanProperty property : ChiseledCanopyShelfBlock.SLOT_OCCUPIED_PROPERTIES) {
					if (state.hasProperty(property) && state.getValue(property)) {
						fullSlots++;
					}
				}

				if (fullSlots == 0) {
					level.setBlockAndUpdate(pos, state.setValue(ChiseledCanopyShelfBlock.SPAWNER, false));
				}
			}
		}
	}

	private void delay(Level level, BlockPos pos) {
		RandomSource randomsource = level.getRandom();
		if (this.maxSpawnDelay <= this.minSpawnDelay) {
			this.spawnDelay = this.minSpawnDelay;
		} else {
			this.spawnDelay = this.minSpawnDelay + randomsource.nextInt(this.maxSpawnDelay - this.minSpawnDelay);
		}

		this.spawnPotentials.getRandom(randomsource).ifPresent(p_337965_ -> this.setNextSpawnData(level, pos, p_337965_));
		this.broadcastEvent(level, pos, 1);
		if (level instanceof ServerLevel serverLevel) {
			this.markOwnerChanged(serverLevel, pos);
		}
	}

	private void markOwnerChanged(ServerLevel level, BlockPos pos) {
		if (level.getBlockEntity(pos) instanceof ChiseledCanopyShelfBlockEntity shelf) {
			shelf.setChanged();
		}
	}

	public static Optional<LegacyTomeSpawnerState> readLegacyState(ValueInput input) {
		boolean hasCurrentData = input.getInt("Delay").isPresent()
			|| input.child("SpawnData").isPresent()
			|| input.childrenList("SpawnPotentials").isPresent();
		if (hasCurrentData) {
			return Optional.empty();
		}

		Optional<String> entityType = input.getString(LEGACY_ENTITY_TYPE_TAG);
		Optional<Integer> remaining = input.getInt(LEGACY_REMAINING_TAG);
		Optional<Integer> spawnDelay = input.getInt(LEGACY_SPAWN_DELAY_TAG);
		Optional<Integer> playerRange = input.getInt(LEGACY_PLAYER_RANGE_TAG);
		if (entityType.isEmpty() && remaining.isEmpty() && spawnDelay.isEmpty() && playerRange.isEmpty()) {
			return Optional.empty();
		}
		if (entityType.isEmpty() || spawnDelay.isEmpty() || playerRange.isEmpty()) {
			throw new IllegalStateException("Incomplete legacy Tome Spawner data");
		}
		if (entityType.get().isBlank()) {
			throw new IllegalStateException("Legacy Tome Spawner EntityType must not be blank");
		}
		int delay = requireShort(LEGACY_SPAWN_DELAY_TAG, spawnDelay.get());
		int range = requireShort(LEGACY_PLAYER_RANGE_TAG, playerRange.get());
		int remainingSpawns = Math.clamp(remaining.orElse(10), 0, 10);
		return Optional.of(new LegacyTomeSpawnerState(entityType.get(), remainingSpawns, delay, range, remaining.isEmpty()));
	}

	private static int requireShort(String key, int value) {
		if (value < Short.MIN_VALUE || value > Short.MAX_VALUE) {
			throw new IllegalStateException("Legacy Tome Spawner " + key + " is outside the supported short range: " + value);
		}
		return value;
	}

	public void loadLegacyState(@Nullable Level level, BlockPos pos, LegacyTomeSpawnerState legacy) {
		this.spawnDelay = legacy.spawnDelay();
		this.minSpawnDelay = legacy.spawnDelay();
		this.maxSpawnDelay = legacy.spawnDelay();
		this.maxNearbyEntities = Short.MAX_VALUE;
		this.requiredPlayerRange = legacy.playerRange();
		SpawnData spawnData = new SpawnData();
		spawnData.getEntityToSpawn().putString("id", legacy.entityType());
		this.setNextSpawnData(level, pos, spawnData);
		this.spawnPotentials = WeightedList.of(spawnData);
	}

	PersistentState persistentState() {
		String entityType = this.nextSpawnData == null
			? ""
			: this.nextSpawnData.getEntityToSpawn().getString("id").orElse("");
		return new PersistentState(this.spawnDelay, this.minSpawnDelay, this.maxSpawnDelay,
			this.maxNearbyEntities, this.requiredPlayerRange, entityType);
	}

	public void load(@Nullable Level level, BlockPos pos, ValueInput input) {
		this.spawnDelay = input.getShortOr("Delay", (short) this.spawnDelay);
		input.read("SpawnData", SpawnData.CODEC)
			.ifPresent(spawndata -> this.setNextSpawnData(level, pos, spawndata));

		this.spawnPotentials = input.read("SpawnPotentials", SpawnData.LIST_CODEC)
			.orElseGet(() -> WeightedList.of(this.nextSpawnData != null ? this.nextSpawnData : new SpawnData()));

		this.minSpawnDelay = input.getShortOr("MinSpawnDelay", (short) this.minSpawnDelay);
		this.maxSpawnDelay = input.getShortOr("MaxSpawnDelay", (short) this.maxSpawnDelay);
		this.maxNearbyEntities = input.getShortOr("MaxNearbyEntities", (short) this.maxNearbyEntities);
		this.requiredPlayerRange = input.getShortOr("RequiredPlayerRange", (short) this.requiredPlayerRange);
		this.spawnRange = input.getShortOr("SpawnRange", (short) this.spawnRange);
		this.spawnCheckRange = input.getShortOr("SpawnCheckRange", (short) this.spawnCheckRange);
	}

	public void save(ValueOutput output) {
		output.putShort("Delay", (short) this.spawnDelay);
		output.putShort("MinSpawnDelay", (short) this.minSpawnDelay);
		output.putShort("MaxSpawnDelay", (short) this.maxSpawnDelay);
		output.putShort("MaxNearbyEntities", (short) this.maxNearbyEntities);
		output.putShort("RequiredPlayerRange", (short) this.requiredPlayerRange);
		output.putShort("SpawnRange", (short) this.spawnRange);
		output.putShort("SpawnCheckRange", (short) this.spawnCheckRange);
		if (this.nextSpawnData != null) {
			output.store("SpawnData", SpawnData.CODEC, this.nextSpawnData);
		}

		output.store("SpawnPotentials", SpawnData.LIST_CODEC, this.spawnPotentials);
	}

	public boolean onEventTriggered(Level level, int id) {
		if (id == 1) {
			if (level.isClientSide()) {
				this.spawnDelay = this.minSpawnDelay;
			}

			return true;
		} else {
			return false;
		}
	}

	protected void setNextSpawnData(@Nullable Level level, BlockPos pos, SpawnData data) {
		this.nextSpawnData = data;
	}

	@Nullable
	public SpawnData getNextSpawnData() {
		return this.nextSpawnData;
	}

	private SpawnData getOrCreateNextSpawnData(@Nullable Level level, RandomSource pRandom, BlockPos pos) {
		if (this.nextSpawnData == null) {
			this.setNextSpawnData(level, pos, this.spawnPotentials.getRandom(pRandom).orElseGet(SpawnData::new));
		}
		return this.nextSpawnData;
	}

	public abstract void broadcastEvent(Level level, BlockPos pos, int id);

	public boolean attemptSpawnTome(int slot, ServerLevel level, BlockPos pos, Direction facing, boolean fire,
		@Nullable LivingEntity assailant, int maxTries) {
		RandomSource random = level.getRandom();
		SpawnData data = this.getOrCreateNextSpawnData(level, random, pos);
		CompoundTag tag = data.entityToSpawn();
		ValueInput entityInput = TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), tag);
		Optional<EntityType<?>> optional = EntityType.by(entityInput);
		//if the assigned entity doesn't exist or the bookshelf is blocked off, fail early
		if (optional.isEmpty() || !level.getBlockState(pos.relative(facing)).canBeReplaced()) {
			this.delay(level, pos);
			return false;
		}

		//pick random spot in front of the shelf
		double x = pos.relative(facing).getX() + (random.nextDouble() - random.nextDouble()) * 2.0D;
		double y = (double) pos.getY() + (random.nextDouble() - random.nextDouble());
		double z = pos.relative(facing).getZ() + (random.nextDouble() - random.nextDouble()) * 2.0D;

		//apply spawning logic like vanilla spawners do
		if (level.noCollision(optional.get().getSpawnAABB(x, y, z))) {
			boolean difficultyPreventsSpawn = !optional.get().getCategory().isFriendly() && level.getDifficulty() == Difficulty.PEACEFUL;

			BlockPos blockpos = BlockPos.containing(x, y, z);
			if (data.getCustomSpawnRules().isPresent()) {
				if (difficultyPreventsSpawn) {
					return false;
				}

				SpawnData.CustomSpawnRules rules = data.getCustomSpawnRules().get();
				if (!rules.isValidPosition(blockpos, level) && !fire) {
					return false;
				}
			} else if (difficultyPreventsSpawn) {
				this.delay(level, pos);
				return false;
			}

			Entity entity = EntityType.loadEntityRecursive(tag, level, new EntitySpawnRequest(EntitySpawnReason.SPAWNER, true), processed -> {
				processed.setPos(x, y, z);
				//set entity on fire if told to do so
				if (fire) {
					processed.setRemainingFireTicks(200);
				}

				//target whoever was responsible for spawning the mob
				if (assailant != null && processed instanceof Mob mob) {
					mob.setTarget(assailant);
				}

				return processed;
			});
			if (entity == null) {
				this.delay(level, pos);
				return false;
			}

			int k = level.getEntities(EntityTypeTest.forExactClass(entity.getClass()), new AABB(pos).inflate(this.spawnCheckRange), EntitySelector.NO_SPECTATORS).size();
			if (k >= this.maxNearbyEntities && !fire) {
				this.delay(level, pos);
				return false;
			}

			float yaw = random.nextFloat() * 360.0F;
			entity.setYRot(yaw);
			entity.setXRot(0.0F);
			if (entity instanceof Mob mob) {
				mob.setYHeadRot(yaw);
			}
			if (entity instanceof Mob mob) {
				boolean flag1 = data.getEntityToSpawn().size() == 1 && data.getEntityToSpawn().contains("id");
				// Vanilla spawner behavior: only call finalizeSpawn if the spawn tag is just an "id".
				if (flag1) {
					mob.finalizeSpawn(level, level.getCurrentDifficultyAt(entity.blockPosition()), EntitySpawnReason.SPAWNER, null);
				}

				data.getEquipment().ifPresent(mob::equip);
			}

			if (!level.tryAddFreshEntityWithPassengers(entity)) {
				this.delay(level, pos);
				return false;
			}

			level.gameEvent(entity, GameEvent.ENTITY_PLACE, blockpos);
			if (entity instanceof Mob mob) {
				mob.spawnAnim();
			}

			//after mob is spawned, clear that book's spot from the shelf
			if (level.getBlockEntity(pos) instanceof ChiseledCanopyShelfBlockEntity be) {
				be.consumeSpawnerBook(slot);
			}
			return true;
		} else {
			if (maxTries != 0) {
				return this.attemptSpawnTome(slot, level, pos, facing, fire, assailant, maxTries - 1);
			}
		}
		return false;
	}

	public record LegacyTomeSpawnerState(String entityType, int remainingSpawns, int spawnDelay, int playerRange,
		boolean remainingWasInferred) {
	}

	record PersistentState(int spawnDelay, int minSpawnDelay, int maxSpawnDelay, int maxNearbyEntities,
		int requiredPlayerRange, String entityType) {
	}
}
