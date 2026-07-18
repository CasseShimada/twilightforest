package twilightforest.init;

import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Unit;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import twilightforest.TwilightForestMod;
import twilightforest.components.item.*;
import twilightforest.entity.MagicPaintingVariant;
import twilightforest.init.custom.MagicPaintingVariants;

import java.util.UUID;

public class TFDataComponents {
	public static final DataComponentType<Unit> EMPERORS_CLOTH = register("emperors_cloth", DataComponentType.<Unit>builder().persistent(Unit.CODEC).networkSynchronized(StreamCodec.unit(Unit.INSTANCE)).build());
	public static final DataComponentType<PotionFlaskComponent> POTION_FLASK_CONTENTS = register("flask_contents", DataComponentType.<PotionFlaskComponent>builder().persistent(PotionFlaskComponent.CODEC).networkSynchronized(PotionFlaskComponent.STREAM_CODEC).build());
	public static final DataComponentType<Unit> INFINITE_GLASS_SWORD = register("infinite_glass_sword", DataComponentType.<Unit>builder().persistent(Unit.CODEC).networkSynchronized(StreamCodec.unit(Unit.INSTANCE)).build());
	public static final DataComponentType<UUID> THROWN_PROJECTILE = register("thrown_projectile", DataComponentType.<UUID>builder().persistent(UUIDUtil.CODEC).networkSynchronized(UUIDUtil.STREAM_CODEC).build());
	public static final DataComponentType<String> EXPERIMENT_115_VARIANTS = register("e115_variant", DataComponentType.<String>builder().persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8).build());
	public static final DataComponentType<SkullCandles> SKULL_CANDLES = register("skull_candles", DataComponentType.<SkullCandles>builder().persistent(SkullCandles.CODEC).networkSynchronized(SkullCandles.STREAM_CODEC).build());
	public static final DataComponentType<CandelabraData> CANDELABRA_DATA = register("candelabra_data", DataComponentType.<CandelabraData>builder().persistent(CandelabraData.CODEC).build());
	public static final DataComponentType<Holder<MagicPaintingVariant>> MAGIC_PAINTING_VARIANT = register("magic_painting_variant", DataComponentType.<Holder<MagicPaintingVariant>>builder().persistent(MagicPaintingVariants.CODEC).networkSynchronized(MagicPaintingVariants.STREAM_CODEC).build());
	public static final DataComponentType<Unit> TRANSLATABLE_BOOK = register("translatable_book", DataComponentType.<Unit>builder().persistent(Unit.CODEC).networkSynchronized(StreamCodec.unit(Unit.INSTANCE)).build());
	public static final DataComponentType<JarLid> JAR_LID = register("jar_lid", JarLid.CODEC);
	public static final DataComponentType<Integer> CASKET_DAMAGE = register("casket_damage", DataComponentType.<Integer>builder().persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT).build());

	public static final DataComponentType<OreScannerComponent> ORE_SCANNING = register("ore_scanner", OreScannerComponent.CODEC);
	public static final DataComponentType<OreScannerData> ORE_DATA = register("ore_data", OreScannerData.CODEC, OreScannerData.STREAM_CODEC);
	public static final DataComponentType<Integer> ORE_LOADING = register("ore_loading", DataComponentType.<Integer>builder().persistent(ExtraCodecs.NON_NEGATIVE_INT.orElse(0)).networkSynchronized(ByteBufCodecs.VAR_INT).cacheEncoding().build());
	public static final DataComponentType<Integer> ORE_RANGE = register("ore_range", DataComponentType.<Integer>builder().persistent(ExtraCodecs.NON_NEGATIVE_INT.orElse(1)).networkSynchronized(ByteBufCodecs.VAR_INT).cacheEncoding().build());
	public static final DataComponentType<Block> ORE_FILTER = register("ore_filter", DataComponentType.<Block>builder().persistent(BuiltInRegistries.BLOCK.byNameCodec().orElse(Blocks.AIR)).networkSynchronized(ByteBufCodecs.registry(Registries.BLOCK)).cacheEncoding().build());

	public static final DataComponentType<Unit> IS_TRAVELLERS_GEAR = unit("travellers_armor");
	public static final DataComponentType<ItemAttributeModifiers> STORED_BROKEN_ATTRIBUTES = register("stored_broken_attributes", DataComponentType.<ItemAttributeModifiers>builder().persistent(ItemAttributeModifiers.CODEC).networkSynchronized(ItemAttributeModifiers.STREAM_CODEC).cacheEncoding().build());

	public static final DataComponentType<Unit> TRAVELLERS_HAS_CHESTPLATE = unit("has_travellers_chestplate");
	public static final DataComponentType<Unit> TRAVELLERS_HAS_GLOVES = unit("has_travellers_gloves");
	public static final DataComponentType<Unit> TRAVELLERS_HAS_BELT = unit("has_travellers_belt");
	public static final DataComponentType<Unit> TRAVELLERS_HAS_WINGS = unit("has_travellers_wings");
	public static final DataComponentType<Unit> TRAVELLERS_HAS_BOOTS = unit("has_travellers_boots");

	public static final DataComponentType<Float> AUTO_REPAIR_PROBABILITY = register("auto_repair_probability", cached(ExtraCodecs.POSITIVE_FLOAT, ByteBufCodecs.FLOAT));
	public static final DataComponentType<Float> ZOOM_ABILITY_MODIFIER = register("zoom_ability_modifier", cached(ExtraCodecs.POSITIVE_FLOAT, ByteBufCodecs.FLOAT));
	public static final DataComponentType<Unit> RED_THREAD_VISION = unit("red_thread_vision");
	public static final DataComponentType<Unit> STEALTH_CROUCHING = unit("stealth_crouching");
	public static final DataComponentType<Unit> ARROW_MAGNETISM = unit("arrow_magnetism");
	public static final DataComponentType<Float> EFFICIENT_EATER = register("efficient_eater", cached(Codec.FLOAT, ByteBufCodecs.FLOAT));
	public static final DataComponentType<Float> PERFECT_DODGE_PROBABILITY = register("perfect_dodge_probability", cached(ExtraCodecs.POSITIVE_FLOAT, ByteBufCodecs.FLOAT));
	public static final DataComponentType<Integer> HASTE_AMPLIFIER = register("haste_amplifier", cached(ExtraCodecs.UNSIGNED_BYTE, ByteBufCodecs.INT));
	public static final DataComponentType<Unit> SWAP_HOTBAR_ABILITY = unit("swap_hotbar_ability");
	public static final DataComponentType<Unit> SWAP_HOTBAR_MODIFIER = unit("swap_hotbar_modifier");
	public static final DataComponentType<Integer> HIGH_JUMP_AMPLIFIER = register("high_jump_amplifier", cached(ExtraCodecs.UNSIGNED_BYTE, ByteBufCodecs.INT));
	public static final DataComponentType<Float> GRADUALLY_GLIDING_MULTIPLIER = register("gradually_gliding_multiplier", cached(ExtraCodecs.POSITIVE_FLOAT, ByteBufCodecs.FLOAT));
	public static final DataComponentType<Float> AGILE_RANGER_MODIFIER = register("agile_ranger_modifier", cached(ExtraCodecs.POSITIVE_FLOAT, ByteBufCodecs.FLOAT));
	public static final DataComponentType<Unit> DOUBLE_JUMP = unit("double_jump");
	public static final DataComponentType<Long> SIDESTEP_COOLDOWN = register("sidestep_cooldown", cached(Codec.LONG, ByteBufCodecs.VAR_LONG));
	public static final DataComponentType<Double> STRAIGHT_AHEAD_MULTIPLIER = register("straight_ahead_multiplier", cached(Codec.DOUBLE, ByteBufCodecs.DOUBLE));
	public static final DataComponentType<Float> SLIMY_SOLES_COEFFICIENT = register("slimy_soles_coefficient", cached(ExtraCodecs.POSITIVE_FLOAT, ByteBufCodecs.FLOAT));
	public static final DataComponentType<Unit> WATER_WALK = unit("water_walk");
	public static final DataComponentType<Unit> ALL_NIGHT_GOGGLES = unit("all_night_goggles");
	public static final DataComponentType<ItemDisplayContents> ITEM_DISPLAY = register("item_display", DataComponentType.<ItemDisplayContents>builder().persistent(ItemDisplayContents.CODEC).networkSynchronized(ItemDisplayContents.STREAM_CODEC).cacheEncoding().build());
	public static final DataComponentType<Unit> UNRESTRAINED = unit("unrestrained");

	public static final DataComponentType<Unit> SWIFT_SWIM = unit("swift_swim");
	public static final DataComponentType<Unit> HIGH_STEP = unit("high_step");
	public static final DataComponentType<Unit> AQUATIC_AGILITY = unit("aquatic_agility");

	public static void init() {
	}

	private static @NotNull <T> DataComponentType<T> register(String name, DataComponentType<T> type) {
		return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, TwilightForestMod.prefix(name), type);
	}

	private static DataComponentType<Unit> unit(String name) {
		return register(name, DataComponentType.<Unit>builder()
			.persistent(Unit.CODEC)
			.networkSynchronized(StreamCodec.unit(Unit.INSTANCE))
			.cacheEncoding()
			.build());
	}

	private static <T> DataComponentType<T> cached(Codec<T> codec, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
		return DataComponentType.<T>builder()
			.persistent(codec)
			.networkSynchronized(streamCodec)
			.cacheEncoding()
			.build();
	}

	private static @NotNull <T> DataComponentType<T> register(String name, final Codec<T> codec) {
		return register(name, codec, null);
	}

	private static @NotNull <T> DataComponentType<T> register(String name, final Codec<T> codec, @Nullable final StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
		if (streamCodec == null) {
			return register(name, DataComponentType.<T>builder().persistent(codec).build());
		} else {
			return register(name, DataComponentType.<T>builder().persistent(codec).networkSynchronized(streamCodec).build());
		}
	}
}
