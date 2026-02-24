package twilightforest.world;

import com.google.common.collect.Maps;
import net.minecraft.core.BlockPos;
import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class TeleporterCache extends SavedData {

	private static final Codec<TeleporterCache> CODEC = CompoundTag.CODEC.xmap(TeleporterCache::load, cache -> cache.save(new CompoundTag()));
	private static final SavedDataType<TeleporterCache> TYPE = new SavedDataType<>("twilightforest_teleporter_cache", TeleporterCache::new, CODEC, DataFixTypes.SAVED_DATA_COMMAND_STORAGE);

	// destinationCoordinateCache is (src -> dest) [DestWorld, [SrcPos, DestPos]]
	private final Map<Identifier, Map<ColumnPos, TFTeleporter.PortalPosition>> destinationCoordinateCache = new HashMap<>();

	private TeleporterCache() {
		this.setDirty();
	}

	public static TeleporterCache get(ServerLevel level) {
		ServerLevel server = level.getServer().overworld();
		DimensionDataStorage storage = server.getDataStorage();
		return storage.computeIfAbsent(TeleporterCache.TYPE);
	}

	public void addBlockToCache(Identifier dimension, ColumnPos columnPos, TFTeleporter.PortalPosition position) {
		this.destinationCoordinateCache.putIfAbsent(dimension, Maps.newHashMapWithExpectedSize(4096));
		this.destinationCoordinateCache.get(dimension).put(columnPos, position);
		this.setDirty();
	}

	@Nullable
	public TFTeleporter.PortalPosition getPortalPosition(Identifier dimension, ColumnPos pos) {
		if (this.destinationCoordinateCache.containsKey(dimension)) {
			return this.destinationCoordinateCache.get(dimension).get(pos);
		}
		return null;
	}

	public void removeInvalidPos(Identifier dimension, ColumnPos pos) {
		this.destinationCoordinateCache.get(dimension).remove(pos);
		this.setDirty();
	}

	public CompoundTag save(CompoundTag tag) {
		ListTag dcc = new ListTag();
		this.destinationCoordinateCache.forEach((rl, map) -> {
			CompoundTag ct = new CompoundTag();
			ListTag links = new ListTag();
			map.forEach((columnPos, portalPos) -> {
				CompoundTag link = new CompoundTag();
				CompoundTag column = new CompoundTag();
				column.putInt("x", columnPos.x());
				column.putInt("z", columnPos.z());
				link.put("column", column);
				CompoundTag portal = new CompoundTag();
				portal.putLong("time", portalPos.lastUpdateTime);
				portal.putLong("pos", portalPos.pos.asLong());
				link.put("portal", portal);
				links.add(link);
			});
			ct.put("links", links);
			ct.putString("name", rl.toString());
			dcc.add(ct);
		});
		tag.put("dest", dcc);
		return tag;
	}

	public static TeleporterCache load(CompoundTag tag) {
		TeleporterCache cache = new TeleporterCache();
		tag.getListOrEmpty("dest").stream().map(CompoundTag.class::cast).forEach(dest -> {
			Identifier name = Identifier.parse(dest.getStringOr("name", ""));
			cache.destinationCoordinateCache.putIfAbsent(name, Maps.newHashMapWithExpectedSize(4096));
			dest.getListOrEmpty("links").stream().map(CompoundTag.class::cast).forEach(link -> {
				CompoundTag column = link.getCompoundOrEmpty("column");
				CompoundTag portal = link.getCompoundOrEmpty("portal");
				cache.destinationCoordinateCache.get(name).put(new ColumnPos(column.getIntOr("x", 0), column.getIntOr("z", 0)), new TFTeleporter.PortalPosition(BlockPos.of(portal.getLongOr("pos", 0L)), portal.getLongOr("time", 0L)));
			});
		});
		return cache;
	}
}
