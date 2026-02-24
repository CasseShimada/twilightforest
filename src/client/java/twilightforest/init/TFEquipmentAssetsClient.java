package twilightforest.init;

import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.equipment.EquipmentAsset;
import twilightforest.TwilightForestMod;
import twilightforest.item.ArcticArmorItem;

import java.util.Optional;
import java.util.function.BiConsumer;

public final class TFEquipmentAssetsClient {
	private TFEquipmentAssetsClient() {
	}

	public static void bootstrap(BiConsumer<ResourceKey<EquipmentAsset>, EquipmentClientInfo> consumer) {
		consumer.accept(TFEquipmentAssets.IRONWOOD, EquipmentClientInfo.builder().addHumanoidLayers(TwilightForestMod.prefix("ironwood"), false).build());
		consumer.accept(TFEquipmentAssets.STEELEAF, EquipmentClientInfo.builder().addHumanoidLayers(TwilightForestMod.prefix("steeleaf"), false).build());
		consumer.accept(TFEquipmentAssets.NAGA, EquipmentClientInfo.builder().addHumanoidLayers(TwilightForestMod.prefix("naga_scale"), false).build());
		consumer.accept(TFEquipmentAssets.FIERY, EquipmentClientInfo.builder().addHumanoidLayers(TwilightForestMod.prefix("fiery"), false).build());
		consumer.accept(TFEquipmentAssets.KNIGHTMETAL, EquipmentClientInfo.builder().addHumanoidLayers(TwilightForestMod.prefix("knightmetal"), false).build());
		consumer.accept(TFEquipmentAssets.PHANTOM, EquipmentClientInfo.builder().addMainHumanoidLayer(TwilightForestMod.prefix("phantom"), false).build());
		consumer.accept(TFEquipmentAssets.ARCTIC, EquipmentClientInfo.builder()
			.addLayers(EquipmentClientInfo.LayerType.HUMANOID, arcticDyeable(TwilightForestMod.prefix("arctic"), true))
			.addLayers(EquipmentClientInfo.LayerType.HUMANOID, arcticDyeable(TwilightForestMod.prefix("arctic_overlay"), false))
			.addLayers(EquipmentClientInfo.LayerType.HUMANOID_LEGGINGS, arcticDyeable(TwilightForestMod.prefix("arctic"), true))
			.addLayers(EquipmentClientInfo.LayerType.HUMANOID_LEGGINGS, arcticDyeable(TwilightForestMod.prefix("arctic_overlay"), false))
			.build());
		consumer.accept(TFEquipmentAssets.YETI, EquipmentClientInfo.builder().addHumanoidLayers(TwilightForestMod.prefix("yeti"), false).build());
	}

	public static EquipmentClientInfo.Layer arcticDyeable(Identifier textureId, boolean dyeable) {
		return new EquipmentClientInfo.Layer(textureId, dyeable ? Optional.of(new EquipmentClientInfo.Dyeable(Optional.of(ArcticArmorItem.DEFAULT_COLOR))) : Optional.empty(), false);
	}
}
