package twilightforest.util;

/**
 * Client-only flags toggled from common code without directly referencing client-only classes.
 *
 * <p>These are only meaningful on the physical client.</p>
 */
public final class TFClientFlags {
	private TFClientFlags() {
	}

	public static boolean urGhastAlive = false;
}

