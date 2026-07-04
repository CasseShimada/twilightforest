package twilightforest.block.entity.spawner;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import twilightforest.init.TFBlocks;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class SinisterSpawnerLogic extends BaseSpawner {
	private static final Codec<List<ParticleOptions>> PARTICLES_CODEC = ParticleTypes.CODEC.listOf();
	private static final double PARTICLE_PLAYER_RANGE = 16.0D;

	private final Set<ParticleOptions> particleOptions = new HashSet<>();

	protected abstract Either<BlockEntity, Entity> getOwner();

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

	@Override
	public void clientTick(Level level, BlockPos pos) {
		super.clientTick(level, pos);
		if (this.particleOptions.isEmpty() || !isPlayerNearby(level, pos)) return;

		RandomSource randomsource = level.getRandom();
		double pX = (double) pos.getX() + randomsource.nextDouble();
		double pY = (double) pos.getY() + randomsource.nextDouble();
		double pZ = (double) pos.getZ() + randomsource.nextDouble();
		for (ParticleOptions particleOptions : this.particleOptions) {
			level.addParticle(particleOptions, pX, pY, pZ, 0.0, 0.0, 0.0);
		}
	}

	@Override
	public void serverTick(ServerLevel serverLevel, BlockPos blockEntityPos) {
		super.serverTick(serverLevel, blockEntityPos);
	}

	@Override
	public void load(@Nullable Level level, BlockPos pos, ValueInput input) {
		super.load(level, pos, input);
		this.particleOptions.clear();
		input.read("ParticleOptions", PARTICLES_CODEC).ifPresent(this.particleOptions::addAll);
	}

	@Override
	public void save(ValueOutput output) {
		super.save(output);
		if (!this.particleOptions.isEmpty()) {
			output.store("ParticleOptions", PARTICLES_CODEC, List.copyOf(this.particleOptions));
		}
	}

	@Override
	public void broadcastEvent(Level level, BlockPos pos, int eventId) {
		level.blockEvent(pos, TFBlocks.SINISTER_SPAWNER, eventId, 0);
	}

	@Override
	protected void setNextSpawnData(@Nullable Level level, BlockPos pos, SpawnData nextSpawnData) {
		super.setNextSpawnData(level, pos, nextSpawnData);
		if (level != null) {
			BlockState blockstate = level.getBlockState(pos);
			level.sendBlockUpdated(pos, blockstate, blockstate, 4);
		}
	}

	private static boolean isPlayerNearby(Level level, BlockPos pos) {
		double x = pos.getX() + 0.5D;
		double y = pos.getY() + 0.5D;
		double z = pos.getZ() + 0.5D;
		double rangeSq = PARTICLE_PLAYER_RANGE * PARTICLE_PLAYER_RANGE;
		for (Player player : level.players()) {
			if (!player.isSpectator() && player.distanceToSqr(x, y, z) <= rangeSq) {
				return true;
			}
		}
		return false;
	}
}
