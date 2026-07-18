package twilightforest.item.mapdata;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
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
import net.minecraft.world.level.storage.SavedDataStorage;
import net.minecraft.world.level.saveddata.maps.*;
import org.jetbrains.annotations.Nullable;
import twilightforest.init.TFMapDecorations;
import twilightforest.item.MagicMapItem;
import twilightforest.network.MagicMapPacket;
import twilightforest.util.Codecs;

import java.nio.ByteBuffer;
import java.nio.file.Path;
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
				DecodedDecorations storedDecorations = readDecorations(ops, input);
				for (DecorationHolder decoration : storedDecorations.values()) {
					tfdata.restoreDecoration(decoration);
				}
				tfdata.conqueredStructures.addAll(readStringList(ops, input, "conquered_structures"));
				tfdata.legacyFeatures = readByteArray(ops, input, "features");
				if (!storedDecorations.present() && tfdata.legacyFeatures.length > 0) {
					tfdata.importLegacyFeatures(tfdata.legacyFeatures);
					tfdata.setDirty();
				}

				return Pair.of(tfdata, pair.getSecond());
			});
		}

		@Override
		public <T> DataResult<T> encode(TFMagicMapData value, DynamicOps<T> ops, T prefix) {
			List<DecorationHolder> decorations = new ArrayList<>();
			value.decorations.forEach((id, decoration) -> {
				if (decoration.type().value().showOnItemFrame()) {
					decorations.add(new DecorationHolder(id, decoration));
				}
			});

			DataResult<T> result = MapItemSavedData.CODEC.encode(value, ops, prefix)
				.flatMap(tag -> DecorationHolder.CODEC.listOf().encodeStart(ops, decorations)
					.flatMap(list -> ops.mergeToMap(tag, ops.createString("decorations"), list)));
			if (!value.conqueredStructures.isEmpty()) {
				result = result.flatMap(tag -> Codec.STRING.listOf().encodeStart(ops, value.conqueredStructures)
					.flatMap(list -> ops.mergeToMap(tag, ops.createString("conquered_structures"), list)));
			}
			if (value.legacyFeatures.length > 0) {
				ByteBuffer bytes = ByteBuffer.wrap(value.legacyFeatures);
				result = result.flatMap(tag -> Codec.BYTE_BUFFER.encodeStart(ops, bytes)
					.flatMap(features -> ops.mergeToMap(tag, ops.createString("features"), features)));
			}
			return result;
		}
	};
	public final List<String> conqueredStructures = new ArrayList<>();
	private byte[] legacyFeatures = new byte[0];

	public TFMagicMapData(int x, int z, byte scale, boolean trackpos, boolean unlimited, boolean locked, ResourceKey<Level> dim) {
		super(x, z, scale, trackpos, unlimited, locked, dim);
	}

	// [VanillaCopy] Adapted from World.getMapData
	@Nullable
	public static TFMagicMapData getMagicMapData(Level level, String name) {
		if (level instanceof ServerLevel serverLevel) return LegacyMapSavedData.get(serverLevel, name, type(name), CODEC);
		else return CLIENT_DATA.get(name);
	}

	@Nullable
	static TFMagicMapData getMagicMapData(
		SavedDataStorage storage,
		Path currentDataFolder,
		Path legacyWorldDataFolder,
		HolderLookup.Provider registries,
		String name) {
		return LegacyMapSavedData.get(storage, currentDataFolder, legacyWorldDataFolder, registries, name, type(name), CODEC);
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

	private void restoreDecoration(DecorationHolder holder) {
		MapDecoration decoration = holder.decoration();
		MapDecoration previous = this.decorations.put(holder.id(), decoration);
		if (decoration.equals(previous)) {
			return;
		}
		if (previous != null && previous.type().value().trackCount()) {
			this.trackedDecorationCount--;
		}
		if (decoration.type().value().trackCount()) {
			this.trackedDecorationCount++;
		}
		this.setDecorationsDirty();
	}

	private void importLegacyFeatures(byte[] features) {
		if (features.length % 3 != 0) {
			throw new IllegalArgumentException("Legacy magic-map features length is not divisible by three: " + features.length);
		}
		for (int offset = 0; offset < features.length; offset += 3) {
			int featureInfo = Byte.toUnsignedInt(features[offset]);
			Holder<MapDecorationType> decorationType = legacyDecorationType(featureInfo & 0x7F);
			if (decorationType == null) {
				continue;
			}
			byte mapX = features[offset + 1];
			byte mapZ = features[offset + 2];
			int blocksPerPixel = 1 << Byte.toUnsignedInt(this.scale);
			int worldX = this.centerX + mapX * blocksPerPixel / 2;
			int worldZ = this.centerZ + mapZ * blocksPerPixel / 2;
			MapDecoration decoration = new MapDecoration(decorationType, mapX, mapZ, (byte) 8, java.util.Optional.empty());
			this.restoreDecoration(new DecorationHolder(MagicMapItem.makeName(decorationType, worldX, worldZ), decoration));
			if ((featureInfo & 0x80) != 0) {
				String conqueredId = MagicMapItem.makeName(decorationType, mapX, mapZ);
				if (!this.conqueredStructures.contains(conqueredId)) {
					this.conqueredStructures.add(conqueredId);
				}
			}
		}
	}

	@Nullable
	private static Holder<MapDecorationType> legacyDecorationType(int featureId) {
		return switch (featureId) {
			case 1 -> TFMapDecorations.SMALL_HOLLOW_HILL;
			case 2 -> TFMapDecorations.MEDIUM_HOLLOW_HILL;
			case 3 -> TFMapDecorations.LARGE_HOLLOW_HILL;
			case 4 -> TFMapDecorations.HEDGE_MAZE;
			case 5 -> TFMapDecorations.NAGA_COURTYARD;
			case 6 -> TFMapDecorations.LICH_TOWER;
			case 7 -> TFMapDecorations.AURORA_PALACE;
			case 9 -> TFMapDecorations.QUEST_GROVE;
			case 12 -> TFMapDecorations.HYDRA_LAIR;
			case 13 -> TFMapDecorations.LABYRINTH;
			case 14 -> TFMapDecorations.DARK_TOWER;
			case 15 -> TFMapDecorations.KNIGHT_STRONGHOLD;
			case 17 -> TFMapDecorations.YETI_LAIR;
			case 18 -> TFMapDecorations.TROLL_CAVES;
			case 19 -> TFMapDecorations.FINAL_CASTLE;
			default -> null;
		};
	}

	private static SavedDataType<TFMagicMapData> type(String id) {
		return TYPES.computeIfAbsent(id, key -> new SavedDataType<>(Identifier.withDefaultNamespace(key), () -> {
			throw new IllegalStateException("Should never create an empty map saved data");
		}, CODEC, DataFixTypes.SAVED_DATA_MAP_DATA));
	}

	private static <T> boolean readBoolean(DynamicOps<T> ops, T input, String key, boolean defaultValue) {
		return ops.get(input, key).result()
			.map(value -> ops.getBooleanValue(value).getOrThrow())
			.orElse(defaultValue);
	}

	private static <T> List<String> readStringList(DynamicOps<T> ops, T input, String key) {
		return ops.get(input, key).result()
			.map(value -> Codec.STRING.listOf().parse(ops, value).getOrThrow())
			.orElse(List.of());
	}

	private static <T> DecodedDecorations readDecorations(DynamicOps<T> ops, T input) {
		return ops.get(input, "decorations").result()
			.map(value -> new DecodedDecorations(true, DecorationHolder.CODEC.listOf().parse(ops, value).getOrThrow()))
			.orElseGet(() -> new DecodedDecorations(false, List.of()));
	}

	private static <T> byte[] readByteArray(DynamicOps<T> ops, T input, String key) {
		return ops.get(input, key).result().map(value -> {
			ByteBuffer buffer = Codec.BYTE_BUFFER.parse(ops, value).getOrThrow().duplicate();
			byte[] bytes = new byte[buffer.remaining()];
			buffer.get(bytes);
			return bytes;
		}).orElseGet(() -> new byte[0]);
	}

	private record DecodedDecorations(boolean present, List<DecorationHolder> values) {
	}
}
