package twilightforest.ci;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FabricPipelineTests {
	@Test
	void azurePipelineUsesTheProductionFabricToolchainAndArtifactName() throws IOException {
		String pipeline = Files.readString(Path.of("azure-pipelines.yml"));
		String normalized = pipeline.toLowerCase(Locale.ROOT);
		String buildScript = Files.readString(Path.of("build.gradle"));

		assertTrue(pipeline.contains("vmImage: 'ubuntu-24.04'"));
		assertTrue(pipeline.contains("$(JAVA_HOME_25_X64)"));
		assertTrue(pipeline.contains("src/main/resources/twilightforest.accesswidener"));
		assertTrue(pipeline.contains("twilightforest-*-fabric-*.jar"));
		assertTrue(pipeline.contains("-fabric-$($env:JAVAPROPS_MOD_VERSION).jar"));
		assertTrue(pipeline.contains("MAVEN_USER: $(Artifactory.User)"));
		assertTrue(pipeline.contains("MAVEN_PASS: $(Artifactory.Password)"));
		assertTrue(buildScript.contains("url = uri(\"https://maven.tamaized.com/releases\")"));
		assertTrue(buildScript.contains("providers.environmentVariable(\"MAVEN_USER\").orNull"));
		assertTrue(buildScript.contains("providers.environmentVariable(\"MAVEN_PASS\").orNull"));

		assertFalse(normalized.contains("accesstransformer"));
		assertFalse(normalized.contains("universal.jar"));
		assertFalse(normalized.contains("jdkversionoption"));
		assertFalse(normalized.contains("cirevision"));
		assertFalse(normalized.contains("citype"));
		assertFalse(pipeline.contains("ARTIFACTORY_USER:"));
		assertFalse(pipeline.contains("ARTIFACTORY_PASS:"));
	}
}
