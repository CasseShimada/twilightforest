package twilightforest.client.event;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import twilightforest.TwilightForestMod;
import twilightforest.init.TFBiomes;
import twilightforest.init.TFDimension;

public class FogHandler {
	private static boolean skyInitialized = false;
	private static float skyEnd = 0.0F;

	private static boolean terrainInitialized = false;
	private static float terrainStart = 0.0F;
	private static float terrainEnd = 0.0F;
	private static long lastFogTraceTick = Long.MIN_VALUE;

	public static void reset() {
		skyInitialized = false;
		terrainInitialized = false;
		skyEnd = 0.0F;
		terrainStart = 0.0F;
		terrainEnd = 0.0F;
		lastFogTraceTick = Long.MIN_VALUE;
	}

	public static void applyTwilightFog(FogData fogData, @Nullable ClientLevel level, @Nullable Entity cameraEntity) {
		if (!(cameraEntity instanceof LocalPlayer player) || level == null || !TFDimension.isTwilightWorldOnClient(level)) {
			return;
		}

		boolean spooky = isSpooky(level, player);
		float initialSkyEnd = fogData.skyEnd;
		float initialTerrainStart = fogData.environmentalStart;
		float initialTerrainEnd = fogData.environmentalEnd;

		float targetSkyEnd = spooky ? fogData.skyEnd * 0.5F : fogData.skyEnd;
		if (!skyInitialized) {
			skyInitialized = true;
			skyEnd = targetSkyEnd;
		}
		skyEnd = Mth.lerp(0.003F, skyEnd, targetSkyEnd);
		fogData.skyEnd = skyEnd;

		float targetEnd = spooky ? fogData.environmentalEnd * 0.5F : fogData.environmentalEnd;
		float targetStart = spooky ? targetEnd * 0.75F : fogData.environmentalStart;
		if (!terrainInitialized) {
			terrainInitialized = true;
			terrainEnd = targetEnd;
			terrainStart = targetStart;
		}
		terrainEnd = Mth.lerp(0.003F, terrainEnd, targetEnd);
		terrainStart = Mth.lerp(0.003F * (terrainStart < targetStart ? 0.5F : 2.0F), terrainStart, targetStart);
		fogData.environmentalEnd = terrainEnd;
		fogData.environmentalStart = terrainStart;
		logFogTrace(level, player, spooky, initialSkyEnd, targetSkyEnd, initialTerrainStart, targetStart, initialTerrainEnd, targetEnd, fogData);
	}

	private static boolean isSpooky(@Nullable ClientLevel level, @Nullable LocalPlayer player) {
		return level != null && player != null && level.getBiome(player.blockPosition()).is(TFBiomes.SPOOKY_FOREST);
	}

	private static void logFogTrace(ClientLevel level, LocalPlayer player, boolean spooky, float initialSkyEnd, float targetSkyEnd, float initialTerrainStart, float targetTerrainStart, float initialTerrainEnd, float targetTerrainEnd, FogData fogData) {
		long gameTime = level.getGameTime();
		if (gameTime % 40L != 0L || gameTime == lastFogTraceTick) {
			return;
		}
		lastFogTraceTick = gameTime;

		BlockPos pos = player.blockPosition();
		TwilightForestMod.LOGGER.info(
			"TF sky trace: fog snapshot dim={} biome={} pos={} gameTime={} spooky={} skyEnd={} targetSkyEnd={} appliedSkyEnd={} terrainStart={} targetTerrainStart={} appliedTerrainStart={} terrainEnd={} targetTerrainEnd={} appliedTerrainEnd={}",
			level.dimension().identifier(),
			level.registryAccess().lookupOrThrow(Registries.BIOME).getKey(level.getBiome(pos).value()),
			pos,
			gameTime,
			spooky,
			initialSkyEnd,
			targetSkyEnd,
			fogData.skyEnd,
			initialTerrainStart,
			targetTerrainStart,
			fogData.environmentalStart,
			initialTerrainEnd,
			targetTerrainEnd,
			fogData.environmentalEnd
		);
	}
}
