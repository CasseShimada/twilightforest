package twilightforest.events;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.util.TriState;
import net.fabricmc.fabric.api.entity.FakePlayer;
import twilightforest.network.PacketDistributor;
import twilightforest.TwilightForestMod;
import twilightforest.tags.TFBlockTags;
import twilightforest.entity.monster.Kobold;
import twilightforest.network.AreaProtectionPacket;
import twilightforest.util.landmarks.LandmarkUtil;
import twilightforest.world.components.structures.TFStructureComponent;
import twilightforest.world.components.structures.util.ProgressionStructure;

import java.util.List;
import java.util.Optional;

/**
 * A class to store events relating to progression
 */
public class ProgressionEvents {
	public static boolean shouldCancelBlockBreak(ServerLevel level, Player player, BlockPos pos) {
		return isBlockProtectedFromBreaking(level, pos) && isAreaProtected(level, player, pos);
	}

	public static boolean shouldCancelBlockPlacement(ServerLevel level, Player player, BlockPos pos) {
		return isBlockProtectedFromBreaking(level, pos) && isAreaProtected(level, player, pos);
	}

	public static TriState shouldAllowBlockInteraction(ServerLevel level, Player player, BlockPos pos) {
		return (isBlockProtectedFromInteraction(level, pos) && isAreaProtected(level, player, pos)) ? TriState.FALSE : TriState.TRUE;
	}

	static boolean isBlockProtectedFromInteraction(BlockGetter level, BlockPos pos) {
		return level.getBlockState(pos).is(TFBlockTags.STRUCTURE_BANNED_INTERACTIONS);
	}

	static boolean isBlockProtectedFromBreaking(BlockGetter level, BlockPos pos) {
		return !level.getBlockState(pos).is(TFBlockTags.PROGRESSION_ALLOW_BREAKING);
	}

	/**
	 * Return if the area at the coordinates is considered protected for that player.
	 * Currently, if we return true, we also send the area protection packet here.
	 */
	static boolean isAreaProtected(ServerLevel level, Player player, BlockPos pos) {
		if (player.getAbilities().instabuild || player.isSpectator() ||
			!LandmarkUtil.isProgressionEnforced(level) || player instanceof FakePlayer) {
			return false;
		}

		Optional<StructureStart> struct = LandmarkUtil.locateNearestLandmarkStart(level, SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()));
		if (struct.isPresent()) {
			StructureStart structureStart = struct.get();
			if (structureStart.getPieces().stream().anyMatch(structurePiece -> structurePiece.getBoundingBox().isInside(pos) && (!(structurePiece instanceof TFStructureComponent tfStructureComponent) || tfStructureComponent.isComponentProtected())) && structureStart.getStructure() instanceof ProgressionStructure structureHints) {
				if (!structureHints.doesPlayerHaveRequiredAdvancements(player)/* && chunkGenerator.isBlockProtected(pos)*/) {
					// send protection packet
					List<BoundingBox> boxes = structureStart.getPieces().stream()
						.filter(piece -> piece.getBoundingBox().isInside(pos))
						.map(StructurePiece::getBoundingBox)
						.toList();

					sendAreaProtectionPacket(level, pos, boxes);

					// send a hint monster?
					structureHints.trySpawnHintMonster(level, player, pos);

					return true;
				}
			}
		}
		return false;
	}

	private static void sendAreaProtectionPacket(ServerLevel level, BlockPos pos, List<BoundingBox> sbb) {
		PacketDistributor.sendToPlayersNear(level, null, pos.getX(), pos.getY(), pos.getZ(), 64, new AreaProtectionPacket(sbb, pos));
	}

	public static boolean shouldCancelAttackInProtectedArea(LivingEntity target, Entity sourceEntity) {
		if (!(target.level() instanceof ServerLevel serverLevel)) {
			return false;
		}
		if (!(target instanceof Enemy) || target instanceof Kobold) {
			return false;
		}
		if (!(sourceEntity instanceof Player player)) {
			return false;
		}
		return isAreaProtected(serverLevel, player, target.blockPosition());
	}
}
