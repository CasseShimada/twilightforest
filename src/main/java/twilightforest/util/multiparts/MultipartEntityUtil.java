package twilightforest.util.multiparts;

import net.minecraft.world.entity.Entity;
import twilightforest.network.PacketDistributor;
import twilightforest.entity.TFMultipartEntity;
import twilightforest.entity.TFPart;
import twilightforest.network.UpdateTFMultipartPacket;

public class MultipartEntityUtil {

	public Entity sendDirtyMultipartEntityData(Entity entity) {
		if (entity instanceof TFMultipartEntity) {
			TFPart.assignPartIDs(entity);
			PacketDistributor.sendToPlayersTrackingEntity(entity, new UpdateTFMultipartPacket(entity));
		}
		return entity;
	}

}
