package twilightforest.mixin;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twilightforest.entity.TFMultipartEntity;
import twilightforest.entity.TFPart;
import twilightforest.network.UpdateTFMultipartPacket;
import twilightforest.util.multiparts.MultipartEntityUtil;

@Mixin(ServerEntity.class)
public class ServerEntityMixin {
	@Shadow private Entity entity;

	@Inject(method = "sendDirtyEntityData", at = @At("TAIL"))
	private void twilightforest$sendMultipartUpdates(CallbackInfo ci) {
		MultipartEntityUtil.sendDirtyMultipartEntityData(this.entity);
	}

	@Inject(method = "sendChanges", at = @At("TAIL"))
	private void twilightforest$sendMultipartTickUpdates(CallbackInfo ci) {
		if (this.entity instanceof TFMultipartEntity) {
			TFPart.assignPartIDs(this.entity);
			UpdateTFMultipartPacket packet = new UpdateTFMultipartPacket(this.entity);
			PlayerLookup.tracking(this.entity).forEach(player -> ServerPlayNetworking.send(player, packet));
		}
	}
}
