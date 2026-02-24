package twilightforest.client.model.generators.template;

import com.google.gson.JsonObject;
import net.minecraft.resources.Identifier;

public abstract class CustomLoaderBuilder {
	private final Identifier loaderId;
	@SuppressWarnings("unused")
	private final boolean optional;

	protected CustomLoaderBuilder(Identifier loaderId, boolean optional) {
		this.loaderId = loaderId;
		this.optional = optional;
	}

	public JsonObject toJson(JsonObject json) {
		json.addProperty("fabric:type", loaderId.toString());
		return json;
	}

	protected abstract CustomLoaderBuilder copyInternal();
}
