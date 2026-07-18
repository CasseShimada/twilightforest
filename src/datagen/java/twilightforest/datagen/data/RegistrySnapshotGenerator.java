package twilightforest.datagen.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.SharedConstants;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;

public final class RegistrySnapshotGenerator implements DataProvider {
	private static final int SCHEMA_VERSION = 1;

	private final Path outputPath;
	private final CompletableFuture<HolderLookup.Provider> registries;

	public RegistrySnapshotGenerator(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		this.outputPath = output.createPathProvider(PackOutput.Target.DATA_PACK, "migration")
			.json(Identifier.fromNamespaceAndPath("twilightforest", "registry_snapshot"));
		this.registries = registries;
	}

	@Override
	public CompletableFuture<?> run(CachedOutput output) {
		return this.registries.thenCompose(provider -> DataProvider.saveStable(output, createSnapshot(provider), this.outputPath));
	}

	@Override
	public String getName() {
		return "Twilight Forest Registry Compatibility Snapshot";
	}

	private static JsonObject createSnapshot(HolderLookup.Provider provider) {
		Map<String, List<String>> staticRegistries = collectStaticRegistries();
		Map<String, List<String>> datapackRegistries = collectDatapackRegistries(provider);

		JsonObject snapshot = new JsonObject();
		snapshot.addProperty("schema", SCHEMA_VERSION);
		snapshot.addProperty("minecraft_version", SharedConstants.getCurrentVersion().name());
		snapshot.addProperty("static_entry_count", entryCount(staticRegistries));
		snapshot.addProperty("datapack_entry_count", entryCount(datapackRegistries));
		snapshot.add("static_registries", toJson(staticRegistries));
		snapshot.add("datapack_registries", toJson(datapackRegistries));
		return snapshot;
	}

	private static Map<String, List<String>> collectStaticRegistries() {
		Map<String, List<String>> snapshot = new TreeMap<>();
		BuiltInRegistries.REGISTRY.entrySet().forEach(entry -> {
			Identifier registryId = entry.getKey().identifier();
			Registry<?> registry = entry.getValue();
			List<String> ids = registry.keySet().stream()
				.filter(RegistrySnapshotGenerator::isTwilightId)
				.map(Identifier::toString)
				.sorted()
				.toList();
			if (isTwilightId(registryId) || !ids.isEmpty()) {
				snapshot.put(registryId.toString(), ids);
			}
		});
		return snapshot;
	}

	private static Map<String, List<String>> collectDatapackRegistries(HolderLookup.Provider provider) {
		Map<String, List<String>> snapshot = new TreeMap<>();
		provider.listRegistries().forEach(registry -> {
			Identifier registryId = registry.key().identifier();
			if (BuiltInRegistries.REGISTRY.containsKey(registryId)) {
				return;
			}
			List<String> ids = registry.listElementIds()
				.map(ResourceKey::identifier)
				.filter(RegistrySnapshotGenerator::isTwilightId)
				.map(Identifier::toString)
				.sorted()
				.toList();
			if (isTwilightId(registryId) || !ids.isEmpty()) {
				snapshot.put(registryId.toString(), ids);
			}
		});
		return snapshot;
	}

	private static boolean isTwilightId(Identifier id) {
		return id.getNamespace().equals("twilightforest") || id.getNamespace().equals("twilight");
	}

	private static int entryCount(Map<String, List<String>> registries) {
		return registries.values().stream().mapToInt(List::size).sum();
	}

	private static JsonObject toJson(Map<String, List<String>> registries) {
		JsonObject json = new JsonObject();
		registries.forEach((registry, ids) -> {
			JsonArray entries = new JsonArray(ids.size());
			ids.forEach(entries::add);
			json.add(registry, entries);
		});
		return json;
	}
}
