package twilightforest.api;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PublicApiBaselineTests {
	private static final Path BASELINE = Path.of("src", "test", "resources", "twilightforest", "api", "public-api-v1.txt");

	@Test
	void everyBaselineSymbolStillExistsAndIsPublic() throws ReflectiveOperationException, IOException {
		assertTrue(Files.isRegularFile(BASELINE), "public API baseline must remain tracked");
		for (String line : baselineEntries()) {
			String[] fields = line.split("\\s+");
			switch (fields[0]) {
				case "TYPE" -> verifyType(fields);
				case "FIELD" -> verifyField(fields);
				case "METHOD" -> verifyMethod(fields);
				case "CONSTRUCTOR" -> verifyConstructor(fields);
				case "ENUM" -> verifyEnumConstant(fields);
				default -> throw new AssertionError("Unknown public API baseline entry: " + line);
			}
		}
	}

	@Test
	void apiVersionMatchesGradleAndProcessedFabricMetadata() throws IOException {
		Properties gradleProperties = new Properties();
		try (InputStream input = Files.newInputStream(Path.of("gradle.properties"))) {
			gradleProperties.load(input);
		}
		assertEquals(TwilightForestApi.API_VERSION, gradleProperties.getProperty("api_version"));

		String template = Files.readString(Path.of("src", "main", "resources", "fabric.mod.json"), StandardCharsets.UTF_8);
		assertTrue(template.contains("\"twilightforest:api_version\": \"${api_version}\""));

		String processedMetadata;
		try (InputStream input = PublicApiBaselineTests.class.getClassLoader().getResourceAsStream("fabric.mod.json")) {
			assertNotNull(input, "processed Fabric metadata must be on the test runtime classpath");
			processedMetadata = new String(Objects.requireNonNull(input).readAllBytes(), StandardCharsets.UTF_8);
		}
		assertFalse(processedMetadata.contains("${api_version}"), "API version token must be expanded");
		assertTrue(processedMetadata.contains(
			"\"twilightforest:api_version\": \"" + TwilightForestApi.API_VERSION + "\""
		));
	}

	private static List<String> baselineEntries() throws IOException {
		return Files.readAllLines(BASELINE, StandardCharsets.UTF_8).stream()
			.map(String::trim)
			.filter(line -> !line.isEmpty() && !line.startsWith("#"))
			.toList();
	}

	private static void verifyType(String[] fields) throws ClassNotFoundException {
		assertEquals(2, fields.length, "TYPE baseline syntax");
		assertTrue(Modifier.isPublic(resolve(fields[1]).getModifiers()), fields[1] + " must remain public");
	}

	private static void verifyField(String[] fields) throws ReflectiveOperationException {
		assertEquals(4, fields.length, "FIELD baseline syntax");
		Field field = resolve(fields[1]).getDeclaredField(fields[2]);
		assertEquals(resolve(fields[3]), field.getType(), fields[1] + "." + fields[2] + " field type");
		assertTrue(Modifier.isPublic(field.getModifiers()), fields[1] + "." + fields[2] + " must remain public");
		assertTrue(Modifier.isStatic(field.getModifiers()), fields[1] + "." + fields[2] + " must remain static");
		assertTrue(Modifier.isFinal(field.getModifiers()), fields[1] + "." + fields[2] + " must remain final");
	}

	private static void verifyMethod(String[] fields) throws ReflectiveOperationException {
		assertTrue(fields.length >= 4, "METHOD baseline syntax");
		Class<?>[] parameterTypes = resolveAll(fields, 4);
		Method method = resolve(fields[1]).getDeclaredMethod(fields[2], parameterTypes);
		assertEquals(resolve(fields[3]), method.getReturnType(), fields[1] + "." + fields[2] + " return type");
		assertTrue(Modifier.isPublic(method.getModifiers()), fields[1] + "." + fields[2] + " must remain public");
	}

	private static void verifyConstructor(String[] fields) throws ReflectiveOperationException {
		assertTrue(fields.length >= 2, "CONSTRUCTOR baseline syntax");
		Constructor<?> constructor = resolve(fields[1]).getDeclaredConstructor(resolveAll(fields, 2));
		assertTrue(Modifier.isPublic(constructor.getModifiers()), fields[1] + " constructor must remain public");
	}

	private static void verifyEnumConstant(String[] fields) throws ClassNotFoundException {
		assertEquals(3, fields.length, "ENUM baseline syntax");
		Class<?> enumType = resolve(fields[1]);
		assertTrue(enumType.isEnum(), fields[1] + " must remain an enum");
		assertTrue(
			Arrays.stream(Objects.requireNonNull(enumType.getEnumConstants()))
				.map(constant -> ((Enum<?>) constant).name())
				.anyMatch(fields[2]::equals),
			fields[1] + "." + fields[2] + " must remain an enum constant"
		);
	}

	private static Class<?>[] resolveAll(String[] fields, int start) throws ClassNotFoundException {
		Class<?>[] classes = new Class<?>[fields.length - start];
		for (int index = start; index < fields.length; index++) {
			classes[index - start] = resolve(fields[index]);
		}
		return classes;
	}

	private static Class<?> resolve(String name) throws ClassNotFoundException {
		return switch (name) {
			case "boolean" -> boolean.class;
			case "int" -> int.class;
			case "void" -> void.class;
			default -> Class.forName(name, false, PublicApiBaselineTests.class.getClassLoader());
		};
	}
}
