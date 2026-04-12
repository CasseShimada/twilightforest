package twilightforest.client.renderer.special;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3fc;
import org.jetbrains.annotations.Nullable;
import twilightforest.client.renderer.block.JarLidModels;
import twilightforest.components.item.JarLid;
import twilightforest.init.TFDataComponents;

import java.util.Optional;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public record MasonJarSpecialRenderer(Optional<Item> defaultLid) implements SpecialModelRenderer<DataComponentMap> {

	@Override
	public @Nullable DataComponentMap extractArgument(ItemStack stack) {
		return stack.getComponents();
	}

	@Override
	public void submit(@Nullable DataComponentMap map, PoseStack poseStack, SubmitNodeCollector nodeCollector, int packedLight, int packedOverlay, boolean hasFoil, int outlineColor) {
		if (map == null) return;

		poseStack.pushPose();

		JarLid jarLid = map.get(TFDataComponents.JAR_LID.get());
		Item lidItem = jarLid != null ? jarLid.lid() : this.defaultLid.orElse(null);
		BlockState lidState = resolveLidState(lidItem);
		if (lidState != null) {
			poseStack.pushPose();
			BlockStateModel lidModel = lidItem != null ? JarLidModels.getModel(lidItem) : null;
			if (lidModel != null) {
				List<BlockStateModelPart> parts = new ArrayList<>();
				lidModel.collectParts(net.minecraft.util.RandomSource.create(0L), parts);
				nodeCollector.submitBlockModel(poseStack, RenderTypes.solidMovingBlock(), parts, BlockModelRenderState.EMPTY_TINTS, packedLight, packedOverlay, outlineColor);
			} else {
				ItemStackRenderState lidRenderState = new ItemStackRenderState();
				ItemModelResolver resolver = Minecraft.getInstance().getItemModelResolver();
				resolver.updateForTopItem(lidRenderState, new ItemStack(lidItem), ItemDisplayContext.FIXED, null, null, 0);
				poseStack.translate(0.5D, 0.875D, 0.5D);
				poseStack.scale(0.5F, 0.25F, 0.5F);
				poseStack.translate(-0.5D, -0.5D, -0.5D);
				lidRenderState.submit(poseStack, nodeCollector, packedLight, packedOverlay, outlineColor);
			}
			poseStack.popPose();
		}

		ItemContainerContents contents = map.get(DataComponents.CONTAINER);
		if (contents != null) {
			ItemStack stackInside = contents.copyOne();
			if (!stackInside.isEmpty()) {
				ItemStackRenderState renderState = new ItemStackRenderState();
				ItemModelResolver resolver = Minecraft.getInstance().getItemModelResolver();
				resolver.updateForTopItem(renderState, stackInside, ItemDisplayContext.FIXED, null, null, 0);

				poseStack.pushPose();
				poseStack.translate(0.5D, 0.4375D, 0.5D);
				poseStack.scale(0.5F, 0.5F, 0.5F);
				renderState.submit(poseStack, nodeCollector, packedLight, packedOverlay, outlineColor);
				poseStack.popPose();
			}
		}

		poseStack.popPose();
	}

	@Override
	public void getExtents(Consumer<Vector3fc> output) {
		// Conservative bounds: jar base is in baked model; lid and contents remain within block bounds.
		output.accept(new org.joml.Vector3f(0.0F, 0.0F, 0.0F));
		output.accept(new org.joml.Vector3f(1.0F, 1.0F, 1.0F));
	}

	private static @Nullable BlockState resolveLidState(@Nullable Item lid) {
		if (lid == null) {
			return null;
		}
		if (lid instanceof BlockItem blockItem) {
			return blockItem.getBlock().defaultBlockState();
		}
		return Blocks.OAK_LOG.defaultBlockState();
	}

	public record Unbaked(Optional<Item> defaultLid) implements SpecialModelRenderer.Unbaked<DataComponentMap> {
		public static final MapCodec<MasonJarSpecialRenderer.Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				BuiltInRegistries.ITEM.byNameCodec().optionalFieldOf("default_lid").forGetter(MasonJarSpecialRenderer.Unbaked::defaultLid))
			.apply(instance, MasonJarSpecialRenderer.Unbaked::new));

		public Unbaked(Item item) {
			this(Optional.of(item));
		}

		public Unbaked() {
			this(Optional.empty());
		}

		@Override
		public MapCodec<MasonJarSpecialRenderer.Unbaked> type() {
			return MAP_CODEC;
		}

		@Override
		public SpecialModelRenderer<DataComponentMap> bake(SpecialModelRenderer.BakingContext context) {
			return new MasonJarSpecialRenderer(this.defaultLid());
		}
	}
}
