package twilightforest.util.registry;

import net.minecraft.resources.Identifier;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Minimal DeferredHolder replacement used to decouple the mod codebase from loader-specific helpers.
 *
 * <p>Values are created and registered when {@link DeferredRegister#register()} is called.</p>
 */
public class DeferredHolder<R, T extends R> implements Supplier<T> {
	private final Identifier id;
	final Supplier<? extends T> factory; // package-private for DeferredRegister
	private T value;

	DeferredHolder(Identifier id, Supplier<? extends T> factory) {
		this.id = Objects.requireNonNull(id, "id");
		this.factory = Objects.requireNonNull(factory, "factory");
	}

	public Identifier getId() {
		return id;
	}

	public T value() {
		return this.get();
	}

	@Override
	public T get() {
		if (value == null) throw new IllegalStateException("Deferred value not registered yet: " + id);
		return value;
	}

	void bind(T value) {
		this.value = Objects.requireNonNull(value, "value");
	}
}
