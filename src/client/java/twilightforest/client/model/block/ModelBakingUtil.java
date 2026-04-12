package twilightforest.client.model.block;

import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.TextureSlots;

public final class ModelBakingUtil {
	private ModelBakingUtil() {
	}

	public static Material.Baked resolveMaterial(ModelBaker baker, TextureSlots slots, String key, ModelDebugName name) {
		return baker.materials().resolveSlot(slots, key, name);
	}

	public static int materialFlags(Iterable<BakedQuad> quads) {
		int flags = 0;
		for (BakedQuad quad : quads) {
			flags |= quad.materialInfo().flags();
		}
		return flags;
	}

	public static int materialFlags(BlockStateModelPart[] parts) {
		int flags = 0;
		for (BlockStateModelPart part : parts) {
			flags |= part.materialFlags();
		}
		return flags;
	}
}
