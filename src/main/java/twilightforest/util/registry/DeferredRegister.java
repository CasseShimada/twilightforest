package twilightforest.util.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.block.Block;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twilightforest.mixin.accessor.PoiTypesInvoker;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Minimal DeferredRegister replacement used to decouple the mod codebase from loader-specific helpers.
 *
 * <p>This is intentionally small: it supports the registration patterns used by Twilight Forest
 * and avoids pulling in an external porting library.</p>
 */
public class DeferredRegister<R> {
	private static final Logger LOGGER = LoggerFactory.getLogger("twilightforest");

	protected final ResourceKey<? extends Registry<R>> registryKey;
	protected final String modId;
	protected final Map<String, DeferredHolder<R, ? extends R>> entries = new LinkedHashMap<>();
	protected final Map<Identifier, Identifier> aliases = new LinkedHashMap<>();
	protected boolean registered;

	protected DeferredRegister(ResourceKey<? extends Registry<R>> registryKey, String modId) {
		this.registryKey = Objects.requireNonNull(registryKey, "registryKey");
		this.modId = Objects.requireNonNull(modId, "modId");
	}

	public static <R> DeferredRegister<R> create(ResourceKey<? extends Registry<R>> registryKey, String modId) {
		return new DeferredRegister<>(registryKey, modId);
	}

	public static Blocks createBlocks(String modId) {
		return new Blocks(modId);
	}

	public static Items createItems(String modId) {
		return new Items(modId);
	}

	public <T extends R> DeferredHolder<R, T> register(String name, Supplier<? extends T> factory) {
		if (registered) throw new IllegalStateException("Cannot register new entries after registry has been frozen: " + registryKey.identifier());
		var id = Identifier.fromNamespaceAndPath(modId, name);
		var holder = new DeferredHolder<R, T>(id, factory);
		entries.put(name, holder);
		return holder;
	}

	public java.util.Collection<DeferredHolder<R, ? extends R>> getEntries() {
		return java.util.Collections.unmodifiableCollection(entries.values());
	}

	public void addAlias(Identifier from, Identifier to) {
		if (registered) throw new IllegalStateException("Cannot add aliases after registry has been frozen: " + registryKey.identifier());
		aliases.put(from, to);
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public void register() {
		if (registered) return;
		registered = true;

		Registry<R> registry = (Registry<R>) BuiltInRegistries.REGISTRY.getValue(registryKey.identifier());
		if (registry == null) throw new IllegalStateException("Unknown registry key: " + registryKey.identifier());

		for (DeferredHolder<R, ? extends R> holder : entries.values()) {
			R value = (R) holder.factory.get();
			ResourceKey<R> valueKey = ResourceKey.create(registryKey, holder.getId());
			R registeredValue;
			if (registryKey.equals(Registries.POINT_OF_INTEREST_TYPE) && value instanceof PoiType poiType) {
				Holder.Reference<R> reference = Registry.registerForHolder(registry, valueKey, value);
				registeredValue = reference.value();
				PoiTypesInvoker.twilightforest$registerBlockStates((Holder<PoiType>) (Holder<?>) reference, poiType.matchingStates());
			} else {
				registeredValue = Registry.register(registry, holder.getId(), value);
			}
			((DeferredHolder) holder).bind(registeredValue);
		}
		applyAliases(registry);
	}

	@SuppressWarnings("unchecked")
	private void applyAliases(Registry<R> registry) {
		if (aliases.isEmpty()) return;
		if (registryKey.identifier().equals(Registries.BLOCK.identifier())) {
			LOGGER.warn("Skipping block registry aliases on Fabric to avoid duplicate block state ids.");
			return;
		}
		if (registryKey.identifier().equals(Registries.ITEM.identifier())) {
			LOGGER.warn("Skipping item registry aliases on Fabric to avoid registry sync duplicates.");
			return;
		}
		if (registryKey.identifier().equals(Registries.ENTITY_TYPE.identifier())) {
			LOGGER.warn("Skipping entity type registry aliases on Fabric to avoid registry sync duplicates.");
			return;
		}
		boolean skipMissingTargets = registryKey.identifier().equals(Registries.ITEM.identifier());
		if (!(registry instanceof MappedRegistry<R> mapped)) {
			LOGGER.warn("Registry aliasing is not supported for {}", registryKey.identifier());
			return;
		}

		try {
			Map<Identifier, Holder.Reference<R>> byLocation = resolveMap(mapped, "byLocation", Identifier.class, Holder.Reference.class);
			Map<ResourceKey<R>, Holder.Reference<R>> byKey = resolveMap(mapped, "byKey", ResourceKey.class, Holder.Reference.class);
			Map<ResourceKey<R>, RegistrationInfo> registrationInfos = resolveMap(mapped, "registrationInfos", ResourceKey.class, RegistrationInfo.class);
			if (byLocation == null || byKey == null || registrationInfos == null) {
				LOGGER.warn("Registry aliasing is not supported for {}", registryKey.identifier());
				return;
			}

			for (Map.Entry<Identifier, Identifier> entry : aliases.entrySet()) {
				Identifier from = entry.getKey();
				if (byLocation.containsKey(from)) continue;
				Identifier to = entry.getValue();
				Holder.Reference<R> target = byLocation.get(to);
				if (target == null) {
					if (!skipMissingTargets) {
						LOGGER.warn("Alias target {} missing in registry {}", to, registryKey.identifier());
					}
					continue;
				}
				ResourceKey<R> fromKey = ResourceKey.create(registryKey, from);
				byLocation.put(from, target);
				byKey.put(fromKey, target);
				RegistrationInfo info = registrationInfos.get(target.key());
				if (info != null) {
					registrationInfos.put(fromKey, info);
				}
			}
		} catch (ReflectiveOperationException e) {
			LOGGER.warn("Failed applying aliases for {}", registryKey.identifier(), e);
		}
	}

	@SuppressWarnings("unchecked")
	private static <K, V> Map<K, V> resolveMap(MappedRegistry<?> registry, String nameHint, Class<?> keyClass, Class<?> valueClass) throws ReflectiveOperationException {
		try {
			var field = MappedRegistry.class.getDeclaredField(nameHint);
			field.setAccessible(true);
			return (Map<K, V>) field.get(registry);
		} catch (NoSuchFieldException ignored) {
			for (var field : MappedRegistry.class.getDeclaredFields()) {
				if (!Map.class.isAssignableFrom(field.getType())) {
					continue;
				}
				field.setAccessible(true);
				Object value = field.get(registry);
				if (!(value instanceof Map<?, ?> map) || map.isEmpty()) {
					continue;
				}
				var entry = map.entrySet().iterator().next();
				if (keyClass.isInstance(entry.getKey()) && valueClass.isInstance(entry.getValue())) {
					return (Map<K, V>) map;
				}
			}
			return null;
		}
	}

	public static final class Blocks extends DeferredRegister<Block> {
		private Blocks(String modId) {
			super(Registries.BLOCK, modId);
		}

		public <T extends Block> DeferredBlock<T> register(String name, Supplier<? extends T> factory) {
			if (super.registered) throw new IllegalStateException("Cannot register new entries after registry has been frozen: " + super.registryKey.identifier());
			var id = Identifier.fromNamespaceAndPath(super.modId, name);
			DeferredBlock<T> holder = new DeferredBlock<>(id, factory);
			super.entries.put(name, holder);
			return holder;
		}
	}

	public static final class Items extends DeferredRegister<Item> {
		private Items(String modId) {
			super(Registries.ITEM, modId);
		}

		public <T extends Item> DeferredItem<T> register(String name, Supplier<? extends T> factory) {
			if (super.registered) throw new IllegalStateException("Cannot register new entries after registry has been frozen: " + super.registryKey.identifier());
			var id = Identifier.fromNamespaceAndPath(super.modId, name);
			DeferredItem<T> holder = new DeferredItem<>(id, factory);
			super.entries.put(name, holder);
			return holder;
		}
	}
}
