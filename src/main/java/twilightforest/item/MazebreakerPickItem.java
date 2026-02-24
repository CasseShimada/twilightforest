package twilightforest.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.block.state.BlockState;
import twilightforest.tags.TFBlockTags;

import javax.annotation.Nonnull;

public class MazebreakerPickItem extends Item {
	public MazebreakerPickItem(ToolMaterial material, Properties properties) {
		super(properties.pickaxe(material, 1.0F, -2.8F));
	}

	@Override
	public float getDestroySpeed(@Nonnull ItemStack stack, BlockState state) {
		float destroySpeed = super.getDestroySpeed(stack, state);
		return state.is(TFBlockTags.MAZEBREAKER_ACCELERATED) ? destroySpeed * 16.0F : destroySpeed;
	}
}
