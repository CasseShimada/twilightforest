package twilightforest.init;

import net.minecraft.util.Util;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.equipment.trim.MaterialAssetGroup;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import twilightforest.TwilightForestMod;

public class TFTrimMaterials {

	public static final ResourceKey<TrimMaterial> IRONWOOD = registerKey("ironwood");
	public static final ResourceKey<TrimMaterial> STEELEAF = registerKey("steeleaf");
	public static final ResourceKey<TrimMaterial> KNIGHTMETAL = registerKey("knightmetal");
	public static final ResourceKey<TrimMaterial> FIERY = registerKey("fiery");
	public static final ResourceKey<TrimMaterial> NAGA_SCALE = registerKey("naga_scale");
	public static final ResourceKey<TrimMaterial> CARMINITE = registerKey("carminite");

	private static ResourceKey<TrimMaterial> registerKey(String name) {
		return ResourceKey.create(Registries.TRIM_MATERIAL, TwilightForestMod.prefix(name));
	}

	public static void bootstrap(BootstrapContext<TrimMaterial> context) {
		register(context, IRONWOOD, Style.EMPTY.withColor(7037281), MaterialAssetGroup.create("ironwood"));
		register(context, STEELEAF, Style.EMPTY.withColor(4814643), MaterialAssetGroup.create("steeleaf"));
		register(context, KNIGHTMETAL, Style.EMPTY.withColor(8424562), MaterialAssetGroup.create("knightmetal"));
		register(context, FIERY, Style.EMPTY.withColor(16758076), MaterialAssetGroup.create("fiery"));
		register(context, NAGA_SCALE, Style.EMPTY.withColor(2381586), MaterialAssetGroup.create("naga_scale"));
		register(context, CARMINITE, Style.EMPTY.withColor(10092544), MaterialAssetGroup.create("carminite"));
	}

	private static void register(BootstrapContext<TrimMaterial> context, ResourceKey<TrimMaterial> trimKey, Style color, MaterialAssetGroup assets) {
		TrimMaterial material = new TrimMaterial(assets, Component.translatable(Util.makeDescriptionId("trim_material", trimKey.identifier())).withStyle(color));
		context.register(trimKey, material);
	}
}
