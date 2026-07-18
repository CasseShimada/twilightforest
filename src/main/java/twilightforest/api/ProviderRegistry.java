package twilightforest.api;

import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Internal storage shared by the public API facades.
 */
final class ProviderRegistry<T> {
	private final String extensionPoint;
	private final Map<Identifier, T> registrations = new HashMap<>();
	private volatile List<Entry<T>> snapshot = List.of();
	private volatile boolean frozen;

	ProviderRegistry(String extensionPoint) {
		this.extensionPoint = Objects.requireNonNull(extensionPoint, "extensionPoint");
	}

	void register(Identifier id, T provider) {
		Objects.requireNonNull(id, "id");
		Objects.requireNonNull(provider, "provider");
		synchronized (this) {
			if (this.frozen) {
				throw new IllegalStateException("Registration for " + this.extensionPoint + " is already frozen: " + id);
			}
			if (this.registrations.putIfAbsent(id, provider) != null) {
				throw new IllegalArgumentException("Duplicate provider ID for " + this.extensionPoint + ": " + id);
			}
			this.rebuildSnapshot();
		}
	}

	void freeze() {
		synchronized (this) {
			if (!this.frozen) {
				this.rebuildSnapshot();
				this.frozen = true;
			}
		}
	}

	boolean isFrozen() {
		return this.frozen;
	}

	List<Entry<T>> entries() {
		return this.snapshot;
	}

	List<Identifier> ids() {
		return this.snapshot.stream().map(Entry::id).toList();
	}

	void resetForTests() {
		synchronized (this) {
			this.registrations.clear();
			this.snapshot = List.of();
			this.frozen = false;
		}
	}

	private void rebuildSnapshot() {
		List<Entry<T>> ordered = new ArrayList<>(this.registrations.size());
		this.registrations.forEach((id, provider) -> ordered.add(new Entry<>(id, provider)));
		ordered.sort((left, right) -> left.id().toString().compareTo(right.id().toString()));
		this.snapshot = List.copyOf(ordered);
	}

	record Entry<T>(Identifier id, T provider) {
		Entry {
			Objects.requireNonNull(id, "id");
			Objects.requireNonNull(provider, "provider");
		}
	}
}
