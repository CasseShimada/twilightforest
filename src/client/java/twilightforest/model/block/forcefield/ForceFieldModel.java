package twilightforest.client.model.block.forcefield;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import twilightforest.block.ForceFieldBlock;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class ForceFieldModel {
	private ForceFieldModel() {
	}

	protected static boolean skipRender(Map<ExtraDirection, List<Direction>> directions, ExtraDirection direction, boolean supposedToBe, List<ExtraDirection> parents, Direction side) {
		if (direction == null) return false;
		for (ExtraDirection parent : parents) if (!directions.containsKey(parent)) return true;
		boolean hasKey = directions.containsKey(direction);
		if (hasKey != supposedToBe) return true;
		if (hasKey) return directions.get(direction).contains(side);
		return false;
	}

	public static List<ExtraDirection> getExtraDirections(BlockState state, BlockGetter level, BlockPos pos) {
		List<ExtraDirection> directions = new ArrayList<>();

		boolean down = state.getValue(ForceFieldBlock.DOWN);
		boolean up = state.getValue(ForceFieldBlock.UP);
		boolean north = state.getValue(ForceFieldBlock.NORTH);
		boolean south = state.getValue(ForceFieldBlock.SOUTH);
		boolean west = state.getValue(ForceFieldBlock.WEST);
		boolean east = state.getValue(ForceFieldBlock.EAST);

		if (down) {
			directions.add(ExtraDirection.DOWN);
			if (north && ForceFieldBlock.cornerConnects(level, pos, Direction.DOWN, Direction.NORTH)) directions.add(ExtraDirection.DOWN_NORTH);
			if (south && ForceFieldBlock.cornerConnects(level, pos, Direction.DOWN, Direction.SOUTH)) directions.add(ExtraDirection.DOWN_SOUTH);
			if (west && ForceFieldBlock.cornerConnects(level, pos, Direction.DOWN, Direction.WEST)) directions.add(ExtraDirection.DOWN_WEST);
			if (east && ForceFieldBlock.cornerConnects(level, pos, Direction.DOWN, Direction.EAST)) directions.add(ExtraDirection.DOWN_EAST);
		}
		if (up) {
			directions.add(ExtraDirection.UP);
			if (north && ForceFieldBlock.cornerConnects(level, pos, Direction.UP, Direction.NORTH)) directions.add(ExtraDirection.UP_NORTH);
			if (south && ForceFieldBlock.cornerConnects(level, pos, Direction.UP, Direction.SOUTH)) directions.add(ExtraDirection.UP_SOUTH);
			if (west && ForceFieldBlock.cornerConnects(level, pos, Direction.UP, Direction.WEST)) directions.add(ExtraDirection.UP_WEST);
			if (east && ForceFieldBlock.cornerConnects(level, pos, Direction.UP, Direction.EAST)) directions.add(ExtraDirection.UP_EAST);
		}
		if (north) {
			directions.add(ExtraDirection.NORTH);
			if (west && ForceFieldBlock.cornerConnects(level, pos, Direction.NORTH, Direction.WEST)) directions.add(ExtraDirection.NORTH_WEST);
			if (east && ForceFieldBlock.cornerConnects(level, pos, Direction.NORTH, Direction.EAST)) directions.add(ExtraDirection.NORTH_EAST);
		}
		if (south) {
			directions.add(ExtraDirection.SOUTH);
			if (west && ForceFieldBlock.cornerConnects(level, pos, Direction.SOUTH, Direction.WEST)) directions.add(ExtraDirection.SOUTH_WEST);
			if (east && ForceFieldBlock.cornerConnects(level, pos, Direction.SOUTH, Direction.EAST)) directions.add(ExtraDirection.SOUTH_EAST);
		}
		if (west) directions.add(ExtraDirection.WEST);
		if (east) directions.add(ExtraDirection.EAST);

		return directions;
	}

	public enum ExtraDirection implements StringRepresentable {
		DOWN("down", 0, 1, 0),
		UP("up", 1, 0, 1),
		NORTH("north", 2, 2, 3),
		SOUTH("south", 3, 3, 2),
		WEST("west", 5, 4, 4),
		EAST("east", 4, 5, 5),

		DOWN_NORTH("down_north", 6, 10, 7),
		DOWN_SOUTH("down_south", 7, 11, 6),
		DOWN_WEST("down_west", 9, 12, 8),
		DOWN_EAST("down_east", 8, 13, 9),

		UP_NORTH("up_north", 10, 6, 11),
		UP_SOUTH("up_south", 11, 7, 10),
		UP_WEST("up_west", 13, 8, 12),
		UP_EAST("up_east", 12, 9, 13),

		NORTH_WEST("north_west", 15, 14, 16),
		NORTH_EAST("north_east", 14, 15, 17),
		SOUTH_WEST("south_west", 17, 16, 14),
		SOUTH_EAST("south_east", 16, 17, 15);

		private final String serializedName;
		private final int tex;
		private final int flipX;
		private final int flipY;

		ExtraDirection(String name, int tex, int flipX, int flipY) {
			this.serializedName = name;
			this.tex = tex;
			this.flipX = flipX;
			this.flipY = flipY;
		}

		public static ExtraDirection byName(String name) {
			for (ExtraDirection dir : values()) {
				if (dir.serializedName.equals(name)) return dir;
			}
			return UP;
		}

		public int textureIndex() {
			return this.tex;
		}

		public int flipX() {
			return this.flipX;
		}

		public int flipY() {
			return this.flipY;
		}

		public ExtraDirection mirrored(Direction.Axis axis) {
			return switch (axis) {
				case X -> values()[this.flipX];
				case Y -> values()[this.flipY];
				case Z -> values()[this.flipX];
			};
		}

		@Override
		public String getSerializedName() {
			return this.serializedName;
		}
	}
}
