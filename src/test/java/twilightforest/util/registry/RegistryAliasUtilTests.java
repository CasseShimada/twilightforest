package twilightforest.util.registry;

import net.fabricmc.fabric.api.event.registry.FabricRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RegistryAliasUtilTests {
	private static final Identifier OLD_ID = Identifier.fromNamespaceAndPath("twilightforest", "old_id");
	private static final Identifier NEW_ID = Identifier.fromNamespaceAndPath("twilightforest", "new_id");

	@Test
	@SuppressWarnings("unchecked")
	void appliesAliasWhenSourceIsFreeAndTargetExists() {
		Registry<Object> registry = mock(Registry.class);
		when(registry.containsKey(NEW_ID)).thenReturn(true);

		RegistryAliasUtil.applyAliases(registry, Map.of(OLD_ID, NEW_ID));

		verify((FabricRegistry) registry).addAlias(OLD_ID, NEW_ID);
	}

	@Test
	@SuppressWarnings("unchecked")
	void preservesRegisteredSourceId() {
		Registry<Object> registry = mock(Registry.class);
		when(registry.containsKey(OLD_ID)).thenReturn(true);

		RegistryAliasUtil.applyAliases(registry, Map.of(OLD_ID, NEW_ID));

		verify((FabricRegistry) registry, never()).addAlias(OLD_ID, NEW_ID);
	}

	@Test
	@SuppressWarnings("unchecked")
	void skipsAliasWhenTargetIsMissing() {
		Registry<Object> registry = mock(Registry.class);

		RegistryAliasUtil.applyAliases(registry, Map.of(OLD_ID, NEW_ID));

		verify((FabricRegistry) registry, never()).addAlias(OLD_ID, NEW_ID);
	}
}
