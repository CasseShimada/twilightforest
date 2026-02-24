package twilightforest.client.properties;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

public record NaturalDimension() implements ConditionalItemModelProperty {

	public static final MapCodec<NaturalDimension> TYPE = MapCodec.unit(new NaturalDimension());

	public boolean get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed, ItemDisplayContext context) {
		return level != null && level.dimensionType().hasSkyLight();
	}

	@Override
	public MapCodec<? extends ConditionalItemModelProperty> type() {
		return TYPE;
	}
}
