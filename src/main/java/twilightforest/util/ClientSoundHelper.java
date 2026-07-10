package twilightforest.util;

import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.LevelAccessor;

public final class ClientSoundHelper {
	private ClientSoundHelper() {
	}

	public static void stopSound(LevelAccessor level, Identifier soundId, SoundSource source) {
		if (level instanceof Access access) {
			access.twilightforest$stopSound(soundId, source);
		}
	}

	public interface Access {
		void twilightforest$stopSound(Identifier soundId, SoundSource source);
	}
}
