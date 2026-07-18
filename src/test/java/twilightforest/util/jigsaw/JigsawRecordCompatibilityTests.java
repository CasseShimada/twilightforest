package twilightforest.util.jigsaw;

import net.minecraft.core.BlockPos;
import net.minecraft.core.FrontAndTop;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JigsawRecordCompatibilityTests {
	@Test
	void preservesPoolAcrossNbtRoundTrip() {
		JigsawRecord original = new JigsawRecord(
			7,
			FrontAndTop.UP_SOUTH,
			new BlockPos(3, -2, 11),
			"twilightforest:camp/main_path",
			"twilightforest:camp/path",
			"twilightforest:camp/path"
		);

		assertEquals(original, JigsawRecord.fromTag(original.toTag()));
	}

	@Test
	void missingLegacyPoolDefaultsToMinecraftEmptyAndIsRetainedOnSave() {
		CompoundTag legacy = new CompoundTag();
		legacy.putInt("priority", 2);
		legacy.putInt("facing", FrontAndTop.NORTH_UP.ordinal());
		legacy.putInt("x", 1);
		legacy.putInt("y", 2);
		legacy.putInt("z", 3);
		legacy.putString("name", "legacy");
		legacy.putString("target", "target");

		JigsawRecord decoded = JigsawRecord.fromTag(legacy);
		assertEquals("minecraft:empty", decoded.pool());
		assertEquals("minecraft:empty", decoded.toTag().getStringOr("pool", ""));
	}
}
