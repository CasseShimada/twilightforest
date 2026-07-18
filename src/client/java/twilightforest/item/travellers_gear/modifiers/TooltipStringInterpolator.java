package twilightforest.item.travellers_gear.modifiers;

import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TooltipStringInterpolator {
	private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\{([^}]+)}");
	private static final Map<String, Function<String, String>> RESOLVERS = Map.of(
		"tfkeybinds", TooltipStringInterpolator::resolveKeybind
	);

	private TooltipStringInterpolator() {
	}

	public static MutableComponent render(String translationKey) {
		Matcher matcher = VARIABLE_PATTERN.matcher(Component.translatable(translationKey).getString());
		StringBuilder result = new StringBuilder();
		while (matcher.find()) {
			String replacement = resolveVariable(matcher.group(1));
			matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
		}
		matcher.appendTail(result);
		return Component.literal(result.toString());
	}

	private static String resolveVariable(String variable) {
		String[] parts = variable.split("/", 2);
		if (parts.length != 2 || parts[0].isEmpty() || parts[1].isEmpty()) {
			throw new IllegalArgumentException("Expected namespaced tooltip variable, got " + variable);
		}
		Function<String, String> resolver = RESOLVERS.get(parts[0]);
		if (resolver == null) {
			throw new IllegalArgumentException("Unknown tooltip variable namespace " + parts[0]);
		}
		return resolver.apply(parts[1]);
	}

	private static String resolveKeybind(String keyName) {
		KeyMapping mapping = KeyMapping.get(keyName);
		return mapping == null ? "Nonexistent key" : mapping.getTranslatedKeyMessage().getString();
	}
}
