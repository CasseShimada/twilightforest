package twilightforest.client.model.entity;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.rendertype.RenderTypes;

public class MistWolfModel extends HostileWolfModel {

	public MistWolfModel(ModelPart root) {
		super(RenderTypes::entityTranslucent, root);
	}
}
