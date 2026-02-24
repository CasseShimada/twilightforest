package twilightforest.client.model.block.giantblock;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.fabricmc.fabric.api.client.model.loading.v1.UnbakedModelDeserializer;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.util.GsonHelper;

public class GiantBlockModelLoader implements UnbakedModelDeserializer {
	public static final GiantBlockModelLoader INSTANCE = new GiantBlockModelLoader();

	@Override
	public UnbakedGiantBlockModel deserialize(JsonObject object, JsonDeserializationContext deserializationContext) throws JsonParseException {
		JsonObject stripped = object.deepCopy();
		stripped.remove("fabric:type");
		stripped.remove("loader");
		BlockModel baseModel = deserializationContext.deserialize(stripped, BlockModel.class);

		return new UnbakedGiantBlockModel(baseModel);
	}
}
