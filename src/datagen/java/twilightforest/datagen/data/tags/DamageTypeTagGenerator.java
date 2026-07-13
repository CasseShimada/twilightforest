package twilightforest.datagen.data.tags;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import twilightforest.init.TFDamageTypes;
import twilightforest.tags.TFDamageTypeTags;

import java.util.concurrent.CompletableFuture;

public final class DamageTypeTagGenerator extends FabricTagsProvider<DamageType> {
	public DamageTypeTagGenerator(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, Registries.DAMAGE_TYPE, registries);
	}

	@Override
	protected void addTags(HolderLookup.Provider registries) {
		this.builder(DamageTypeTags.ALWAYS_MOST_SIGNIFICANT_FALL).add(TFDamageTypes.EXPIRED);
		this.builder(DamageTypeTags.AVOIDS_GUARDIAN_THORNS)
			.add(TFDamageTypes.GHAST_TEAR)
			.add(TFDamageTypes.HYDRA_FIRE)
			.add(TFDamageTypes.HYDRA_MORTAR)
			.add(TFDamageTypes.SLAM)
			.add(TFDamageTypes.YEETED);
		this.builder(DamageTypeTags.BYPASSES_ARMOR)
			.add(TFDamageTypes.GHAST_TEAR)
			.add(TFDamageTypes.LICH_BOLT)
			.add(TFDamageTypes.LICH_BOMB)
			.add(TFDamageTypes.SLAM)
			.add(TFDamageTypes.YEETED)
			.add(TFDamageTypes.LEAF_BRAIN)
			.add(TFDamageTypes.LOST_WORDS)
			.add(TFDamageTypes.SCHOOLED)
			.add(TFDamageTypes.LIFEDRAIN)
			.add(TFDamageTypes.EXPIRED)
			.add(TFDamageTypes.ACID_RAIN)
			.add(TFDamageTypes.OMINOUS_FIRE);
		this.builder(DamageTypeTags.BYPASSES_ENCHANTMENTS).add(TFDamageTypes.FALLING_ICE);
		this.builder(DamageTypeTags.BYPASSES_INVULNERABILITY).add(TFDamageTypes.EXPIRED);
		this.builder(DamageTypeTags.BYPASSES_RESISTANCE).add(TFDamageTypes.EXPIRED);
		this.builder(DamageTypeTags.BYPASSES_WOLF_ARMOR)
			.add(TFDamageTypes.LICH_BOLT)
			.add(TFDamageTypes.LICH_BOMB)
			.add(TFDamageTypes.FROZEN)
			.add(TFDamageTypes.LEAF_BRAIN)
			.add(TFDamageTypes.LOST_WORDS)
			.add(TFDamageTypes.SCHOOLED)
			.add(TFDamageTypes.LIFEDRAIN)
			.add(TFDamageTypes.EXPIRED)
			.add(TFDamageTypes.ACID_RAIN);
		this.builder(DamageTypeTags.DAMAGES_HELMET)
			.add(TFDamageTypes.GHAST_TEAR)
			.add(TFDamageTypes.THROWN_BLOCK);
		this.builder(DamageTypeTags.IS_EXPLOSION).add(TFDamageTypes.LICH_BOMB);
		this.builder(DamageTypeTags.IS_FALL).add(TFDamageTypes.YEETED);
		this.builder(DamageTypeTags.NO_ANGER).add(TFDamageTypes.SLAM);
		this.builder(DamageTypeTags.NO_KNOCKBACK).add(TFDamageTypes.OMINOUS_FIRE);
		this.builder(DamageTypeTags.PANIC_ENVIRONMENTAL_CAUSES).add(TFDamageTypes.OMINOUS_FIRE);
		this.builder(DamageTypeTags.WITHER_IMMUNE_TO).add(TFDamageTypes.OMINOUS_FIRE);
		this.builder(TFDamageTypeTags.BREAKS_LICH_SHIELDS)
			.add(TFDamageTypes.LICH_BOLT)
			.add(TFDamageTypes.TWILIGHT_SCEPTER)
			.add(DamageTypes.MAGIC)
			.add(DamageTypes.INDIRECT_MAGIC)
			.add(DamageTypes.SONIC_BOOM);
	}
}
