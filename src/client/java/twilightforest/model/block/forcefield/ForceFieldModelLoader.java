package twilightforest.client.model.block.forcefield;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.fabricmc.fabric.api.client.model.loading.v1.UnbakedModelDeserializer;
import net.minecraft.client.resources.model.cuboid.CuboidModelElement;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ForceFieldModelLoader implements UnbakedModelDeserializer {
	public static final ForceFieldModelLoader INSTANCE = new ForceFieldModelLoader();

	@Override
	public UnbakedForceFieldModel deserialize(JsonObject json, JsonDeserializationContext context) throws JsonParseException {
		Map<CuboidModelElement, Condition> elementsAndConditions = new HashMap<>();

		if (json.has("elements")) {
			for (JsonElement jsonElement : GsonHelper.getAsJsonArray(json, "elements")) {
				ForceFieldModel.ExtraDirection direction = null;
				boolean b = false;
				List<ForceFieldModel.ExtraDirection> parents = new ArrayList<>();

				if (jsonElement instanceof JsonObject element) {
					if (element.get("condition") instanceof JsonObject condition) {
						direction = ForceFieldModel.ExtraDirection.byName(GsonHelper.getAsString(condition, "direction", "up"));
						b = GsonHelper.getAsBoolean(condition, "if", true);
						for (JsonElement parentElement : GsonHelper.getAsJsonArray(condition, "parents")) {
							parents.add(ForceFieldModel.ExtraDirection.byName(parentElement.getAsString()));
						}
					}
				}
				elementsAndConditions.put(context.deserialize(jsonElement, CuboidModelElement.class), new Condition(direction, b, parents));
			}
		}

		JsonObject stripped = json.deepCopy();
		stripped.remove("fabric:type");
		stripped.remove("loader");
		UnbakedModel baseModel = context.deserialize(stripped, UnbakedModel.class);

		return new UnbakedForceFieldModel(baseModel, elementsAndConditions);
	}

	public record Condition(@Nullable ForceFieldModel.ExtraDirection direction, boolean b, List<ForceFieldModel.ExtraDirection> parents) {
	}
}
