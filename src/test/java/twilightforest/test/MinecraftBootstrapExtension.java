package twilightforest.test;

import java.util.concurrent.atomic.AtomicBoolean;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public final class MinecraftBootstrapExtension implements BeforeAllCallback {
	private static final AtomicBoolean BOOTSTRAPPED = new AtomicBoolean(false);

	@Override
	public void beforeAll(ExtensionContext context) {
		if (BOOTSTRAPPED.compareAndSet(false, true)) {
			SharedConstants.tryDetectVersion();
			Bootstrap.bootStrap();
		}
	}
}
