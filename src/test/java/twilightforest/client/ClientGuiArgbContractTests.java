package twilightforest.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClientGuiArgbContractTests {
	private static final Path OVERLAY = Path.of("src/client/java/twilightforest/event/OverlayHandler.java");
	private static final Path UNCRAFTING = Path.of("src/client/java/twilightforest/UncraftingScreen.java");
	private static final Path LOCKED_BIOME_TOAST = Path.of("src/client/java/twilightforest/LockedBiomeToast.java");
	private static final Path MISSING_ADVANCEMENT_TOAST = Path.of("src/client/java/twilightforest/MissingAdvancementToast.java");
	private static final Path RECIPE_VIEWER_CONSTANTS = Path.of("src/client/java/twilightforest/compat/RecipeViewerConstants.java");
	private static final Path JEI_UNCRAFTING = Path.of("src/client/java/twilightforest/compat/jei/categories/JEIUncraftingCategory.java");
	private static final List<Path> JEI_OPAQUE_CATEGORIES = List.of(
		JEI_UNCRAFTING,
		Path.of("src/client/java/twilightforest/compat/jei/categories/MoonwormQueenCategory.java"),
		Path.of("src/client/java/twilightforest/compat/jei/categories/TransformationPowderCategory.java"),
		Path.of("src/client/java/twilightforest/compat/jei/categories/OminousFireCategory.java"),
		Path.of("src/client/java/twilightforest/compat/jei/categories/CrumbleHornCategory.java")
	);
	private static final Path JEI_DRYING = Path.of("src/client/java/twilightforest/compat/jei/categories/DryingRackCategory.java");
	private static final Path JEI_COMPAT = Path.of("src/client/java/twilightforest/compat/jei/JEICompat.java");
	private static final Path FABRIC_METADATA = Path.of("src/main/resources/fabric.mod.json");
	private static final String ARGB_CAPTURE = "(0x[0-9A-Fa-f_]+)";

	@Test
	void allElevenAffectedTextPathsUseOpaqueArgbOrAnOpaqueXpColorProvider() throws IOException {
		List<DirectTextPath> directPaths = List.of(
			new DirectTextPath(OVERLAY, "ore meter scan status",
				"graphics\\.text\\(Minecraft\\.getInstance\\(\\)\\.font,component,4,4," + ARGB_CAPTURE + ",false\\);", 0xFFFFFF),
			new DirectTextPath(OVERLAY, "ore meter data row",
				"graphics\\.text\\(Minecraft\\.getInstance\\(\\)\\.font,rowText,textXPos,yOff," + ARGB_CAPTURE + ",false\\);", 0xFFFFFF),
			new DirectTextPath(OVERLAY, "ore meter header row",
				"graphics\\.text\\(Minecraft\\.getInstance\\(\\)\\.font,headerRowText,xOff,yOff," + ARGB_CAPTURE + ",false\\);", 0xFFFFFF),
			new DirectTextPath(UNCRAFTING, "uncrafting title",
				"graphics\\.text\\(this\\.font,this\\.title,6,6," + ARGB_CAPTURE + ",false\\);", 0x404040),
			new DirectTextPath(UNCRAFTING, "uncrafting disabled label",
				"graphics\\.text\\(this\\.font,Component\\.translatable\\(\"container\\.twilightforest\\.uncrafting_table\\.uncrafting_disabled\"\\)\\.withStyle\\(ChatFormatting\\.DARK_RED\\),6,this\\.imageHeight-96\\+2," + ARGB_CAPTURE + ",false\\);", 0x404040),
			new DirectTextPath(UNCRAFTING, "uncrafting inventory label",
				"graphics\\.text\\(this\\.font,I18n\\.get\\(\"container\\.inventory\"\\),7,this\\.imageHeight-96\\+2," + ARGB_CAPTURE + ",false\\);", 0x404040),
			new DirectTextPath(LOCKED_BIOME_TOAST, "locked biome description",
				"graphics\\.text\\(font,DESCRIPTION,25,18," + ARGB_CAPTURE + ",false\\);", 0xFFFFFF),
			new DirectTextPath(MISSING_ADVANCEMENT_TOAST, "missing advancement title",
				"graphics\\.text\\(font,this\\.title,25,18," + ARGB_CAPTURE + ",false\\);", 0xFFFFFF)
		);
		List<DelegatedTextPath> delegatedPaths = List.of(
			new DelegatedTextPath(UNCRAFTING, "uncrafting cost",
				"graphics\\.text\\(this\\.font,cost,frameX\\+48-this\\.font\\.width\\(cost\\),frameY\\+38,color\\);"),
			new DelegatedTextPath(UNCRAFTING, "recrafting cost",
				"graphics\\.text\\(this\\.font,cost,frameX\\+130-this\\.font\\.width\\(cost\\),frameY\\+38,color\\);"),
			new DelegatedTextPath(JEI_UNCRAFTING, "JEI uncrafting cost",
				"graphics\\.text\\(Minecraft\\.getInstance\\(\\)\\.font,costStr,45-Minecraft\\.getInstance\\(\\)\\.font\\.width\\(costStr\\),22,RecipeViewerConstants\\.getXPColor\\(cost\\),true\\);"
			)
		);

		assertEquals(11, directPaths.size() + delegatedPaths.size());
		for (DirectTextPath path : directPaths) {
			Matcher matcher = Pattern.compile(path.callPattern()).matcher(compactSource(path.source()));
			assertTrue(matcher.find(), () -> "Missing text path contract: " + path.description());
			assertOpaqueArgb(parseArgb(matcher.group(1)), path.expectedRgb(), path.description());
		}
		for (DelegatedTextPath path : delegatedPaths) {
			assertTrue(Pattern.compile(path.callPattern()).matcher(compactSource(path.source())).find(),
				() -> "Missing delegated text path contract: " + path.description());
		}
	}

	@Test
	void bothXpColorBranchesAreOpaqueInTheScreenAndRecipeViewerHelper() throws IOException {
		String uncrafting = compactSource(UNCRAFTING);
		String recipeViewerConstants = compactSource(RECIPE_VIEWER_CONSTANTS);

		assertEquals(2, occurrences(uncrafting, "color=0xFFA00000;"), "Both screen cost paths must use opaque unaffordable red");
		assertEquals(2, occurrences(uncrafting, "color=0xFF80FF20;"), "Both screen cost paths must use opaque affordable green");
		assertEquals(1, occurrences(recipeViewerConstants, "return0xFFA00000;"), "JEI's unaffordable branch must be opaque");
		assertEquals(1, occurrences(recipeViewerConstants, "return0xFF80FF20;"), "JEI's affordable branch must be opaque");
		assertOpaqueArgb(parseArgb("0xFFA00000"), 0xA00000, "unaffordable XP color");
		assertOpaqueArgb(parseArgb("0xFF80FF20"), 0x80FF20, "affordable XP color");
	}

	@Test
	void uncraftingRenderingUsesReachableRecipeBookBackgroundHook() throws IOException {
		String uncrafting = compactSource(UNCRAFTING);
		assertTrue(uncrafting.contains("publicvoidextractBackground(GuiGraphicsExtractorgraphics,intmouseX,intmouseY,floatpartialTicks){"));
		assertTrue(uncrafting.contains("super.extractBackground(graphics,mouseX,mouseY,partialTicks);"));
		assertEquals(0, occurrences(uncrafting, "publicvoidextractContents(GuiGraphicsExtractorgraphics,intmouseX,intmouseY,floatpartialTicks){"),
			"AbstractRecipeBookScreen bypasses subclass extractContents on its normal render path");
	}

	@Test
	void fabricMetadataExposesTheJeiPluginEntrypoint() throws IOException {
		JsonObject metadata = JsonParser.parseString(Files.readString(FABRIC_METADATA)).getAsJsonObject();
		JsonArray plugins = metadata.getAsJsonObject("entrypoints").getAsJsonArray("jei_mod_plugin");
		assertEquals(1, plugins.size());
		assertEquals("twilightforest.compat.jei.JEICompat", plugins.get(0).getAsString());
	}

	@Test
	void jeiRegistersTheSyntheticMoonwormRecipeCategoryUnconditionally() throws IOException {
		String jei = compactSource(JEI_COMPAT);
		int methodStart = jei.indexOf("publicvoidregisterCategories(IRecipeCategoryRegistrationregistration){");
		int methodEnd = jei.indexOf("@OverridepublicvoidregisterVanillaCategoryExtensions", methodStart);
		assertTrue(methodStart >= 0 && methodEnd > methodStart);
		String method = jei.substring(methodStart, methodEnd);
		assertTrue(method.contains("registration.addRecipeCategories(newMoonwormQueenCategory(registration.getJeiHelpers().getGuiHelper()));"));
		assertEquals(0, occurrences(method, "if("), "Every synthetic recipe added below must have its category registered");
	}

	@Test
	void jeiStaticBackgroundsDrawBelowRecipeSlotsInsteadOfAsLateExtras() throws IOException {
		for (Path category : JEI_OPAQUE_CATEGORIES) {
			String source = compactSource(category);
			assertEquals(0, occurrences(source, "builder.addDrawable(this.background"),
				() -> category + " must not draw an opaque background after JEI draws its recipe slots");
			assertEquals(1, occurrences(source, "this.background.draw(graphics,0,0);"),
				() -> category + " must draw its background from IRecipeCategory.draw, before JEI draws slots");
		}

		String drying = compactSource(JEI_DRYING);
		assertEquals(0, occurrences(drying, "builder.addDrawable(this.background"),
			"The blank drying-rack background is unnecessary and must not become a late overlay");
		assertEquals(0, occurrences(drying, "IDrawablebackground;"));
	}

	private static String compactSource(Path source) throws IOException {
		return Files.readString(source).replaceAll("\\s+", "");
	}

	private static int parseArgb(String literal) {
		return (int) Long.parseLong(literal.substring(2).replace("_", ""), 16);
	}

	private static void assertOpaqueArgb(int color, int expectedRgb, String description) {
		assertEquals(0xFF, color >>> 24, description + " must have an opaque alpha channel");
		assertEquals(expectedRgb, color & 0xFFFFFF, description + " must preserve its original RGB channels");
	}

	private static int occurrences(String source, String marker) {
		return (source.length() - source.replace(marker, "").length()) / marker.length();
	}

	private record DirectTextPath(Path source, String description, String callPattern, int expectedRgb) {
	}

	private record DelegatedTextPath(Path source, String description, String callPattern) {
	}
}
