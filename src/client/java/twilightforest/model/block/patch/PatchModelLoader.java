package twilightforest.client.model.block.patch;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.fabricmc.fabric.api.client.model.loading.v1.UnbakedModelDeserializer;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.util.GsonHelper;

public class PatchModelLoader implements UnbakedModelDeserializer {
	public static final PatchModelLoader INSTANCE = new PatchModelLoader();

	@Override
	public UnbakedPatchModel deserialize(JsonObject object, JsonDeserializationContext context) throws JsonParseException {
		boolean shaggify = GsonHelper.getAsBoolean(object, "shaggify", false);

		JsonObject stripped = object.deepCopy();
		stripped.remove("fabric:type");
		stripped.remove("loader");
		UnbakedModel baseModel = context.deserialize(stripped, UnbakedModel.class);

		return new UnbakedPatchModel(baseModel, shaggify);
	}
}
