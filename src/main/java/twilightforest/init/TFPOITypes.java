package twilightforest.init;

import com.google.common.collect.ImmutableSet;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import twilightforest.TwilightForestMod;
import twilightforest.mixin.accessor.PoiTypesInvoker;

public class TFPOITypes {
	private static boolean registered;

	public static final ResourceKey<PoiType> GHAST_TRAP_KEY = ResourceKey.create(Registries.POINT_OF_INTEREST_TYPE, TwilightForestMod.prefix("ghast_trap"));
	public static PoiType GHAST_TRAP;

	public static void register() {
		if (registered) {
			return;
		}

		registered = true;
		GHAST_TRAP = new PoiType(ImmutableSet.copyOf(TFBlocks.GHAST_TRAP.get().getStateDefinition().getPossibleStates()), 0, 1);
		Holder.Reference<PoiType> holder = Registry.registerForHolder(BuiltInRegistries.POINT_OF_INTEREST_TYPE, GHAST_TRAP_KEY, GHAST_TRAP);
		PoiTypesInvoker.twilightforest$registerBlockStates(holder, GHAST_TRAP.matchingStates());
	}
}
