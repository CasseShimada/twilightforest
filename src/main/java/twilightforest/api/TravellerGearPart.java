package twilightforest.api;

import net.minecraft.world.entity.EquipmentSlot;

/**
 * Stable classification of the six Traveller's Gear pieces.
 */
public enum TravellerGearPart {
	/** Head-slot goggles. */
	GOGGLES(EquipmentSlot.HEAD),
	/** Chest-slot vest. */
	VEST(EquipmentSlot.CHEST),
	/** Chest-slot gloves. */
	GLOVES(EquipmentSlot.CHEST),
	/** Leg-slot wings. */
	WINGS(EquipmentSlot.LEGS),
	/** Leg-slot belt. */
	BELT(EquipmentSlot.LEGS),
	/** Feet-slot boots. */
	BOOTS(EquipmentSlot.FEET);

	private final EquipmentSlot equipmentSlot;

	TravellerGearPart(EquipmentSlot equipmentSlot) {
		this.equipmentSlot = equipmentSlot;
	}

	/**
	 * Returns the vanilla armor slot that stores this part. Vest/gloves and
	 * wings/belt intentionally share slots because merged gear stores both part
	 * classifications on one stack.
	 *
	 * @return fixed armor-slot mapping
	 */
	public EquipmentSlot equipmentSlot() {
		return this.equipmentSlot;
	}
}
