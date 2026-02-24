package twilightforest.init;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import twilightforest.TwilightForestMod;

public class TFEquipmentAssets {

	public static final ResourceKey<EquipmentAsset> IRONWOOD = createId("ironwood");
	public static final ResourceKey<EquipmentAsset> STEELEAF = createId("steeleaf");
	public static final ResourceKey<EquipmentAsset> NAGA = createId("naga_scale");
	public static final ResourceKey<EquipmentAsset> FIERY = createId("fiery");
	public static final ResourceKey<EquipmentAsset> KNIGHTMETAL = createId("knightmetal");
	public static final ResourceKey<EquipmentAsset> PHANTOM = createId("phantom");
	public static final ResourceKey<EquipmentAsset> ARCTIC = createId("arctic");
	public static final ResourceKey<EquipmentAsset> YETI = createId("yeti");

	static ResourceKey<EquipmentAsset> createId(String name) {
		return ResourceKey.create(EquipmentAssets.ROOT_ID, TwilightForestMod.prefix(name));
	}
}
