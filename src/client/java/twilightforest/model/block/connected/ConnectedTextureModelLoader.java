package twilightforest.client.model.block.connected;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Pair;
import net.fabricmc.fabric.api.client.model.loading.v1.UnbakedModelDeserializer;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class ConnectedTextureModelLoader implements UnbakedModelDeserializer {
	public static final ConnectedTextureModelLoader INSTANCE = new ConnectedTextureModelLoader();

	@Override
	public UnbakedConnectedTextureModel deserialize(JsonObject jsonObject, JsonDeserializationContext deserializationContext) throws JsonParseException {
		JsonObject baseTextureInfo = GsonHelper.getAsJsonObject(jsonObject, "base", new JsonObject());
		int baseTintIndex = GsonHelper.getAsInt(baseTextureInfo, "tint_index", -1);
		int baseEmissivity = GsonHelper.getAsInt(baseTextureInfo, "emissivity", 0);

		Pair<Vector3f, Vector3f> element;
		if (jsonObject.has("element")) {
			JsonObject obj = jsonObject.getAsJsonObject("element");
			element = Pair.of(this.deserializeVec(obj, "from"), this.deserializeVec(obj, "to"));
		} else {
			element = Pair.of(new Vector3f(0, 0, 0), new Vector3f(16, 16, 16));
		}

		JsonObject overlayInfo = GsonHelper.getAsJsonObject(jsonObject, "connected_texture", new JsonObject());
		int tintIndex = GsonHelper.getAsInt(overlayInfo, "tint_index", -1);
		int emissivity = GsonHelper.getAsInt(overlayInfo, "emissivity", 0);
		boolean renderDisabled = GsonHelper.getAsBoolean(overlayInfo, "always_render_overlay", true);
		EnumSet<Direction> faces = this.parseEnabledFaces(overlayInfo, "faces");

		List<Block> connectables = this.parseConnectableBlocks(jsonObject);
		JsonObject stripped = jsonObject.deepCopy();
		stripped.remove("fabric:type");
		stripped.remove("loader");
		UnbakedModel baseModel = deserializationContext.deserialize(stripped, UnbakedModel.class);

		return new UnbakedConnectedTextureModel(
			baseModel,
			element,
			faces,
			renderDisabled,
			connectables,
			baseTintIndex,
			baseEmissivity,
			tintIndex,
			emissivity
		);
	}

	private EnumSet<Direction> parseEnabledFaces(JsonObject object, String key) {
		if (!object.has(key)) {
			return EnumSet.allOf(Direction.class);
		} else {
			EnumSet<Direction> faces = EnumSet.noneOf(Direction.class);

			for (JsonElement element : object.getAsJsonArray(key)) {
				Direction face = Direction.byName(element.getAsString());
				if (face == null) {
					throw new JsonParseException("Invalid face: " + element.getAsString());
				}

				faces.add(face);
			}

			return faces;
		}
	}

	private List<Block> parseConnectableBlocks(JsonObject object) {
		if (!object.has("connectable_blocks")) {
			return List.of();
		} else {
			List<Block> blocks = new ArrayList<>();

			for (JsonElement element : object.getAsJsonArray("connectable_blocks")) {
				if (element.getAsString().startsWith("#")) {
					Identifier tag = Identifier.tryParse(element.getAsString().substring(1));
					if (tag != null) {
						BuiltInRegistries.BLOCK.getTagOrEmpty(TagKey.create(Registries.BLOCK, tag)).forEach(blockHolder -> blocks.add(blockHolder.value()));
					} else {
						throw new JsonParseException("Invalid block tag: " + element.getAsString());
					}
				} else {
					Block block = BuiltInRegistries.BLOCK.getValue(Identifier.tryParse(element.getAsString()));
					if (block == Blocks.AIR) {
						throw new JsonParseException("Invalid block: " + element.getAsString());
					}
					blocks.add(block);
				}
			}

			return blocks;
		}
	}

	private Vector3f deserializeVec(JsonObject object, String name) {
		JsonArray from = object.getAsJsonArray(name);
		if (from.asList().size() == 3) {
			return new Vector3f(from.get(0).getAsFloat(), from.get(1).getAsFloat(), from.get(2).getAsFloat());
		}
		return new Vector3f(0, 0, 0);
	}
}
