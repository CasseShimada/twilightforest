package twilightforest.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import twilightforest.init.TFDimension;

import java.util.Locale;
import java.util.Set;

public class TFTeleportCommand {
	static final SimpleCommandExceptionType PLAYER_ONLY = new SimpleCommandExceptionType(Component.translatable("commands.tffeature.teleport.player_only"));
	static final SimpleCommandExceptionType DIMENSION_MISSING = new SimpleCommandExceptionType(Component.translatable("commands.tffeature.teleport.dimension_missing"));
	static final SimpleCommandExceptionType INVALID_POSITION = new SimpleCommandExceptionType(Component.translatable("commands.teleport.invalidPosition"));

	public LiteralArgumentBuilder<CommandSourceStack> register() {
		return Commands.literal("tp")
			.requires(source -> Commands.LEVEL_GAMEMASTERS.check(source.permissions()))
			.executes(this::run);
	}

	private int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		CommandSourceStack source = context.getSource();
		if (!(source.getEntity() instanceof ServerPlayer player)) {
			throw PLAYER_ONLY.create();
		}

		ServerLevel twilight = source.getServer().getLevel(TFDimension.DIMENSION_KEY);
		if (twilight == null) {
			throw DIMENSION_MISSING.create();
		}

		Vec3 sourcePosition = source.getPosition();
		Level sourceLevel = player.level();
		double targetY = convertY(sourcePosition.y(), sourceLevel.getMinY(), sourceLevel.getMaxY(), twilight.getMinY(), twilight.getMaxY());
		Vec3 targetPosition = new Vec3(sourcePosition.x(), targetY, sourcePosition.z());
		if (!twilight.isInWorldBounds(BlockPos.containing(targetPosition))) {
			throw INVALID_POSITION.create();
		}

		player.teleportTo(twilight, targetPosition.x(), targetPosition.y(), targetPosition.z(), Set.of(), player.getYRot(), player.getXRot(), false);
		String formattedX = formatCoordinate(targetPosition.x());
		String formattedY = formatCoordinate(targetPosition.y());
		String formattedZ = formatCoordinate(targetPosition.z());
		source.sendSuccess(() -> Component.translatable("commands.tffeature.teleport.success", formattedX, formattedY, formattedZ), false);
		return Command.SINGLE_SUCCESS;
	}

	static double convertY(double y, int sourceMinY, int sourceMaxY, int targetMinY, int targetMaxY) {
		return (y - sourceMinY) / (sourceMaxY - sourceMinY) * (targetMaxY - targetMinY) + targetMinY;
	}

	static String formatCoordinate(double coordinate) {
		return String.format(Locale.ROOT, "%.1f", coordinate);
	}
}
