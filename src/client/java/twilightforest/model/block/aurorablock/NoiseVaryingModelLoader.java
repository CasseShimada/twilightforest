package twilightforest.client.model.block.aurorablock;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.fabricmc.fabric.api.client.model.loading.v1.UnbakedModelDeserializer;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.util.GsonHelper;

public class NoiseVaryingModelLoader implements UnbakedModelDeserializer {
	public static final NoiseVaryingModelLoader INSTANCE = new NoiseVaryingModelLoader();

	@Override
	public UnbakedNoiseVaryingModel deserialize(JsonObject json, JsonDeserializationContext context) throws JsonParseException {
		JsonArray variantsArray = GsonHelper.getAsJsonArray(json, "variants");
		String[] variants = new String[variantsArray.size()];
		for (int i = 0; i < variantsArray.size(); i++) {
			variants[i] = variantsArray.get(i).getAsString();
		}

		JsonObject stripped = json.deepCopy();
		stripped.remove("fabric:type");
		stripped.remove("loader");
		UnbakedModel baseModel = context.deserialize(stripped, UnbakedModel.class);

		return new UnbakedNoiseVaryingModel(baseModel, variants);
	}
}
