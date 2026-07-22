package twilightforest.compat;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import twilightforest.TwilightForestMod;

/** Shared, loader-independent policy tags used by optional integrations. */
public final class CompatTags {
	public static final TagKey<Block> DIGGUS_UNSAFE_BLOCKS = block("compat/diggus_unsafe_blocks");
	public static final TagKey<Block> CARRY_ON_SAFE_BLOCKS = block("compat/carryon_safe_blocks");
	public static final TagKey<Block> CARRY_ON_UNSAFE_BLOCKS = block("compat/carryon_unsafe_blocks");
	public static final TagKey<EntityType<?>> CARRY_ON_SAFE_ENTITIES = entity("compat/carryon_safe_entities");
	public static final TagKey<EntityType<?>> CARRY_ON_UNSAFE_ENTITIES = entity("compat/carryon_unsafe_entities");

	private CompatTags() {
	}

	private static TagKey<Block> block(String path) {
		return TagKey.create(Registries.BLOCK, TwilightForestMod.prefix(path));
	}

	private static TagKey<EntityType<?>> entity(String path) {
		return TagKey.create(Registries.ENTITY_TYPE, TwilightForestMod.prefix(path));
	}
}
