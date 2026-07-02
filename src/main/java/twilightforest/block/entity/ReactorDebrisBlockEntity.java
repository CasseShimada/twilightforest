package twilightforest.block.entity;

import com.google.common.base.MoreObjects;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import twilightforest.init.TFBlockEntities;

import java.util.List;
import java.util.Random;

public class ReactorDebrisBlockEntity extends BlockEntity {
	private static final Identifier[] TEXTURES = {
		Identifier.withDefaultNamespace("block/netherrack"),
		Identifier.withDefaultNamespace("block/bedrock"),
		Identifier.withDefaultNamespace("block/nether_portal"),
		Identifier.withDefaultNamespace("block/obsidian"),
	};
	public static final Identifier DEFAULT_TEXTURE = TEXTURES[0];
	private static final float Z_FIGHTING_MIN = 0.008F;
	private static final float Z_FIGHTING_MAX = 1 - 0.008F;
	private static final Random RANDOM = new Random();
	private boolean rerolls = false;
	private boolean willDisappear = true;
	private byte timeAlive = 0;
	public VoxelShape shape = Shapes.empty();

	public Identifier[] textures = new Identifier[6];
	public Vector3f minPos = new Vector3f(Z_FIGHTING_MIN);
	public Vector3f maxPos = new Vector3f(Z_FIGHTING_MAX);

	public ReactorDebrisBlockEntity(BlockPos pos, BlockState blockState) {
		super(TFBlockEntities.REACTOR_DEBRIS, pos, blockState);
	}

	public void randomizeTextures() {
		for (int i = 0; i < this.textures.length; i++) {
			this.textures[i] = TEXTURES[RANDOM.nextInt(TEXTURES.length)];
		}
	}

	public void randomizeDimensions() {
		this.shape = calculateVoxelShape();
		AABB aabb = this.shape.bounds();
		this.minPos = new Vector3f((float) aabb.minX, (float) aabb.minY, (float) aabb.minZ);
		this.maxPos = new Vector3f((float) aabb.maxX, (float) aabb.maxY, (float) aabb.maxZ);
	}

	public static VoxelShape calculateVoxelShape() {
		float minX = RANDOM.nextInt(16) / 16F;
		float minY = RANDOM.nextInt(16) / 16F;
		float minZ = RANDOM.nextInt(16) / 16F;
		float lengthX = RANDOM.nextInt(1, (int) (17 - minX * 16)) / 16F;
		float lengthY = RANDOM.nextInt(1, (int) (17 - minY * 16)) / 16F;
		float lengthZ = RANDOM.nextInt(1, (int) (17 - minZ * 16)) / 16F;

		if (lengthX * lengthY * lengthZ < 1 / 8.0) {
			return calculateVoxelShape();
		}

		return Shapes.box(clampToSmallerCube(minX), clampToSmallerCube(minY), clampToSmallerCube(minZ),
			clampToSmallerCube(minX + lengthX), clampToSmallerCube(minY + lengthY), clampToSmallerCube(minZ + lengthZ));
	}

	private static double clampToSmallerCube(double value) {
		return Math.min(Math.max(value, Z_FIGHTING_MIN), Z_FIGHTING_MAX);
	}

	public static void tick(Level level, BlockPos blockPos, BlockState blockState, ReactorDebrisBlockEntity reactorDebrisBlockEntity) {
		if (reactorDebrisBlockEntity.willDisappear && reactorDebrisBlockEntity.timeAlive == 5 ||
			reactorDebrisBlockEntity.rerolls && RANDOM.nextInt(5) == 0) {
			reactorDebrisBlockEntity.randomizeDimensions();
			reactorDebrisBlockEntity.randomizeTextures();
		}

		if (!reactorDebrisBlockEntity.willDisappear)
			return;
		reactorDebrisBlockEntity.timeAlive++;
		if (reactorDebrisBlockEntity.timeAlive >= 60) {
			level.destroyBlock(blockPos, false);
		}
	}

	@NotNull
	private static Identifier nonEmptyNotNull(String texturesString) {
		if (texturesString.isBlank()) {
			return ReactorDebrisBlockEntity.DEFAULT_TEXTURE;
		}
		return MoreObjects.firstNonNull(Identifier.tryParse(texturesString), ReactorDebrisBlockEntity.DEFAULT_TEXTURE);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);

		input.child("textures").ifPresent(textures -> {
			this.textures[0] = nonEmptyNotNull(textures.getStringOr("west", ""));
			this.textures[1] = nonEmptyNotNull(textures.getStringOr("east", ""));
			this.textures[2] = nonEmptyNotNull(textures.getStringOr("bottom", ""));
			this.textures[3] = nonEmptyNotNull(textures.getStringOr("top", ""));
			this.textures[4] = nonEmptyNotNull(textures.getStringOr("north", ""));
			this.textures[5] = nonEmptyNotNull(textures.getStringOr("south", ""));
		});

		List<Float> posList = input.listOrEmpty("pos", Codec.FLOAT).stream().toList();
		if (posList.size() == 3) {
			this.minPos = new Vector3f(posList.get(0), posList.get(1), posList.get(2));
		}
		if (!new AABB(0, 0, 0, 1, 1, 1).contains(this.minPos.x, this.minPos.y, this.minPos.z)) {
			this.minPos = new Vector3f();
		}

		List<Float> sizeList = input.listOrEmpty("sizes", Codec.FLOAT).stream().toList();
		if (sizeList.size() == 3) {
			this.maxPos = new Vector3f(sizeList.get(0), sizeList.get(1), sizeList.get(2)).add(this.minPos);
		}
		if (!new AABB(0, 0, 0, 1, 1, 1).contains(this.minPos.x, this.minPos.y, this.minPos.z)) {
			this.maxPos = new Vector3f(1);
		}

		this.shape = Shapes.box(this.minPos.x, this.minPos.y, this.minPos.z, this.maxPos.x, this.maxPos.y, this.maxPos.z);
		this.rerolls = input.getBooleanOr("rerolls", this.rerolls);
		this.willDisappear = input.getBooleanOr("will_disappear", this.willDisappear);
		this.timeAlive = input.getByteOr("time_alive", this.timeAlive);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		if (this.textures[0] != null) {
			ValueOutput textures = output.child("textures");
			textures.putString("west", this.textures[0].toString());
			textures.putString("east", this.textures[1].toString());
			textures.putString("bottom", this.textures[2].toString());
			textures.putString("top", this.textures[3].toString());
			textures.putString("north", this.textures[4].toString());
			textures.putString("south", this.textures[5].toString());
		}
		ValueOutput.TypedOutputList<Float> posList = output.list("pos", Codec.FLOAT);
		posList.add(this.minPos.x);
		posList.add(this.minPos.y);
		posList.add(this.minPos.z);

		ValueOutput.TypedOutputList<Float> sizeList = output.list("sizes", Codec.FLOAT);
		sizeList.add(this.maxPos.x - this.minPos.x);
		sizeList.add(this.maxPos.y - this.minPos.y);
		sizeList.add(this.maxPos.z - this.minPos.z);

		output.putBoolean("rerolls", this.rerolls);
		output.putBoolean("will_disappear", this.willDisappear);
		output.putByte("time_alive", this.timeAlive);
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		return this.saveCustomOnly(registries);
	}
}
