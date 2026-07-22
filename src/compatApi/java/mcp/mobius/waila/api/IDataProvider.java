// SPDX-License-Identifier: CC-BY-NC-SA-4.0
package mcp.mobius.waila.api;

/** Minimal declaration-only surface used by Twilight Forest. */
public interface IDataProvider<T> {
	void appendData(IDataWriter data, IServerAccessor<T> accessor, IPluginConfig config);
}
