package twilightforest.item.recipe;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.minecraft.resources.RegistryOps;
import twilightforest.TwilightForestMod;
import twilightforest.config.TFConfig;

public class UncraftingTableCondition implements ResourceCondition {

	public static final UncraftingTableCondition INSTANCE = new UncraftingTableCondition();
	public static final MapCodec<UncraftingTableCondition> CODEC = MapCodec.unit(INSTANCE);
	public static final ResourceConditionType<UncraftingTableCondition> TYPE = ResourceConditionType.create(
		TwilightForestMod.prefix("uncrafting_table_enabled"),
		CODEC
	);

	@Override
	public ResourceConditionType<?> getType() {
		return TYPE;
	}

	@Override
	public boolean test(RegistryOps.RegistryInfoLookup registryInfo) {
		return !TFConfig.disableEntireTable;
	}

	@Override
	public String toString() {
		return "Uncrafting Table Enabled";
	}
}
