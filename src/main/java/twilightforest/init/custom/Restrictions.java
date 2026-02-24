package twilightforest.init.custom;

import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import twilightforest.TFRegistries;
import twilightforest.TwilightForestMod;
import twilightforest.init.TFBiomes;
import twilightforest.init.TFBlocks;
import twilightforest.init.TFItems;
import twilightforest.init.TFStructures;
import twilightforest.util.Enforcement;
import twilightforest.util.Restriction;
import twilightforest.util.registry.DeferredHolder;

import java.util.List;

public class Restrictions {
	public static final Codec<Holder<Restriction>> CODEC = RegistryFileCodec.create(TFRegistries.Keys.RESTRICTIONS, Restriction.CODEC, false);

	public static final ResourceKey<Restriction> DARK_FOREST = makeKey(TFBiomes.DARK_FOREST.identifier());
	public static final ResourceKey<Restriction> DARK_FOREST_CENTER = makeKey(TFBiomes.DARK_FOREST_CENTER.identifier());
	public static final ResourceKey<Restriction> FINAL_PLATEAU = makeKey(TFBiomes.FINAL_PLATEAU.identifier());
	public static final ResourceKey<Restriction> FIRE_SWAMP = makeKey(TFBiomes.FIRE_SWAMP.identifier());
	public static final ResourceKey<Restriction> GLACIER = makeKey(TFBiomes.GLACIER.identifier());
	public static final ResourceKey<Restriction> HIGHLANDS = makeKey(TFBiomes.HIGHLANDS.identifier());
	public static final ResourceKey<Restriction> SNOWY_FOREST = makeKey(TFBiomes.SNOWY_FOREST.identifier());
	public static final ResourceKey<Restriction> SWAMP = makeKey(TFBiomes.SWAMP.identifier());
	public static final ResourceKey<Restriction> THORNLANDS = makeKey(TFBiomes.THORNLANDS.identifier());

	private static ResourceKey<Restriction> makeKey(Identifier name) {
		return ResourceKey.create(TFRegistries.Keys.RESTRICTIONS, name);
	}

	private static ResourceKey<Enforcement> enforcementKey(DeferredHolder<Enforcement, Enforcement> holder) {
		return ResourceKey.create(TFRegistries.Keys.ENFORCEMENT, holder.getId());
	}

	public static void bootstrap(BootstrapContext<Restriction> context) {
		context.register(DARK_FOREST, new Restriction(TFStructures.KNIGHT_STRONGHOLD, enforcementKey(Enforcements.DARKNESS), 0.0F, asStack(TFBlocks.LICH_TOWER_MINIATURE_STRUCTURE.get()), List.of(TwilightForestMod.prefix("progress_lich"))));
		context.register(DARK_FOREST_CENTER, new Restriction(TFStructures.DARK_TOWER, enforcementKey(Enforcements.DARKNESS), 0.0F, asStack(TFBlocks.KNIGHT_PHANTOM_TROPHY.get()), List.of(TwilightForestMod.prefix("progress_knights"))));
		context.register(FINAL_PLATEAU, new Restriction(TFStructures.FINAL_CASTLE, enforcementKey(Enforcements.ACID_RAIN), 1.5F, asStack(TFItems.LAMP_OF_CINDERS.get()), List.of(TwilightForestMod.prefix("progress_troll"))));
		context.register(FIRE_SWAMP, new Restriction(TFStructures.HYDRA_LAIR, enforcementKey(Enforcements.FIRE), 8.0F, asStack(TFItems.MEEF_STROGANOFF.get()), List.of(TwilightForestMod.prefix("progress_labyrinth"))));
		context.register(GLACIER, new Restriction(TFStructures.AURORA_PALACE, enforcementKey(Enforcements.FROST), 1.0F, asStack(TFItems.ALPHA_YETI_FUR.get()), List.of(TwilightForestMod.prefix("progress_yeti"))));
		context.register(HIGHLANDS, new Restriction(TFStructures.TROLL_CAVE, enforcementKey(Enforcements.ACID_RAIN), 0.5F, asStack(TFBlocks.UBEROUS_SOIL.get()), List.of(TwilightForestMod.prefix("progress_merge"))));
		context.register(SNOWY_FOREST, new Restriction(TFStructures.YETI_CAVE, enforcementKey(Enforcements.FROST), 0.0F, asStack(TFBlocks.LICH_TOWER_MINIATURE_STRUCTURE.get()), List.of(TwilightForestMod.prefix("progress_lich"))));
		context.register(SWAMP, new Restriction(TFStructures.LABYRINTH, enforcementKey(Enforcements.HUNGER), 1.0F, asStack(TFBlocks.LICH_TOWER_MINIATURE_STRUCTURE.get()), List.of(TwilightForestMod.prefix("progress_lich"))));
		context.register(THORNLANDS, new Restriction(TFStructures.FINAL_CASTLE, enforcementKey(Enforcements.ACID_RAIN), 1.0F, asStack(TFItems.LAMP_OF_CINDERS.get()), List.of(TwilightForestMod.prefix("progress_troll"))));
	}

	public static ItemStack asStack(ItemLike itemLike) {
		return new ItemStack(itemLike);
	}
}
