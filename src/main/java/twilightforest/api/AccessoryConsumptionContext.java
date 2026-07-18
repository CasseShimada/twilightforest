package twilightforest.api;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;

import java.util.Objects;

/**
 * Server-side request to consume an item from an accessory-like inventory.
 *
 * @param player player whose external inventory should be searched
 * @param requestedItem exact item requested by Twilight Forest
 */
public record AccessoryConsumptionContext(ServerPlayer player, Item requestedItem) {
	/**
	 * Creates a logical-server consumption request. Constructing a context does
	 * not consume, persist, or synchronize anything.
	 *
	 * @param player player whose provider-owned storage should be searched
	 * @param requestedItem exact item requested by Twilight Forest
	 */
	public AccessoryConsumptionContext {
		Objects.requireNonNull(player, "player");
		Objects.requireNonNull(requestedItem, "requestedItem");
	}

	/**
	 * Returns the server player whose provider-owned storage should be searched.
	 *
	 * @return logical-server player
	 */
	@Override
	public ServerPlayer player() {
		return this.player;
	}

	/**
	 * Returns the exact item that the provider may consume.
	 *
	 * @return requested item
	 */
	@Override
	public Item requestedItem() {
		return this.requestedItem;
	}
}
