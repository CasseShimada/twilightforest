package twilightforest.mixin;

import net.minecraft.server.level.ServerEntity;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twilightforest.ASMHooks;
import twilightforest.entity.TFMultipartEntity;
import twilightforest.entity.TFPart;
import twilightforest.network.PacketDistributor;
import twilightforest.network.UpdateTFMultipartPacket;

@Mixin(ServerEntity.class)
public class ServerEntityMixin {
	@Shadow private Entity entity;

	@Inject(method = "sendDirtyEntityData", at = @At("TAIL"))
	private void twilightforest$sendMultipartUpdates(CallbackInfo ci) {
		ASMHooks.sendDirtyEntityData(this.entity);
	}

	@Inject(method = "sendChanges", at = @At("TAIL"))
	private void twilightforest$sendMultipartTickUpdates(CallbackInfo ci) {
		if (this.entity instanceof TFMultipartEntity) {
			TFPart.assignPartIDs(this.entity);
			PacketDistributor.sendToPlayersTrackingEntity(this.entity, new UpdateTFMultipartPacket(this.entity));
		}
	}
}
