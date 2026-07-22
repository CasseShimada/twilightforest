// SPDX-License-Identifier: CC-BY-NC-SA-4.0
package mcp.mobius.waila.api;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Minimal declaration-only surface used by Twilight Forest. */
public interface ICommonRegistrar {
	void featureConfig(Identifier key, boolean clientOnly);
	<T, BE extends BlockEntity> void blockData(IDataProvider<BE> provider, Class<T> clazz);
	<T, E extends Entity> void entityData(IDataProvider<E> provider, Class<T> clazz);
}
