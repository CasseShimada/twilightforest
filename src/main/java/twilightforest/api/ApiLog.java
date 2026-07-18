package twilightforest.api;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;

final class ApiLog {
	private static final Logger LOGGER = LogUtils.getLogger();

	private ApiLog() {
	}

	static void providerFailure(String extensionPoint, Identifier providerId, Throwable failure) {
		LOGGER.error("Twilight Forest API provider {} failed in {}; continuing with the next provider", providerId, extensionPoint, failure);
	}
}
