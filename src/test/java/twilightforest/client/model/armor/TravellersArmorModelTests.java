package twilightforest.client.model.armor;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class TravellersArmorModelTests {
	@Test
	void bakesEveryTravellerArmorLayerAndRequiredChild() {
		assertDoesNotThrow(() -> new HumanoidModel<HumanoidRenderState>(LayerDefinition.create(
			TravellersGearModels.addGogglePieces(new CubeDeformation(1.0F)), 64, 32).bakeRoot()));
		assertDoesNotThrow(() -> new HumanoidModel<HumanoidRenderState>(LayerDefinition.create(
			TravellersGearModels.addGlovePieces(new CubeDeformation(0.295F), false), 64, 32).bakeRoot()));
		assertDoesNotThrow(() -> new HumanoidModel<HumanoidRenderState>(LayerDefinition.create(
			TravellersGearModels.addGlovePieces(new CubeDeformation(0.295F), true), 64, 32).bakeRoot()));
		assertDoesNotThrow(() -> new HumanoidModel<HumanoidRenderState>(LayerDefinition.create(
			TravellersGearModels.addBootPieces(new CubeDeformation(0.5F)), 64, 32).bakeRoot()));

		TravellersWingsModel wings = assertDoesNotThrow(() -> new TravellersWingsModel(
			TravellersWingsModel.createLayer(0.25F).bakeRoot()));
		assertDoesNotThrow(() -> {
			wings.resetPose();
			wings.setWingsVisible(true);
			wings.setBeltVisible(true);
			wings.setWingsVisible(false);
			wings.setBeltVisible(false);
		});
	}
}
