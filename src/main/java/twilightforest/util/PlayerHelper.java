package twilightforest.util;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PlayerHelper {
	/**
	 * Fulfills all remaining criteria of the given advancement
	 */
	@Deprecated
	public static void grantAdvancement(ServerPlayer player, Identifier id) {
		PlayerAdvancements advancements = player.getAdvancements();
		AdvancementHolder holder = player.level().getServer().getAdvancements().get(id);
		if (holder != null) {
			for (String criterion : advancements.getOrStartProgress(holder).getRemainingCriteria()) {
				advancements.award(holder, criterion);
			}
		}
	}

	@Deprecated
	public static void grantCriterion(ServerPlayer player, Identifier id, String criterion) {
		PlayerAdvancements advancements = player.getAdvancements();
		AdvancementHolder holder = player.level().getServer().getAdvancements().get(id);
		if (holder != null) {
			advancements.award(holder, criterion);
		}
	}

	@Nullable
	public static AdvancementHolder getAdvancement(Player player, Identifier advancementLocation) {
		if (player instanceof ServerPlayer serverPlayer) {
			ServerLevel world = serverPlayer.level();
			return world.getServer().getAdvancements().get(advancementLocation);
		} else if (player.level().isClientSide()) {
			return getClientAdvancement(player, advancementLocation);
		}

		return null;
	}

	public static boolean doesPlayerHaveRequiredAdvancement(Player player, @Nullable AdvancementHolder holder) {
		if (player instanceof ServerPlayer) {
			return holder != null && ((ServerPlayer) player).getAdvancements().getOrStartProgress(holder).isDone();
		}
		if (player.level().isClientSide()) {
			return hasClientAdvancement(player, holder);
		}
		return false;
	}

	public static boolean doesPlayerHaveRequiredAdvancements(Player player, List<Identifier> requiredAdvancements) {
		return PlayerHelper.playerHasRequiredAdvancements(player, requiredAdvancements);
	}

	public static boolean doesPlayerHaveRequiredAdvancements(Player player, Identifier... requiredAdvancements) {
		return PlayerHelper.playerHasRequiredAdvancements(player, List.of(requiredAdvancements));
	}

	public static boolean playerHasRequiredAdvancements(Player player, Iterable<Identifier> requiredAdvancements) {
		for (Identifier advancementLocation : requiredAdvancements) {
			if (player instanceof ServerPlayer sp) {
				ServerLevel world = sp.level();
				AdvancementHolder adv = world.getServer().getAdvancements().get(advancementLocation);
				return adv != null && sp.getAdvancements().getOrStartProgress(adv).isDone();
			}
			if (player.level().isClientSide()) {
				AdvancementHolder adv = getClientAdvancement(player, advancementLocation);
				return adv != null && hasClientAdvancement(player, adv);
			}
			return false;
		}
		return true;
	}

	@Nullable
	private static AdvancementHolder getClientAdvancement(Player player, Identifier advancementLocation) {
		if (FabricLoader.getInstance().getEnvironmentType() != EnvType.CLIENT) {
			return null;
		}

		try {
			Class<?> helper = Class.forName("twilightforest.client.ClientAdvancementHelper");
			return (AdvancementHolder) helper.getMethod("getAdvancement", Player.class, Identifier.class).invoke(null, player, advancementLocation);
		} catch (Throwable ignored) {
			return null;
		}
	}

	private static boolean hasClientAdvancement(Player player, @Nullable AdvancementHolder holder) {
		if (FabricLoader.getInstance().getEnvironmentType() != EnvType.CLIENT) {
			return false;
		}

		try {
			Class<?> helper = Class.forName("twilightforest.client.ClientAdvancementHelper");
			return (Boolean) helper.getMethod("hasAdvancement", Player.class, AdvancementHolder.class).invoke(null, player, holder);
		} catch (Throwable ignored) {
			return false;
		}
	}
}
