package twilightforest.mixin;

import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twilightforest.item.MagicMapItem;
import twilightforest.item.MazeMapItem;
import twilightforest.item.mapdata.TFMagicMapData;
import twilightforest.item.mapdata.TFMazeMapData;

@Mixin(MapItem.class)
public class MapItemMixin {

	@Inject(
		method = "getSavedData(Lnet/minecraft/world/level/saveddata/maps/MapId;Lnet/minecraft/world/level/Level;)Lnet/minecraft/world/level/saveddata/maps/MapItemSavedData;",
		at = @At("RETURN"),
		cancellable = true
	)
	private static void twilightforest$getCustomSavedData(MapId mapId, Level level, CallbackInfoReturnable<MapItemSavedData> cir) {
		if (mapId == null || cir.getReturnValue() != null) {
			return;
		}

		TFMagicMapData magicData = TFMagicMapData.getMagicMapData(level, MagicMapItem.getMapName(mapId.id()));
		if (magicData != null) {
			cir.setReturnValue(magicData);
			return;
		}

		TFMazeMapData mazeData = TFMazeMapData.getMazeMapData(level, MazeMapItem.getMapName(mapId.id()));
		if (mazeData != null) {
			cir.setReturnValue(mazeData);
		}
	}
}
