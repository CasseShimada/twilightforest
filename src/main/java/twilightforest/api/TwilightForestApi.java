package twilightforest.api;

/**
 * Version and lifecycle entry point for the Twilight Forest public API.
 *
 * <p>This common class is safe to load on either physical side. Freezing is a
 * lifecycle operation and has no save-data or network effect.</p>
 */
public final class TwilightForestApi {
	/**
	 * Semantic version of the public API contract, independent of the mod version.
	 */
	public static final String API_VERSION = "1.0.0";

	/**
	 * Major API version for consumers that only need compatibility gating.
	 */
	public static final int API_MAJOR_VERSION = 1;

	private TwilightForestApi() {
	}

	/**
	 * Freezes every provider registry. This operation is idempotent.
	 *
	 * <p>Twilight Forest calls this during its server-start lifecycle. API
	 * consumers should register providers from their common mod initializer and
	 * must not call this method themselves. A late registration fails immediately
	 * at its registration call.</p>
	 */
	public static void freezeRegistrations() {
		AccessoryApi.freezeRegistrations();
		TravellerGearApi.freezeRegistrations();
		ArmorApi.freezeRegistrations();
		WeaponApi.freezeRegistrations();
	}

	/**
	 * Returns whether every public provider registry is frozen. This common-side
	 * query is thread-safe and does not invoke providers.
	 *
	 * @return true only after all registries have frozen
	 */
	public static boolean registrationsFrozen() {
		return AccessoryApi.registrationsFrozen()
			&& TravellerGearApi.registrationsFrozen()
			&& ArmorApi.registrationsFrozen()
			&& WeaponApi.registrationsFrozen();
	}

	static void resetForTests() {
		AccessoryApi.resetForTests();
		TravellerGearApi.resetForTests();
		ArmorApi.resetForTests();
		WeaponApi.resetForTests();
	}
}
