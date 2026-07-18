package twilightforest.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import twilightforest.TwilightForestMod;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public class TFSimpleArmorRenderer implements ArmorRenderer {
	public static final List<TFSimpleArmorRenderer> INSTANCES = new ArrayList<>();
	private final CachedSupplier<HumanoidModel<HumanoidRenderState>> innerArmorModel;
	private final CachedSupplier<HumanoidModel<HumanoidRenderState>> outerArmorModel;
	private final CachedSupplier<EquipmentLayerRenderer> equipmentRenderer;
	private final boolean forceFullbright;

	public TFSimpleArmorRenderer(Function<ModelPart, HumanoidModel<HumanoidRenderState>> createModelInstance, ModelLayerLocation innerLayerLocation, ModelLayerLocation outerLayerLocation) {
		this(createModelInstance, innerLayerLocation, outerLayerLocation, false);
	}

	public TFSimpleArmorRenderer(Function<ModelPart, HumanoidModel<HumanoidRenderState>> createModelInstance, ModelLayerLocation innerLayerLocation, ModelLayerLocation outerLayerLocation, boolean forceFullbright) {
		INSTANCES.add(this);
		this.innerArmorModel = new CachedSupplier<>(() -> createModelInstance.apply(Minecraft.getInstance().getEntityModels().bakeLayer(innerLayerLocation)));
		this.outerArmorModel = new CachedSupplier<>(() -> createModelInstance.apply(Minecraft.getInstance().getEntityModels().bakeLayer(outerLayerLocation)));
		this.equipmentRenderer = new CachedSupplier<>(TFSimpleArmorRenderer::createEquipmentRenderer);
		this.forceFullbright = forceFullbright;
	}

	// can be overridden
	public void resetModelCache() {
		innerArmorModel.invalidate();
		outerArmorModel.invalidate();
		equipmentRenderer.invalidate();
	}

	public static void resetAllModelCache() {
		INSTANCES.forEach(TFSimpleArmorRenderer::resetModelCache);
	}

	@Override
	public void render(PoseStack matrices, SubmitNodeCollector collector, ItemStack stack, HumanoidRenderState renderState, EquipmentSlot slot, int light, HumanoidModel<HumanoidRenderState> contextModel) {
		Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
		if (equippable == null || equippable.slot() != slot || equippable.assetId().isEmpty()) {
			return;
		}

		EquipmentLayerRenderer equipmentRenderer = this.equipmentRenderer.get();
		if (equipmentRenderer == null) {
			return;
		}

		HumanoidModel<HumanoidRenderState> model = slot == EquipmentSlot.LEGS ? innerArmorModel.get() : outerArmorModel.get();
		model.resetPose();
		model.setupAnim(renderState);
		setPartVisibility(model, slot);

		EquipmentClientInfo.LayerType layerType = slot == EquipmentSlot.LEGS
			? EquipmentClientInfo.LayerType.HUMANOID_LEGGINGS
			: EquipmentClientInfo.LayerType.HUMANOID;

		int renderLight = this.forceFullbright ? 0xF000F0 : light;
		equipmentRenderer.renderLayers(layerType, equippable.assetId().orElseThrow(), model, renderState, stack, matrices, collector, renderLight, renderState.outlineColor);
	}

	private static EquipmentLayerRenderer createEquipmentRenderer() {
		Minecraft mc = Minecraft.getInstance();
		return new EquipmentLayerRenderer(
			mc.getEntityRenderDispatcher().equipmentAssets,
			mc.getAtlasManager().getAtlasOrThrow(AtlasIds.ARMOR_TRIMS)
		);
	}

	private static void setPartVisibility(HumanoidModel<HumanoidRenderState> model, EquipmentSlot slot) {
		model.head.visible = false;
		model.hat.visible = false;
		model.body.visible = false;
		model.rightArm.visible = false;
		model.leftArm.visible = false;
		model.rightLeg.visible = false;
		model.leftLeg.visible = false;
		switch (slot) {
			case HEAD -> {
				model.head.visible = true;
				model.hat.visible = true;
			}
			case CHEST -> {
				model.body.visible = true;
				model.rightArm.visible = true;
				model.leftArm.visible = true;
			}
			case LEGS -> {
				model.body.visible = true;
				model.rightLeg.visible = true;
				model.leftLeg.visible = true;
			}
			case FEET -> {
				model.rightLeg.visible = true;
				model.leftLeg.visible = true;
			}
		}
	}

	public static final class ResourceReloadListener implements ResourceManagerReloadListener, IdentifiableResourceReloadListener {
		@Override
		public void onResourceManagerReload(ResourceManager resourceManager) {
			TFSimpleArmorRenderer.resetAllModelCache();
			TravellersArmorRenderer.INSTANCE.resetModelCache();
		}

		@Override
		public Identifier getFabricId() {
			return TwilightForestMod.prefix("armor_models");
		}
	}

	private static final class CachedSupplier<T> implements Supplier<T> {
		private final Supplier<T> supplier;
		private T value;
		private boolean loaded;

		private CachedSupplier(Supplier<T> supplier) {
			this.supplier = Objects.requireNonNull(supplier, "supplier");
		}

		@Override
		public T get() {
			if (!loaded) {
				value = supplier.get();
				loaded = true;
			}
			return value;
		}

		private void invalidate() {
			loaded = false;
			value = null;
		}
	}
}
