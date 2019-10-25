package global.simpleway.logger;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class PropertyUtils {

	public static String RAPTOR_FORCE_PREFIX = "raptor.force.property_";

	public static String getSystemOrEnvString(String key) {
		return getSystemOrEnvString(key, null);
	}

	public static String getSystemOrEnvString(String key, String defaultValue) {
		return getSystemOrEnv(key, defaultValue, value -> value);
	}

	public static Boolean getSystemOrEnvBoolean(String key) {
		return getSystemOrEnvBoolean(key, false);
	}

	public static Boolean getSystemOrEnvBoolean(String key, Boolean defaultValue) {
		return getSystemOrEnv(key, defaultValue, Boolean::valueOf);
	}

	public static Integer getSystemOrEnvInteger(String key) {
		return getSystemOrEnvInteger(key, 0);
	}

	public static Integer getSystemOrEnvInteger(String key, Integer defaultValue) {
		return getSystemOrEnv(key, defaultValue, Integer::valueOf);
	}

	private static <T> T getSystemOrEnv(String key, T defaultValue, Function<String, T> function) {
		//		Preconditions.checkArgument(function != null, "Cannot get function value.");

		String value = System.getProperty(RAPTOR_FORCE_PREFIX + key);
		if (value != null) return function.apply(value);

		value = System.getenv(RAPTOR_FORCE_PREFIX + key);
		if (value != null) return function.apply(value);

		value = System.getProperty(key);
		if (value != null) return function.apply(value);

		value = System.getenv(key);
		if (value != null) return function.apply(value);

		return defaultValue;
	}

	public static Map<String, String> getPropertiesAndEnv() {
		final Map<String, String> allSettings = new HashMap<>();
		allSettings.putAll(System.getenv());
		System.getProperties().forEach((key, value) -> {
			if (key instanceof String == false) return;
			if (value != null && value instanceof String == false) return;

			if (value != null) {
				allSettings.put("" + key, "" + value);
			} else {
				allSettings.put("" + key, null);
			}
		});
		return allSettings;
	}

}
