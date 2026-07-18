package twilightforest.init;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;
import twilightforest.TwilightForestMod;

public final class TFKeyBinds {
	public static final KeyMapping RED_THREAD_VISION_KEY = register("red_thread_vision", GLFW.GLFW_KEY_UNKNOWN);
	public static final KeyMapping ITEM_DISPLAY_MAP_CYCLE_KEY = register("item_display_map_cycle", GLFW.GLFW_KEY_C);
	public static final KeyMapping ZOOM_KEY = register("zoom", GLFW.GLFW_KEY_Z);
	public static final KeyMapping SWAP_HOTBAR_KEY = register("swap_hotbar", GLFW.GLFW_KEY_V);

	private TFKeyBinds() {
	}

	private static KeyMapping register(String name, int key) {
		return KeyMappingHelper.registerKeyMapping(new KeyMapping(
			"key." + TwilightForestMod.ID + "." + name,
			InputConstants.Type.KEYSYM,
			key,
			TFKeyBindsCategories.TRAVELLERS_GEAR
		));
	}

	public static void init() {
	}
}
