package twilightforest.util;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Unit;
import net.minecraft.world.level.storage.ValueInput;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Optional;
import java.util.Set;

/**
 * Converts framework-owned entity persistence containers without loading either legacy framework.
 * Current Fabric data always wins. Older roots fill only data absent from the newest usable root,
 * while every encountered legacy container is retained in a dedicated backup attachment.
 */
public final class LegacyAttachmentDataFix {
	private static final Logger LOGGER = LogUtils.getLogger();

	public static final String FABRIC_ROOT = "fabric:attachments";
	public static final String NEOFORGE_ROOT = "neoforge:attachments";
	public static final String FORGE_CAPS_ROOT = "ForgeCaps";
	public static final String FORGE_DATA_ROOT = "ForgeData";
	public static final String LEGACY_FRAMEWORK_ATTACHMENT = "twilightforest:legacy_framework_data";

	private static final String LEGACY_FORWARD_BOOST = "twilightforest:temporary_saved_forward_boost";
	private static final String CURRENT_STRAIGHT_AHEAD = "twilightforest:temporary_saved_straight_ahead";
	private static final String LEGACY_CONTROLLED_FALL = "twilightforest:is_controlled_falling";
	private static final String CURRENT_GRADUAL_GLIDE = "twilightforest:is_gradually_gliding";
	private static final Set<String> CURRENT_PERSISTENT_ATTACHMENTS = Set.of(
		"twilightforest:feather_fan_falling",
		"twilightforest:flask_doses",
		"twilightforest:fortification_shields",
		"twilightforest:giant_pickaxe_mining",
		"twilightforest:ore_scanner",
		"twilightforest:yeti_throwing",
		"twilightforest:smash_blocks",
		"twilightforest:zombified_player",
		"twilightforest:leashed_pathfinder_override",
		"twilightforest:twilightforest_banished",
		"twilightforest:ender_bow_arrow",
		"twilightforest:charm_data",
		"twilightforest:is_using_goggles_zoom_modifier",
		"twilightforest:travellers_goggles_red_thread_vision",
		"twilightforest:last_tick_water_walking",
		"twilightforest:has_double_jump",
		"twilightforest:double_jump_validator",
		"twilightforest:double_jump_validator_last_check",
		CURRENT_STRAIGHT_AHEAD,
		"twilightforest:last_damage_armor_time",
		"twilightforest:last_jump_key_press_time",
		"twilightforest:last_horizontal_impulse",
		"twilightforest:last_non_horizontal_impulse",
		"twilightforest:last_horizontal_walking_time",
		"twilightforest:sidestep_validator",
		"twilightforest:sidestep_validator_last_check",
		CURRENT_GRADUAL_GLIDE,
		"twilightforest:slimy_soles_bounce_info"
	);

	private LegacyAttachmentDataFix() {
	}

	/**
	 * Keeps the current named-field representation for writes and accepts the pre-2026 scalar payload on reads.
	 */
	public static <A> Codec<A> wrappedScalarCodec(Codec<A> scalarCodec, String fieldName) {
		return Codec.withAlternative(scalarCodec.fieldOf(fieldName).codec(), scalarCodec);
	}

	/** Keeps the current empty-map representation for writes and accepts the older unit codec on reads. */
	public static Codec<Unit> unitCodec() {
		return Codec.withAlternative(MapCodec.unit(Unit.INSTANCE).codec(), MapCodec.unitCodec(Unit.INSTANCE));
	}

	public static <A> Optional<A> readAttachmentData(ValueInput input, String currentRoot, Codec<A> currentCodec) {
		if (input == null) {
			return Optional.empty();
		}

		// PASSTHROUGH distinguishes an absent field from a present field with the wrong tag type.
		// Every precedence decision uses raw presence: corrupt newer data is isolated, never replaced by stale data.
		Optional<Tag> current = readRaw(input, currentRoot);
		Optional<Tag> neoForgeAttachments = readRaw(input, NEOFORGE_ROOT);
		Optional<Tag> forgeCaps = readRaw(input, FORGE_CAPS_ROOT);
		Optional<Tag> forgeData = readRaw(input, FORGE_DATA_ROOT);

		if (current.isPresent()) {
			return readCurrentWithLegacyBackups(
				input, currentRoot, currentCodec, current.orElseThrow(), neoForgeAttachments, forgeCaps, forgeData);
		}

		if (neoForgeAttachments.isPresent()) {
			CompoundTag normalized = new CompoundTag();
			CompoundTag neoForgeCompound = compoundRoot(NEOFORGE_ROOT, neoForgeAttachments);
			CompoundTag forgeCapsCompound = compoundRoot(FORGE_CAPS_ROOT, forgeCaps);
			CompoundTag forgeDataCompound = compoundRoot(FORGE_DATA_ROOT, forgeData);
			if (neoForgeCompound != null) {
				normalized = normalizeNeoForgeAttachments(neoForgeCompound, forgeCapsCompound, forgeDataCompound);
			}
			preserveRawRoots(normalized, neoForgeAttachments, forgeCaps, forgeData);
			return decode(input, currentCodec, normalized);
		}

		if (forgeCaps.isPresent() || forgeData.isPresent()) {
			CompoundTag forgeCapsCompound = compoundRoot(FORGE_CAPS_ROOT, forgeCaps);
			CompoundTag forgeDataCompound = compoundRoot(FORGE_DATA_ROOT, forgeData);
			CompoundTag normalized = normalizeForgeData(
				forgeCapsCompound != null ? forgeCapsCompound : new CompoundTag(), forgeDataCompound);
			preserveRawRoots(normalized, Optional.empty(), forgeCaps, forgeData);
			return decode(input, currentCodec, normalized);
		}

		return input.read(currentRoot, currentCodec);
	}

	private static <A> Optional<A> readCurrentWithLegacyBackups(
		ValueInput input,
		String currentRoot,
		Codec<A> currentCodec,
		Tag current,
		Optional<Tag> neoForgeAttachments,
		Optional<Tag> forgeCaps,
		Optional<Tag> forgeData
	) {
		Optional<A> decodedCurrent = input.read(currentRoot, currentCodec);
		boolean hasLegacyRoots = neoForgeAttachments.isPresent() || forgeCaps.isPresent() || forgeData.isPresent();
		if (!hasLegacyRoots) {
			return decodedCurrent;
		}

		if (decodedCurrent.isPresent() && current instanceof CompoundTag currentCompound) {
			CompoundTag augmentedCurrent = currentCompound.copy();
			preserveRawRoots(augmentedCurrent, neoForgeAttachments, forgeCaps, forgeData);
			return decode(input, currentCodec, augmentedCurrent).or(() -> decodedCurrent);
		}

		// The current root remains authoritative even when corrupt. Preserve it and every old root in the
		// dedicated backup attachment, but do not import any stale values as live attachment state.
		CompoundTag isolated = new CompoundTag();
		CompoundTag preserved = new CompoundTag();
		preserved.put(currentRoot, current.copy());
		putRawRoot(preserved, NEOFORGE_ROOT, neoForgeAttachments);
		putRawRoot(preserved, FORGE_CAPS_ROOT, forgeCaps);
		putRawRoot(preserved, FORGE_DATA_ROOT, forgeData);
		isolated.put(LEGACY_FRAMEWORK_ATTACHMENT, preserved);
		Optional<A> isolatedCurrent = decode(input, currentCodec, isolated);
		if (isolatedCurrent.isPresent()) {
			LOGGER.error("Isolated malformed current attachment root {} without falling back to legacy roots", currentRoot);
		}
		return isolatedCurrent;
	}

	public static CompoundTag normalizeNeoForgeAttachments(CompoundTag attachments, @Nullable CompoundTag forgeData) {
		return normalizeNeoForgeAttachments(attachments, null, forgeData);
	}

	static CompoundTag normalizeNeoForgeAttachments(CompoundTag attachments, @Nullable CompoundTag forgeCaps, @Nullable CompoundTag forgeData) {
		CompoundTag normalized = new CompoundTag();
		for (String currentId : CURRENT_PERSISTENT_ATTACHMENTS) {
			copyIfPresent(attachments, currentId, normalized, currentId);
		}
		copyIfTargetAbsent(attachments, LEGACY_FORWARD_BOOST, normalized, CURRENT_STRAIGHT_AHEAD);
		copyIfTargetAbsent(attachments, LEGACY_CONTROLLED_FALL, normalized, CURRENT_GRADUAL_GLIDE);
		if (forgeCaps != null) {
			mapForgeCapabilities(normalized, forgeCaps);
		}
		if (forgeData != null) {
			mapForgePersistentData(normalized, forgeData);
		}

		CompoundTag preserved = new CompoundTag();
		preserved.put(NEOFORGE_ROOT, attachments.copy());
		if (forgeCaps != null) {
			preserved.put(FORGE_CAPS_ROOT, forgeCaps.copy());
		}
		if (forgeData != null) {
			preserved.put(FORGE_DATA_ROOT, forgeData.copy());
		}
		normalized.put(LEGACY_FRAMEWORK_ATTACHMENT, preserved);
		return normalized;
	}

	public static CompoundTag normalizeForgeData(CompoundTag forgeCaps, @Nullable CompoundTag forgeData) {
		CompoundTag normalized = new CompoundTag();
		mapForgeCapabilities(normalized, forgeCaps);

		CompoundTag preserved = new CompoundTag();
		preserved.put(FORGE_CAPS_ROOT, forgeCaps.copy());
		if (forgeData != null) {
			preserved.put(FORGE_DATA_ROOT, forgeData.copy());
			mapForgePersistentData(normalized, forgeData);
		}
		normalized.put(LEGACY_FRAMEWORK_ATTACHMENT, preserved);
		return normalized;
	}

	private static void mapForgePersistentData(CompoundTag normalized, CompoundTag forgeData) {
		CompoundTag playerPersisted = forgeData.getCompoundOrEmpty("PlayerPersisted");
		if (!playerPersisted.isEmpty() && !normalized.contains("twilightforest:charm_data")) {
			normalized.put("twilightforest:charm_data", playerPersisted.copy());
		}
		if (playerPersisted.getBooleanOr("twilightforest_banished", false)
			&& !normalized.contains("twilightforest:twilightforest_banished")) {
			normalized.put("twilightforest:twilightforest_banished", new CompoundTag());
		}
		if (forgeData.getBooleanOr("twilightforest:ender", false)
			&& !normalized.contains("twilightforest:ender_bow_arrow")) {
			normalized.putBoolean("twilightforest:ender_bow_arrow", true);
		}
	}

	private static void mapForgeCapabilities(CompoundTag normalized, CompoundTag forgeCaps) {
		copyIfTargetAbsent(forgeCaps, "twilightforest:cap_shield", normalized, "twilightforest:fortification_shields");
		copyIfTargetAbsent(forgeCaps, "twilightforest:giant_pick_mine", normalized, "twilightforest:giant_pickaxe_mining");
		copyIfTargetAbsent(forgeCaps, "twilightforest:cap_thrown", normalized, "twilightforest:yeti_throwing");

		CompoundTag oldFeatherFan = forgeCaps.getCompoundOrEmpty("twilightforest:cap_feather_fan_fall");
		if (oldFeatherFan.contains("featherFanFalling") && !normalized.contains("twilightforest:feather_fan_falling")) {
			CompoundTag featherFan = new CompoundTag();
			featherFan.putBoolean("feather_fan_falling", oldFeatherFan.getBooleanOr("featherFanFalling", false));
			normalized.put("twilightforest:feather_fan_falling", featherFan);
		}
	}

	private static void copyIfPresent(CompoundTag source, String sourceKey, CompoundTag target, String targetKey) {
		Tag payload = source.get(sourceKey);
		if (payload != null) {
			target.put(targetKey, payload.copy());
		}
	}

	private static void copyIfTargetAbsent(CompoundTag source, String sourceKey, CompoundTag target, String targetKey) {
		if (target.contains(targetKey)) {
			return;
		}
		copyIfPresent(source, sourceKey, target, targetKey);
	}

	private static Optional<Tag> readRaw(ValueInput input, String root) {
		return input.read(root, Codec.PASSTHROUGH)
			.map(dynamic -> dynamic.convert(NbtOps.INSTANCE).getValue());
	}

	private static @Nullable CompoundTag compoundRoot(String root, Optional<Tag> raw) {
		if (raw.isEmpty()) {
			return null;
		}
		if (raw.orElseThrow() instanceof CompoundTag compound) {
			return compound;
		}
		LOGGER.error("Legacy attachment root {} is not a compound; preserving it without using an older fallback", root);
		return null;
	}

	private static void preserveRawRoots(
		CompoundTag normalized,
		Optional<Tag> neoForgeAttachments,
		Optional<Tag> forgeCaps,
		Optional<Tag> forgeData
	) {
		if (neoForgeAttachments.isEmpty() && forgeCaps.isEmpty() && forgeData.isEmpty()) {
			return;
		}
		CompoundTag preserved = normalized.getCompoundOrEmpty(LEGACY_FRAMEWORK_ATTACHMENT).copy();
		putRawRoot(preserved, NEOFORGE_ROOT, neoForgeAttachments);
		putRawRoot(preserved, FORGE_CAPS_ROOT, forgeCaps);
		putRawRoot(preserved, FORGE_DATA_ROOT, forgeData);
		normalized.put(LEGACY_FRAMEWORK_ATTACHMENT, preserved);
	}

	private static void putRawRoot(CompoundTag preserved, String root, Optional<Tag> raw) {
		raw.ifPresent(tag -> preserved.put(root, tag.copy()));
	}

	private static <A> Optional<A> decode(ValueInput input, Codec<A> codec, CompoundTag normalized) {
		DataResult<A> result = codec.parse(input.lookup().createSerializationContext(NbtOps.INSTANCE), normalized);
		return result.resultOrPartial(message -> LOGGER.error("Failed to decode migrated legacy attachment data: {}", message));
	}
}
