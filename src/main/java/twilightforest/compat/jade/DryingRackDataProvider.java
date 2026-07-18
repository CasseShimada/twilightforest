package twilightforest.compat.jade;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.StreamServerDataProvider;
import twilightforest.TwilightForestMod;
import twilightforest.block.entity.DryingRackBlockEntity;

public enum DryingRackDataProvider implements StreamServerDataProvider<BlockAccessor, DryingRackDataProvider.Data> {
	INSTANCE;

	@Override
	public Data streamData(BlockAccessor accessor) {
		if (!(accessor.getBlockEntity() instanceof DryingRackBlockEntity rack) || !rack.isDrying()) {
			return null;
		}

		CompoundTag tag = rack.getUpdateTag(accessor.getLevel().registryAccess());
		return new Data(tag.getIntOr("dry_time", 0), tag.getIntOr("total_dry_time", DryingRackBlockEntity.DEFAULT_DRYING_TIME));
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, Data> streamCodec() {
		return Data.STREAM_CODEC.cast();
	}

	@Override
	public Identifier getUid() {
		return TwilightForestMod.prefix("drying_rack");
	}

	public record Data(int progress, int total) {
		private static final StreamCodec<ByteBuf, Data> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT,
			Data::progress,
			ByteBufCodecs.VAR_INT,
			Data::total,
			Data::new);
	}
}
