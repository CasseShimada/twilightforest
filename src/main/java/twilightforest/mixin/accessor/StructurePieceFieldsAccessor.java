package twilightforest.mixin.accessor;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(StructurePiece.class)
public interface StructurePieceFieldsAccessor {
	@Accessor("rotation")
	Rotation twilightforest$getRotation();

	@Accessor("rotation")
	void twilightforest$setRotation(Rotation rotation);

	@Accessor("mirror")
	Mirror twilightforest$getMirror();

	@Accessor("mirror")
	void twilightforest$setMirror(Mirror mirror);

	@Accessor("orientation")
	Direction twilightforest$getOrientation();

	@Accessor("orientation")
	void twilightforest$setOrientation(Direction orientation);
}
