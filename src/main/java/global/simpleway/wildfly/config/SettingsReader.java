package global.simpleway.wildfly.config;

import static global.simpleway.wildfly.config.Settings.INTERNAL_LOGGER_PREFIX;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SettingsReader {

	private static final Logger logger = LoggerFactory.getLogger(INTERNAL_LOGGER_PREFIX + SettingsReader.class);

	private final Properties configFile;
	private final Path baseDir;
	private final Map<String, String> loadedConfiguration = new HashMap<>();

	public SettingsReader() {
		logger.info("Parsing application settings");

		baseDir = findBaseDir();
		final Path config = baseDir.resolve("config").resolve("wildfly-reader.properties");

		configFile = readConfig(config);
	}

	public Settings read() {
		final Settings settings = new Settings();
		settings.activeMqUrl = readString("activeMq.url", "localhost:61616");
		settings.activeMqUsername = readString("activeMq.username", null);
		settings.activeMqPassword = readString("activeMq.password", null);
		settings.activeMqQueue = readString("activeMq.queue", "ATS");
		settings.activeMqTopic = readString("activeMq.topic", "topic");

		settings.wildFlyUrl = readString("wildfly.url", "localhost:8080");
		settings.wildFlyUsername = readString("wildfly.username","admin");
		settings.wildFlyPassword = readString("wildfly.password", "admin");
		settings.wildFlyQueue = readString("wildfly.queue", "queue");
		settings.wildFlyTopic= readString("wildfly.topic", "MyTopic");
		return settings;
	}


	private Properties readConfig(Path config) {
		final Properties properties = new Properties();
		if (!Files.exists(config)) {
			logger.info("Unable to load config file (doesn't exist) at: {}", config.toUri());
			return properties;
		}
		try (InputStream is = Files.newInputStream(config, StandardOpenOption.READ)) {
			properties.load(is);
			logger.debug("Loaded {} values form configuration file {}", properties.size(), config.toAbsolutePath().toUri());
		} catch (Exception e) {
			logger.warn("Cannot read config at {} ", config.toUri(), e);
		}
		return properties;
	}


	private String readString(String propertyName, String defaultValue) {
		logger.debug("Start finding property {}", propertyName);
		String value = readSystemPropertyOrEnvironment(propertyName, null, logger);
		if (value != null) {
			loadedConfiguration.put(propertyName, value);
			return value;
		}
		value = configFile.getProperty(propertyName);
		if (value != null) {
			logger.debug("Setting found {} in the config file, value: {}", propertyName, value);
			loadedConfiguration.put(propertyName, value);
			return value;
		}
		value = defaultValue;
		logger.debug("Setting not found {} using fallback value: {}", propertyName, value);
		loadedConfiguration.put(propertyName, value);
		return value;
	}

	private Path findBaseDir() {
		final String propertyName = "base.dir";
		String baseDirString = readSystemPropertyOrEnvironment(propertyName, new File("locateBaseDir.tmp").getAbsoluteFile().getParent(), logger);
		loadedConfiguration.put(propertyName, baseDirString);
		final Path base = Paths.get(baseDirString);
		logger.debug("Using base dir {}", base.toUri());
		return base;
	}

	public Map<String, String> configMap() {
		return loadedConfiguration;
	}

	public static String readSystemPropertyOrEnvironment(String propertyName, String defaultValue, Logger logger) {
		String value = readSystemAndEnv(propertyName, logger);
		if (value != null) {
			return value;
		}
		value = readSystemAndEnv(propertyName.replace(".", "_").toUpperCase(), logger);
		if (value != null) {
			return value;
		}
		return defaultValue;
	}

	private static String readSystemAndEnv(String propertyName, Logger logger) {
		String value = System.getProperty(propertyName);
		if (value != null && !value.trim().isEmpty()) {
			if (logger != null)	logger.debug("Setting found {} as system property, value: {}", propertyName, value);
			return value;
		}
		value = System.getenv(propertyName);
		if (value != null && !value.trim().isEmpty()) {
			if (logger != null) logger.debug("Setting found {} in environment, value: {}", propertyName, value);
			return value;
		}
		return null;
	}
}
