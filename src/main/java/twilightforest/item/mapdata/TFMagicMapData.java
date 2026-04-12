package twilightforest.item.mapdata;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.saveddata.maps.*;
import org.jetbrains.annotations.Nullable;
import twilightforest.TwilightForestMod;
import twilightforest.item.MagicMapItem;
import twilightforest.network.MagicMapPacket;
import twilightforest.util.Codecs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TFMagicMapData extends MapItemSavedData {

	private static final Map<String, TFMagicMapData> CLIENT_DATA = new HashMap<>();
	private static final Map<String, SavedDataType<TFMagicMapData>> TYPES = new HashMap<>();
	private static final Codec<TFMagicMapData> CODEC = new Codec<>() {
		@Override
		public <T> DataResult<Pair<TFMagicMapData, T>> decode(DynamicOps<T> ops, T input) {
			return MapItemSavedData.CODEC.decode(ops, input).map(pair -> {
				MapItemSavedData data = pair.getFirst();
				boolean trackingPosition = readBoolean(ops, input, "trackingPosition", true);
				boolean unlimitedTracking = readBoolean(ops, input, "unlimitedTracking", false);
				boolean locked = readBoolean(ops, input, "locked", data.locked);
				TFMagicMapData tfdata = new TFMagicMapData(data.centerX, data.centerZ, data.scale, trackingPosition, unlimitedTracking, locked, data.dimension);

				tfdata.colors = data.colors;
				tfdata.bannerMarkers.putAll(data.bannerMarkers);
				tfdata.frameMarkers.putAll(data.frameMarkers);
				tfdata.decorations.putAll(data.decorations);
				tfdata.trackedDecorationCount = data.trackedDecorationCount;
				tfdata.conqueredStructures.addAll(readStringList(ops, input, "conquered_structures"));

				return Pair.of(tfdata, pair.getSecond());
			});
		}

		@Override
		public <T> DataResult<T> encode(TFMagicMapData value, DynamicOps<T> ops, T prefix) {
			DataResult<T> base = MapItemSavedData.CODEC.encode(value, ops, prefix);
			if (value.conqueredStructures.isEmpty()) {
				return base;
			}
			return base.flatMap(tag -> Codec.STRING.listOf().encodeStart(ops, value.conqueredStructures)
				.flatMap(list -> ops.mergeToMap(tag, ops.createString("conquered_structures"), list)));
		}
	};
	public final List<String> conqueredStructures = new ArrayList<>();

	public TFMagicMapData(int x, int z, byte scale, boolean trackpos, boolean unlimited, boolean locked, ResourceKey<Level> dim) {
		super(x, z, scale, trackpos, unlimited, locked, dim);
	}

	// [VanillaCopy] Adapted from World.getMapData
	@Nullable
	public static TFMagicMapData getMagicMapData(Level level, String name) {
		if (level instanceof ServerLevel serverLevel) return serverLevel.getServer().overworld().getDataStorage().get(type(name));
		else return CLIENT_DATA.get(name);
	}

	// Like the method above, but if we know we're on client
	@Nullable
	public static TFMagicMapData getClientMagicMapData(String name) {
		return CLIENT_DATA.get(name);
	}

	// [VanillaCopy] Adapted from World.registerMapData
	public static void registerMagicMapData(Level level, TFMagicMapData data, String id) {
		if (level instanceof ServerLevel serverLevel) serverLevel.getServer().overworld().getDataStorage().set(type(id), data);
		else CLIENT_DATA.put(id, data);
	}

	@Nullable
	@Override
	public Packet<?> getUpdatePacket(MapId mapId, Player player) {
		Packet<?> packet = super.getUpdatePacket(mapId, player);
		return packet instanceof ClientboundMapItemDataPacket mapItemDataPacket ? new ClientboundCustomPayloadPacket(new MagicMapPacket(mapItemDataPacket, this.conqueredStructures)) : packet;
	}

	public void addTFDecoration(Holder<MapDecorationType> decorationType, @Nullable LevelAccessor level, String id, double x, double z, double yRot, boolean conquered) {
		// Keep structure IDs internal-only; rendering this name in 1.21.11 shows debug-like text labels on the map.
		this.addDecoration(decorationType, level, id, x, z, yRot, null);
		MapDecoration deco = this.decorations.get(id);
		if (deco != null) {
			String conqueredID = MagicMapItem.makeName(decorationType, deco.x(), deco.y());
			if (conquered && !this.conqueredStructures.contains(conqueredID)) {
				this.conqueredStructures.add(conqueredID);
			} else if (!conquered) {
				this.conqueredStructures.remove(conqueredID);
			}
		}
	}

	public record DecorationHolder(String id, MapDecoration decoration) {
		public static final Codec<DecorationHolder> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.fieldOf("id").forGetter(DecorationHolder::id),
			Codecs.DECORATION_CODEC.fieldOf("decoration").forGetter(DecorationHolder::decoration)
		).apply(instance, DecorationHolder::new));
	}

	private static SavedDataType<TFMagicMapData> type(String id) {
		return TYPES.computeIfAbsent(id, key -> new SavedDataType<>(Identifier.withDefaultNamespace(key), () -> {
			throw new IllegalStateException("Should never create an empty map saved data");
		}, CODEC, DataFixTypes.SAVED_DATA_MAP_DATA));
	}

	private static <T> boolean readBoolean(DynamicOps<T> ops, T input, String key, boolean defaultValue) {
		return ops.get(input, key).flatMap(ops::getBooleanValue).result().orElse(defaultValue);
	}

	private static <T> List<String> readStringList(DynamicOps<T> ops, T input, String key) {
		return ops.get(input, key).flatMap(value -> Codec.STRING.listOf().parse(ops, value)).result().orElse(List.of());
	}
}
