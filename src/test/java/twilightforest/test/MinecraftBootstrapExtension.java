package twilightforest.test;

import java.util.concurrent.atomic.AtomicBoolean;

import net.minecraft.SharedConstants;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import twilightforest.init.TFDataComponents;

public final class MinecraftBootstrapExtension implements BeforeAllCallback {
	private static final AtomicBoolean BOOTSTRAPPED = new AtomicBoolean(false);

	@Override
	public void beforeAll(ExtensionContext context) {
		if (BOOTSTRAPPED.compareAndSet(false, true)) {
			SharedConstants.tryDetectVersion();
			Bootstrap.bootStrap();
			registerDataComponents();
		}
	}

	private static void registerDataComponents() {
		MappedRegistry<?> registry = (MappedRegistry<?>) BuiltInRegistries.DATA_COMPONENT_TYPE;
		try {
			// Plain Minecraft bootstrap freezes registries before Fabric entrypoints can add mod values.
			var frozen = MappedRegistry.class.getDeclaredField("frozen");
			frozen.setAccessible(true);
			boolean wasFrozen = frozen.getBoolean(registry);
			frozen.setBoolean(registry, false);
			try {
				TFDataComponents.init();
			} finally {
				frozen.setBoolean(registry, wasFrozen);
			}
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("Failed to prepare the data component registry for mod test bootstrap", e);
		}
	}
}
