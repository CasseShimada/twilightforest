package twilightforest.util.woods;

import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import twilightforest.TwilightForestMod;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class TFWoodTypes {

	public static final BlockSetType TWILIGHT_OAK_SET = registerBlockSetType(new BlockSetType(TwilightForestMod.prefix("twilight_oak").toString()));
	public static final BlockSetType CANOPY_WOOD_SET = registerBlockSetType(new BlockSetType(TwilightForestMod.prefix("canopy").toString()));
	public static final BlockSetType MANGROVE_WOOD_SET = registerBlockSetType(new BlockSetType(TwilightForestMod.prefix("mangrove").toString()));
	public static final BlockSetType DARK_WOOD_SET = registerBlockSetType(new BlockSetType(TwilightForestMod.prefix("dark").toString()));
	public static final BlockSetType TIME_WOOD_SET = registerBlockSetType(new BlockSetType(TwilightForestMod.prefix("time").toString()));
	public static final BlockSetType TRANSFORMATION_WOOD_SET = registerBlockSetType(new BlockSetType(TwilightForestMod.prefix("transformation").toString()));
	public static final BlockSetType MINING_WOOD_SET = registerBlockSetType(new BlockSetType(TwilightForestMod.prefix("mining").toString()));
	public static final BlockSetType SORTING_WOOD_SET = registerBlockSetType(new BlockSetType(TwilightForestMod.prefix("sorting").toString()));

	public static final WoodType TWILIGHT_OAK_WOOD_TYPE = registerWoodType(new WoodType(TwilightForestMod.prefix("twilight_oak").toString(), TWILIGHT_OAK_SET));
	public static final WoodType CANOPY_WOOD_TYPE = registerWoodType(new WoodType(TwilightForestMod.prefix("canopy").toString(), CANOPY_WOOD_SET));
	public static final WoodType MANGROVE_WOOD_TYPE = registerWoodType(new WoodType(TwilightForestMod.prefix("mangrove").toString(), MANGROVE_WOOD_SET));
	public static final WoodType DARK_WOOD_TYPE = registerWoodType(new WoodType(TwilightForestMod.prefix("dark").toString(), DARK_WOOD_SET));
	public static final WoodType TIME_WOOD_TYPE = registerWoodType(new WoodType(TwilightForestMod.prefix("time").toString(), TIME_WOOD_SET));
	public static final WoodType TRANSFORMATION_WOOD_TYPE = registerWoodType(new WoodType(TwilightForestMod.prefix("transformation").toString(), TRANSFORMATION_WOOD_SET));
	public static final WoodType MINING_WOOD_TYPE = registerWoodType(new WoodType(TwilightForestMod.prefix("mining").toString(), MINING_WOOD_SET));
	public static final WoodType SORTING_WOOD_TYPE = registerWoodType(new WoodType(TwilightForestMod.prefix("sorting").toString(), SORTING_WOOD_SET));

	private static BlockSetType registerBlockSetType(BlockSetType type) {
		try {
			Method method = findRegistrationMethod(BlockSetType.class, BlockSetType.class);
			return (BlockSetType) method.invoke(null, type);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("Failed registering BlockSetType " + type, e);
		}
	}

	private static WoodType registerWoodType(WoodType type) {
		try {
			Method method = findRegistrationMethod(WoodType.class, WoodType.class);
			return (WoodType) method.invoke(null, type);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("Failed registering WoodType " + type, e);
		}
	}

	private static Method findRegistrationMethod(Class<?> owner, Class<?> paramType) throws NoSuchMethodException {
		for (Method method : owner.getDeclaredMethods()) {
			if (!Modifier.isStatic(method.getModifiers())) {
				continue;
			}
			if (method.getParameterCount() != 1 || method.getParameterTypes()[0] != paramType) {
				continue;
			}
			if (method.getReturnType() != paramType) {
				continue;
			}
			method.setAccessible(true);
			return method;
		}
		throw new NoSuchMethodException("No registration method found for " + owner.getName());
	}
}
