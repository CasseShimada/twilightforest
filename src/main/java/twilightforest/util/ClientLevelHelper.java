package twilightforest.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class ClientLevelHelper {
	private ClientLevelHelper() {
	}

	@Nullable
	public static Level getClientLevel() {
		if (FabricLoader.getInstance().getEnvironmentType() != EnvType.CLIENT) {
			return null;
		}

		try {
			Class<?> minecraftClass = Class.forName("net.minecraft.client.Minecraft");
			Method getInstance = minecraftClass.getMethod("getInstance");
			Object minecraft = getInstance.invoke(null);
			try {
				Field levelField = minecraftClass.getField("level");
				return (Level) levelField.get(minecraft);
			} catch (NoSuchFieldException ignored) {
				Method getLevel = minecraftClass.getMethod("getLevel");
				return (Level) getLevel.invoke(minecraft);
			}
		} catch (Throwable ignored) {
			return null;
		}
	}
}
