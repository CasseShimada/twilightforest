package twilightforest.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import twilightforest.util.landmarks.LandmarkUtil;
import twilightforest.world.components.structures.util.StructureConqueredData;

import java.util.Optional;

public class ConquerCommand {

	private final SimpleCommandExceptionType NOT_IN_STRUCTURE = new SimpleCommandExceptionType(Component.translatable("commands.tffeature.structure.required"));

	public LiteralArgumentBuilder<CommandSourceStack> register() {
		LiteralArgumentBuilder<CommandSourceStack> conquer = Commands.literal("conquer").requires(cs -> Commands.LEVEL_GAMEMASTERS.check(cs.permissions())).executes(ctx -> changeStructureActivity(ctx.getSource(), true));
		LiteralArgumentBuilder<CommandSourceStack> reactivate = Commands.literal("reactivate").requires(cs -> Commands.LEVEL_GAMEMASTERS.check(cs.permissions())).executes(ctx -> changeStructureActivity(ctx.getSource(), false));
		return conquer.then(reactivate);
	}

	private int changeStructureActivity(CommandSourceStack source, boolean flag) throws CommandSyntaxException {
		BlockPos pos = BlockPos.containing(source.getPosition());
		Optional<StructureStart> struct = LandmarkUtil.locateNearestLandmarkStart(source.getLevel(), SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()));

		if (struct.isPresent() && struct.get().getBoundingBox().isInside(pos)) {
			StructureStart start = struct.get();
			var registry = source.getLevel().registryAccess().lookupOrThrow(Registries.STRUCTURE);
			var key = registry.getResourceKey(start.getStructure()).orElse(null);
			if (key == null) {
				throw NOT_IN_STRUCTURE.create();
			}
			boolean current = StructureConqueredData.get(source.getLevel()).isConquered(key, start.getChunkPos());
			source.sendSuccess(() -> Component.translatable("commands.tffeature.structure.conquer.update", current, flag), true);
			StructureConqueredData.get(source.getLevel()).setConquered(key, start.getChunkPos(), flag);
		} else {
			throw NOT_IN_STRUCTURE.create();
		}

		return Command.SINGLE_SUCCESS;
	}
}
