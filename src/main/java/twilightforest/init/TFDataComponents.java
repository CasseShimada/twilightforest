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

	public static void init() {
	}

	private static @NotNull <T> DataComponentType<T> register(String name, DataComponentType<T> type) {
		return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, TwilightForestMod.prefix(name), type);
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
