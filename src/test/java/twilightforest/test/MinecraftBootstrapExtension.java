package twilightforest.test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.Map;

import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import twilightforest.init.TFDataComponents;
import twilightforest.init.TFMapDecorations;

public final class MinecraftBootstrapExtension implements BeforeAllCallback {
	private static final AtomicBoolean BOOTSTRAPPED = new AtomicBoolean(false);

	@Override
	public void beforeAll(ExtensionContext context) {
		if (BOOTSTRAPPED.compareAndSet(false, true)) {
			SharedConstants.tryDetectVersion();
			Bootstrap.bootStrap();
			registerIntoFrozenRegistry((MappedRegistry<?>) BuiltInRegistries.DATA_COMPONENT_TYPE, TFDataComponents::init);
			registerIntoFrozenRegistry((MappedRegistry<?>) BuiltInRegistries.MAP_DECORATION_TYPE, TFMapDecorations::init);
		}
	}

	private static void registerIntoFrozenRegistry(MappedRegistry<?> registry, Runnable registration) {
		try {
			// Plain Minecraft bootstrap freezes registries before Fabric entrypoints can add mod values.
			var frozen = MappedRegistry.class.getDeclaredField("frozen");
			frozen.setAccessible(true);
			boolean wasFrozen = frozen.getBoolean(registry);
			frozen.setBoolean(registry, false);
			try {
				registration.run();
				bindValuesAddedAfterVanillaFreeze(registry);
			} finally {
				frozen.setBoolean(registry, wasFrozen);
			}
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("Failed to prepare a static registry for mod test bootstrap", e);
		}
	}

	private static void bindValuesAddedAfterVanillaFreeze(MappedRegistry<?> registry) throws ReflectiveOperationException {
		var byValueField = MappedRegistry.class.getDeclaredField("byValue");
		byValueField.setAccessible(true);
		var bindValue = Holder.Reference.class.getDeclaredMethod("bindValue", Object.class);
		bindValue.setAccessible(true);
		for (Map.Entry<?, ?> entry : ((Map<?, ?>) byValueField.get(registry)).entrySet()) {
			Holder.Reference<?> holder = (Holder.Reference<?>) entry.getValue();
			if (!holder.isBound()) {
				bindValue.invoke(holder, entry.getKey());
			}
		}
	}
}
