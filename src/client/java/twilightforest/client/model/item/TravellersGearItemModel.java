package twilightforest.client.model.item;

import com.mojang.math.Transformation;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.renderer.item.CompositeModel;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemModels;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.cuboid.ItemModelGenerator;
import net.minecraft.client.resources.model.cuboid.ItemTransforms;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.MaterialBaker;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;
import twilightforest.init.TFDataComponents;
import twilightforest.init.custom.TravellersModifiersManager;
import twilightforest.item.travellers_gear.TravellersArmorItem;
import twilightforest.item.travellers_gear.modifiers.TravellersModifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class TravellersGearItemModel implements ItemModel {
	private static final ModelDebugName DEBUG_NAME = () -> "TravellersGearItemModel";

	private final ItemModel baseModel;
	private final ItemModel brokenModel;
	private final String modifierDirectory;
	private final String brokenModifierDirectory;
	private final BakingContext bakingContext;
	private final Matrix4fc transformation;
	private final ItemTransforms itemTransforms;
	private final Map<String, ItemModel> possibleCombinations = new HashMap<>();

	private TravellersGearItemModel(ItemModel baseModel, ItemModel brokenModel, String modifierDirectory,
								  String brokenModifierDirectory, BakingContext bakingContext, Matrix4fc transformation) {
		this.baseModel = baseModel;
		this.brokenModel = brokenModel;
		this.modifierDirectory = modifierDirectory;
		this.brokenModifierDirectory = brokenModifierDirectory;
		this.bakingContext = bakingContext;
		this.transformation = transformation;
		this.itemTransforms = bakingContext.blockModelBaker()
			.getModel(Identifier.withDefaultNamespace("item/generated"))
			.getTopTransforms();
	}

	@Override
	public void update(ItemStackRenderState state, ItemStack stack, ItemModelResolver resolver, ItemDisplayContext context,
					  @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
		boolean broken = TravellersArmorItem.isTravellersArmorAndBroken(stack);
		(broken ? this.brokenModel : this.baseModel).update(state, stack, resolver, context, level, owner, seed);

		if (!stack.has(TFDataComponents.IS_TRAVELLERS_GEAR) || level == null) {
			return;
		}

		List<Holder.Reference<TravellersModifier>> modifiers = TravellersModifiersManager.findAllInsertableModifiers(level, stack);
		if (modifiers.isEmpty()) {
			return;
		}

		String key = (broken ? "broken:" : "base:")
			+ BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath()
			+ getModifiersSuffix(modifiers);
		this.possibleCombinations.computeIfAbsent(key, ignored -> this.getModifiedGear(modifiers, broken))
			.update(state, stack, resolver, context, level, owner, seed);
	}

	private ItemModel getModifiedGear(List<Holder.Reference<TravellersModifier>> modifiers, boolean broken) {
		ModelBaker baker = this.bakingContext.blockModelBaker();
		MaterialBaker materials = baker.materials();
		String directory = broken ? this.brokenModifierDirectory : this.modifierDirectory;
		List<ItemModel> modelLayers = new ArrayList<>();
		int layer = 1;

		for (Holder.Reference<TravellersModifier> modifier : modifiers) {
			Material.Baked sprite = getModifierSprite(modifier.key(), directory, materials);
			if (sprite.sprite().contents().name().equals(MissingTextureAtlasSprite.getLocation())) {
				continue;
			}

			ModelRenderProperties renderProperties = new ModelRenderProperties(false, sprite, this.itemTransforms);
			Transformation scale = new Transformation(null, null, new Vector3f(1.0F + layer * 0.001F), null);
			QuadCollection quads = baker.compute(new ItemModelGenerator.ItemLayerKey(sprite, new ScaledModelState(scale), layer));
			modelLayers.add(new CuboidItemModelWrapper(List.of(), quads, renderProperties, this.transformation));
			layer++;
		}
		return new CompositeModel(modelLayers);
	}

	private static String getModifiersSuffix(List<Holder.Reference<TravellersModifier>> modifiers) {
		StringBuilder suffix = new StringBuilder();
		for (Holder.Reference<TravellersModifier> modifier : modifiers) {
			suffix.append('_').append(modifier.key().identifier().getPath());
		}
		return suffix.toString();
	}

	private static Material.Baked getModifierSprite(ResourceKey<TravellersModifier> modifier, String directory, MaterialBaker baker) {
		return baker.get(new Material(modifier.identifier().withPrefix("item/" + directory)), DEBUG_NAME);
	}

	private record ScaledModelState(Transformation transformation) implements ModelState {
	}

	public record Unbaked(ItemModel.Unbaked baseModel, ItemModel.Unbaked brokenModel, String modifierDirectory,
						  String brokenModifierDirectory) implements ItemModel.Unbaked {
		public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			ItemModels.CODEC.fieldOf("base_model").forGetter(Unbaked::baseModel),
			ItemModels.CODEC.fieldOf("broken_model").forGetter(Unbaked::brokenModel),
			Codec.STRING.fieldOf("modifier_directory").forGetter(Unbaked::modifierDirectory),
			Codec.STRING.fieldOf("broken_modifier_directory").forGetter(Unbaked::brokenModifierDirectory)
		).apply(instance, Unbaked::new));

		@Override
		public ItemModel bake(BakingContext context, Matrix4fc transformation) {
			return new TravellersGearItemModel(this.baseModel.bake(context, transformation),
				this.brokenModel.bake(context, transformation), this.modifierDirectory,
				this.brokenModifierDirectory, context, transformation);
		}

		@Override
		public void resolveDependencies(Resolver resolver) {
			this.baseModel.resolveDependencies(resolver);
			this.brokenModel.resolveDependencies(resolver);
		}

		@Override
		public MapCodec<? extends ItemModel.Unbaked> type() {
			return MAP_CODEC;
		}
	}
}
