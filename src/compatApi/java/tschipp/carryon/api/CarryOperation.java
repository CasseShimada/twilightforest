// SPDX-License-Identifier: LGPL-3.0-only
package tschipp.carryon.api;

/** Declaration-only API 1 snapshot; never packaged by Twilight Forest. */
public enum CarryOperation {
	PICKUP_BLOCK, PICKUP_ENTITY, PLACE_BLOCK, PLACE_ENTITY, STACK_ENTITY, RELEASE, CANCEL,
	RESPAWN_TRANSFER, DEATH_CLEANUP, DISCONNECT_CLEANUP, DAMAGE_CLEANUP
}
