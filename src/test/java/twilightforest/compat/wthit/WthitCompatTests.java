package twilightforest.compat.wthit;

import mcp.mobius.waila.api.IDataWriter;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.IServerAccessor;
import net.minecraft.server.level.ServerPlayer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import tamaized.beanification.junit.MockitoFixer;
import twilightforest.block.entity.bookshelf.ChiseledCanopyShelfBlockEntity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoFixer.class)
class WthitCompatTests {
	@Test
	void survivalViewerReceivesNoSensitiveShelfPayload() {
		IDataWriter writer = mock(IDataWriter.class);
		IPluginConfig config = mock(IPluginConfig.class);
		@SuppressWarnings("unchecked") IServerAccessor<ChiseledCanopyShelfBlockEntity> accessor = mock(IServerAccessor.class);
		ServerPlayer player = mock(ServerPlayer.class);
		when(config.getBoolean(WthitConstants.CANOPY_SHELF_SPAWNER)).thenReturn(true);
		when(accessor.getPlayer()).thenReturn(player);
		when(player.isCreative()).thenReturn(false);

		WthitDataProviders.CANOPY_SHELF.appendData(writer, accessor, config);

		verifyNoInteractions(writer);
		verify(accessor, never()).getTarget();
	}

	@Test
	void descriptorUsesSplitCurrentEntrypointsAndNoInternalApi() throws IOException {
		String descriptor = Files.readString(Path.of("src/main/resources/waila_plugins.json"));
		assertTrue(descriptor.contains("\"common\": \"twilightforest.compat.wthit.WthitCommonCompat\""));
		assertTrue(descriptor.contains("\"client\": \"twilightforest.compat.wthit.WthitClientCompat\""));
		assertFalse(descriptor.contains("initializer"));

		for (Path root : new Path[]{Path.of("src/main/java/twilightforest/compat/wthit"), Path.of("src/client/java/twilightforest/compat/wthit")}) {
			try (var files = Files.walk(root)) {
				for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
					assertFalse(Files.readString(file).contains(".__internal__"), file.toString());
				}
			}
		}
	}
}
