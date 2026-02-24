package twilightforest.compat.jei.renderers;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import twilightforest.compat.jei.FakeEntityType;

public class EntityHelper implements IIngredientHelper<FakeEntityType> {

	@Override
	public IIngredientType<FakeEntityType> getIngredientType() {
		return FakeEntityType.ENTITY_TYPE;
	}

	@Override
	public String getDisplayName(FakeEntityType type) {
		return BuiltInRegistries.ENTITY_TYPE.getOptional(type.type())
			.map(entityType -> entityType.getDescription().getString())
			.orElse("");
	}

	@Override
	public Object getUid(FakeEntityType type, UidContext context) {
		return this.getIdentifier(type).toString();
	}

	@Override
	public Identifier getIdentifier(FakeEntityType type) {
		return type.type().identifier();
	}

	@Override
	public FakeEntityType copyIngredient(FakeEntityType type) {
		return type;
	}

	@Override
	public String getErrorInfo(@Nullable FakeEntityType type) {
		if (type == null) return "null";
		return type.type().identifier().toString();
	}
}
