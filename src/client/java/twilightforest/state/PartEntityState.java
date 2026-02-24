package twilightforest.client.state;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

public class PartEntityState extends EntityRenderState {
	@Nullable
	public Identifier rendererId;
	public float partialTick;
	public float yRot;
	public float yRotO;
	public float xRot;
	public float deathTime;
	public float walkAnimationPos;
	public float walkAnimationSpeed;
	public boolean isUpsideDown;
	public boolean isInWater;
	public boolean hasRedOverlay;
	public boolean isInvisibleToPlayer;
	public boolean appearsGlowing;
	@Nullable
	public Component customName;
}
