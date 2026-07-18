package twilightforest.mixin;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.storage.ValueInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import twilightforest.util.LegacyAttachmentDataFix;

import java.util.Optional;

@Mixin(targets = "net.fabricmc.fabric.impl.attachment.AttachmentSerializingImpl", remap = false)
public abstract class AttachmentSerializingImplMixin {
	@Redirect(
		method = "deserializeAttachmentData",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/storage/ValueInput;read(Ljava/lang/String;Lcom/mojang/serialization/Codec;)Ljava/util/Optional;",
			remap = true
		),
		remap = false
	)
	@SuppressWarnings({"rawtypes", "unchecked"})
	private static Optional twilightforest$readLegacyAttachmentRoots(ValueInput input, String currentRoot, Codec codec) {
		return LegacyAttachmentDataFix.readAttachmentData(input, currentRoot, codec);
	}
}
