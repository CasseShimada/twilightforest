package twilightforest.block;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.BeaconBeamBlock;
import net.minecraft.world.level.block.TransparentBlock;

public class AuroralizedGlassBlock extends TransparentBlock implements BeaconBeamBlock {

	public AuroralizedGlassBlock(Properties properties) {
		super(properties);
	}

	@Override
	public DyeColor getColor() {
		return DyeColor.CYAN;
	}
}
