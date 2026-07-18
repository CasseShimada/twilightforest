package twilightforest.init;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.Entity;
import twilightforest.TwilightForestMod;
import twilightforest.components.entity.*;
import twilightforest.components.item.OreScannerComponent;
import twilightforest.util.Codecs;
import twilightforest.util.LegacyAttachmentDataFix;

import java.util.function.Supplier;

public class TFDataAttachments {
	public static void init() {
		// Ensure attachments are registered during mod initialization.
	}

	public static final AttachmentType<Boolean> FEATHER_FAN = AttachmentRegistry.create(TwilightForestMod.prefix("feather_fan_falling"), builder ->
		builder.initializer(() -> false).persistent(LegacyAttachmentDataFix.wrappedScalarCodec(Codec.BOOL, "feather_fan_falling")));

	public static final AttachmentType<CompoundTag> LEGACY_FRAMEWORK_DATA = AttachmentRegistry.create(TwilightForestMod.prefix("legacy_framework_data"), builder ->
		builder.initializer(CompoundTag::new).persistent(CompoundTag.CODEC).copyOnDeath());

	public static final AttachmentType<PotionFlaskTrackingAttachment> FLASK_DOSES = AttachmentRegistry.create(TwilightForestMod.prefix("flask_doses"), builder ->
		builder.initializer(PotionFlaskTrackingAttachment::new).persistent(PotionFlaskTrackingAttachment.CODEC));

	public static final AttachmentType<FortificationShieldAttachment> FORTIFICATION_SHIELDS = AttachmentRegistry.create(TwilightForestMod.prefix("fortification_shields"), builder ->
		builder.initializer(FortificationShieldAttachment::new).persistent(FortificationShieldAttachment.CODEC));

	public static final AttachmentType<GiantPickaxeMiningAttachment> GIANT_PICKAXE_MINING = AttachmentRegistry.create(TwilightForestMod.prefix("giant_pickaxe_mining"), builder ->
		builder.initializer(GiantPickaxeMiningAttachment::new).persistent(GiantPickaxeMiningAttachment.CODEC));

	public static final AttachmentType<OreScannerComponent> ORE_SCANNER = AttachmentRegistry.create(TwilightForestMod.prefix("ore_scanner"), builder ->
		builder.initializer(OreScannerComponent::getEmpty).persistent(OreScannerComponent.CODEC));

	public static final AttachmentType<YetiThrowAttachment> YETI_THROWING = AttachmentRegistry.create(TwilightForestMod.prefix("yeti_throwing"), builder ->
		builder.initializer(YetiThrowAttachment::new).persistent(YetiThrowAttachment.CODEC));

	public static final AttachmentType<MultiplayerInclusivityAttachment> MULTIPLAYER_FIGHT = AttachmentRegistry.createDefaulted(TwilightForestMod.prefix("multiplayer_fight"), MultiplayerInclusivityAttachment::new);

	public static final AttachmentType<TFPortalAttachment> TF_PORTAL_COOLDOWN = AttachmentRegistry.createDefaulted(TwilightForestMod.prefix("tf_portal_cooldown"), TFPortalAttachment::new);

	public static final AttachmentType<SmashBlocksEnchantmentAttachment> SMASH_BLOCKS = AttachmentRegistry.create(TwilightForestMod.prefix("smash_blocks"), builder ->
		builder.initializer(SmashBlocksEnchantmentAttachment::new).persistent(SmashBlocksEnchantmentAttachment.CODEC));

	public static final AttachmentType<GameProfile> ZOMBIFIED_PLAYER = AttachmentRegistry.create(TwilightForestMod.prefix("zombified_player"), builder ->
		builder.initializer(() -> UUIDUtil.createOfflineProfile("GizmoTheMoonPig")).persistent(Codecs.SIMPLE_GAME_PROFILE));

	public static final AttachmentType<Unit> LEASH_PATHFINDER_OVERRIDE = AttachmentRegistry.create(TwilightForestMod.prefix("leashed_pathfinder_override"), builder ->
		builder.initializer(() -> Unit.INSTANCE).persistent(LegacyAttachmentDataFix.unitCodec()));

	public static final AttachmentType<Unit> BANISHED_TO_TWILIGHT_FOREST = AttachmentRegistry.create(TwilightForestMod.prefix("twilightforest_banished"), builder ->
		builder.initializer(() -> Unit.INSTANCE).persistent(LegacyAttachmentDataFix.unitCodec()).copyOnDeath());

	public static final AttachmentType<Boolean> ENDER_BOW_ARROW = AttachmentRegistry.create(TwilightForestMod.prefix("ender_bow_arrow"), builder ->
		builder.initializer(() -> false).persistent(com.mojang.serialization.Codec.BOOL));

	public static final AttachmentType<CompoundTag> CHARM_DATA = AttachmentRegistry.create(TwilightForestMod.prefix("charm_data"), builder ->
		builder.initializer(CompoundTag::new).persistent(CompoundTag.CODEC).copyOnDeath());

	public static final AttachmentType<TravellersWingsAttachment> TRAVELLERS_WINGS = AttachmentRegistry.createDefaulted(TwilightForestMod.prefix("travellers_wings"), TravellersWingsAttachment::new);
	public static final AttachmentType<TravellersWingsAnimAttachment> TRAVELLERS_WINGS_ANIM = AttachmentRegistry.createDefaulted(TwilightForestMod.prefix("travellers_wings_anim"), TravellersWingsAnimAttachment::new);
	public static final AttachmentType<Boolean> IS_USING_GOGGLES_ZOOM_MODIFIER = persistent("is_using_goggles_zoom_modifier", () -> false, LegacyAttachmentDataFix.wrappedScalarCodec(Codec.BOOL, "zooming"), ByteBufCodecs.BOOL);
	public static final AttachmentType<Boolean> TRAVELLERS_GOGGLES_RED_THREAD_VISION = persistent("travellers_goggles_red_thread_vision", () -> true, LegacyAttachmentDataFix.wrappedScalarCodec(Codec.BOOL, "red_thread_vision"));
	public static final AttachmentType<Long> LAST_TICK_WATER_WALKING = persistent("last_tick_water_walking", () -> 0L, LegacyAttachmentDataFix.wrappedScalarCodec(Codec.LONG, "last_water_walking_tick"));
	public static final AttachmentType<Boolean> HAS_DOUBLE_JUMP = persistent("has_double_jump", () -> false, LegacyAttachmentDataFix.wrappedScalarCodec(Codec.BOOL, "double_jump"));
	public static final AttachmentType<Integer> DOUBLE_JUMP_VALIDATOR = persistent("double_jump_validator", () -> 0, LegacyAttachmentDataFix.wrappedScalarCodec(Codec.INT, "double_jump_count"));
	public static final AttachmentType<Integer> DOUBLE_JUMP_VALIDATOR_LAST_CHECK = persistent("double_jump_validator_last_check", () -> 0, LegacyAttachmentDataFix.wrappedScalarCodec(Codec.INT, "last_double_jump_count"));
	public static final AttachmentType<Double> TEMPORARY_SAVED_STRAIGHT_AHEAD = persistent("temporary_saved_straight_ahead", () -> 1.0D, LegacyAttachmentDataFix.wrappedScalarCodec(Codec.DOUBLE, "straight_ahead"));
	public static final AttachmentType<Long> LAST_DAMAGE_ARMOR_TIME = persistent("last_damage_armor_time", () -> 0L, LegacyAttachmentDataFix.wrappedScalarCodec(Codec.LONG, "last_armor_damage_timestamp"));
	public static final AttachmentType<Integer> LAST_JUMP_KEY_PRESS_TIME = persistent("last_jump_key_press_time", () -> 0, LegacyAttachmentDataFix.wrappedScalarCodec(Codec.INT, "last_jump_key_press"));
	public static final AttachmentType<Float> LAST_HORIZONTAL_IMPULSE = persistent("last_horizontal_impulse", () -> 0.0F, LegacyAttachmentDataFix.wrappedScalarCodec(Codec.FLOAT, "last_horizontal_impulse"));
	public static final AttachmentType<Float> LAST_NON_ZERO_HORIZONTAL_IMPULSE = persistent("last_non_horizontal_impulse", () -> 0.0F, LegacyAttachmentDataFix.wrappedScalarCodec(Codec.FLOAT, "last_non_horizontal_impulse"));
	public static final AttachmentType<Integer> LAST_HORIZONTAL_WALKING_TIME = persistent("last_horizontal_walking_time", () -> 0, LegacyAttachmentDataFix.wrappedScalarCodec(Codec.INT, "last_horizontal_walk_time"));
	public static final AttachmentType<Integer> SIDESTEP_VALIDATOR = persistent("sidestep_validator", () -> 0, LegacyAttachmentDataFix.wrappedScalarCodec(Codec.INT, "side_step_count"));
	public static final AttachmentType<Integer> SIDESTEP_VALIDATOR_LAST_CHECK = persistent("sidestep_validator_last_check", () -> 0, LegacyAttachmentDataFix.wrappedScalarCodec(Codec.INT, "last_side_step_count"));
	public static final AttachmentType<Boolean> IS_GRADUALLY_GLIDING = persistent("is_gradually_gliding", () -> false, LegacyAttachmentDataFix.wrappedScalarCodec(Codec.BOOL, "gliding"), ByteBufCodecs.BOOL);
	public static final AttachmentType<SlimySolesAttachment> SLIMY_SOLES_BOUNCE_INFO = persistent("slimy_soles_bounce_info", SlimySolesAttachment::new, SlimySolesAttachment.CODEC.codec());

	private static <A> AttachmentType<A> persistent(String name, Supplier<A> initializer, Codec<A> codec) {
		return AttachmentRegistry.create(TwilightForestMod.prefix(name), builder -> builder.initializer(initializer).persistent(codec));
	}

	private static <A> AttachmentType<A> persistent(String name, Supplier<A> initializer, Codec<A> codec, StreamCodec<? super RegistryFriendlyByteBuf, A> streamCodec) {
		return AttachmentRegistry.create(TwilightForestMod.prefix(name), builder -> builder
			.initializer(initializer)
			.persistent(codec)
			.syncWith(streamCodec, AttachmentSyncPredicate.all()));
	}

	public static <A> A get(AttachmentTarget target, AttachmentType<A> type) {
		return target.getAttachedOrCreate(type);
	}

	public static <A> A get(Entity entity, AttachmentType<A> type) {
		return get((AttachmentTarget) entity, type);
	}

	public static boolean has(Entity entity, AttachmentType<?> type) {
		return ((AttachmentTarget) entity).hasAttached(type);
	}

	public static <A> void set(Entity entity, AttachmentType<A> type, A value) {
		((AttachmentTarget) entity).setAttached(type, value);
	}

	public static <A> A remove(Entity entity, AttachmentType<A> type) {
		return ((AttachmentTarget) entity).removeAttached(type);
	}
}
