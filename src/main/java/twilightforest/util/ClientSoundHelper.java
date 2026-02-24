package twilightforest.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;

import java.lang.reflect.Method;

public final class ClientSoundHelper {
	private ClientSoundHelper() {
	}

	public static void stopSound(Identifier soundId, SoundSource source) {
		if (FabricLoader.getInstance().getEnvironmentType() != EnvType.CLIENT) {
			return;
		}

		try {
			Class<?> minecraftClass = Class.forName("net.minecraft.client.Minecraft");
			Method getInstance = minecraftClass.getMethod("getInstance");
			Object minecraft = getInstance.invoke(null);
			Method getSoundManager = minecraftClass.getMethod("getSoundManager");
			Object soundManager = getSoundManager.invoke(minecraft);
			Method stop = soundManager.getClass().getMethod("stop", Identifier.class, SoundSource.class);
			stop.invoke(soundManager, soundId, source);
		} catch (Throwable ignored) {
		}
	}
}
