package twilightforest.mixin.client.accessor;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Hud.class)
public interface HudInvoker {
	@Invoker("canRenderCrosshairForSpectator")
	boolean twilightforest$canRenderCrosshairForSpectator(HitResult hitResult);

	@Invoker("extractFood")
	void twilightforest$extractFood(GuiGraphicsExtractor graphics, Player player, int yLineBase, int xRight);
}
