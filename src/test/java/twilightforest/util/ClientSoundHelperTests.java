package twilightforest.util;

import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.LevelAccessor;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.withSettings;

class ClientSoundHelperTests {
	@Test
	void ignoresLevelsWithoutClientAccess() {
		LevelAccessor level = mock(LevelAccessor.class);

		ClientSoundHelper.stopSound(level, Identifier.parse("twilightforest:cicada"), SoundSource.NEUTRAL);

		verifyNoInteractions(level);
	}

	@Test
	void delegatesToClientLevelAccess() {
		ClientSoundHelper.Access access = mock(ClientSoundHelper.Access.class, withSettings().extraInterfaces(LevelAccessor.class));
		Identifier soundId = Identifier.parse("twilightforest:cicada");

		ClientSoundHelper.stopSound((LevelAccessor) access, soundId, SoundSource.BLOCKS);

		verify(access).twilightforest$stopSound(soundId, SoundSource.BLOCKS);
	}
}
