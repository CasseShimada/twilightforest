package twilightforest.block.entity;

import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DryingRackSerializationTests {
	@Test
	void emptyRackRoundTripsWithoutCodecError() {
		assertSame(ItemStack.OPTIONAL_CODEC, DryingRackBlockEntity.STORED_ITEM_CODEC);
		Tag encoded = DryingRackBlockEntity.STORED_ITEM_CODEC
			.encodeStart(NbtOps.INSTANCE, ItemStack.EMPTY)
			.getOrThrow();
		ItemStack decoded = DryingRackBlockEntity.STORED_ITEM_CODEC
			.parse(NbtOps.INSTANCE, encoded)
			.getOrThrow();

		assertTrue(decoded.isEmpty());
	}
}
