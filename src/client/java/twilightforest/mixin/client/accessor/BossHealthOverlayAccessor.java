package twilightforest.mixin.client.accessor;

import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import java.util.UUID;

@Mixin(BossHealthOverlay.class)
public interface BossHealthOverlayAccessor {
	@Accessor("OVERLAY_BACKGROUND_SPRITES")
	static Identifier[] twilightforest$getOverlayBackgroundSprites() {
		throw new AssertionError("Mixin accessor");
	}

	@Accessor("OVERLAY_PROGRESS_SPRITES")
	static Identifier[] twilightforest$getOverlayProgressSprites() {
		throw new AssertionError("Mixin accessor");
	}

	@Accessor("events")
	Map<UUID, LerpingBossEvent> twilightforest$getEvents();
}
