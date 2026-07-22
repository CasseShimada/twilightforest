package twilightforest.compat;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class OptionalCompatIsolationTests {
	@Test
	void externalImportsAreConfinedToTheirOptionalIntegrationPackages() throws IOException {
		List<Path> roots = List.of(Path.of("src/main/java"), Path.of("src/client/java"));
		for (Path root : roots) {
			try (var files = Files.walk(root)) {
				for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
					String source = Files.readString(file);
					if (source.contains("import net.kyrptonaught.diggusmaximus.api.")) {
						assertTrue(normalize(file).contains("/twilightforest/compat/diggus/"), file.toString());
					}
					if (source.contains("import tschipp.carryon.api.")) {
						assertTrue(normalize(file).contains("/twilightforest/compat/carryon/"), file.toString());
					}
					if (source.contains("import mcp.mobius.waila.api.")) {
						assertTrue(normalize(file).contains("/twilightforest/compat/wthit/"), file.toString());
					}
				}
			}
		}
	}

	@Test
	void staticPoliciesPublishExpectedHardSafetyTags() throws IOException {
		String relocation = Files.readString(Path.of("src/generated/fabric/data/c/tags/block/relocation_not_supported.json"));
		for (String id : List.of("twilight_portal", "antibuilder", "carminite_reactor", "carminite_builder",
			"red_thread", "beanstalk_grower", "chiseled_canopy_bookshelf", "brazier", "naga_boss_spawner")) {
			if (!relocation.contains("twilightforest:" + id)) {
				fail("missing hard relocation denial for " + id);
			}
		}

		String diggusTools = Files.readString(Path.of("src/main/resources/data/diggusmaximus/tags/item/excluded_tools.json"));
		assertTrue(diggusTools.contains("twilightforest:giant_pickaxe"));
	}

	private static String normalize(Path path) {
		return "/" + path.toString().replace('\\', '/');
	}
}
