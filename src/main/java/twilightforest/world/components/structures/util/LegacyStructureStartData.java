package twilightforest.world.components.structures.util;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public final class LegacyStructureStartData {
	public static final String CONQUERED_KEY = "conquered";

	private LegacyStructureStartData() {
	}

	public static Optional<Boolean> read(CompoundTag tag) {
		return tag.getBoolean(CONQUERED_KEY);
	}

	public static void writePending(CompoundTag tag, @Nullable Boolean conquered) {
		if (conquered != null) {
			tag.putBoolean(CONQUERED_KEY, conquered);
		}
	}
}
