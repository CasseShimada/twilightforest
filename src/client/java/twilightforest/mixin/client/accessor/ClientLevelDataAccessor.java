package twilightforest.mixin.client.accessor;

import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientLevel.ClientLevelData.class)
public interface ClientLevelDataAccessor {
	@Accessor("dayTime")
	long twilightforest$getDayTime();
}
