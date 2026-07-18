/**
 * Stable, loader-neutral integration points for Twilight Forest.
 *
 * <p>The API in this package is common code. It must not acquire references to
 * client-only Minecraft classes. Registration happens on the loader's common
 * initialization thread, before Twilight Forest freezes registrations at
 * server start. Queries invoke providers synchronously on the caller's thread;
 * the accessory consumption entry point is restricted to the logical server
 * thread.</p>
 *
 * <p>API version 1 does not create saved data, data attachments, or network
 * payloads. Provider IDs are lifecycle and diagnostic keys only; registering a
 * provider does not write its ID to a world or send it to a client.</p>
 *
 * <p>A common initializer can register only the capabilities it owns:</p>
 * <pre>{@code
 * TravellerGearApi.registerClassifier(
 *     Identifier.fromNamespaceAndPath("example", "gear"), stack -> Set.of());
 * ArmorApi.registerClassifier(
 *     Identifier.fromNamespaceAndPath("example", "armor"), stack -> Set.of());
 * WeaponApi.registerClassifier(
 *     Identifier.fromNamespaceAndPath("example", "weapon"), stack -> Set.of());
 * AccessoryApi.registerItemConsumer(
 *     Identifier.fromNamespaceAndPath("example", "storage"), context ->
 *     AccessoryConsumptionResult.pass());
 * }</pre>
 *
 * <p>Real accessory consumers remove exactly one requested item from their own
 * server-side storage before returning a complete pre-consumption stack through
 * {@link twilightforest.api.AccessoryConsumptionResult#consumed(net.minecraft.world.item.ItemStack)}.
 * Classifiers remain observational and return immutable-capable value sets; the
 * API itself snapshots and validates their results.</p>
 */
package twilightforest.api;
