package twilightforest.util.multiparts;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;
import twilightforest.entity.TFMultipartEntity;
import twilightforest.entity.TFPart;
import twilightforest.network.PacketDistributor;
import twilightforest.network.UpdateTFMultipartPacket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Predicate;

public class MultipartEntityUtil {

	private static final Map<Level, Set<Entity>> TRACKED_MULTIPARTS = Collections.synchronizedMap(new WeakHashMap<>());

	public static void trackMultipartEntity(Entity entity) {
		if (!(entity instanceof TFMultipartEntity multipart) || multipart.getParts() == null) {
			return;
		}

		Set<Entity> trackedForLevel;
		synchronized (TRACKED_MULTIPARTS) {
			trackedForLevel = TRACKED_MULTIPARTS.computeIfAbsent(entity.level(), level -> Collections.newSetFromMap(new WeakHashMap<>()));
		}

		synchronized (trackedForLevel) {
			trackedForLevel.add(entity);
		}
	}

	public static List<Entity> injectTFPartEntities(Level level, @Nullable Entity except, AABB box, Predicate<? super Entity> predicate, List<Entity> entities) {
		Set<Entity> trackedForLevel = TRACKED_MULTIPARTS.get(level);
		if (trackedForLevel == null || trackedForLevel.isEmpty()) {
			return entities;
		}

		List<Entity> withParts = null;
		Set<Integer> seenIds = null;

		synchronized (trackedForLevel) {
			Iterator<Entity> iterator = trackedForLevel.iterator();
			while (iterator.hasNext()) {
				Entity parent = iterator.next();
				if (parent == null || parent.isRemoved() || parent.level() != level || !(parent instanceof TFMultipartEntity multipart)) {
					iterator.remove();
					continue;
				}

				TFPart<?>[] parts = multipart.getParts();
				if (parts == null) {
					continue;
				}

				for (TFPart<?> part : parts) {
					if (part == null || part == except || part.isRemoved() || !part.getBoundingBox().intersects(box) || !predicate.test(part)) {
						continue;
					}

					if (withParts == null) {
						withParts = new ArrayList<>(entities);
						seenIds = new HashSet<>(entities.size() + parts.length);
						for (Entity entity : entities) {
							seenIds.add(entity.getId());
						}
					}

					if (seenIds.add(part.getId())) {
						withParts.add(part);
					}
				}
			}
		}

		return withParts != null ? withParts : entities;
	}

	@Nullable
	public static Entity getEntityOrPart(Level level, int entityId) {
		Set<Entity> trackedForLevel = TRACKED_MULTIPARTS.get(level);
		if (trackedForLevel == null || trackedForLevel.isEmpty()) {
			return null;
		}

		synchronized (trackedForLevel) {
			Iterator<Entity> iterator = trackedForLevel.iterator();
			while (iterator.hasNext()) {
				Entity parent = iterator.next();
				if (parent == null || parent.isRemoved() || parent.level() != level || !(parent instanceof TFMultipartEntity multipart)) {
					iterator.remove();
					continue;
				}

				TFPart<?>[] parts = multipart.getParts();
				if (parts == null) {
					continue;
				}

				for (TFPart<?> part : parts) {
					if (part != null && !part.isRemoved() && part.getId() == entityId) {
						return part;
					}
				}
			}
		}

		return null;
	}

	public Entity sendDirtyMultipartEntityData(Entity entity) {
		if (entity instanceof TFMultipartEntity) {
			TFPart.assignPartIDs(entity);
			PacketDistributor.sendToPlayersTrackingEntity(entity, new UpdateTFMultipartPacket(entity));
		}
		return entity;
	}
}
