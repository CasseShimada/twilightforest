package twilightforest.init;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Unit;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import twilightforest.TwilightForestMod;
import twilightforest.components.entity.*;
import twilightforest.components.item.OreScannerComponent;
import twilightforest.util.Codecs;
import net.minecraft.world.entity.Entity;

public class TFDataAttachments {
	public static void init() {
		// Ensure attachments are registered during mod initialization.
	}

	public static final AttachmentType<Boolean> FEATHER_FAN = AttachmentRegistry.create(TwilightForestMod.prefix("feather_fan_falling"), builder ->
		builder.initializer(() -> false).persistent(com.mojang.serialization.Codec.BOOL));

	public static final AttachmentType<PotionFlaskTrackingAttachment> FLASK_DOSES = AttachmentRegistry.create(TwilightForestMod.prefix("flask_doses"), builder ->
		builder.initializer(PotionFlaskTrackingAttachment::new).persistent(PotionFlaskTrackingAttachment.CODEC));

	public static final AttachmentType<FortificationShieldAttachment> FORTIFICATION_SHIELDS = AttachmentRegistry.create(TwilightForestMod.prefix("fortification_shields"), builder ->
		builder.initializer(FortificationShieldAttachment::new).persistent(FortificationShieldAttachment.CODEC));

	public static final AttachmentType<GiantPickaxeMiningAttachment> GIANT_PICKAXE_MINING = AttachmentRegistry.createDefaulted(TwilightForestMod.prefix("giant_pickaxe_mining"), GiantPickaxeMiningAttachment::new);

	public static final AttachmentType<OreScannerComponent> ORE_SCANNER = AttachmentRegistry.create(TwilightForestMod.prefix("ore_scanner"), builder ->
		builder.initializer(OreScannerComponent::getEmpty).persistent(OreScannerComponent.CODEC));

	public static final AttachmentType<YetiThrowAttachment> YETI_THROWING = AttachmentRegistry.createDefaulted(TwilightForestMod.prefix("yeti_throwing"), YetiThrowAttachment::new);

	public static final AttachmentType<MultiplayerInclusivityAttachment> MULTIPLAYER_FIGHT = AttachmentRegistry.createDefaulted(TwilightForestMod.prefix("multiplayer_fight"), MultiplayerInclusivityAttachment::new);

	public static final AttachmentType<TFPortalAttachment> TF_PORTAL_COOLDOWN = AttachmentRegistry.createDefaulted(TwilightForestMod.prefix("tf_portal_cooldown"), TFPortalAttachment::new);

	public static final AttachmentType<SmashBlocksEnchantmentAttachment> SMASH_BLOCKS = AttachmentRegistry.create(TwilightForestMod.prefix("smash_blocks"), builder ->
		builder.initializer(SmashBlocksEnchantmentAttachment::new).persistent(SmashBlocksEnchantmentAttachment.CODEC));

	public static final AttachmentType<GameProfile> ZOMBIFIED_PLAYER = AttachmentRegistry.create(TwilightForestMod.prefix("zombified_player"), builder ->
		builder.initializer(() -> UUIDUtil.createOfflineProfile("GizmoTheMoonPig")).persistent(Codecs.SIMPLE_GAME_PROFILE));

	public static final AttachmentType<Unit> LEASH_PATHFINDER_OVERRIDE = AttachmentRegistry.create(TwilightForestMod.prefix("leashed_pathfinder_override"), builder ->
		builder.initializer(() -> Unit.INSTANCE).persistent(Unit.CODEC));

	public static final AttachmentType<Unit> BANISHED_TO_TWILIGHT_FOREST = AttachmentRegistry.create(TwilightForestMod.prefix("twilightforest_banished"), builder ->
		builder.initializer(() -> Unit.INSTANCE).persistent(Unit.CODEC).copyOnDeath());

	public static final AttachmentType<Boolean> ENDER_BOW_ARROW = AttachmentRegistry.create(TwilightForestMod.prefix("ender_bow_arrow"), builder ->
		builder.initializer(() -> false).persistent(com.mojang.serialization.Codec.BOOL));

	public static final AttachmentType<CompoundTag> CHARM_DATA = AttachmentRegistry.create(TwilightForestMod.prefix("charm_data"), builder ->
		builder.initializer(CompoundTag::new).persistent(CompoundTag.CODEC));

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
