package twilightforest.util.entities;

import com.mojang.math.Axis;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import twilightforest.TwilightForestMod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EntityRenderingUtil {

	private static final Set<EntityType<?>> IGNORED_ENTITIES = new HashSet<>();
	private static final Map<EntityType<?>, Entity> ENTITY_MAP = new HashMap<>();
	private static final float BASE_Y_OFFSET = 0.0625F;
	private static final int FULL_BRIGHT = 15728880;

	@Nullable
	public static Entity fetchEntity(EntityType<?> type, @Nullable Level level) {
		if (level != null && !IGNORED_ENTITIES.contains(type)) {
			Entity entity;
			if (type == EntityTypes.PLAYER) {
				entity = Minecraft.getInstance().player;
			} else {
				entity = ENTITY_MAP.computeIfAbsent(type, t -> {
					Entity created = t.create(level, EntitySpawnReason.LOAD);
					if (created instanceof Mob mob) {
						mob.setNoAi(true);
					}
					return created;
				});
			}
			return entity;
		}
		return null;
	}

	public static void renderEntity(GuiGraphicsExtractor graphics, EntityType<?> type, int size) {
		Entity entity = fetchEntity(type, Minecraft.getInstance().level);
		if (entity instanceof LivingEntity living) {
			// scale down large mobs, but don't scale up small ones
			int scale = size / 2;
			float height = entity.getBbHeight();
			float width = entity.getBbWidth();
			if (height > 2.25F || width > 2.25F) {
				scale = (int) (20 / Math.max(height, width));
			}
			// catch exceptions drawing the entity to be safe, any caught exceptions blacklist the entity
			try {
				renderTheEntity(graphics, size, scale, living);
			} catch (Exception e) {
				TwilightForestMod.LOGGER.error("Error drawing entity " + BuiltInRegistries.ENTITY_TYPE.getKey(type), e);
				IGNORED_ENTITIES.add(type);
				ENTITY_MAP.remove(type);
			}
		}
	}

	private static void renderTheEntity(GuiGraphicsExtractor graphics, int size, int scale, LivingEntity entity) {
		EntityRenderState state = extractRenderState(entity, 1.0F);
		if (state instanceof LivingEntityRenderState livingState) {
			livingState.bodyRot = 180.0F;
			livingState.yRot = 0.0F;
			livingState.xRot = 0.0F;
			livingState.boundingBoxWidth /= livingState.scale;
			livingState.boundingBoxHeight /= livingState.scale;
			livingState.scale = 1.0F;
		}

		float extraScale = additionalScale(entity.getType());
		float extraYOffset = additionalYOffset(entity.getType());
		Vector3f translation = new Vector3f(0.0F, state.boundingBoxHeight / 2.0F + BASE_Y_OFFSET + extraYOffset, 0.0F);
		float finalScale = scale * extraScale;

		Quaternionf camera = new Quaternionf().rotateX(20.0F * Mth.DEG_TO_RAD);
		Quaternionf rotation = new Quaternionf().rotateZ((float) Math.PI);
		rotation.mul(camera);
		rotation.mul(Axis.XN.rotationDegrees(35.0F));
		rotation.mul(Axis.YN.rotationDegrees(145.0F));

		graphics.entity(state, finalScale, translation, rotation, camera, 0, 0, size, size);
	}

	private static EntityRenderState extractRenderState(Entity entity, float partialTicks) {
		EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
		EntityRenderState state = dispatcher.extractEntity(entity, partialTicks);
		state.lightCoords = FULL_BRIGHT;
		state.shadowPieces.clear();
		state.outlineColor = 0;
		return state;
	}

	private static float additionalScale(EntityType<?> entity) {
		if (entity == EntityTypes.GHAST) return 0.5F;
		if (entity == EntityTypes.ELDER_GUARDIAN) return 0.6F;
		return 1.0F;
	}

	private static float additionalYOffset(EntityType<?> entity) {
		if (entity == EntityTypes.GHAST) return -12.5F;
		if (entity == EntityTypes.ENDER_DRAGON) return -4.0F;
		if (entity == EntityTypes.WITHER) return 8.0F;
		if (entity == EntityTypes.SQUID || entity == EntityTypes.GLOW_SQUID) return -19.0F;
		return 0.0F;
	}

	public static void renderItemEntity(GuiGraphicsExtractor graphics, ItemStack stack, float bobOffset) {
		if (stack.isEmpty()) return;
		Minecraft minecraft = Minecraft.getInstance();
		Level level = minecraft.level;
		if (level == null) return;

		ItemEntity itemEntity = new ItemEntity(level, 0.0D, 0.0D, 0.0D, stack);
		float partialTicks = minecraft.getDeltaTracker().getGameTimeDeltaTicks();
		EntityRenderState state = extractRenderState(itemEntity, partialTicks);
		if (state instanceof ItemEntityRenderState itemState) {
			itemState.bobOffset = bobOffset;
			itemState.ageInTicks = level.getGameTime() + partialTicks;
		}

		Vector3f translation = new Vector3f(0.0F, state.boundingBoxHeight / 2.0F + BASE_Y_OFFSET, 0.0F);
		Quaternionf camera = new Quaternionf().rotateX(20.0F * Mth.DEG_TO_RAD);
		Quaternionf rotation = new Quaternionf().rotateZ((float) Math.PI);
		rotation.mul(camera);
		rotation.mul(Axis.XN.rotationDegrees(35.0F));
		rotation.mul(Axis.YN.rotationDegrees(145.0F));

		graphics.entity(state, 50.0F, translation, rotation, camera, 0, 0, 32, 32);
	}

	public static List<Component> getMobTooltip(EntityType<?> type, ResourceKey<EntityType<?>> key) {
		List<Component> components = new ArrayList<>();
		components.add(type.getDescription());
		if (Minecraft.getInstance().options.advancedItemTooltips) {
			components.add(Component.literal(key.identifier().toString()).withStyle(ChatFormatting.DARK_GRAY));
		}
		return components;
	}

	public static String getModIdForTooltip(String modId) {
		return FabricLoader.getInstance().getModContainer(modId)
			.map(container -> container.getMetadata().getName())
			.orElseGet(() -> StringUtils.capitalize(modId));
	}
}
