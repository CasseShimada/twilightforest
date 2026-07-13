package twilightforest.datagen.data.tags;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageType;
import twilightforest.init.TFDamageTypes;

import java.util.concurrent.CompletableFuture;

public final class DamageTypeTagGenerator extends FabricTagsProvider<DamageType> {
	public DamageTypeTagGenerator(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, Registries.DAMAGE_TYPE, registries);
	}

	@Override
	protected void addTags(HolderLookup.Provider registries) {
		this.builder(DamageTypeTags.ALWAYS_MOST_SIGNIFICANT_FALL).add(TFDamageTypes.EXPIRED);
		this.builder(DamageTypeTags.BYPASSES_ENCHANTMENTS).add(TFDamageTypes.FALLING_ICE);
		this.builder(DamageTypeTags.BYPASSES_INVULNERABILITY).add(TFDamageTypes.EXPIRED);
		this.builder(DamageTypeTags.BYPASSES_RESISTANCE).add(TFDamageTypes.EXPIRED);
		this.builder(DamageTypeTags.IS_EXPLOSION).add(TFDamageTypes.LICH_BOMB);
		this.builder(DamageTypeTags.IS_FALL).add(TFDamageTypes.YEETED);
		this.builder(DamageTypeTags.NO_ANGER).add(TFDamageTypes.SLAM);
		this.builder(DamageTypeTags.NO_KNOCKBACK).add(TFDamageTypes.OMINOUS_FIRE);
		this.builder(DamageTypeTags.PANIC_ENVIRONMENTAL_CAUSES).add(TFDamageTypes.OMINOUS_FIRE);
		this.builder(DamageTypeTags.WITHER_IMMUNE_TO).add(TFDamageTypes.OMINOUS_FIRE);
	}
}
