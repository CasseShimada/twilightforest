package twilightforest.util;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class PlayerHelperTests {
	@Test
	void delegatesClientAdvancementLookup() {
		Player player = mock(Player.class, withSettings().extraInterfaces(PlayerHelper.ClientAdvancementAccess.class));
		PlayerHelper.ClientAdvancementAccess access = (PlayerHelper.ClientAdvancementAccess) player;
		Identifier advancementId = Identifier.parse("twilightforest:progress_lich");
		AdvancementHolder advancement = Advancement.Builder.advancement().build(advancementId);
		when(access.twilightforest$getAdvancement(advancementId)).thenReturn(advancement);

		assertSame(advancement, PlayerHelper.getAdvancement(player, advancementId));
		verify(access).twilightforest$getAdvancement(advancementId);
	}

	@Test
	void delegatesClientAdvancementProgress() {
		Player player = mock(Player.class, withSettings().extraInterfaces(PlayerHelper.ClientAdvancementAccess.class));
		PlayerHelper.ClientAdvancementAccess access = (PlayerHelper.ClientAdvancementAccess) player;
		AdvancementHolder advancement = Advancement.Builder.advancement().build(Identifier.parse("twilightforest:progress_lich"));
		when(access.twilightforest$hasAdvancement(advancement)).thenReturn(true);

		assertTrue(PlayerHelper.doesPlayerHaveRequiredAdvancement(player, advancement));
		verify(access).twilightforest$hasAdvancement(advancement);
	}
}
