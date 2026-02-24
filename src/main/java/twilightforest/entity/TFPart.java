package twilightforest.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import twilightforest.TwilightForestMod;
import twilightforest.network.UpdateTFMultipartPacket;

import java.util.Objects;

/**
 * Fabric/vanilla replacement for the previous multipart entity API.
 *
 * <p>These entities are not spawned via vanilla networking. They exist as logical hitboxes attached
 * to a parent and are synchronized using {@link UpdateTFMultipartPacket}.</p>
 */
public abstract class TFPart<T extends Entity> extends Entity {

	public static final Identifier RENDERER = TwilightForestMod.prefix("noop");

	private final T parent;

	protected EntityDimensions realSize = EntityDimensions.fixed(1F, 1F);

	protected int newPosRotationIncrements;
	protected double interpTargetX;
	protected double interpTargetY;
	protected double interpTargetZ;
	protected double interpTargetYaw;
	protected double interpTargetPitch;
	public float renderYawOffset;
	public float prevRenderYawOffset;

	public int deathTime;
	public int hurtTime;

	protected TFPart(T parent) {
		// Marker is inert and safe for parts that should never be spawned via vanilla networking.
		this(EntityType.MARKER, parent.level(), parent);
	}

	protected TFPart(EntityType<?> type, Level level, T parent) {
		super(type, level);
		this.parent = Objects.requireNonNull(parent, "parent");
	}

	public T getParent() {
		return this.parent;
	}

	public Identifier renderer() {
		return RENDERER;
	}

	public void setPositionAndRotationDirect(double x, double y, double z, float yaw, float pitch, int posRotationIncrements) {
		this.interpTargetX = x;
		this.interpTargetY = y;
		this.interpTargetZ = z;
		this.interpTargetYaw = yaw;
		this.interpTargetPitch = pitch;
		this.newPosRotationIncrements = posRotationIncrements;
	}

	@Override
	public void tick() {
		updateLastPos();
		super.tick();

		if (this.newPosRotationIncrements > 0) {
			double d0 = this.getX() + (this.interpTargetX - this.getX()) / (double) this.newPosRotationIncrements;
			double d2 = this.getY() + (this.interpTargetY - this.getY()) / (double) this.newPosRotationIncrements;
			double d4 = this.getZ() + (this.interpTargetZ - this.getZ()) / (double) this.newPosRotationIncrements;
			double d6 = Mth.wrapDegrees(this.interpTargetYaw - (double) this.getYRot());
			this.setYRot((float) ((double) this.getYRot() + d6 / (double) this.newPosRotationIncrements));
			this.setXRot((float) ((double) this.getXRot() + (this.interpTargetPitch - (double) this.getXRot()) / (double) this.newPosRotationIncrements));
			--this.newPosRotationIncrements;
			this.setPos(d0, d2, d4);
			this.setRot(this.getYRot(), this.getXRot());
		}

		while (getYRot() - this.yRotO < -180F) this.yRotO -= 360F;
		while (getYRot() - this.yRotO >= 180F) this.yRotO += 360F;

		while (this.renderYawOffset - this.prevRenderYawOffset < -180F) this.prevRenderYawOffset -= 360F;
		while (this.renderYawOffset - this.prevRenderYawOffset >= 180F) this.prevRenderYawOffset += 360F;

		while (getXRot() - this.xRotO < -180F) this.xRotO -= 360F;
		while (getXRot() - this.xRotO >= 180F) this.xRotO += 360F;
	}

	private void updateLastPos() {
		this.xOld = this.getX();
		this.yOld = this.getY();
		this.zOld = this.getZ();
		this.xo = this.getX();
		this.yo = this.getY();
		this.zo = this.getZ();
		this.yRotO = this.getYRot();
		this.xRotO = this.getXRot();
	}

	protected void setSize(EntityDimensions size) {
		this.realSize = size;
		this.refreshDimensions();
	}

	@Override
	public boolean isCurrentlyGlowing() {
		return this.parent.isCurrentlyGlowing();
	}

	@Override
	public boolean isInvisible() {
		return this.parent.isInvisible();
	}

	@Override
	public EntityDimensions getDimensions(Pose pose) {
		return this.realSize;
	}

	@Override
	public boolean isPickable() {
		return true;
	}

	@Override
	public InteractionResult interact(Player player, InteractionHand hand) {
		return this.parent.interact(player, hand);
	}

	@Override
	public void setId(int id) {
		// Keep ids distinct from the parent entity id space.
		super.setId(id + 1);
	}

	public UpdateTFMultipartPacket.PartDataHolder writeData() {
		return new UpdateTFMultipartPacket.PartDataHolder(
			this.getX(),
			this.getY(),
			this.getZ(),
			this.getYRot(),
			this.getXRot(),
			this.getDimensions(this.getPose()).width(),
			this.getDimensions(this.getPose()).height(),
			this.getDimensions(this.getPose()).fixed(),
			getEntityData().packDirty());
	}

	public void readData(UpdateTFMultipartPacket.PartDataHolder data) {
		Vec3 vec = new Vec3(data.x(), data.y(), data.z());
		this.setPositionAndRotationDirect(vec.x(), vec.y(), vec.z(), data.yRot(), data.xRot(), 3);
		// Apply immediately in case this part is not ticked by the world every frame.
		this.setPos(vec.x(), vec.y(), vec.z());
		this.setRot(data.yRot(), data.xRot());
		final float w = data.width();
		final float h = data.height();
		this.setSize(data.fixed() ? EntityDimensions.fixed(w, h) : EntityDimensions.scalable(w, h));
		if (data.data() != null) {
			getEntityData().assignValues(data.data());
		}
		this.refreshDimensions();
	}

	public static void assignPartIDs(Entity parent) {
		if (!(parent instanceof TFMultipartEntity multipart)) return;
		TFPart<?>[] parts = multipart.getParts();
		if (parts == null) return;
		for (int i = 0; i < parts.length; i++) {
			parts[i].setId(parent.getId() + i);
		}
	}

	@Override
	public boolean shouldBeSaved() {
		return false;
	}

	@Override
	public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity entity) {
		throw new UnsupportedOperationException("TFPart should never be spawned via vanilla networking");
	}

	// Parts should never be persisted; keep legacy CompoundTag overloads for existing subclasses.
	protected void readAdditionalSaveData(CompoundTag compound) {
	}

	protected void addAdditionalSaveData(CompoundTag compound) {
	}

	@Override
	protected final void readAdditionalSaveData(ValueInput input) {
	}

	@Override
	protected final void addAdditionalSaveData(ValueOutput output) {
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
		return false;
	}
}
