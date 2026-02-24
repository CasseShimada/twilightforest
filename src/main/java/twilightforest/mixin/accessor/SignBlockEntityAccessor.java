package twilightforest.mixin.accessor;

import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SignBlockEntity.class)
public interface SignBlockEntityAccessor {
	@Accessor("frontText")
	SignText twilightforest$getFrontText();

	@Accessor("frontText")
	void twilightforest$setFrontText(SignText text);
}
