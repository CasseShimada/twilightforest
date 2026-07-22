// SPDX-License-Identifier: CC-BY-NC-SA-4.0
package mcp.mobius.waila.api;

/** Minimal declaration-only surface used by Twilight Forest. */
public interface IClientRegistrar {
	<T> void body(IBlockComponentProvider provider, Class<T> clazz);
	<T> void body(IEntityComponentProvider provider, Class<T> clazz);
}
