package twilightforest.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/** Validates the client-resource union as a graph, including indirect model and texture edges. */
class ClientResourceGraphTests {
	private static final List<Path> ROOTS = List.of(
		Path.of("src/main/resources"),
		Path.of("src/generated/resources"),
		Path.of("src/generated/fabric"),
		Path.of("src/client/resources"));
	private static final Set<String> LEGAL_BUILTIN_MODELS = Set.of("minecraft:builtin/generated");
	private static final Set<String> CUSTOM_BLOCK_MODEL_TYPES = Set.of(
		"twilightforest:connected_texture_block", "twilightforest:noise_varying", "twilightforest:patch",
		"twilightforest:force_field", "twilightforest:giant_block");
	private static final Set<String> CUSTOM_ITEM_TYPES = Set.of(
		"twilightforest:travellers_gear", "twilightforest:tf_chest", "twilightforest:skull_chest",
		"twilightforest:keepsake_casket", "twilightforest:candelabra", "twilightforest:brazier", "twilightforest:cicada",
		"twilightforest:firefly", "twilightforest:moonworm", "twilightforest:mason_jar",
		"twilightforest:skull_candle", "twilightforest:boss_trophy", "twilightforest:mystic_crown",
		"twilightforest:knightmetal_shield", "twilightforest:natural_dimension", "twilightforest:potion_flask",
		"twilightforest:moonworm_queen_pulse", "twilightforest:ore_meter_flash",
		"twilightforest:potion_flask_dosage", "twilightforest:potion_flask_damage",
		"twilightforest:experiment_115_variant");
	private static final Map<String, Path> RESOURCES = new TreeMap<>();
	private static final Map<String, JsonObject> MODELS = new HashMap<>();
	private static final Set<String> ENTRY_MODELS = new HashSet<>();

	@BeforeAll
	static void loadResourceUnion() throws IOException {
		Map<String, List<Path>> duplicates = new TreeMap<>();
		for (Path root : ROOTS) {
			if (!Files.isDirectory(root)) {
				continue;
			}
			try (var paths = Files.walk(root)) {
				paths.filter(Files::isRegularFile).forEach(file -> {
					String relative = root.relativize(file).toString().replace('\\', '/');
					Path previous = RESOURCES.putIfAbsent(relative, file);
					if (previous != null) {
						duplicates.computeIfAbsent(relative, ignored -> new ArrayList<>(List.of(previous))).add(file);
					}
				});
			}
		}
		assertTrue(duplicates.isEmpty(), () -> "duplicate resource paths in final union: " + duplicates);
		for (Map.Entry<String, Path> entry : RESOURCES.entrySet()) {
			String path = entry.getKey();
			if (path.matches("assets/[^/]+/models/.+\\.json")) {
				String[] segments = path.split("/", 4);
				String id = segments[1] + ":" + segments[3].substring(0, segments[3].length() - 5);
				MODELS.put(id, readObject(entry.getValue()));
			}
		}
		for (Map.Entry<String, Path> entry : RESOURCES.entrySet()) {
			if (!entry.getKey().matches("assets/twilightforest/(?:items|blockstates)/.+\\.json")) {
				continue;
			}
			walk(readObject(entry.getValue()), (key, value) -> {
				if ((key.equals("model") || key.equals("base")) && value.isJsonPrimitive()
					&& value.getAsJsonPrimitive().isString()) {
					String model = normalize(value.getAsString());
					if (model.startsWith("twilightforest:")) {
						ENTRY_MODELS.add(model);
					}
				}
			});
		}
	}

	@Test
	void everyDeclaredItemHasAnItemDefinitionAndEveryDefinitionHasAModel() throws IOException {
		Set<String> definitions = new HashSet<>();
		for (String path : RESOURCES.keySet()) {
			if (path.matches("assets/twilightforest/items/[^/]+\\.json")) {
				definitions.add(path.substring(path.lastIndexOf('/') + 1, path.length() - 5));
				JsonObject definition = readObject(RESOURCES.get(path));
				assertTrue(definition.has("model") && definition.get("model").isJsonObject(), path + " has no item model object");
				validateItemModelGraph(definition.getAsJsonObject("model"), path);
			}
		}

		assertTrue(definitions.size() >= 650, "unexpectedly small item-definition set: " + definitions.size());
		Set<String> declaredItems = new HashSet<>();
		collectLiteralIds(Path.of("src/main/java/twilightforest/init/TFItems.java"),
			Pattern.compile("\\bregister\\(\"([^\"]+)\""), declaredItems);
		collectLiteralIds(Path.of("src/main/java/twilightforest/init/TFBlocks.java"),
			Pattern.compile("\\b(?:registerDirectWithItem|registerDirectWithTooltipItem|registerDirectFireResistantItem|registerDirectDoubleBlockItem|registerWroughtFence)\\(\"([^\"]+)\""), declaredItems);
		Set<String> missing = new TreeSet<>(declaredItems);
		missing.removeAll(definitions);
		assertTrue(missing.isEmpty(), () -> "registered items without item definitions: " + missing);
	}

	@Test
	void everyBlockstateModelEdgeResolves() throws IOException {
		int blockstates = 0;
		for (Map.Entry<String, Path> entry : RESOURCES.entrySet()) {
			if (!entry.getKey().matches("assets/twilightforest/blockstates/.+\\.json")) {
				continue;
			}
			blockstates++;
			JsonObject blockstate = readObject(entry.getValue());
			walk(blockstate, (key, value) -> {
				if (key.equals("model") && value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
					validateModelReference(value.getAsString(), entry.getKey());
				}
				if (key.equals("fabric:type") && value.isJsonPrimitive() && value.getAsString().startsWith("twilightforest:")) {
					assertTrue(CUSTOM_BLOCK_MODEL_TYPES.contains(value.getAsString()),
						() -> entry.getKey() + " uses an unregistered blockstate model type " + value.getAsString());
				}
			});
		}
		assertTrue(blockstates >= 500, "unexpectedly small blockstate set: " + blockstates);
	}

	@Test
	void everyModelParentTextureAndCustomLoaderEdgeResolves() {
		assertTrue(MODELS.size() >= 2500, "unexpectedly small model set: " + MODELS.size());
		for (String model : MODELS.keySet()) {
			validateModel(model, new HashSet<>());
		}
	}

	private static void validateItemModelGraph(JsonObject root, String source) {
		walk(root, (key, value) -> {
			if ((key.equals("model") || key.equals("base")) && value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
				validateModelReference(value.getAsString(), source);
			}
			if (key.equals("type") && value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
				String type = value.getAsString();
				if (type.startsWith("twilightforest:")) {
					assertTrue(CUSTOM_ITEM_TYPES.contains(type), () -> source + " uses an unregistered item model/property type " + type);
				}
			}
		});
	}

	private static void validateModelReference(String rawId, String source) {
		String id = normalize(rawId);
		if (id.startsWith("minecraft:builtin/")) {
			assertTrue(LEGAL_BUILTIN_MODELS.contains(id), () -> source + " references a builtin model unavailable in 26.2: " + id);
			return;
		}
		if (id.startsWith("twilightforest:")) {
			assertTrue(MODELS.containsKey(id), () -> source + " references missing model " + id);
		}
	}

	private static void validateModel(String modelId, Set<String> visiting) {
		if (!visiting.add(modelId)) {
			fail("model parent cycle: " + visiting + " -> " + modelId);
		}
		JsonObject model = MODELS.get(modelId);
		if (model == null) {
			return;
		}

		Map<String, String> textures = new LinkedHashMap<>();
		List<JsonObject> chain = new ArrayList<>();
		String current = modelId;
		Set<String> parentChain = new HashSet<>();
		while (current != null && MODELS.containsKey(current)) {
			if (!parentChain.add(current)) {
				fail("model parent cycle: " + parentChain + " -> " + current);
			}
			JsonObject currentModel = MODELS.get(current);
			chain.add(currentModel);
			if (currentModel.has("textures") && currentModel.get("textures").isJsonObject()) {
				for (Map.Entry<String, JsonElement> texture : currentModel.getAsJsonObject("textures").entrySet()) {
					if (texture.getValue().isJsonPrimitive() && texture.getValue().getAsJsonPrimitive().isString()) {
						textures.putIfAbsent(texture.getKey(), texture.getValue().getAsString());
					}
				}
			}
			String parent = currentModel.has("parent") ? normalize(currentModel.get("parent").getAsString()) : null;
			if (parent != null) {
				validateModelReference(parent, current);
			}
			current = parent;
		}

		for (JsonObject modelInChain : chain) {
			walk(modelInChain, (key, value) -> {
			if (key.equals("fabric:type") && value.isJsonPrimitive() && value.getAsString().startsWith("twilightforest:")) {
				assertTrue(CUSTOM_BLOCK_MODEL_TYPES.contains(value.getAsString()),
					() -> modelId + " uses an unregistered custom model loader " + value.getAsString());
			}
			if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) {
				return;
			}
			String reference = value.getAsString();
			if (reference.startsWith("#")) {
				String resolved = resolveTexture(reference.substring(1), textures, new HashSet<>());
				if (ENTRY_MODELS.contains(modelId)) {
					assertFalse(resolved == null, () -> modelId + " has unresolved texture variable " + reference);
				}
				if (resolved != null) validateTexture(resolved, modelId);
			} else if (key.equals("texture") || key.equals("particle")) {
				validateTexture(reference, modelId);
			}
			});
		}
		for (String texture : textures.values()) {
			if (!texture.startsWith("#")) {
				validateTexture(texture, modelId);
			}
		}
		visiting.remove(modelId);
	}

	private static String resolveTexture(String name, Map<String, String> textures, Set<String> resolving) {
		if (!resolving.add(name)) {
			return null;
		}
		String value = textures.get(name);
		if (value == null) {
			return null;
		}
		return value.startsWith("#") ? resolveTexture(value.substring(1), textures, resolving) : value;
	}

	private static void validateTexture(String rawId, String source) {
		if (rawId.isBlank() || rawId.startsWith("#")) {
			return;
		}
		String id = normalize(rawId);
		int separator = id.indexOf(':');
		if (!id.substring(0, separator).equals("twilightforest")) {
			return;
		}
		String path = "assets/twilightforest/textures/" + id.substring(separator + 1) + ".png";
		assertTrue(RESOURCES.containsKey(path), () -> source + " references missing texture " + id + " (" + path + ")");
	}

	private static String normalize(String id) {
		return id.indexOf(':') >= 0 ? id : "minecraft:" + id;
	}

	private static void collectLiteralIds(Path source, Pattern pattern, Set<String> output) throws IOException {
		String withoutComments = Files.readString(source)
			.replaceAll("(?s)/\\*.*?\\*/", "")
			.replaceAll("(?m)//.*$", "");
		Matcher matcher = pattern.matcher(withoutComments);
		while (matcher.find()) {
			output.add(matcher.group(1));
		}
	}

	private static JsonObject readObject(Path path) throws IOException {
		try (Reader reader = Files.newBufferedReader(path)) {
			JsonElement json = JsonParser.parseReader(reader);
			assertTrue(json.isJsonObject(), path + " must contain a JSON object");
			return json.getAsJsonObject();
		}
	}

	private static void walk(JsonElement element, BiConsumer<String, JsonElement> visitor) {
		if (element.isJsonObject()) {
			for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
				visitor.accept(entry.getKey(), entry.getValue());
				walk(entry.getValue(), visitor);
			}
		} else if (element.isJsonArray()) {
			element.getAsJsonArray().forEach(child -> walk(child, visitor));
		}
	}
}
