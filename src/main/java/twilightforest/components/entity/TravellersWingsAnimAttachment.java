package twilightforest.components.entity;

import java.util.Random;

public final class TravellersWingsAnimAttachment {
	public double accumulatedPhase = new Random().nextDouble(0, 2 * Math.PI);
	public double oldAgeInTicks;
	public float xRotOld;
	public float yRotOldLeft;
	public float yRotOldRight;
	public float zRotOld;
	public boolean doubleJump;
}
