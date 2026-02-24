package twilightforest.mixin;

import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import twilightforest.world.components.TFGrassColorModifier;
import twilightforest.world.components.TFGrassColorModifierHolder;

@Mixin(Biome.class)
public class BiomeMixin implements TFGrassColorModifierHolder {
	@Unique
	private TFGrassColorModifier twilightforest$grassColorModifier = TFGrassColorModifier.NONE;

	@Override
	public TFGrassColorModifier twilightforest$getGrassColorModifier() {
		return twilightforest$grassColorModifier;
	}

	@Override
	public void twilightforest$setGrassColorModifier(TFGrassColorModifier modifier) {
		this.twilightforest$grassColorModifier = modifier == null ? TFGrassColorModifier.NONE : modifier;
	}
}
