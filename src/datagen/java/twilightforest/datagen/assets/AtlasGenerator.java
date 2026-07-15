package twilightforest.datagen.assets;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSources;
import net.minecraft.client.renderer.texture.atlas.sources.SingleFile;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import twilightforest.TFRegistries;
import twilightforest.TwilightForestMod;
import twilightforest.client.MagicPaintingTextureManager;
import twilightforest.entity.MagicPaintingVariant;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public final class AtlasGenerator implements DataProvider {
	private static final Identifier SHIELD_PATTERNS_ATLAS = Identifier.withDefaultNamespace("shield_patterns");

	private final PackOutput.PathProvider pathProvider;
	private final CompletableFuture<HolderLookup.Provider> registries;

	public AtlasGenerator(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		this.pathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "atlases");
		this.registries = registries;
	}

	@Override
	public CompletableFuture<?> run(CachedOutput output) {
		return this.registries.thenCompose(provider -> {
			Map<Identifier, List<SpriteSource>> atlases = new LinkedHashMap<>();
			atlases.put(SHIELD_PATTERNS_ATLAS, List.of(new SingleFile(TwilightForestMod.prefix("entity/knightmetal_shield"))));
			atlases.put(MagicPaintingTextureManager.ATLAS_INFO_LOCATION, magicPaintingSources(provider));
			return DataProvider.saveAll(output, SpriteSources.FILE_CODEC, this.pathProvider, atlases);
		});
	}

	@Override
	public String getName() {
		return "Twilight Forest Atlases";
	}

	private static List<SpriteSource> magicPaintingSources(HolderLookup.Provider provider) {
		List<Holder.Reference<MagicPaintingVariant>> paintings = provider.lookupOrThrow(TFRegistries.Keys.MAGIC_PAINTINGS)
			.listElements()
			.sorted(Comparator.comparing(reference -> reference.key().identifier()))
			.toList();
		List<SpriteSource> sources = new ArrayList<>();
		paintings.stream()
			.map(Holder.Reference::value)
			.map(MagicPaintingVariant::backTexture)
			.distinct()
			.sorted()
			.map(SingleFile::new)
			.forEach(sources::add);
		paintings.forEach(reference -> {
			Identifier paintingPath = reference.key().identifier().withPrefix(MagicPaintingTextureManager.MAGIC_PAINTING_PATH + "/");
			for (MagicPaintingVariant.Layer layer : reference.value().layers()) {
				sources.add(new SingleFile(paintingPath.withSuffix("/" + layer.path())));
			}
		});
		return List.copyOf(sources);
	}
}
