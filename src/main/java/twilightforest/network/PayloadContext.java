package twilightforest.network;

import net.minecraft.world.entity.player.Player;

/**
 * Minimal Fabric-facing payload context used by TF packets.
 *
 * <p>This replaces the previous payload handling context without pulling in a porting layer.</p>
 */
public interface PayloadContext {
	Player player();
	void enqueueWork(Runnable task);
}
